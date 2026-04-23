package com.sparta.delivhub.domain.review.service;

import com.sparta.delivhub.common.dto.BusinessException;
import com.sparta.delivhub.common.dto.ErrorCode;
import com.sparta.delivhub.domain.order.entity.Order;
import com.sparta.delivhub.domain.order.entity.OrderStatus;
import com.sparta.delivhub.domain.order.repository.OrderRepository;
import com.sparta.delivhub.domain.review.dto.*;
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
    // ==========================================
    // 4. 리뷰 수정 기능 테스트
    // ==========================================

    @Test
    @DisplayName("리뷰 수정 성공 - 본인이 작성한 리뷰 수정")
    void updateReview_Success() {
        // [1] Given
        UUID reviewId = UUID.randomUUID();
        String currentUserId = "user123";
        String userRole = "CUSTOMER";

        // 수정할 내용이 담긴 Request DTO
        ReviewUpdateRequestDto request = ReviewUpdateRequestDto.builder()
                .rating(4)
                .content("다시 먹어보니 조금 짜네요. 그래도 맛있습니다.")
                .build();

        // 진짜 주인(user123) 객체 생성
        User owner = User.builder().username("user123").build();

        // 기존에 작성되어 있던 가짜 리뷰 객체 생성 (수정 전 상태: 5점, "완벽해요")
        Review myReview = Review.builder()
                .user(owner)
                .rating(5)
                .content("완벽해요!")
                .build();
        ReflectionTestUtils.setField(myReview, "id", reviewId);

        // 레포지토리 대본: "reviewId로 찾으면 myReview를 줘라"
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(myReview));

        // [2] When
        ReviewResponseDto response = reviewService.updateReview(reviewId, request, currentUserId, userRole);

        // [3] Then
        assertNotNull(response);
        // 별점과 내용이 request 대로 잘 바뀌었는지(더티 체킹 업데이트) 확인
        assertEquals(4, response.getRating());
        assertEquals("다시 먹어보니 조금 짜네요. 그래도 맛있습니다.", response.getContent());
    }

    @Test
    @DisplayName("리뷰 수정 실패 - 로그인한 사용자가 리뷰 작성자가 아닐 때 (403 방어)")
    void updateReview_Fail_NotOwner() {
        // [1] Given
        UUID reviewId = UUID.randomUUID();
        String currentUserId = "hacker999"; // 남의 리뷰를 수정하려는 유저
        String userRole = "CUSTOMER";

        ReviewUpdateRequestDto request = ReviewUpdateRequestDto.builder()
                .rating(1)
                .content("맛없음으로 조작하기")
                .build();

        // 진짜 주인(user123) 객체 생성
        User realOwner = User.builder().username("user123").build();

        // 진짜 주인이 쓴 가짜 리뷰
        Review otherPersonReview = Review.builder()
                .user(realOwner)
                .rating(5)
                .content("정말 맛있어요!")
                .build();
        ReflectionTestUtils.setField(otherPersonReview, "id", reviewId);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(otherPersonReview));

        // [2] When & [3] Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> reviewService.updateReview(reviewId, request, currentUserId, userRole));

        // 명세서에 명시된 대로 권한 없음(FORBIDDEN -> ACCESS_DENIED) 에러가 터지는지 확인
        assertEquals(ErrorCode.ACCESS_DENIED, exception.getErrorCode());
    }

    @Test
    @DisplayName("리뷰 수정 실패 - 존재하지 않는 리뷰를 수정하려 할 때")
    void updateReview_Fail_NotFound() {
        // [1] Given
        UUID reviewId = UUID.randomUUID();
        String currentUserId = "user123";
        String userRole = "CUSTOMER";

        ReviewUpdateRequestDto request = ReviewUpdateRequestDto.builder()
                .rating(4)
                .content("내용수정")
                .build();

        // DB에서 리뷰를 찾지 못한 상황 연출
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        // [2] When & [3] Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> reviewService.updateReview(reviewId, request, currentUserId, userRole));

        // R001: 리뷰를 찾을 수 없습니다 에러 발생 확인
        assertEquals(ErrorCode.REVIEW_NOT_FOUND, exception.getErrorCode());
    }

    // ==========================================
    // 5. 리뷰 삭제(소프트 딜리트) 기능 테스트
    // ==========================================

    @Test
    @DisplayName("리뷰 삭제 성공 - 본인(CUSTOMER)이 작성한 리뷰 삭제")
    void deleteReview_Success_Customer() {
        // [1] Given
        UUID reviewId = UUID.randomUUID();
        String currentUserId = "user123";
        String userRole = "CUSTOMER";

        User owner = User.builder().username("user123").build();
        Review myReview = Review.builder().user(owner).build();
        ReflectionTestUtils.setField(myReview, "id", reviewId);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(myReview));

        // [2] When
        reviewService.deleteReview(reviewId, currentUserId, userRole);

        // [3] Then
        // 소프트 딜리트 필드가 잘 채워졌는지 확인
        assertNotNull(myReview.getDeletedAt());
        assertEquals("user123", myReview.getDeletedBy());
    }

    @Test
    @DisplayName("리뷰 삭제 성공 - 가게 주인(OWNER)이 본인 가게 리뷰 삭제 (Store, User 엔티티 완벽 반영)")
    void deleteReview_Success_Owner() {
        // [1] Given
        UUID reviewId = UUID.randomUUID();
        String ownerId = "owner777"; // 현재 로그인한 사장님 ID
        String userRole = "OWNER";

        // 1. 진짜 사장님(User) 객체 생성
        User storeOwner = User.builder().username(ownerId).build();

        // 2. 가게(Store) 객체 생성 시, owner 필드에 사장님 객체를 쏙 넣어줍니다.
        Store myStore = Store.builder().owner(storeOwner).build();

        // 3. 손님(User) 객체 생성 (이 리뷰를 쓴 사람)
        User customer = User.builder().username("customer1").build();

        // 4. 리뷰(Review) 객체 조립
        Review review = Review.builder()
                .user(customer) // 작성자는 손님
                .store(myStore) // 작성된 가게는 위에서 만든 내 가게
                .build();
        ReflectionTestUtils.setField(review, "id", reviewId);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        // [2] When
        // 사장님이 삭제를 시도합니다.
        reviewService.deleteReview(reviewId, ownerId, userRole);

        // [3] Then
        // 권한 에러(403) 없이 무사히 넘어왔고, 삭제 시간이 기록되었는지 확인
        assertNotNull(review.getDeletedAt());
        assertEquals(ownerId, review.getDeletedBy());
    }

    @Test
    @DisplayName("리뷰 삭제 성공 - 관리자(MASTER)의 강제 삭제")
    void deleteReview_Success_Master() {
        // [1] Given
        UUID reviewId = UUID.randomUUID();
        String adminId = "admin_master";
        String userRole = "MASTER";

        User customer = User.builder().username("user1").build();
        Review review = Review.builder().user(customer).build();

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        // [2] When
        reviewService.deleteReview(reviewId, adminId, userRole);

        // [3] Then
        assertNotNull(review.getDeletedAt());
        assertEquals(adminId, review.getDeletedBy());
    }

    @Test
    @DisplayName("리뷰 삭제 실패 - 타인의 리뷰를 삭제하려 할 때 (403 방어)")
    void deleteReview_Fail_AccessDenied() {
        // [1] Given
        UUID reviewId = UUID.randomUUID();
        String hackerId = "hacker111";
        String userRole = "CUSTOMER";

        User realOwner = User.builder().username("user123").build();
        Review review = Review.builder().user(realOwner).build();

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        // [2] When & [3] Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> reviewService.deleteReview(reviewId, hackerId, userRole));

        assertEquals(ErrorCode.ACCESS_DENIED, exception.getErrorCode());
        // 삭제 필드가 여전히 null 인지도 확인하면 더 정확합니다.
        assertNull(review.getDeletedAt());
    }

    @Test
    @DisplayName("리뷰 삭제 실패 - 이미 삭제되었거나 존재하지 않는 리뷰")
    void deleteReview_Fail_NotFound() {
        // [1] Given
        UUID reviewId = UUID.randomUUID();
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        // [2] When & [3] Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> reviewService.deleteReview(reviewId, "user1", "CUSTOMER"));

        assertEquals(ErrorCode.REVIEW_NOT_FOUND, exception.getErrorCode());
    }
}