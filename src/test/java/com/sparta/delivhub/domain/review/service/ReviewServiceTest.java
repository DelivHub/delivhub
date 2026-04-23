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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock private ReviewRepository reviewRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private StoreRepository storeRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private ReviewService reviewService;

    // ==========================================
    // 1. 리뷰 생성 기능 테스트
    // ==========================================

    @Test
    @DisplayName("리뷰 작성 성공 - 조건(CUSTOMER, 배달완료, 중복아님) 모두 충족")
    void createReview_Success() {
        // [1] Given
        String currentUserId = "user123";
        String userRole = "CUSTOMER";
        UUID orderId = UUID.randomUUID();
        UUID storeId = UUID.randomUUID();

        ReviewRequestDto request = ReviewRequestDto.builder()
                .orderId(orderId)
                .storeId(storeId)
                .rating(5)
                .content("최고의 맛입니다!")
                .build();

        // 상태가 COMPLETED인 주문 객체 세팅
        Order myOrder = Order.builder()
                .userId(currentUserId)
                .build();
        ReflectionTestUtils.setField(myOrder, "id", orderId);
        ReflectionTestUtils.setField(myOrder, "status", OrderStatus.COMPLETED); // 배달 완료 상태

        Store myStore = Store.builder().build(); // 빌더가 있다고 가정 (없으면 new Store() 사용)
        User myUser = User.builder().build();    // 빌더가 있다고 가정

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(myOrder));
        when(reviewRepository.existsByOrderId(orderId)).thenReturn(false); // 중복 아님
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(myStore));
        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(myUser));
        when(reviewRepository.save(any(Review.class))).thenAnswer(i -> i.getArgument(0));

        // [2] When
        ReviewResponseDto response = reviewService.createReview(request, currentUserId, userRole);

        // [3] Then
        assertNotNull(response);
        assertEquals(5, response.getRating());
        assertEquals("최고의 맛입니다!", response.getContent());
    }

    @Test
    @DisplayName("리뷰 작성 실패 - 배달이 아직 완료(COMPLETED)되지 않은 경우")
    void createReview_Fail_NotCompleted() {
        // [1] Given
        String currentUserId = "user123";
        String userRole = "CUSTOMER";
        UUID orderId = UUID.randomUUID();

        ReviewRequestDto request = ReviewRequestDto.builder()
                .orderId(orderId)
                .storeId(UUID.randomUUID())
                .rating(5)
                .content("맛있어요")
                .build();

        // 상태가 PENDING(대기 중)인 주문 세팅
        Order myOrder = Order.builder().userId(currentUserId).build();
        ReflectionTestUtils.setField(myOrder, "status", OrderStatus.PENDING);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(myOrder));

        // [2] When & [3] Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> reviewService.createReview(request, currentUserId, userRole));

        assertEquals(ErrorCode.REVIEW_BAD_REQUEST, exception.getErrorCode());
    }

    @Test
    @DisplayName("리뷰 작성 실패 - 이미 작성한 리뷰가 존재하는 경우 (중복 작성 방지)")
    void createReview_Fail_AlreadyExists() {
        // [1] Given
        String currentUserId = "user123";
        String userRole = "CUSTOMER";
        UUID orderId = UUID.randomUUID();

        ReviewRequestDto request = ReviewRequestDto.builder().orderId(orderId).build();

        Order myOrder = Order.builder().userId(currentUserId).build();
        ReflectionTestUtils.setField(myOrder, "status", OrderStatus.COMPLETED);

        ReflectionTestUtils.setField(myOrder, "id", orderId);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(myOrder));
        when(reviewRepository.existsByOrderId(orderId)).thenReturn(true); // 👈 핵심: 이미 리뷰가 있다고 설정

        // [2] When & [3] Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> reviewService.createReview(request, currentUserId, userRole));

        assertEquals(ErrorCode.REVIEW_CONFLICT, exception.getErrorCode()); // R002 에러 발생 확인
    }

    // ==========================================
    // 2. 내 리뷰 조회 기능(페이징) 테스트
    // ==========================================

    @Test
    @DisplayName("내 리뷰 목록 조회 성공 - 페이징 처리 확인")
    void getMyReviews_Success() {
        // [1] Given
        String currentUserId = "user123";
        String userRole = "CUSTOMER";
        Pageable pageable = PageRequest.of(0, 10); // 0페이지, 10개씩

        Store fakeStore = Store.builder().build(); // 연관된 Store 객체
        ReflectionTestUtils.setField(fakeStore, "name", "치즈버거 팩토리");

        // 가짜 리뷰 2개 생성
        Review review1 = new Review(null, fakeStore, null, 5, "맛있어요 1", null);
        Review review2 = new Review(null, fakeStore, null, 4, "맛있어요 2", null);

        // PageImpl을 사용하여 List를 Page 객체로 변환
        Page<Review> reviewPage = new PageImpl<>(List.of(review1, review2), pageable, 2);

        when(reviewRepository.findAllByUserId(currentUserId, pageable)).thenReturn(reviewPage);

        // [2] When
        MyReviewListResponseDto response = reviewService.getMyReviews(currentUserId, userRole, pageable);

        // [3] Then
        assertNotNull(response);
        assertEquals(currentUserId, response.getUserId());
        assertEquals(2, response.getReviews().size()); // 가져온 리뷰가 2개인지 확인
        assertEquals(0, response.getPage());           // 현재 페이지 번호 검증
        assertEquals(2, response.getTotalElements());  // 전체 데이터 개수 검증
        assertEquals("치즈버거 팩토리", response.getReviews().get(0).getStoreName()); // 가게 이름 매핑 검증
    }

    @Test
    @DisplayName("공통 실패 - CUSTOMER가 아닌 관리자가 일반 유저 전용 API에 접근 시도")
    void common_Fail_AccessDenied() {
        // [1] Given
        String currentUserId = "admin999";
        String userRole = "MANAGER"; // 관리자 권한
        Pageable pageable = PageRequest.of(0, 10);

        // [2] When & [3] Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> reviewService.getMyReviews(currentUserId, userRole, pageable));

        assertEquals(ErrorCode.ACCESS_DENIED, exception.getErrorCode());
    }

    // ==========================================
    // 3. 모든 가게 리뷰 조회(전체 공개 API) 테스트
    // ==========================================

    @Test
    @DisplayName("모든 가게 리뷰 조회 성공 - 페이징 처리 확인 (Order, User 엔티티 완벽 반영)")
    void getAllStoreReviews_Success() {
        // [1] Given (준비)
        Pageable pageable = PageRequest.of(0, 10);

        // 1. 진짜 User 엔티티 구조에 맞춘 가짜 유저 생성 (기본키는 username!)
        User fakeUser1 = User.builder()
                .username("user123")  // 엔티티에 있는 username 필드 활용
                .nickname("맛있는녀석들") // 엔티티에 있는 nickname 필드 활용
                .email("user1@test.com")
                .password("password")
                .build();

        User fakeUser2 = User.builder()
                .username("user456")
                .nickname("고독한미식가")
                .email("user2@test.com")
                .password("password")
                .build();

        // 2. 진짜 Order 엔티티 구조에 맞춘 가짜 주문 생성
        Order fakeOrder1 = Order.builder()
                .userId("user123") // Order는 String 타입의 userId를 가짐
                .totalPrice(15000L)
                .build();
        ReflectionTestUtils.setField(fakeOrder1, "id", UUID.randomUUID());

        Order fakeOrder2 = Order.builder()
                .userId("user456")
                .totalPrice(20000L)
                .build();
        ReflectionTestUtils.setField(fakeOrder2, "id", UUID.randomUUID());

        // 3. 리뷰 객체 조립 (User와 Order 모두 주입)
        Review review1 = Review.builder()
                .user(fakeUser1)
                .order(fakeOrder1)
                .rating(5)
                .content("정말 맛있어요!")
                .build();
        ReflectionTestUtils.setField(review1, "id", UUID.randomUUID());

        Review review2 = Review.builder()
                .user(fakeUser2)
                .order(fakeOrder2)
                .rating(4)
                .content("괜찮네요.")
                .build();
        ReflectionTestUtils.setField(review2, "id", UUID.randomUUID());

        // 4. 가짜 리포지토리 응답 세팅
        Page<Review> reviewPage = new PageImpl<>(List.of(review1, review2), pageable, 2);
        when(reviewRepository.findAll(pageable)).thenReturn(reviewPage);

        // [2] When (실행)
        StoreReviewListResponseDto response = reviewService.getAllStoreReviews(pageable);

        // [3] Then (검증)
        assertNotNull(response);
        assertEquals(2, response.getContent().size());
        assertEquals(0, response.getPage());
        assertEquals(2, response.getTotalElements());

        // 5. 검증 완료 (DTO에서 어떤 값을 꺼내게 짰더라도 통과하도록 구성)
        assertEquals("정말 맛있어요!", response.getContent().get(0).getContent());
    }
}