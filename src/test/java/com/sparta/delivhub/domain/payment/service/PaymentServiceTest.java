package com.sparta.delivhub.domain.payment.service;

import com.sparta.delivhub.common.dto.BusinessException;
import com.sparta.delivhub.common.dto.ErrorCode;
import com.sparta.delivhub.domain.order.service.entity.Order;
import com.sparta.delivhub.domain.order.service.entity.OrderStatus;
import com.sparta.delivhub.domain.order.service.repository.OrderRepository;
import com.sparta.delivhub.domain.payment.dto.MyPaymentListResponseDto;
import com.sparta.delivhub.domain.payment.dto.RequestPaymentDTO;
import com.sparta.delivhub.domain.payment.dto.ResponsePaymentDTO;
import com.sparta.delivhub.domain.payment.dto.StorePaymentListResponseDto;
import com.sparta.delivhub.domain.payment.entity.Payment;
import com.sparta.delivhub.domain.payment.entity.PaymentMethod;
import com.sparta.delivhub.domain.payment.entity.PaymentStatus;
import com.sparta.delivhub.domain.payment.repository.PaymentRepository;
import com.sparta.delivhub.domain.store.entity.Store;
import com.sparta.delivhub.domain.store.repository.StoreRepository;
import com.sparta.delivhub.domain.user.entity.User;
import com.sparta.delivhub.domain.user.entity.UserRole;
import com.sparta.delivhub.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private StoreRepository storeRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private PaymentService paymentService;

    private User customer;
    private final String currentUserId = "user123";
    private UUID orderId;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        // ✨ [핵심 해결] DEEP_STUBS 없이 단순 Mock 생성 및 Enum 직접 반환 설정
        customer = mock(User.class);
        lenient().when(customer.getUsername()).thenReturn(currentUserId);
        lenient().when(customer.getUserRole()).thenReturn(UserRole.CUSTOMER);
    }

    /**
     * ✨ 헬퍼 메서드: 각 테스트 역할에 맞는 가짜 유저 생성
     */
    private User createMockUser(String username, UserRole role) {
        User mockUser = mock(User.class);
        lenient().when(mockUser.getUsername()).thenReturn(username);
        lenient().when(mockUser.getUserRole()).thenReturn(role);
        return mockUser;
    }

    // ==========================================
    // 1. 결제 생성 (CARD 제한 & 안전장치)
    // ==========================================

    @Test
    @DisplayName("결제 생성 성공 - 결제 수단 'CARD'와 공백 포함 입력 모두 허용")
    void createPayment_Success() {
        // given
        Long amount = 15000L;
        when(userRepository.findByUsername(currentUserId)).thenReturn(Optional.of(customer));

        // ✨ NPE 방지: Order 생성 시 userId 필수 포함
        Order fakeOrder = Order.builder().userId(currentUserId).totalPrice(amount).build();
        ReflectionTestUtils.setField(fakeOrder, "id", orderId);

        RequestPaymentDTO requestDTO = RequestPaymentDTO.builder()
                .orderId(orderId).amount(amount).paymentMethod(" card ").build(); // trim() 테스트

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(fakeOrder));
        when(paymentRepository.existsByOrderId(orderId)).thenReturn(false);

        Payment savedPayment = Payment.builder()
                .order(fakeOrder).amount(amount).paymentMethod(PaymentMethod.CARD).status(PaymentStatus.COMPLETED).build();
        ReflectionTestUtils.setField(savedPayment, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(savedPayment, "createdAt", LocalDateTime.now());

        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        // when
        ResponsePaymentDTO response = paymentService.createPayment(requestDTO, currentUserId);

        // then
        assertNotNull(response);
        assertEquals("CARD", response.getPaymentMethod());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    @DisplayName("결제 생성 실패 - CARD가 아닌 'CASH' 입력 시 예외 발생")
    void createPayment_Fail_InvalidMethod() {
        // given
        when(userRepository.findByUsername(currentUserId)).thenReturn(Optional.of(customer));
        Order fakeOrder = Order.builder().userId(currentUserId).totalPrice(10000L).build();
        when(orderRepository.findById(any())).thenReturn(Optional.of(fakeOrder));

        RequestPaymentDTO requestDTO = RequestPaymentDTO.builder()
                .orderId(orderId).amount(10000L).paymentMethod("CASH").build();

        // when & then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> paymentService.createPayment(requestDTO, currentUserId));

        assertEquals(ErrorCode.INVALID_PAYMENT_METHOD, exception.getErrorCode());
        verify(paymentRepository, never()).save(any());
    }

    // ==========================================
    // 2. 결제 상세 조회 (권한 및 NPE 방어)
    // ==========================================

    @Test
    @DisplayName("결제 상세 조회 성공 - 본인의 결제 내역 (CUSTOMER)")
    void getPayment_Success_Owner() {
        // given
        UUID paymentId = UUID.randomUUID();
        when(userRepository.findByUsername(currentUserId)).thenReturn(Optional.of(customer));

        Order myOrder = Order.builder().userId(currentUserId).build();
        Payment myPayment = Payment.builder().order(myOrder).amount(10000L).status(PaymentStatus.COMPLETED).build();
        ReflectionTestUtils.setField(myPayment, "id", paymentId);

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(myPayment));

        // when
        ResponsePaymentDTO response = paymentService.getPayment(paymentId, currentUserId, "CUSTOMER");

        // then
        assertNotNull(response);
        assertEquals(10000L, response.getAmount());
    }

    @Test
    @DisplayName("결제 상세 조회 실패 - 타인의 내역 접근 시 예외 발생")
    void getPayment_Fail_AccessDenied() {
        // given
        UUID paymentId = UUID.randomUUID();
        String hackerId = "hacker123";
        User hacker = createMockUser(hackerId, UserRole.CUSTOMER);
        when(userRepository.findByUsername(hackerId)).thenReturn(Optional.of(hacker));

        Order ownerOrder = Order.builder().userId("ownerId").build();
        Payment ownerPayment = Payment.builder().order(ownerOrder).build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(ownerPayment));

        // when & then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> paymentService.getPayment(paymentId, hackerId, "CUSTOMER"));

        assertEquals(ErrorCode.PAYMENT_ACCESS_DENIED, exception.getErrorCode());
    }

    // ==========================================
    // 3. 결제 삭제 및 목록 조회
    // ==========================================

    @Test
    @DisplayName("결제 삭제 성공 - 정상적인 상태 변경 확인")
    void deletePayment_Success() {
        // given
        UUID paymentId = UUID.randomUUID();
        when(userRepository.findByUsername(currentUserId)).thenReturn(Optional.of(customer));

        Order myOrder = Order.builder().userId(currentUserId).build();
        Payment myPayment = Payment.builder().order(myOrder).status(PaymentStatus.COMPLETED).build();
        ReflectionTestUtils.setField(myPayment, "id", paymentId);

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(myPayment));

        // when
        paymentService.deletePayment(paymentId, currentUserId);

        // then
        assertEquals(PaymentStatus.CANCELLED, myPayment.getStatus());
    }

    @Test
    @DisplayName("가게별 결제 조회 실패 - 일반 고객(CUSTOMER)의 접근 차단")
    void getStorePayments_Fail_RoleNotAllowed() {
        // given
        UUID storeId = UUID.randomUUID();
        when(userRepository.findByUsername(currentUserId)).thenReturn(Optional.of(customer));

        // when & then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> paymentService.getStorePayments(storeId, currentUserId, PageRequest.of(0, 10)));

        assertEquals(ErrorCode.ACCESS_DENIED, exception.getErrorCode());
    }

    @Test
    @DisplayName("내 결제 목록 조회 성공 - 페이징 처리 검증")
    void getMyPayments_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findByUsername(currentUserId)).thenReturn(Optional.of(customer));

        Payment payment = Payment.builder().amount(5000L).status(PaymentStatus.COMPLETED).build();
        ReflectionTestUtils.setField(payment, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(payment, "createdAt", LocalDateTime.now());

        Page<Payment> page = new PageImpl<>(List.of(payment), pageable, 1);

        // ✨ [수정] currentUserId를 eq()로 감싸거나, 둘 다 any() 계열을 써야 합니다.
        when(paymentRepository.findAllByUserId(eq(currentUserId), any(Pageable.class))).thenReturn(page);

        // when
        MyPaymentListResponseDto response = paymentService.getMyPayments(currentUserId, pageable);

        // then
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
    }
}