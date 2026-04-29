# 🍽️ DelivHub

> 배달 주문 관리 플랫폼

- API 서버 : http://16.176.103.233:8080

<br>

## 📌 목차

- [프로젝트 개요](#-프로젝트-개요)
- [팀원 역할 분담](#-팀원-역할-분담)
- [기술 스택](#-기술-스택)
- [ERD](#-erd)
- [인프라 아키텍처](#-인프라-아키텍처)
- [API 문서](#-API-문서)
- [프로젝트 구조](#-프로젝트-구조)

<br>

## 🧩 프로젝트 개요

### 목적

> 광화문 근처 음식점의 배달 주문, 결제, 주문 내역 관리 기능 제공

### 주요 기능

- **보안**: Spring Security + JWT: RBAC(CUSTOMER, OWNER, MANAGER, MASTER) 및 토큰 기반 인증 강화
- **주문**: 카드 결제 가상 연동 및 주문 상태 관리(접수, 배달 중, 완료)
- **AI 연동**: Gemini API 활용하여 메뉴 등록 시 키워드만으로 풍부한 상품 설명 자동 생성, 점주의 운영 편의성 증대 및 플랫폼 콘텐츠 퀄리티 상향 평준화

### 개발 기간

| 기간                      | 내용 |
|-------------------------|------|
| 2026.04.16 ~ 2026.04.20 | 기획 및 설계 |
| 2026.04.20 ~ 2026.04.26 | 개발 |
| 2026.04.27 ~ 2026.04.30 | 테스트 및 배포 |

<br>

## 👥 팀원 역할 분담

| 이름  | 역할 | 담당 기능                     |
|-----|------|---------------------------|
| 이성근 | 팀장 / Backend | 유저 도메인 구현, 인증/인가          |
| 이재훈 | Backend | 주문 도메인 구현, CI/CD          |
| 하준영 | Backend | 결제 · 리뷰 도메인 구현, PPT 제작    |
| 조혜은 | Backend | 메뉴 · 옵션 도메인 구현, AI 연동     |
| 김민우 | Backend | 가게 · 카테고리 · 지역 도메인 구현, 배포 |

<br>

## 🛠 기술 스택

### Backend
![Java](https://img.shields.io/badge/Java_17-007396?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_4.0.5-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=flat-square&logo=springsecurity&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=flat-square&logo=spring&logoColor=white)
![Spring Validation](https://img.shields.io/badge/Spring_Validation-6DB33F?style=flat-square&logo=spring&logoColor=white)
![JWT](https://img.shields.io/badge/JWT_0.12.6-000000?style=flat-square&logo=jsonwebtokens&logoColor=white)
![Lombok](https://img.shields.io/badge/Lombok-CA0124?style=flat-square&logo=lombok&logoColor=white)
![Swagger](https://img.shields.io/badge/Swagger_SpringDoc_2.8.8-85EA2D?style=flat-square&logo=swagger&logoColor=black)

### Database
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=flat-square&logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=flat-square&logo=redis&logoColor=white)
![Hibernate Spatial](https://img.shields.io/badge/Hibernate_Spatial-59666C?style=flat-square&logo=hibernate&logoColor=white)

### Test
![JUnit5](https://img.shields.io/badge/JUnit5-25A162?style=flat-square&logo=junit5&logoColor=white)
![Mockito](https://img.shields.io/badge/Mockito_5.11-C5D9C8?style=flat-square&logoColor=black)

### Infra
![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?style=flat-square&logo=githubactions&logoColor=white)
![AWS EC2](https://img.shields.io/badge/AWS_EC2-FF9900?style=flat-square&logo=amazonec2&logoColor=white)

### Tools
![Git](https://img.shields.io/badge/Git-F05032?style=flat-square&logo=git&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A?style=flat-square&logo=gradle&logoColor=white)
![Notion](https://img.shields.io/badge/Notion-000000?style=flat-square&logo=notion&logoColor=white)
![Slack](https://img.shields.io/badge/Slack-4A154B?style=flat-square&logo=slack&logoColor=white)

<br>

## 📐 ERD

![ERD](https://github.com/user-attachments/assets/a1b4d996-2c8e-405c-9586-8348f21b4110)


<details>
<summary>테이블 목록 보기</summary>

| 테이블 | 설명 |
|--------|------|
| p_user | 사용자 |
| p_store | 가게 |
| p_category | 가게 카테고리 |
| p_area | 지역 |
| p_menu | 메뉴 |
| p_option | 메뉴 옵션 |
| p_option_item | 옵션 항목 |
| p_order | 주문 |
| p_order_item | 주문 항목 |
| p_payment | 결제 |
| p_review | 리뷰 |
| p_address | 배송지 |
| p_ai_log | AI 요청 로그 |

</details>

<br>


## 🏗️ 인프라 아키텍처
![System Architecture](https://github.com/user-attachments/assets/c1ab6550-c31c-4aff-9069-bde1a56a891f)

<br>

## 📄 API 문서

- Swagger UI: http://16.176.103.233:8080/swagger-ui/index.html

### 주요 API 목록

<details>
<summary>👤 회원 API</summary>

| Method | URI | 설명 | 권한     |
|--------|-----|------|--------|
| POST | /api/v1/auth/signup | 회원가입 | 전체     |
| POST | /api/v1/auth/login | 로그인 | 전체     |
| GET | /api/v1/users/{username} | 회원 조회 | 본인/관리자 |
| PUT | /api/v1/users/{username} | 회원 수정 | 본인     |
| DELETE | /api/v1/users/{username} | 회원 탈퇴 | 본인/관리자 |

</details>

<details>
<summary>🏪 가게 API</summary>

| Method | URI | 설명 | 권한 |
|--------|-----|------|------|
| POST | /api/v1/stores | 가게 등록 | OWNER |
| GET | /api/v1/stores | 가게 목록 조회 | 전체 |
| GET | /api/v1/stores/{storeId} | 가게 상세 조회 | 전체 |
| PUT | /api/v1/stores/{storeId} | 가게 수정 | OWNER |
| DELETE | /api/v1/stores/{storeId} | 가게 삭제 | OWNER/관리자 |

</details>

<details>
<summary>🛒 주문 API</summary>

| Method | URI | 설명                 | 권한 |
|--------|-----|--------------------|------|
| POST | /api/v1/orders | 주문 생성              | CUSTOMER |
| GET | /api/v1/orders | 주문 목록 조회           | 본인/관리자 |
| GET | /api/v1/orders/{orderId} | 주문 상세 조회           | 본인/관리자 |
| PATCH | /api/v1/orders/{orderId}/cancel | 주문 취소 (생성 후 5분 이내) | 본인 |

</details>

<details>
<summary>💳 결제 API</summary>

| Method | URI | 설명                  | 권한 |
|--------|-----|---------------------|------|
| POST | /api/v1/payments | 결제 요청 (CARD 방식만 허용) | CUSTOMER |
| GET | /api/v1/payments/{paymentId} | 결제 조회               | 본인/관리자 |
| PATCH | /api/v1/payments/{paymentId}/cancel | 결제 취소               | 본인 |

</details>

<details>
<summary>⭐ 리뷰 API</summary>

| Method | URI | 설명 | 권한 |
|--------|-----|------|------|
| POST | /api/v1/reviews | 리뷰 작성 | CUSTOMER |
| GET | /api/v1/stores/{storeId}/reviews | 가게 리뷰 목록 | 전체 |
| PUT | /api/v1/reviews/{reviewId} | 리뷰 수정 | 본인 |
| DELETE | /api/v1/reviews/{reviewId} | 리뷰 삭제 | 본인/관리자 |

</details>

<br>

## 📁 프로젝트 구조

```
src
├─main
│  ├─java
│  │  └─com
│  │      └─sparta
│  │          └─delivhub
│  │              ├─common
│  │              │  ├─dto
│  │              │  ├─entity
│  │              │  ├─handler
│  │              │  └─util
│  │              ├─config
│  │              │  ├─JpaAuditingConfig
│  │              │  ├─RedisConfig
│  │              │  ├─RestClientConfig
│  │              │  ├─SecurityConfig
│  │              │  └─SwaggerConfig
│  │              ├─domain
│  │              │  ├─address
│  │              │  ├─ai
│  │              │  ├─area
│  │              │  ├─auth
│  │              │  ├─category
│  │              │  ├─menu
│  │              │  ├─option
│  │              │  ├─order
│  │              │  ├─payment
│  │              │  ├─review
│  │              │  ├─store
│  │              │  └─user
│  │              └─security
│  │                 ├─JwtAuthenticationEntryPoint
│  │                 ├─JwtAuthenticationFilter
│  │                 ├─JwtTokenProvider
│  │                 ├─TokenService
│  │                 ├─UserDetailServiceImpl    
│  │                 └─UserDetailsImpl
│  └─resources
└─test

```
