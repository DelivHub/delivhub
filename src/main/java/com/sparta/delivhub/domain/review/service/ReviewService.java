package com.sparta.delivhub.domain.review.service;

import com.sparta.delivhub.common.dto.BusinessException;
import com.sparta.delivhub.common.dto.ErrorCode;
import com.sparta.delivhub.domain.order.entity.Order;
import com.sparta.delivhub.domain.order.entity.OrderStatus;
import com.sparta.delivhub.domain.order.repository.OrderRepository;
import com.sparta.delivhub.domain.review.dto.MyReviewListResponseDto;
import com.sparta.delivhub.domain.review.dto.ReviewRequestDto;
import com.sparta.delivhub.domain.review.dto.ReviewResponseDto;
import com.sparta.delivhub.domain.review.dto.StoreReviewListResponseDto;
import com.sparta.delivhub.domain.review.entity.Review;
import com.sparta.delivhub.domain.review.repository.ReviewRepository;
import com.sparta.delivhub.domain.store.entity.Store;
import com.sparta.delivhub.domain.store.repository.StoreRepository;
import com.sparta.delivhub.domain.user.entity.User;
import com.sparta.delivhub.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository; // User 엔티티 조회를 위해 필요

    @Transactional
    public ReviewResponseDto createReview(ReviewRequestDto request, String currentUserId, String userRole) {
        // 1. 권한 검사 (명세서 Auth: CUSTOMER)
        if (!"CUSTOMER".equals(userRole)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        // 2. 주문 내역 조회 및 소유자 확인
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getUserId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        // 3. 주문 상태 확인 (COMPLETED 인지 확인)
        // 주의: 프로젝트의 OrderStatus Enum에 "COMPLETED" (또는 DELIVERED)가 있다고 가정합니다.
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.REVIEW_BAD_REQUEST);
        }

        // 4. 리뷰 중복 작성 방지 (이미 이 주문으로 작성한 리뷰가 있는지 검사)
        if (reviewRepository.existsByOrderId(order.getId())) {
            throw new BusinessException(ErrorCode.REVIEW_CONFLICT);
        }

        // 5. 연관된 Store, User 엔티티 조회
        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        User user = userRepository.findById(currentUserId)
                // 만약 userId 타입이 UUID이거나 Long이면 타입 변환이 필요할 수 있습니다.
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 6. 리뷰 엔티티 생성 및 저장
        Review review = new Review(
                order,
                store,
                user,
                request.getRating(),
                request.getContent(),
                request.getImageUrl()
        );
        Review savedReview = reviewRepository.save(review);

        // 7. DTO로 변환하여 반환
        return new ReviewResponseDto(savedReview);
    }

    /**
     * 내 리뷰 목록 조회 (페이징)
     */
    @Transactional(readOnly = true) // 단순 조회용이므로 성능 최적화를 위해 readOnly 적용
    public MyReviewListResponseDto getMyReviews(String currentUserId, String userRole, Pageable pageable) {

        // 1. 권한 검사 (CUSTOMER 만 조회 가능)
        if (!"CUSTOMER".equals(userRole)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        // 2. DB에서 페이징된 리뷰 데이터 가져오기
        Page<Review> reviewPage = reviewRepository.findAllByUserId(currentUserId, pageable);

        // 3. DTO로 묶어서 반환
        return new MyReviewListResponseDto(currentUserId, reviewPage);
    }

    /**
     * 모든 가게 리뷰 조회 (전체 공개 API)
     */
    @Transactional(readOnly = true)
    public StoreReviewListResponseDto getAllStoreReviews(Pageable pageable) {

        // DB에서 모든 리뷰를 페이징하여 가져옵니다.
        Page<Review> reviewPage = reviewRepository.findAll(pageable);

        // DTO로 변환하여 반환
        return new StoreReviewListResponseDto(reviewPage);
    }
}