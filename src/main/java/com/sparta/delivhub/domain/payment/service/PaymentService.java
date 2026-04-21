package com.sparta.delivhub.domain.payment.service;

import com.sparta.delivhub.common.dto.BusinessException;
import com.sparta.delivhub.common.dto.ErrorCode;
import com.sparta.delivhub.domain.order.Entity.Order;
import com.sparta.delivhub.domain.order.repository.OrderRepository;
import com.sparta.delivhub.domain.payment.dto.RequestPaymentDTO;
import com.sparta.delivhub.domain.payment.dto.ResponsePaymentDTO;
import com.sparta.delivhub.domain.payment.entity.Payment;
import com.sparta.delivhub.domain.payment.entity.PaymentMethod;
import com.sparta.delivhub.domain.payment.entity.PaymentStatus;
import com.sparta.delivhub.domain.payment.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository; // 주문 확인을 위해 필요

    @Transactional
    public ResponsePaymentDTO createPayment(RequestPaymentDTO request, String currentUserId) {
        // 1. 주문 엔티티 조회
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        // 2. 권한 검증 (이 주문을 한 사람이 현재 로그인한 유저가 맞는지)
        if (!order.getUser().getId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.PAYMENT_ACCESS_DENIED);
        }

        // 사장 본인 가게 결제 불가 (가짜 리뷰 작성 방지)
        // Store 엔티티에 사장 ID를 가져오는 메서드(getOwnerId 혹은 getUser().getId())에 맞춰 수정해 주세요.
        String storeOwnerId = order.getStore().getOwnerId();
        if (storeOwnerId.equals(currentUserId)) {
            throw new BusinessException(ErrorCode.CANNOT_PAY_OWN_STORE);
        }

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

        // 5. 결제 엔티티 생성 (명세서 요구사항에 따라 상태를 COMPLETED로 고정)
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


    @Transactional
    public void deletePayment(UUID paymentId, String currentUserId) {
        // 1. 결제 내역 조회
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 결제 내역입니다."));

        // 2. 타인의 결제 건인지 확인 (주문한 유저 ID와 현재 로그인한 유저 ID 비교)
        String ownerId = payment.getOrder().getUser().getId();
        if (!ownerId.equals(currentUserId)) {
            throw new BusinessException(ErrorCode.PAYMENT_ACCESS_DENIED);
        }

        // 3. 이미 삭제되었는지 확인 (BaseEntity의 isDeleted() 메서드 활용)
        if (payment.isDeleted()) {
            throw new BusinessException(ErrorCode.PAYMENT_BAD_REQUEST);
        }

        // 4. 안전하게 삭제 처리 (우리가 엔티티에 만든 메서드 호출)
        // repository.delete(payment); (X) 이렇게 하면 DB에서 아예 날아가거나 JPA가 당황합니다.
        payment.cancelPayment(currentUserId); // 상태를 DELETED로 바꾸고 deletedAt, deletedBy 기록
    }
}
