package com.sparta.delivhub.domain.review.service;

import com.sparta.delivhub.common.dto.BusinessException;
import com.sparta.delivhub.common.dto.ErrorCode;
import com.sparta.delivhub.domain.order.service.entity.Order;
import com.sparta.delivhub.domain.order.service.entity.OrderStatus;
import com.sparta.delivhub.domain.order.service.repository.OrderRepository;
import com.sparta.delivhub.domain.review.dto.*;
import com.sparta.delivhub.domain.review.entity.Review;
import com.sparta.delivhub.domain.review.repository.ReviewRepository;
import com.sparta.delivhub.domain.store.entity.Store;
import com.sparta.delivhub.domain.store.repository.StoreRepository;
import com.sparta.delivhub.domain.user.entity.User;
import com.sparta.delivhub.domain.user.entity.UserRole;
import com.sparta.delivhub.domain.user.repository.UserRepository;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock private ReviewRepository reviewRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private StoreRepository storeRepository;
    @Mock private UserRepository userRepository; // ✨ 필수 추가: 가짜 유저 저장소

    @InjectMocks
    private ReviewService reviewService;

    // ==========================================
    // 💡 [유용한 헬퍼 메서드] 가짜 유저와 권한을 쉽게 만들어주는 공장
    // ==========================================
    private User createMockUser(String username, UserRole role) {
        // ✨ DEEP_STUBS 제거: 이제 아주 단순하고 깔끔한 가짜 객체를 만듭니다.
        User mockUser = mock(User.class);

        lenient().when(mockUser.getUsername()).thenReturn(username);

        // ✨ Enum 객체 자체를 반환하도록 설정해두면, 서비스 로직에서
        // user.getUserRole().name()을 호출해도 알아서 예쁘게 String으로 변환해줍니다!
        lenient().when(mockUser.getUserRole()).thenReturn(role);

        // (기존에 있던 name() 관련 꼬임 유발 코드는 삭제했습니다)

        return mockUser;
    }

    // ==========================================
    // 1. 리뷰 생성 기능 테스트
    // ==========================================

    @Test
    @DisplayName("리뷰 작성 성공 - 조건(CUSTOMER, 배달완료, 중복아님) 모두 충족")
    void createReview_Success() {
        // [1] Given
        String currentUserId = "user123";
        String userRoleStr = "CUSTOMER";
        UUID orderId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        UUID savedReviewId = UUID.randomUUID();

        // ✨ 보안 강화 로직 통과를 위한 유저 세팅
        User myUser = createMockUser(currentUserId, UserRole.CUSTOMER);
        when(userRepository.findByUsername(currentUserId)).thenReturn(Optional.of(myUser));

        ReviewRequestDto request = ReviewRequestDto.builder()
                .orderId(orderId)
                .storeId(storeId)
                .rating(5)
                .content("최고의 맛입니다!")
                .build();

        Order myOrder = Order.builder().userId(currentUserId).build();
        ReflectionTestUtils.setField(myOrder, "id", orderId);
        ReflectionTestUtils.setField(myOrder, "status", OrderStatus.COMPLETED);

        Store myStore = Store.builder().build();
        ReflectionTestUtils.setField(myStore, "id", storeId);

        Review savedReview = Review.builder().rating(5).content("최고의 맛입니다!").build();
        ReflectionTestUtils.setField(savedReview, "id", savedReviewId);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(myOrder));
        when(reviewRepository.existsByOrderId(orderId)).thenReturn(false);
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(myStore));
        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);
        when(reviewRepository.calculateAverageRatingByStoreId(storeId)).thenReturn(5.0); // ✨ 평균 별점 계산 추가

        // [2] When
        ReviewResponseDto response = reviewService.createReview(request, currentUserId, userRoleStr);

        // [3] Then
        assertNotNull(response);
        assertEquals(savedReviewId, response.getReviewId());
        assertEquals(5, response.getRating());
    }

    @Test
    @DisplayName("리뷰 작성 실패 - 배달이 아직 완료(COMPLETED)되지 않은 경우")
    void createReview_Fail_NotCompleted() {
        // [1] Given
        String currentUserId = "user123";
        String userRoleStr = "CUSTOMER";
        UUID orderId = UUID.randomUUID();

        User myUser = createMockUser(currentUserId, UserRole.CUSTOMER);
        when(userRepository.findByUsername(currentUserId)).thenReturn(Optional.of(myUser));

        ReviewRequestDto request = ReviewRequestDto.builder().orderId(orderId).storeId(UUID.randomUUID()).build();

        Order myOrder = Order.builder().userId(currentUserId).build();
        ReflectionTestUtils.setField(myOrder, "status", OrderStatus.PENDING);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(myOrder));

        // [2] & [3]
        BusinessException exception = assertThrows(BusinessException.class,
                () -> reviewService.createReview(request, currentUserId, userRoleStr));

        assertEquals(ErrorCode.REVIEW_BAD_REQUEST, exception.getErrorCode());
    }

    // ==========================================
    // 2. 내 리뷰 조회 기능(페이징) 테스트
    // ==========================================

    @Test
    @DisplayName("내 리뷰 목록 조회 성공 - 페이징 처리 확인")
    void getMyReviews_Success() {
        // [1] Given
        String currentUserId = "user123";
        String userRoleStr = "CUSTOMER";
        Pageable pageable = PageRequest.of(0, 10);

        User myUser = createMockUser(currentUserId, UserRole.CUSTOMER);
        when(userRepository.findByUsername(currentUserId)).thenReturn(Optional.of(myUser));

        Store fakeStore = Store.builder().build();
        ReflectionTestUtils.setField(fakeStore, "name", "치즈버거 팩토리");

        Review review1 = new Review(null, fakeStore, null, 5, "맛있어요 1", null);
        Review review2 = new Review(null, fakeStore, null, 4, "맛있어요 2", null);
        Page<Review> reviewPage = new PageImpl<>(List.of(review1, review2), pageable, 2);

        when(reviewRepository.findAllByUserId(currentUserId, pageable)).thenReturn(reviewPage);

        // [2] When
        MyReviewListResponseDto response = reviewService.getMyReviews(currentUserId, userRoleStr, pageable);

        // [3] Then
        assertNotNull(response);
        assertEquals(currentUserId, response.getUserId());
        assertEquals(2, response.getReviews().size());
    }

    // ==========================================
    // 3. 모든 가게 리뷰 조회(전체 공개 API) 테스트
    // ==========================================

    @Test
    @DisplayName("모든 가게 리뷰 조회 성공 - 페이징 처리 확인")
    void getAllStoreReviews_Success() {
        // [1] Given
        Pageable pageable = PageRequest.of(0, 10);

        User fakeUser = createMockUser("user123", UserRole.CUSTOMER);
        lenient().when(fakeUser.getNickname()).thenReturn("맛있는녀석들"); // 닉네임 세팅

        Order fakeOrder = Order.builder().userId("user123").totalPrice(15000L).build();
        ReflectionTestUtils.setField(fakeOrder, "id", UUID.randomUUID());

        Review review1 = Review.builder().user(fakeUser).order(fakeOrder).rating(5).content("정말 맛있어요!").build();
        ReflectionTestUtils.setField(review1, "id", UUID.randomUUID());

        Page<Review> reviewPage = new PageImpl<>(List.of(review1), pageable, 1);
        when(reviewRepository.findAll(pageable)).thenReturn(reviewPage);

        // [2] When
        StoreReviewListResponseDto response = reviewService.getAllStoreReviews(pageable);

        // [3] Then
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
    }

    // ==========================================
    // 4. 리뷰 수정 기능 테스트
    // ==========================================

    @Test
    @DisplayName("리뷰 수정 성공 - 본인이 작성한 리뷰 수정 (평균 평점 갱신 포함)")
    void updateReview_Success() {
        // [1] Given
        UUID reviewId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        String currentUserId = "user123";
        String userRoleStr = "CUSTOMER";

        User myUser = createMockUser(currentUserId, UserRole.CUSTOMER);
        when(userRepository.findByUsername(currentUserId)).thenReturn(Optional.of(myUser));

        ReviewUpdateRequestDto request = ReviewUpdateRequestDto.builder().rating(4).content("수정완료").build();

        Store store = Store.builder().build();
        ReflectionTestUtils.setField(store, "id", storeId);

        Review myReview = Review.builder().user(myUser).store(store).rating(5).content("완벽해요!").build();
        ReflectionTestUtils.setField(myReview, "id", reviewId);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(myReview));
        when(reviewRepository.calculateAverageRatingByStoreId(storeId)).thenReturn(4.0);

        // [2] When
        ReviewResponseDto response = reviewService.updateReview(reviewId, request, currentUserId, userRoleStr);

        // [3] Then
        assertNotNull(response);
        assertEquals(4, response.getRating());
        assertEquals("수정완료", response.getContent());
    }

    // ==========================================
    // 5. 리뷰 삭제(소프트 딜리트) 기능 테스트
    // ==========================================

    @Test
    @DisplayName("리뷰 삭제 성공 - 본인(CUSTOMER)이 작성한 리뷰 삭제")
    void deleteReview_Success_Customer() {
        // [1] Given
        UUID reviewId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();
        String currentUserId = "user123";

        User myUser = createMockUser(currentUserId, UserRole.CUSTOMER);
        when(userRepository.findByUsername(currentUserId)).thenReturn(Optional.of(myUser));

        Store store = Store.builder().build();
        ReflectionTestUtils.setField(store, "id", storeId);

        Review myReview = Review.builder().user(myUser).store(store).build();
        ReflectionTestUtils.setField(myReview, "id", reviewId);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(myReview));
        when(reviewRepository.calculateAverageRatingByStoreId(storeId)).thenReturn(0.0);

        // [2] When
        reviewService.deleteReview(reviewId, currentUserId, "CUSTOMER"); // 파라미터는 하위 호환

        // [3] Then
        assertNotNull(myReview.getDeletedAt());
        assertEquals("user123", myReview.getDeletedBy());
    }

    // ==========================================
    // 6. 특정 가게별 리뷰 조회 (평균 평점 포함) 기능 테스트
    // ==========================================

    @Test
    @DisplayName("가게별 리뷰 조회 성공 - 평균 평점과 리뷰 목록 반환")
    void getReviewsByStore_Success() {
        // [1] Given
        UUID storeId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        Store store = Store.builder().averageRating(new java.math.BigDecimal("4.8")).build();
        ReflectionTestUtils.setField(store, "id", storeId);

        User customer = createMockUser("user123", UserRole.CUSTOMER);
        lenient().when(customer.getNickname()).thenReturn("맛있는녀석들");

        Review review1 = Review.builder().user(customer).store(store).rating(5).content("최고예요!").build();
        ReflectionTestUtils.setField(review1, "id", UUID.randomUUID());

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));
        Page<Review> reviewPage = new PageImpl<>(List.of(review1), pageable, 1);
        when(reviewRepository.findAllByStoreId(storeId, pageable)).thenReturn(reviewPage);

        // [2] When
        StoreReviewPageResponseDto response = reviewService.getReviewsByStore(storeId, pageable);

        // [3] Then
        assertNotNull(response);
        assertEquals(new java.math.BigDecimal("4.8"), response.getAverageRating());
        assertEquals(1, response.getContent().size());
        assertEquals("맛있는녀석들", response.getContent().get(0).getUserNickname());
    }
}