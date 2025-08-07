# 7일 게시판 개발 작업 리스트

## 프로젝트 개요
- **기간**: 7일
- **방법론**: TDD + DDD + 애자일
- **목표**: 게시판 핵심 기능 구현 및 배포
- **기술 스택**: Spring Boot + Thymeleaf + JPA

---

## Day 1: 프로젝트 기반 설정 및 도메인 모델링

### 🔧 환경 설정 (2시간)
- [x] **의존성 추가** (build.gradle)
  - [x] Spring Data JPA
  - [x] H2 Database (개발용)
  - [x] MySQL Connector (배포용)
  - [x] Spring Web
  - [x] Thymeleaf
  - [x] Spring Boot Test
  - [x] Validation
  - [x] Spring Security (2순위에서 사용)

- [x] **application.yml 설정**
  - [x] H2 설정 (개발 환경)
  - [x] JPA 설정 (DDL 자동생성)
  - [x] Thymeleaf 설정
  - [x] 로깅 설정

### 🏗️ 도메인 모델 설계 및 구현 (6시간)

#### Enum 클래스 구현
- [x] `Role` enum (USER, ADMIN)

#### 엔티티 구현 (TDD 방식)
**User 엔티티**
- [x] **Red**: User 엔티티 생성 테스트 작성
- [x] **Green**: User 엔티티 구현
- [x] **Refactor**: 연관관계 및 제약조건 최적화

**Post 엔티티**
- [x] **Red**: Post 엔티티 생성 및 User 연관관계 테스트
- [x] **Green**: Post 엔티티 구현
- [x] **Refactor**: 인덱스 및 성능 최적화

**Comment 엔티티**
- [x] **Red**: Comment 엔티티 및 연관관계 테스트
- [x] **Green**: Comment 엔티티 구현
- [x] **Refactor**: 계층 구조 최적화

#### Repository 계층 구현
- [x] UserRepository 인터페이스 및 기본 쿼리 메소드
- [x] PostRepository 인터페이스 및 검색 쿼리 메소드
- [x] CommentRepository 인터페이스

---

## Day 2: 1순위 기능 - Post CRUD 구현

### 📝 Post 도메인 서비스 구현 (TDD)

#### Post 생성 기능
- [x] **Red**: 게시글 생성 테스트 작성
  - [x] 유효한 데이터로 게시글 생성 성공
  - [x] 필수 필드 누락 시 실패
  - [x] 제목 길이 초과 시 실패
- [x] **Green**: PostService.createPost() 구현
- [x] **Refactor**: 도메인 로직 캡슐화

#### Post 조회 기능
- [x] **Red**: 게시글 조회 테스트 작성
  - [x] ID로 단건 조회
  - [x] 존재하지 않는 ID 조회 시 예외
  - [x] 삭제된 게시글 조회 시 예외
- [x] **Green**: PostService.findById() 구현
- [x] **Refactor**: 조회 최적화

#### Post 수정 기능
- [x] **Red**: 게시글 수정 테스트 작성
  - [x] 작성자만 수정 가능
  - [x] 유효한 데이터로 수정 성공
- [x] **Green**: PostService.updatePost() 구현
- [x] **Refactor**: 권한 검증 로직 분리

#### Post 삭제 기능 (소프트 삭제)
- [x] **Red**: 게시글 삭제 테스트 작성
  - [x] 작성자만 삭제 가능
  - [x] 소프트 삭제 동작 확인
- [x] **Green**: PostService.deletePost() 구현
- [x] **Refactor**: 삭제 정책 캡슐화

### 🌐 Post MVC 구현
- [x] **Red**: PostController 통합 테스트 작성
- [x] **Green**: PostController 구현
  - [x] GET /posts (목록 조회)
  - [x] GET /posts/{id} (단건 조회)
  - [x] GET /posts/new (작성 폼)
  - [x] POST /posts (생성)
  - [x] GET /posts/{id}/edit (수정 폼)
  - [x] PUT /posts/{id} (수정)
  - [x] DELETE /posts/{id} (삭제)
- [x] **Green**: Thymeleaf 템플릿 구현
  - [x] posts/list.html (목록)
  - [x] posts/detail.html (상세)
  - [x] posts/form.html (작성/수정 폼)
- [x] **Refactor**: 예외 처리 및 응답 개선
  - [x] GlobalExceptionHandler 구현
  - [x] 에러 페이지 템플릿 구현 (404, 403, 500, 400)

---

## Day 3: 1순위 기능 - Comment CRUD 및 페이지네이션

### 💬 Comment 도메인 서비스 구현 (TDD)

#### Comment CRUD
- [x] **Red**: 댓글 CRUD 테스트 작성
  - [x] 댓글 생성 (게시글에 종속)
  - [x] 댓글 조회 (게시글별)
  - [x] 댓글 수정 (작성자만)
  - [x] 댓글 삭제 (소프트 삭제)
- [x] **Green**: CommentService 구현
- [x] **Refactor**: Comment 도메인 로직 개선

### 🌐 Comment MVC 구현
- [x] **Red**: CommentController 통합 테스트
- [x] **Green**: CommentController 구현
  - [x] POST /posts/{postId}/comments (댓글 생성)
  - [x] PUT /comments/{id} (댓글 수정)
  - [x] DELETE /comments/{id} (댓글 삭제)
- [x] **Green**: 댓글 관련 Thymeleaf 템플릿 추가
  - [x] comments/form.html (댓글 작성 폼)
  - [x] comments/list.html (댓글 목록 - detail.html에 포함)
- [x] **Refactor**: AJAX 기반 댓글 처리 (선택사항)

### 📄 페이지네이션 구현
- [x] **Red**: 페이지네이션 테스트 작성
  - [x] Post 목록 페이징
  - [x] Comment 목록 페이징
  - [x] 정렬 기능 (최신순, 조회수순, 좋아요순)
- [x] **Green**: Pageable 기반 Repository 메소드 구현
- [x] **Green**: 페이징 UI 컴포넌트 (Thymeleaf)
- [x] **Refactor**: 페이징 성능 최적화

---

## Day 4: 1순위 기능 - 검색 기능 및 통합 테스트

### 🔍 검색 기능 구현
- [x] **Red**: 키워드 검색 테스트 작성
  - [x] 제목 검색
  - [x] 내용 검색
  - [x] 작성자 검색
  - [x] 카테고리 검색
  - [x] 복합 검색
- [x] **Green**: SearchService 구현
  - [x] JPQL 쿼리 작성
  - [x] QueryDSL 적용 (선택사항)
- [x] **Refactor**: 검색 성능 최적화

### 🌐 Search MVC 구현
- [x] **Red**: SearchController 테스트
- [x] **Green**: GET /posts/search 구현
- [x] **Green**: 검색 폼 및 결과 페이지 (Thymeleaf)
- [x] **Refactor**: 검색 결과 형식 개선

### 🧪 1순위 기능 통합 테스트
- [x] 전체 플로우 E2E 테스트
- [x] 성능 테스트 (기본 수준)
- [x] 에러 시나리오 테스트

---

## Day 5: 2순위 기능 - 사용자 인증 및 추천 시스템

### 🔐 사용자 인증 구현

#### User 도메인 서비스
- [x] **Red**: 회원가입 테스트 작성
  - [x] 유효한 데이터로 가입 성공
  - [x] 중복 username/email 검증
  - [x] 비밀번호 암호화 확인
- [x] **Green**: UserService 구현
- [x] **Refactor**: 인증 로직 개선

#### 로그인 기능 (Spring Security + 세션)
- [x] **Red**: 로그인 테스트 작성
- [x] **Green**: Spring Security 설정
- [x] **Green**: 로그인/회원가입 페이지 (Thymeleaf)
  - [x] auth/login.html
  - [x] auth/register.html
- [x] **Refactor**: 세션 관리 개선

### 👍 추천/비추천 시스템
- [x] **Red**: 추천 시스템 테스트 작성
  - [x] 게시글 추천/비추천
  - [x] 중복 추천 방지
  - [x] 추천에서 비추천으로 변경
  - [x] 추천/비추천 취소
- [x] **Green**: LikeService 구현
  - [x] PostLike 엔티티 생성
  - [x] PostLikeRepository 구현
  - [x] 중복 방지 로직 구현
- [x] **Green**: 추천 버튼 UI (AJAX)
  - [x] 게시글 상세 페이지에 추천/비추천 버튼 추가
  - [x] AJAX로 동적 처리
  - [x] 사용자 추천 상태 표시
- [x] **Refactor**: 추천 도메인 로직 개선

### 👁️ 조회수 기능
- [x] **Red**: 조회수 증가 테스트
  - [x] 첫 조회 시 조회수 증가
  - [x] 중복 조회 방지 (세션 기반)
  - [x] 다른 세션에서는 각각 증가
- [x] **Green**: ViewService 구현 (중복 조회 방지)
  - [x] 세션 기반 중복 조회 방지
  - [x] PostController와 연동
- [x] **Refactor**: 조회수 성능 최적화

---

## Day 6: 2순위 기능 완성 및 3순위 기능 시작

### 📝 마크다운 지원 (시간 허용 시)
- [ ] 마크다운 라이브러리 추가
- [ ] 게시글 작성 시 마크다운 변환
- [ ] XSS 방지 처리

### 👤 사용자별 게시글/댓글 모아보기
- [ ] **Red**: 사용자 프로필 조회 테스트
- [ ] **Green**: UserProfileService 구현
- [ ] **Green**: GET /users/{username} 페이지 구현
- [ ] **Refactor**: 조회 성능 개선

### 🔒 기본 관리자 기능
- [ ] **Red**: 관리자 권한 테스트
- [ ] **Green**: AdminService 기본 구현
  - [ ] 게시글/댓글 강제 삭제
  - [ ] 사용자 권한 변경 (기본만)
- [ ] **Green**: 관리자 페이지 (Thymeleaf)
- [ ] **Refactor**: 권한 체계 개선

---

## Day 7: 신고 시스템 및 배포 준비

### 🚨 신고 시스템 (시간 허용 시)
#### 신고 관련 엔티티 구현
- [ ] **Red**: ReportedPost 엔티티 테스트 작성
- [ ] **Green**: ReportedPost 엔티티 구현
- [ ] **Red**: ReportedComment 엔티티 테스트 작성
- [ ] **Green**: ReportedComment 엔티티 구현
- [ ] `ReportStatus` enum (PENDING, RESOLVED, DISMISSED) 구현

#### 신고 기능 구현
- [ ] **Red**: 신고 기능 테스트
- [ ] **Green**: ReportService 기본 구현
- [ ] **Green**: 신고 버튼 및 폼 UI
- [ ] **Refactor**: 신고 처리 로직

### 🚀 배포 준비
- [ ] **프로덕션 설정**
  - [ ] MySQL 연결 설정
  - [ ] 환경별 프로파일 분리
  - [ ] 로깅 설정 개선

- [ ] **Docker 설정** (선택사항)
  - [ ] Dockerfile 작성
  - [ ] docker-compose.yml 작성

- [ ] **최종 테스트 및 문서화**
  - [ ] 전체 기능 테스트
  - [ ] 배포 가이드 작성
  - [ ] 사용자 가이드 작성 (간단한 README)

---

## 📋 기술적 의사결정 사항

### 프론트엔드
- **템플릿 엔진**: Thymeleaf
- **스타일링**: Bootstrap (CDN)
- **JS**: 바닐라 JS + AJAX (추천, 댓글)

### 데이터베이스
- **개발**: H2 (인메모리)
- **배포**: MySQL 8.0

### 보안
- **1차**: Spring Security + 세션 기반 인증
- **2차**: JWT (시간 허용 시)

### 테스트 전략
- **단위 테스트**: 도메인 서비스 레벨
- **통합 테스트**: Repository, Controller 레벨
- **E2E 테스트**: 주요 플로우만

### 성능 최적화
- **페이징**: Offset 기반 (초기 구현)
- **캐싱**: 조회수, 추천수 (필요 시)
- **인덱스**: username, category 필드

---

## 🎯 성공 기준

### 필수 (1순위)
- [ ] 게시글 CRUD 완전 동작
- [ ] 댓글 CRUD 완전 동작
- [ ] 키워드 검색 동작
- [ ] 페이지네이션 동작

### 목표 (2순위)
- [ ] 로그인/회원가입 동작
- [ ] 추천/비추천 동작
- [ ] 조회수 기능 동작

### 우수 (3순위 + α)
- [ ] 관리자 기능 동작
- [ ] 신고 시스템 동작
- [ ] 배포 완료

---

## 📝 URL 설계

### 게시글 관련
- GET /posts - 게시글 목록
- GET /posts/{id} - 게시글 상세
- GET /posts/new - 게시글 작성 폼
- POST /posts - 게시글 생성
- GET /posts/{id}/edit - 게시글 수정 폼
- PUT /posts/{id} - 게시글 수정
- DELETE /posts/{id} - 게시글 삭제
- GET /posts/search - 게시글 검색

### 댓글 관련
- POST /posts/{postId}/comments - 댓글 생성
- PUT /comments/{id} - 댓글 수정
- DELETE /comments/{id} - 댓글 삭제

### 사용자 관련
- GET /login - 로그인 폼
- GET /register - 회원가입 폼
- POST /register - 회원가입 처리
- GET /users/{username} - 사용자 프로필

### 관리자 관련
- GET /admin - 관리자 대시보드
- GET /admin/posts - 게시글 관리
- GET /admin/users - 사용자 관리

---
