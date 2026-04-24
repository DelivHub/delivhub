package com.sparta.delivhub.domain.payment.service;

import com.sparta.delivhub.common.dto.BusinessException;
import com.sparta.delivhub.common.dto.ErrorCode;
import com.sparta.delivhub.domain.order.entity.Order;
import com.sparta.delivhub.domain.order.repository.OrderRepository;
import com.sparta.delivhub.domain.payment.dto.MyPaymentListResponseDto;
import com.sparta.delivhub.domain.payment.dto.RequestPaymentDTO;
import com.sparta.delivhub.domain.payment.dto.ResponsePaymentDTO;
import com.sparta.delivhub.domain.payment.dto.StorePaymentListResponseDto;
import com.sparta.delivhub.domain.payment.entity.Payment;
import com.sparta.delivhub.domain.payment.entity.PaymentStatus;
import com.sparta.delivhub.domain.payment.repository.PaymentRepository;
import com.sparta.delivhub.domain.store.entity.Store;
import com.sparta.delivhub.domain.store.repository.StoreRepository;
import com.sparta.delivhub.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import com.sparta.delivhub.domain.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
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

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private UserRepository userRepository;

    private User createMockUser(String username, String roleName) {
        // RETURNS_DEEP_STUBS를 사용하면 user.getUserRole().name() 같은 연속된 호출도 알아서 가짜로 만들어줍니다!
        User mockUser = mock(User.class, org.mockito.Answers.RETURNS_DEEP_STUBS);
        when(mockUser.getUsername()).thenReturn(username);
        when(mockUser.getUserRole().name()).thenReturn(roleName);
        return mockUser;
    }

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

    // ==========================================
    // 내 결제 내역 조회 기능 테스트
    // ==========================================

    @Test
    @DisplayName("내 결제 내역 조회 성공 - 페이징 처리 및 DTO 변환 확인")
    void getMyPayments_Success() {
        // [1] Given
        String currentUserId = "user123";
        String userRole = "CUSTOMER";
        Pageable pageable = PageRequest.of(0, 10);

        // 1. 가짜 결제 내역 2개 생성
        Payment payment1 = Payment.builder()
                .amount(25000L)
                .status(PaymentStatus.COMPLETED) // 💡 프로젝트의 결제 상태 Enum을 사용하세요!
                .build();
        ReflectionTestUtils.setField(payment1, "id", UUID.randomUUID());
        // createdAt은 BaseEntity에서 자동으로 들어가지만, 테스트를 위해 수동 주입
        ReflectionTestUtils.setField(payment1, "createdAt", LocalDateTime.now());

        Payment payment2 = Payment.builder()
                .amount(15000L)
                .status(PaymentStatus.CANCELLED)
                .build();
        ReflectionTestUtils.setField(payment2, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(payment2, "createdAt", LocalDateTime.now());

        // 2. 가짜 리포지토리 응답 세팅
        Page<Payment> paymentPage = new PageImpl<>(List.of(payment1, payment2), pageable, 2);
        when(paymentRepository.findAllByUserId(currentUserId, pageable)).thenReturn(paymentPage);

        // [2] When
        MyPaymentListResponseDto response = paymentService.getMyPayments(currentUserId, userRole, pageable);

        // [3] Then
        assertNotNull(response);
        assertEquals(2, response.getContent().size()); // 총 2건이 제대로 들어왔는지
        assertEquals(2, response.getTotalElements());
        assertEquals(0, response.getPage());

        // 첫 번째 결제 내역 데이터 매핑 검증
        assertEquals(25000L, response.getContent().get(0).getAmount());
        assertEquals("COMPLETED", response.getContent().get(0).getStatus()); // Enum -> String 변환 검증
    }

    @Test
    @DisplayName("내 결제 내역 조회 성공 - 결제 내역이 하나도 없을 때 빈 리스트 반환")
    void getMyPayments_Success_EmptyList() {
        // [1] Given
        String currentUserId = "user123";
        String userRole = "CUSTOMER";
        Pageable pageable = PageRequest.of(0, 10);

        // 텅 빈 페이지 객체 생성
        Page<Payment> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(paymentRepository.findAllByUserId(currentUserId, pageable)).thenReturn(emptyPage);

        // [2] When
        MyPaymentListResponseDto response = paymentService.getMyPayments(currentUserId, userRole, pageable);

        // [3] Then
        assertNotNull(response);
        assertTrue(response.getContent().isEmpty()); // 리스트가 비어있는지 확인
        assertEquals(0, response.getTotalElements()); // 전체 데이터 개수가 0개인지 확인
    }
// ==========================================
    // 가게별 결제 목록 조회 기능 테스트
    // ==========================================

    @Test
    @DisplayName("가게별 결제 조회 성공 - 사장님(OWNER)이 본인 가게 조회")
    void getStorePayments_Success_Owner() {
        // [1] Given
        UUID storeId = UUID.randomUUID();
        String currentUserId = "owner123";
        String userRole = "OWNER";
        Pageable pageable = PageRequest.of(0, 10);

        // 1. 가게 및 사장님 세팅
        User owner = User.builder().username(currentUserId).build();
        Store store = Store.builder().owner(owner).build();
        ReflectionTestUtils.setField(store, "id", storeId);

        // 2. 결제 내역 세팅 (DTO에서 order.getId()를 호출하므로 Order 객체도 필요!)
        // Order 엔티티 패키지 경로에 맞게 임포트 필요합니다.
        com.sparta.delivhub.domain.order.entity.Order fakeOrder = com.sparta.delivhub.domain.order.entity.Order.builder().build();
        ReflectionTestUtils.setField(fakeOrder, "id", UUID.randomUUID());

        Payment payment = Payment.builder()
                .amount(15000L)
                .status(PaymentStatus.COMPLETED)
                .build();
        ReflectionTestUtils.setField(payment, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(payment, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(payment, "order", fakeOrder); // ✨ 핵심: Payment에 Order 주입

        // 3. Mock 대본 작성
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        Page<Payment> paymentPage = new PageImpl<>(List.of(payment), pageable, 1);
        when(paymentRepository.findAllByStoreId(storeId, pageable)).thenReturn(paymentPage);

        // [2] When
        // 예외 없이 정상적으로 DTO가 반환되는지 실행
        StorePaymentListResponseDto response = paymentService.getStorePayments(storeId, currentUserId, userRole, pageable);

        // [3] Then
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(15000L, response.getContent().get(0).getAmount());
        assertNotNull(response.getContent().get(0).getOrderId()); // DTO에 orderId가 잘 들어갔는지 확인
    }

    @Test
    @DisplayName("가게별 결제 조회 성공 - 관리자(MASTER)는 모든 가게 프리패스 조회")
    void getStorePayments_Success_Master() {
        // [1] Given
        UUID storeId = UUID.randomUUID();
        String currentUserId = "admin_master";
        String userRole = "MASTER";
        Pageable pageable = PageRequest.of(0, 10);

        // 관리자는 남의 가게라도 상관없이 조회 가능해야 함
        User otherOwner = User.builder().username("other_owner_999").build();
        Store store = Store.builder().owner(otherOwner).build();

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(paymentRepository.findAllByStoreId(storeId, pageable)).thenReturn(Page.empty()); // 결과가 없어도 로직 통과만 확인

        // [2] When & [3] Then
        // 에러가 터지지 않고 정상적으로 빈 페이지가 반환되면 성공!
        assertDoesNotThrow(() -> paymentService.getStorePayments(storeId, currentUserId, userRole, pageable));
    }

    @Test
    @DisplayName("가게별 결제 조회 실패 - 일반 고객(CUSTOMER)의 접근 원천 차단 (403 방어)")
    void getStorePayments_Fail_Customer() {
        // [1] Given
        UUID storeId = UUID.randomUUID();
        String currentUserId = "customer123";
        String userRole = "CUSTOMER"; // 차단 대상 권한
        Pageable pageable = PageRequest.of(0, 10);

        // [2] When & [3] Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> paymentService.getStorePayments(storeId, currentUserId, userRole, pageable));

        assertEquals(ErrorCode.ACCESS_DENIED, exception.getErrorCode());
    }

    @Test
    @DisplayName("가게별 결제 조회 실패 - 다른 사장님의 가게를 조회하려 할 때 (S003 방어)")
    void getStorePayments_Fail_WrongOwner() {
        // [1] Given
        UUID storeId = UUID.randomUUID();
        String currentUserId = "hacker_owner"; // 현재 로그인한 엉뚱한 사장님
        String userRole = "OWNER";
        Pageable pageable = PageRequest.of(0, 10);

        // 해당 가게의 진짜 주인
        User realOwner = User.builder().username("real_owner_777").build();
        Store store = Store.builder().owner(realOwner).build();

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        // [2] When & [3] Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> paymentService.getStorePayments(storeId, currentUserId, userRole, pageable));

        // 아까 새로 만들었던 STORE_ACCESS_DENIED 에러가 정확히 터지는지 확인
        assertEquals(ErrorCode.STORE_ACCESS_DENIED, exception.getErrorCode());
    }
}
