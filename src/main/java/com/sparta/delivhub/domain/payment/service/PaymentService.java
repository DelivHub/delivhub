package com.sparta.delivhub.domain.payment.service;

import com.sparta.delivhub.common.dto.BusinessException;
import com.sparta.delivhub.common.dto.ErrorCode;
import com.sparta.delivhub.domain.order.service.entity.Order;
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

    @Transactional
    public ResponsePaymentDTO createPayment(RequestPaymentDTO request, String currentUserId) {
        // [보안 강화] DB 실시간 조회: 유저가 존재하는지, 권한이 강등/삭제되지는 않았는지 재검증
        User currentUser = userRepository.findByUsername(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!"CUSTOMER".equals(currentUser.getUserRole().name())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        // 1. 주문 엔티티 조회
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!currentUserId.equals(order.getUserId())) {
            throw new BusinessException(ErrorCode.PAYMENT_ACCESS_DENIED);
        }

        if (paymentRepository.existsByOrderId(order.getId())) {
            throw new BusinessException(ErrorCode.PAYMENT_ALREADY_EXISTS);
        }

        // 2. 권한 검증 (이 주문을 한 사람이 현재 로그인한 유저가 맞는지)
        if (UserRole.CUSTOMER != currentUser.getUserRole()) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        // 3. 중복 결제 방어 (이미 해당 주문에 대한 결제가 있는지 확인)
        if (paymentRepository.existsByOrderId(order.getId())) {
            throw new BusinessException(ErrorCode.PAYMENT_ALREADY_EXISTS);
        }

        // 4. 결제 수단(Enum) 변환 및 검증 (CARD만 허용)
        PaymentMethod method;
        try {
            method = PaymentMethod.valueOf(request.getPaymentMethod().trim().toUpperCase());

            // CARD가 아닌 결제 수단은 거부
            if (request.getPaymentMethod() == null || method != PaymentMethod.CARD) {
                throw new BusinessException(ErrorCode.INVALID_PAYMENT_METHOD);
            }
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_METHOD);
        }

        // 5. 결제 엔티티 생성

        //결제 금액이 실제 주문 금액과 맞는지 확인
        if (!order.getTotalPrice().equals(request.getAmount())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_DATA);
        }
        Payment payment = new Payment(
                order,
                request.getAmount(),
                method,
                PaymentStatus.COMPLETED
        );

        // 6. 저장
        Payment savedPayment = paymentRepository.save(payment);

        // 7. DTO로 변환
        return new ResponsePaymentDTO(savedPayment);
    }

    @Transactional(readOnly = true)
    public ResponsePaymentDTO getPayment(UUID paymentId, String currentUserId, String userRole) {
        // 1. 결제 내역 조회
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST));

        // ✨ [보안 강화] 토큰의 Role 대신 DB의 최신 Role을 사용하여 검증
        User currentUser = userRepository.findByUsername(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        String dbRole = currentUser.getUserRole().name();

        // 2. 권한 검증
        // CUSTOMER(일반 고객)인 경우, 반드시 자신의 결제 내역만 조회할 수 있어야 합니다.
        // MANAGER나 MASTER는 모든 결제 내역을 조회할 수 있도록 예외를 둡니다.
        // [안전장치] DB Role 직접 비교 (Enum 활용)
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

         // [보안 강화] 토큰의 Role이 아닌, DB에서 방금 꺼내온 확실한 Role로 검증
        User currentUser = userRepository.findByUsername(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        String dbRole = currentUser.getUserRole().name();

        // 1. 권한 검증 (명세서 요구사항: MANAGER, MASTER 만 접근 가능)
        if (!"MANAGER".equals(dbRole) && !"MASTER".equals(dbRole)) {
            // 이전에 정의한 에러코드 (예: ACCESS_DENIED 혹은 PAYMENT_ACCESS_DENIED)
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
            // "CANCELLED", "COMPLETED" 등 약속된 상태값이 아닐 경우 에러
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // 4. 상태 변경 (엔티티 내부의 변경 메서드 호출)
        payment.updateStatus(newStatus);

        // 5. 결과 반환
        // @Transactional 덕분에 코드가 끝날 때 JPA가 알아서 DB에 UPDATE 쿼리를 날려줍니다. (더티 체킹)
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

        // [안전장치] 소유자 확인 (currentUserId를 앞쪽으로)
        if (!currentUserId.equals(payment.getOrder().getUserId())) {
            throw new BusinessException(ErrorCode.PAYMENT_ACCESS_DENIED);
        }

        // [안전장치] 이미 취소된 결제는 다시 취소할 수 없음
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

        // [안전장치] OWNER 권한일 때 본인 가게인지 확인
        if (UserRole.OWNER == dbRole) {
            // [안전장치] store.getOwner()가 null일 경우를 대비한 체이닝 방어
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
