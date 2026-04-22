package com.sparta.delivhub.domain.payment.service;

import com.sparta.delivhub.common.dto.BusinessException;
import com.sparta.delivhub.common.dto.ErrorCode;
import com.sparta.delivhub.domain.order.entity.Order;
import com.sparta.delivhub.domain.order.repository.OrderRepository;
import com.sparta.delivhub.domain.payment.dto.RequestPaymentDTO;
import com.sparta.delivhub.domain.payment.dto.ResponsePaymentDTO;
import com.sparta.delivhub.domain.payment.entity.Payment;
import com.sparta.delivhub.domain.payment.entity.PaymentStatus;
import com.sparta.delivhub.domain.payment.repository.PaymentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class) // 스프링을 띄우지 않고 Mockito(가짜 객체) 기능만 빠르게 사용합니다.
class PaymentServiceTest {

    @Mock
    private OrderRepository orderRepository; // 조연 1: 가짜 주문 저장소

    @Mock
    private PaymentRepository paymentRepository; // 조연 2: 가짜 결제 저장소

    @InjectMocks
    private PaymentService paymentService; // 주인공: 가짜 조연들을 주입받은 실제 서비스

    @Test
    @DisplayName("결제 생성 성공 테스트")
    void createPayment_Success() {
        // =====================================
        // [1] Given (준비: 가짜 데이터와 상황 만들기)
        // =====================================
        UUID orderId = UUID.randomUUID();
        String currentUserId = "user123";
        Long amount = 15000L;

        // 1-1. 가짜 주문 객체 생성 (Order 엔티티에 맞춰서 생성)
        Order fakeOrder = Order.builder()
                .userId(currentUserId)
                .totalPrice(amount)
                .build();

        // 데이터베이스가 없으므로 ID가 자동 생성되지 않음. 강제로 ID를 넣어줍니다.
        ReflectionTestUtils.setField(fakeOrder, "id", orderId);

        // 1-2. 사용자가 보낸 요청 DTO 가짜 생성
        RequestPaymentDTO requestDTO = RequestPaymentDTO.builder()
                .orderId(orderId)
                .amount(amount)
                .paymentMethod("CARD")
                .build();

        // 1-3. 가짜 조연들의 대본(행동) 짜주기
        // "누군가 orderRepository.findById(이 주문ID)를 부르면, 내가 만든 fakeOrder를 반환해!"
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(fakeOrder));

        // "누군가 결제 내역이 있는지 확인하면, 없다고(false) 대답해!"
        when(paymentRepository.existsByOrderId(orderId)).thenReturn(false);

        // "결제 정보를 DB에 저장하려 하면, 그냥 들어온 객체 그대로 반환해!"
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // =====================================
        // [2] When (실행: 실제로 테스트할 메서드 호출)
        // =====================================
        ResponsePaymentDTO response = paymentService.createPayment(requestDTO, currentUserId);

        // =====================================
        // [3] Then (검증: 예상한 결과가 맞는지 확인)
        // =====================================
        assertNotNull(response); // 응답이 null이 아니어야 한다.
        assertEquals(amount, response.getAmount()); // 응답의 금액이 요청한 금액과 같아야 한다.
        assertEquals("COMPLETED", response.getStatus()); // 결제 상태는 COMPLETED여야 한다.
        assertEquals(orderId, response.getOrderId()); // 주문 ID가 일치해야 한다.
    }

    @Test
    @DisplayName("결제 삭제 성공 테스트 - 본인의 결제 내역 취소")
    void deletePayment_Success() {
        // =====================================
        // [1] Given (준비)
        // =====================================
        UUID paymentId = UUID.randomUUID();
        String currentUserId = "user123"; // 현재 로그인한 유저

        // 1-1. 내 주문 객체 생성 (주문자가 currentUserId와 일치해야 함)
        Order myOrder = Order.builder()
                .userId(currentUserId)
                .build();

        // 1-2. 취소할 결제 객체 생성 (상태는 일단 COMPLETED로 가정)
        Payment myPayment = Payment.builder()
                .order(myOrder)
                .amount(15000L)
                .status(PaymentStatus.COMPLETED)
                .build();

        // BaseEntity의 deleted 필드가 false(삭제 안 됨)라고 가정하기 위해 세팅
        ReflectionTestUtils.setField(myPayment, "deletedAt", null);

        // 1-3. 가짜 레포지토리 대본 짜기
        // "누군가 paymentId로 결제 내역을 찾으면, myPayment를 반환해라!"
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(myPayment));

        // =====================================
        // [2] When (실행)
        // =====================================
        // 결제 삭제 메서드 호출
        paymentService.deletePayment(paymentId, currentUserId);

        // =====================================
        // [3] Then (검증)
        // =====================================
        // DB에서 실제로 데이터가 지워지는지(delete 쿼리) 확인하는 것이 아니라,
        // cancelPayment()가 호출되어 상태가 CANCELLED(또는 DELETED)로 잘 바뀌었는지 확인합니다.
        assertEquals(PaymentStatus.CANCELLED, myPayment.getStatus());
    }

    @Test
    @DisplayName("결제 삭제 실패 테스트 - 타인의 결제 내역 접근 시 예외 발생")
    void deletePayment_Fail_AccessDenied() {
        // =====================================
        // [1] Given (준비)
        // =====================================
        UUID paymentId = UUID.randomUUID();
        String currentUserId = "hacker999"; // 취소를 시도하는 엉뚱한 유저
        String realOwnerId = "user123";     // 진짜 주문한 유저

        // 다른 사람(realOwnerId)의 주문 객체 생성
        Order otherPersonOrder = Order.builder()
                .userId(realOwnerId)
                .build();

        // 다른 사람의 결제 객체 생성
        Payment otherPersonPayment = Payment.builder()
                .order(otherPersonOrder)
                .amount(15000L)
                .status(PaymentStatus.COMPLETED)
                .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(otherPersonPayment));

        // =====================================
        // [2] When & [3] Then (실행 및 검증 동시에 진행)
        // =====================================
        // assertThrows: "이 로직을 실행했을 때, BusinessException 에러가 터져야만 테스트 성공이다!"
        BusinessException exception = org.junit.jupiter.api.Assertions.assertThrows(
                BusinessException.class,
                () -> paymentService.deletePayment(paymentId, currentUserId)
        );

        // 터진 에러의 코드가 PAYMENT_ACCESS_DENIED 가 맞는지 꼼꼼하게 한 번 더 확인
        assertEquals(ErrorCode.PAYMENT_ACCESS_DENIED, exception.getErrorCode());
    }

    // ==========================================
    // [결제 상세 조회 테스트 시작]
    // ==========================================

    @Test
    @DisplayName("결제 상세 조회 성공 - 본인의 결제 내역 (CUSTOMER)")
    void getPayment_Success_Owner() {
        // [1] Given
        UUID paymentId = UUID.randomUUID();
        String currentUserId = "user123";
        String userRole = "CUSTOMER";

        Order myOrder = Order.builder().userId(currentUserId).build();
        Payment myPayment = Payment.builder()
                .order(myOrder)
                .amount(15000L)
                .status(PaymentStatus.COMPLETED)
                .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(myPayment));

        // [2] When
        ResponsePaymentDTO response = paymentService.getPayment(paymentId, currentUserId, userRole);

        // [3] Then
        assertNotNull(response);
        assertEquals(15000L, response.getAmount());
    }

    @Test
    @DisplayName("결제 상세 조회 성공 - 타인의 결제 내역을 관리자가 조회 (MANAGER)")
    void getPayment_Success_Manager() {
        // [1] Given
        UUID paymentId = UUID.randomUUID();
        String currentUserId = "admin999"; // 관리자 아이디
        String userRole = "MANAGER";       // 권한은 관리자

        String ownerId = "user123";        // 실제 결제한 일반 유저 아이디

        Order otherPersonOrder = Order.builder().userId(ownerId).build();
        Payment otherPersonPayment = Payment.builder()
                .order(otherPersonOrder)
                .amount(20000L)
                .status(PaymentStatus.COMPLETED)
                .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(otherPersonPayment));

        // [2] When
        // CUSTOMER가 아니므로 남의 결제라도 통과되어야 합니다.
        ResponsePaymentDTO response = paymentService.getPayment(paymentId, currentUserId, userRole);

        // [3] Then
        assertNotNull(response);
        assertEquals(20000L, response.getAmount());
    }

    @Test
    @DisplayName("결제 상세 조회 실패 - 일반 유저가 타인의 결제 내역 접근 시도 (CUSTOMER)")
    void getPayment_Fail_AccessDenied() {
        // [1] Given
        UUID paymentId = UUID.randomUUID();
        String currentUserId = "hacker123"; // 훔쳐보려는 유저
        String userRole = "CUSTOMER";

        String realOwnerId = "user123";     // 진짜 결제한 유저

        Order otherPersonOrder = Order.builder().userId(realOwnerId).build();
        Payment otherPersonPayment = Payment.builder()
                .order(otherPersonOrder)
                .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(otherPersonPayment));

        // [2] When & [3] Then
        BusinessException exception = org.junit.jupiter.api.Assertions.assertThrows(
                BusinessException.class,
                () -> paymentService.getPayment(paymentId, currentUserId, userRole)
        );

        // 에러 코드가 권한 없음(PAYMENT_ACCESS_DENIED)인지 확인
        assertEquals(ErrorCode.PAYMENT_ACCESS_DENIED, exception.getErrorCode());
    }

    @Test
    @DisplayName("결제 상세 조회 실패 - 존재하지 않는 결제 내역")
    void getPayment_Fail_NotFound() {
        // [1] Given
        UUID paymentId = UUID.randomUUID();
        String currentUserId = "user123";
        String userRole = "CUSTOMER";

        // DB에서 조회했는데 아무것도 안 나오는 상황(Optional.empty)을 연출
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        // [2] When & [3] Then
        BusinessException exception = org.junit.jupiter.api.Assertions.assertThrows(
                BusinessException.class,
                () -> paymentService.getPayment(paymentId, currentUserId, userRole)
        );

        // 에러 코드가 INVALID_REQUEST (또는 설정하신 NOT_FOUND 에러)인지 확인
        assertEquals(ErrorCode.INVALID_REQUEST, exception.getErrorCode());
    }
}
