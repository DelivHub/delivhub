package com.sparta.delivhub.domain.review.service;

import com.sparta.delivhub.common.dto.BusinessException;
import com.sparta.delivhub.common.dto.ErrorCode;
import com.sparta.delivhub.domain.order.entity.Order;
import com.sparta.delivhub.domain.order.entity.OrderStatus;
import com.sparta.delivhub.domain.order.repository.OrderRepository;
import com.sparta.delivhub.domain.review.dto.*;
import com.sparta.delivhub.domain.review.entity.Review;
import com.sparta.delivhub.domain.review.repository.ReviewRepository;
import com.sparta.delivhub.domain.store.dto.response.StoreReviewPageResponseDto;
import com.sparta.delivhub.domain.store.entity.Store;
import com.sparta.delivhub.domain.store.repository.StoreRepository;
import com.sparta.delivhub.domain.user.entity.User;
import com.sparta.delivhub.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

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

        //가게의 평균 별점 갱신
        updateStoreAverageRating(store);

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

    /**
     * 리뷰 수정
     */
    @Transactional
    public ReviewResponseDto updateReview(UUID reviewId, ReviewUpdateRequestDto request, String currentUserId, String userRole) {

        // 1. 권한 1차 검증 (CUSTOMER 만 접근 가능)
        if (!"CUSTOMER".equals(userRole)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        // 2. 수정할 리뷰 찾기 (없으면 404 에러)
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

        // 3. 본인 확인 (명세서의 403 에러 방어)
        if (!review.getUser().getUsername().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        // 4. 리뷰 데이터 변경 (더티 체킹 발생)
        review.updateReview(request.getRating(), request.getContent());
        reviewRepository.flush();

        //가게의 평균 별점 갱신
        Store store = review.getStore();
        updateStoreAverageRating(store);

        // 5. 기존에 만들어둔 ReviewResponseDto를 재활용하여 반환
        return new ReviewResponseDto(review);
    }

    /**
     * 리뷰 삭제 (소프트 딜리트)
     */
    @Transactional
    public void deleteReview(UUID reviewId, String currentUserId, String userRole) {

        // 1. 리뷰 조회
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

        // 2. 권한 검증 (가장 중요 ⭐️)
        boolean hasPermission = false;

        switch (userRole) {
            case "MASTER":
                // 최고 관리자는 무조건 삭제 가능
                hasPermission = true;
                break;

            case "OWNER":
                // 가게 주인은 '본인 가게에 달린 리뷰'만 삭제 가능
                if (review.getStore().getOwner().getUsername().equals(currentUserId)) {
                    hasPermission = true;
                }
                break;

            case "CUSTOMER":
                // 일반 고객은 '본인이 작성한 리뷰'만 삭제 가능
                if (review.getUser().getUsername().equals(currentUserId)) {
                    hasPermission = true;
                }
                break;
        }

        // 권한이 없으면 403 에러 발생
        if (!hasPermission) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        reviewRepository.flush();

        //가게의 평균 별점 갱신
        Store store = review.getStore();
        updateStoreAverageRating(store);

        // 3. 삭제 처리 (엔티티의 필드 업데이트 -> 더티 체킹)
        //누가 지웠는지(currentUserId)를 기록
        review.softDelete(currentUserId);
    }

    /**
     * 내부 유틸 메서드: 가게의 평균 별점을 다시 계산하고 업데이트
     */
    private void updateStoreAverageRating(Store store) {
        // 1. DB에서 해당 가게의 평균 별점 계산
        Double average = reviewRepository.calculateAverageRatingByStoreId(store.getId());

        // 2. Double을 BigDecimal로 변환 (소수점 첫째 자리까지만 반올림)
        BigDecimal newAverageRating = BigDecimal.valueOf(average)
                .setScale(1, RoundingMode.HALF_UP);

        // 3. Store 엔티티 업데이트 (더티 체킹 발생)
        store.updateAverageRating(newAverageRating);
    }


    /**
     * 특정 가게별 리뷰 목록 조회 (평균 평점 포함)
     */
    @Transactional(readOnly = true)
    public StoreReviewPageResponseDto getReviewsByStore(UUID storeId, Pageable pageable) {

        // 1. 가게 존재 여부 확인 및 정보(평균 평점) 가져오기
        // (명세서 우측의 STORE_NOT_FOUND 404 에러 방어)
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        // 2. 해당 가게에 달린 리뷰 목록 페이징 조회
        Page<Review> reviewPage = reviewRepository.findAllByStoreId(storeId, pageable);

        // 3. DTO 조립 및 반환 (가게에 저장된 평균 평점을 쏙 빼서 같이 넘겨줍니다)
        return new StoreReviewPageResponseDto(store.getAverageRating(), reviewPage);
    }
}