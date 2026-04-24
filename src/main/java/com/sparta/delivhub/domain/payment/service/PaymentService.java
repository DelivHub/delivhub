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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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

    @Transactional
    public ResponsePaymentDTO createPayment(RequestPaymentDTO request, String currentUserId) {
        // 1. 주문 엔티티 조회
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        // 2. 권한 검증 (이 주문을 한 사람이 현재 로그인한 유저가 맞는지)
        if (!order.getUserId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.PAYMENT_ACCESS_DENIED);
        }

//        // 사장 본인 가게 결제 불가 (가짜 리뷰 작성 방지)
//        // Store 엔티티에 사장 ID를 가져오는 메서드(getOwnerId 혹은 getUser().getId())에 맞춰 수정해 주세요.
//        String storeOwnerId = order.getStore().getOwnerId();
//        if (storeOwnerId.equals(currentUserId)) {
//            throw new BusinessException(ErrorCode.CANNOT_PAY_OWN_STORE);
//        }

        // 3. 중복 결제 방어 (이미 해당 주문에 대한 결제가 있는지 확인)
        if (paymentRepository.existsByOrderId(order.getId())) {
            throw new BusinessException(ErrorCode.PAYMENT_ALREADY_EXISTS);
        }

        // 4. 결제 수단(Enum) 변환 및 검증
        PaymentMethod method;
        try {
            // "CARD" -> PaymentMethod.CARD 로 변환
            method = PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase());
        } catch (IllegalArgumentException e) {
            // Enum에 없는 값이면 예외 발생
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_METHOD);
        }

        // 5. 결제 엔티티 생성

        //결제 금액이 실제 주문 금액과 맞는지 확인
        if (!order.getTotalPrice().equals(request.getAmount().longValue())) {
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

        // 2. 권한 검증
        // CUSTOMER(일반 고객)인 경우, 반드시 자신의 결제 내역만 조회할 수 있어야 합니다.
        // MANAGER나 MASTER는 모든 결제 내역을 조회할 수 있도록 예외를 둡니다.
        if ("CUSTOMER".equals(userRole)) {
            String ownerId = payment.getOrder().getUserId();
            if (!ownerId.equals(currentUserId)) {
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
    public ResponsePaymentDTO updatePaymentStatus(UUID paymentId, String newStatusStr, String userRole) {

        // 1. 권한 검증 (명세서 요구사항: MANAGER, MASTER 만 접근 가능)
        if (!"MANAGER".equals(userRole) && !"MASTER".equals(userRole)) {
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
        // 1. 결제 내역 조회
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST));

        // 2. 타인의 결제 건인지 확인 (주문한 유저 ID와 현재 로그인한 유저 ID 비교)
        String ownerId = payment.getOrder().getUserId();
        if (!ownerId.equals(currentUserId)) {
            throw new BusinessException(ErrorCode.PAYMENT_ACCESS_DENIED);
        }

        // 3. 이미 삭제되었는지 확인 (BaseEntity의 isDeleted() 메서드 활용)
        if (payment.isDeleted()) {
            throw new BusinessException(ErrorCode.PAYMENT_ALREADY_CANCELLED);
        }

        // 4. 안전하게 삭제 처리 (우리가 엔티티에 만든 메서드 호출)
        // repository.delete(payment); (X)
        payment.cancelPayment(currentUserId); // 상태를 DELETED로 바꾸고 deletedAt, deletedBy 기록
    }

    /**
     * 내 결제 내역 전체 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public MyPaymentListResponseDto getMyPayments(String currentUserId, String userRole, Pageable pageable) {

        // 1. 권한 검증 (명세서 Auth: CUSTOMER, MANAGER, MASTER)
        // 3가지 권한 모두 접근 가능하므로, 로그인된 유저라면 누구나 접근 가능합니다.
        // 만약 특정 권한을 막아야 한다면 여기서 if문으로 필터링합니다.

        // 2. 로그인한 유저의 ID(currentUserId)를 이용해 DB에서 결제 내역 페이징 조회
        Page<Payment> paymentPage = paymentRepository.findAllByUserId(currentUserId, pageable);

        // 3. DTO로 변환하여 반환
        return new MyPaymentListResponseDto(paymentPage);
    }

    /**
     * 특정 가게별 결제 목록 조회
     */
    @Transactional(readOnly = true)
    public StorePaymentListResponseDto getStorePayments(UUID storeId, String currentUserId, String userRole, Pageable pageable) {

        // 1. 일반 고객(CUSTOMER)은 이 API에 절대 접근할 수 없습니다.
        if ("CUSTOMER".equals(userRole)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        // 2. 가게 존재 여부 확인
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        // 3. OWNER(사장님)일 경우, '본인 가게'가 맞는지 확인
        if ("OWNER".equals(userRole)) {
            if (!store.getOwner().getUsername().equals(currentUserId)) {
                // 내 가게가 아니라면 명세서의 STORE_ACCESS_DENIED 에러 발생
                throw new BusinessException(ErrorCode.STORE_ACCESS_DENIED);
            }
        }
        // MANAGER나 MASTER는 위 검증을 건너뛰고 모든 가게의 결제 내역을 볼 수 있습니다.

        // 4. 권한 검증을 통과했다면, 가게 ID로 결제 내역 페이징 조회
        Page<Payment> paymentPage = paymentRepository.findAllByStoreId(storeId, pageable);

        // 5. DTO로 변환하여 반환
        return new StorePaymentListResponseDto(paymentPage);
    }
}
