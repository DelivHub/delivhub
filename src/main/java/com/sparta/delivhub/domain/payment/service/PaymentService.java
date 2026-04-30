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
import com.sparta.delivhub.domain.payment.entity.PaymentMethod;
import com.sparta.delivhub.domain.payment.entity.PaymentStatus;
import com.sparta.delivhub.domain.payment.repository.PaymentRepository;
import com.sparta.delivhub.domain.store.entity.Store;
import com.sparta.delivhub.domain.store.repository.StoreRepository;
import com.sparta.delivhub.domain.user.entity.User;
import com.sparta.delivhub.domain.user.entity.UserRole;
import com.sparta.delivhub.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    /**
     * 결제 생성
     */
    @Transactional
    public ResponsePaymentDTO createPayment(RequestPaymentDTO request, String currentUserId) {

        if (request == null || request.getOrderId() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_DATA);
        }

        // 1. 유저 조회 및 권한 재검증
        User currentUser = userRepository.findByUsername(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // Enum 직접 비교로 안전하게 권한 체크
        if (UserRole.CUSTOMER != currentUser.getUserRole()) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        // 2. 주문 엔티티 조회
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        // 소유자 권한 검증
        if (!currentUserId.equals(order.getUserId())) {
            throw new BusinessException(ErrorCode.PAYMENT_ACCESS_DENIED);
        }

        // 3. 중복 결제 방어
        if (paymentRepository.existsByOrderId(order.getId())) {
            throw new BusinessException(ErrorCode.PAYMENT_ALREADY_EXISTS);
        }

        // 4. 결제 수단 검증 (CARD만 허용)
        if (request.getPaymentMethod() == null || !request.getPaymentMethod().trim().equalsIgnoreCase("CARD")) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_METHOD);
        }
        // 오직 CARD만 허용하므로 직접 할당
        PaymentMethod method = PaymentMethod.CARD;

        // 5. 결제 금액 검증
        if (request.getAmount() == null || !order.getTotalPrice().equals(request.getAmount())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_DATA);
        }

        // 6. 결제 엔티티 생성 및 저장
        Payment payment = new Payment(
                order,
                request.getAmount(),
                method,
                PaymentStatus.COMPLETED
        );

        Payment savedPayment = paymentRepository.save(payment);

        return new ResponsePaymentDTO(savedPayment);
    }
    @Transactional(readOnly = true)
    public ResponsePaymentDTO getPayment(UUID paymentId, String currentUserId, String userRole) {
        // 1. 결제 내역 조회
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST));

        // 토큰의 Role 대신 DB의 최신 Role을 사용하여 검증
        User currentUser = userRepository.findByUsername(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        String dbRole = currentUser.getUserRole().name();

        // 2. 권한 검증
        if (UserRole.CUSTOMER == currentUser.getUserRole()) {
            // [안전장치] currentUserId를 앞에 두어 NPE 방어
            if (!currentUserId.equals(payment.getOrder().getUserId())) {
                throw new BusinessException(ErrorCode.PAYMENT_ACCESS_DENIED);
            }
        }

        // 3. DTO 변환 및 반환
        return new ResponsePaymentDTO(payment);
    }

    /**
     * 결제 상태 강제 수정 (관리자 전용 백오피스 기능)
     */
    @Transactional
    public ResponsePaymentDTO updatePaymentStatus(UUID paymentId, String newStatusStr, String currentUserId) {

        User currentUser = userRepository.findByUsername(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        String dbRole = currentUser.getUserRole().name();

        // 1. 권한 검증 (MANAGER, MASTER 만 접근 가능)
        if (!"MANAGER".equals(dbRole) && !"MASTER".equals(dbRole)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        // 2. 결제 내역 조회
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        // 3. 입력받은 String 상태값을 PaymentStatus Enum으로 변환 및 검증
        PaymentStatus newStatus;
        try {
            newStatus = PaymentStatus.valueOf(newStatusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // 4. 상태 변경 (엔티티 내부의 변경 메서드 호출)
        payment.updateStatus(newStatus);

        // 5. 결과 반환
        return new ResponsePaymentDTO(payment);
    }

    @Transactional
    public void deletePayment(UUID paymentId, String currentUserId) {
        // [안전장치] 파라미터 null 체크
        if (paymentId == null || currentUserId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_DATA);
        }

        userRepository.findByUsername(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST));

        if (!currentUserId.equals(payment.getOrder().getUserId())) {
            throw new BusinessException(ErrorCode.PAYMENT_ACCESS_DENIED);
        }

        if (payment.getStatus() == PaymentStatus.CANCELLED || payment.isDeleted()) {
            throw new BusinessException(ErrorCode.PAYMENT_ALREADY_CANCELLED);
        }

        payment.cancelPayment(currentUserId);
    }

    /**
     * 내 결제 내역 전체 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public MyPaymentListResponseDto getMyPayments(String currentUserId, Pageable pageable) {

        userRepository.findByUsername(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Pageable validatedPageable = validatePageable(pageable);

        Page<Payment> paymentPage = paymentRepository.findAllByUserId(currentUserId, validatedPageable);

        return new MyPaymentListResponseDto(paymentPage);
    }

    /**
     * 특정 가게별 결제 목록 조회
     */
    @Transactional(readOnly = true)
    public StorePaymentListResponseDto getStorePayments(UUID storeId, String currentUserId, Pageable pageable) {
        User currentUser = userRepository.findByUsername(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        UserRole dbRole = currentUser.getUserRole();

        if (UserRole.CUSTOMER == dbRole) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        if (UserRole.OWNER == dbRole) {
            if (store.getOwner() == null || !currentUserId.equals(store.getOwner().getUsername())) {
                throw new BusinessException(ErrorCode.STORE_ACCESS_DENIED);
            }
        }

        Pageable validatedPageable = validatePageable(pageable);
        Page<Payment> paymentPage = paymentRepository.findAllByStoreId(storeId, validatedPageable);

        return new StorePaymentListResponseDto(paymentPage);
    }

    // ==========================================
    //  헬퍼 메서드: 페이징 사이즈 검증 (10, 30, 50 고정)
    // ==========================================
    private Pageable validatePageable(Pageable pageable) {
        int size = pageable.getPageSize();
        if (size != 10 && size != 30 && size != 50) {
            size = 10; // 허용되지 않은 값이면 기본값 10으로 강제 조정
        }
        // 정렬 조건(Sort)은 유지하면서 사이즈만 변경하여 새로운 PageRequest 반환
        return PageRequest.of(pageable.getPageNumber(), size, pageable.getSort());
    }
}
