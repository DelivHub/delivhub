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
import com.sparta.delivhub.domain.user.repository.UserRepository; // ✨ 추가
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
import static org.mockito.Mockito.lenient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock // ✨ 조연 3: 가짜 유저 저장소 추가!
    private UserRepository userRepository;

    @InjectMocks
    private PaymentService paymentService;

    // 💡 [유용한 헬퍼 메서드] 가짜 유저와 권한을 쉽게 만들어주는 메서드입니다.
    private User createMockUser(String username, String roleName) {
        // RETURNS_DEEP_STUBS를 사용하면 user.getUserRole().name() 같은 연속된 호출도 알아서 가짜로 만들어줍니다!
        User mockUser = mock(User.class, org.mockito.Answers.RETURNS_DEEP_STUBS);
        lenient().when(mockUser.getUsername()).thenReturn(username);
        lenient().when(mockUser.getUserRole().name()).thenReturn(roleName);
        return mockUser;
    }

    @Test
    @DisplayName("결제 생성 성공 테스트")
    void createPayment_Success() {
        // [1] Given
        UUID orderId = UUID.randomUUID();
        String currentUserId = "user123";
        Long amount = 15000L;

        // ✨ 보안 로직 통과를 위한 유저 세팅 (권한: CUSTOMER)
        User mockUser = createMockUser(currentUserId, "CUSTOMER");
        when(userRepository.findByUsername(currentUserId)).thenReturn(Optional.of(mockUser));

        Order fakeOrder = Order.builder()
                .userId(currentUserId)
                .totalPrice(amount)
                .build();
        ReflectionTestUtils.setField(fakeOrder, "id", orderId);

        RequestPaymentDTO requestDTO = RequestPaymentDTO.builder()
                .orderId(orderId)
                .amount(amount)
                .paymentMethod("CARD")
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(fakeOrder));
        when(paymentRepository.existsByOrderId(orderId)).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // [2] When
        ResponsePaymentDTO response = paymentService.createPayment(requestDTO, currentUserId);

        // [3] Then
        assertNotNull(response);
        assertEquals(amount, response.getAmount());
        assertEquals("COMPLETED", response.getStatus());
        assertEquals(orderId, response.getOrderId());
    }

    @Test
    @DisplayName("결제 삭제 성공 테스트 - 본인의 결제 내역 취소")
    void deletePayment_Success() {
        // [1] Given
        UUID paymentId = UUID.randomUUID();
        String currentUserId = "user123";

        // ✨ 유저 세팅
        User mockUser = createMockUser(currentUserId, "CUSTOMER");
        when(userRepository.findByUsername(currentUserId)).thenReturn(Optional.of(mockUser));

        Order myOrder = Order.builder().userId(currentUserId).build();
        Payment myPayment = Payment.builder()
                .order(myOrder)
                .amount(15000L)
                .status(PaymentStatus.COMPLETED)
                .build();
        ReflectionTestUtils.setField(myPayment, "deletedAt", null);

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(myPayment));

        // [2] When
        paymentService.deletePayment(paymentId, currentUserId);

        // [3] Then
        assertEquals(PaymentStatus.CANCELLED, myPayment.getStatus());
    }

    @Test
    @DisplayName("결제 삭제 실패 테스트 - 타인의 결제 내역 접근 시 예외 발생")
    void deletePayment_Fail_AccessDenied() {
        // [1] Given
        UUID paymentId = UUID.randomUUID();
        String currentUserId = "hacker999";
        String realOwnerId = "user123";

        // ✨ 유저 세팅
        User mockUser = createMockUser(currentUserId, "CUSTOMER");
        when(userRepository.findByUsername(currentUserId)).thenReturn(Optional.of(mockUser));

        Order otherPersonOrder = Order.builder().userId(realOwnerId).build();
        Payment otherPersonPayment = Payment.builder()
                .order(otherPersonOrder)
                .amount(15000L)
                .status(PaymentStatus.COMPLETED)
                .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(otherPersonPayment));

        // [2] & [3]
        BusinessException exception = org.junit.jupiter.api.Assertions.assertThrows(
                BusinessException.class,
                () -> paymentService.deletePayment(paymentId, currentUserId)
        );

        assertEquals(ErrorCode.PAYMENT_ACCESS_DENIED, exception.getErrorCode());
    }

    @Test
    @DisplayName("결제 상세 조회 성공 - 본인의 결제 내역 (CUSTOMER)")
    void getPayment_Success_Owner() {
        // [1] Given
        UUID paymentId = UUID.randomUUID();
        String currentUserId = "user123";
        String userRole = "CUSTOMER";

        // ✨ 유저 세팅
        User mockUser = createMockUser(currentUserId, "CUSTOMER");
        when(userRepository.findByUsername(currentUserId)).thenReturn(Optional.of(mockUser));

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
        String currentUserId = "admin999";
        String userRole = "MANAGER";
        String ownerId = "user123";

        // ✨ 유저 세팅 (권한: MANAGER)
        User mockUser = createMockUser(currentUserId, "MANAGER");
        when(userRepository.findByUsername(currentUserId)).thenReturn(Optional.of(mockUser));

        Order otherPersonOrder = Order.builder().userId(ownerId).build();
        Payment otherPersonPayment = Payment.builder()
                .order(otherPersonOrder)
                .amount(20000L)
                .status(PaymentStatus.COMPLETED)
                .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(otherPersonPayment));

        // [2] When
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
        String currentUserId = "hacker123";
        String userRole = "CUSTOMER";
        String realOwnerId = "user123";

        // ✨ 유저 세팅
        User mockUser = createMockUser(currentUserId, "CUSTOMER");
        when(userRepository.findByUsername(currentUserId)).thenReturn(Optional.of(mockUser));

        Order otherPersonOrder = Order.builder().userId(realOwnerId).build();
        Payment otherPersonPayment = Payment.builder().order(otherPersonOrder).build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(otherPersonPayment));

        // [2] & [3]
        BusinessException exception = org.junit.jupiter.api.Assertions.assertThrows(
                BusinessException.class,
                () -> paymentService.getPayment(paymentId, currentUserId, userRole)
        );

        assertEquals(ErrorCode.PAYMENT_ACCESS_DENIED, exception.getErrorCode());
    }

    @Test
    @DisplayName("결제 상세 조회 실패 - 존재하지 않는 결제 내역")
    void getPayment_Fail_NotFound() {
        // [1] Given
        UUID paymentId = UUID.randomUUID();
        String currentUserId = "user123";
        String userRole = "CUSTOMER";

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        // [2] & [3]
        BusinessException exception = org.junit.jupiter.api.Assertions.assertThrows(
                BusinessException.class,
                () -> paymentService.getPayment(paymentId, currentUserId, userRole)
        );

        assertEquals(ErrorCode.INVALID_REQUEST, exception.getErrorCode());
    }

    @Test
    @DisplayName("내 결제 내역 조회 성공 - 페이징 처리 및 DTO 변환 확인")
    void getMyPayments_Success() {
        // [1] Given
        String currentUserId = "user123";
        String userRole = "CUSTOMER";
        Pageable pageable = PageRequest.of(0, 10);

        // ✨ 유저 세팅
        User mockUser = createMockUser(currentUserId, "CUSTOMER");
        when(userRepository.findByUsername(currentUserId)).thenReturn(Optional.of(mockUser));

        Payment payment1 = Payment.builder().amount(25000L).status(PaymentStatus.COMPLETED).build();
        ReflectionTestUtils.setField(payment1, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(payment1, "createdAt", LocalDateTime.now());

        Payment payment2 = Payment.builder().amount(15000L).status(PaymentStatus.CANCELLED).build();
        ReflectionTestUtils.setField(payment2, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(payment2, "createdAt", LocalDateTime.now());

        Page<Payment> paymentPage = new PageImpl<>(List.of(payment1, payment2), pageable, 2);
        when(paymentRepository.findAllByUserId(currentUserId, pageable)).thenReturn(paymentPage);

        // [2] When
        MyPaymentListResponseDto response = paymentService.getMyPayments(currentUserId, userRole, pageable);

        // [3] Then
        assertNotNull(response);
        assertEquals(2, response.getContent().size());
        assertEquals(2, response.getTotalElements());
        assertEquals(0, response.getPage());
        assertEquals(25000L, response.getContent().get(0).getAmount());
        assertEquals("COMPLETED", response.getContent().get(0).getStatus());
    }

    @Test
    @DisplayName("내 결제 내역 조회 성공 - 결제 내역이 하나도 없을 때 빈 리스트 반환")
    void getMyPayments_Success_EmptyList() {
        // [1] Given
        String currentUserId = "user123";
        String userRole = "CUSTOMER";
        Pageable pageable = PageRequest.of(0, 10);

        // ✨ 유저 세팅
        User mockUser = createMockUser(currentUserId, "CUSTOMER");
        when(userRepository.findByUsername(currentUserId)).thenReturn(Optional.of(mockUser));

        Page<Payment> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(paymentRepository.findAllByUserId(currentUserId, pageable)).thenReturn(emptyPage);

        // [2] When
        MyPaymentListResponseDto response = paymentService.getMyPayments(currentUserId, userRole, pageable);

        // [3] Then
        assertNotNull(response);
        assertTrue(response.getContent().isEmpty());
        assertEquals(0, response.getTotalElements());
    }

    @Test
    @DisplayName("가게별 결제 조회 성공 - 사장님(OWNER)이 본인 가게 조회")
    void getStorePayments_Success_Owner() {
        // [1] Given
        UUID storeId = UUID.randomUUID();
        String currentUserId = "owner123";
        String userRole = "OWNER";
        Pageable pageable = PageRequest.of(0, 10);

        // ✨ 유저 세팅 (권한: OWNER)
        User mockUser = createMockUser(currentUserId, "OWNER");
        when(userRepository.findByUsername(currentUserId)).thenReturn(Optional.of(mockUser));

        Store store = Store.builder().owner(mockUser).build();
        ReflectionTestUtils.setField(store, "id", storeId);

        com.sparta.delivhub.domain.order.entity.Order fakeOrder = com.sparta.delivhub.domain.order.entity.Order.builder().build();
        ReflectionTestUtils.setField(fakeOrder, "id", UUID.randomUUID());

        Payment payment = Payment.builder()
                .amount(15000L)
                .status(PaymentStatus.COMPLETED)
                .build();
        ReflectionTestUtils.setField(payment, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(payment, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(payment, "order", fakeOrder);

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        Page<Payment> paymentPage = new PageImpl<>(List.of(payment), pageable, 1);
        when(paymentRepository.findAllByStoreId(storeId, pageable)).thenReturn(paymentPage);

        // [2] When
        StorePaymentListResponseDto response = paymentService.getStorePayments(storeId, currentUserId, userRole, pageable);

        // [3] Then
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(15000L, response.getContent().get(0).getAmount());
        assertNotNull(response.getContent().get(0).getOrderId());
    }

    @Test
    @DisplayName("가게별 결제 조회 성공 - 관리자(MASTER)는 모든 가게 프리패스 조회")
    void getStorePayments_Success_Master() {
        // [1] Given
        UUID storeId = UUID.randomUUID();
        String currentUserId = "admin_master";
        String userRole = "MASTER";
        Pageable pageable = PageRequest.of(0, 10);

        // ✨ 유저 세팅 (권한: MASTER)
        User mockUser = createMockUser(currentUserId, "MASTER");
        when(userRepository.findByUsername(currentUserId)).thenReturn(Optional.of(mockUser));

        User otherOwner = User.builder().username("other_owner_999").build();
        Store store = Store.builder().owner(otherOwner).build();

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        when(paymentRepository.findAllByStoreId(storeId, pageable)).thenReturn(Page.empty());

        // [2] & [3]
        assertDoesNotThrow(() -> paymentService.getStorePayments(storeId, currentUserId, userRole, pageable));
    }

    @Test
    @DisplayName("가게별 결제 조회 실패 - 일반 고객(CUSTOMER)의 접근 원천 차단 (403 방어)")
    void getStorePayments_Fail_Customer() {
        // [1] Given
        UUID storeId = UUID.randomUUID();
        String currentUserId = "customer123";
        String userRole = "CUSTOMER";
        Pageable pageable = PageRequest.of(0, 10);

        // ✨ 유저 세팅
        User mockUser = createMockUser(currentUserId, "CUSTOMER");
        when(userRepository.findByUsername(currentUserId)).thenReturn(Optional.of(mockUser));

        // [2] & [3]
        BusinessException exception = assertThrows(BusinessException.class,
                () -> paymentService.getStorePayments(storeId, currentUserId, userRole, pageable));

        assertEquals(ErrorCode.ACCESS_DENIED, exception.getErrorCode());
    }

    @Test
    @DisplayName("가게별 결제 조회 실패 - 다른 사장님의 가게를 조회하려 할 때 (S003 방어)")
    void getStorePayments_Fail_WrongOwner() {
        // [1] Given
        UUID storeId = UUID.randomUUID();
        String currentUserId = "hacker_owner";
        String userRole = "OWNER";
        Pageable pageable = PageRequest.of(0, 10);

        // ✨ 유저 세팅
        User mockUser = createMockUser(currentUserId, "OWNER");
        when(userRepository.findByUsername(currentUserId)).thenReturn(Optional.of(mockUser));

        User realOwner = User.builder().username("real_owner_777").build();
        Store store = Store.builder().owner(realOwner).build();

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        // [2] & [3]
        BusinessException exception = assertThrows(BusinessException.class,
                () -> paymentService.getStorePayments(storeId, currentUserId, userRole, pageable));

        assertEquals(ErrorCode.STORE_ACCESS_DENIED, exception.getErrorCode());
    }
}