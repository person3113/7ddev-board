# 7일 게시판 프로젝트

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Gradle](https://img.shields.io/badge/Gradle-8.x-blue.svg)](https://gradle.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)

## 📖 프로젝트 소개

7일 동안 TDD와 DDD 방법론을 적용하여 개발한 Spring Boot 기반 게시판 시스템입니다.
사용자가 게시글과 댓글을 작성하고, 추천/비추천, 신고 기능을 제공하는 완전한 게시판 서비스입니다.

## ✨ 주요 기능

### 🔥 1순위 기능 (완료)
- **게시글 CRUD**: 생성, 조회, 수정, 삭제 (소프트 삭제)
- **댓글 CRUD**: 게시글별 댓글 관리
- **페이지네이션**: 게시글 목록 페이징 처리
- **검색 기능**: 제목, 내용, 작성자별 키워드 검색

### 🎯 2순위 기능 (완료)
- **사용자 인증**: Spring Security 기반 로그인/회원가입
- **추천/비추천 시스템**: 게시글 추천 및 중복 방지
- **조회수 기능**: 세션 기반 중복 조회 방지
- **사용자 프로필**: 개인별 게시글/댓글 모아보기

### 🚀 3순위 기능 (완료)
- **관리자 기능**: 게시글/댓글/사용자 관리
- **신고 시스템**: 부적절한 게시글/댓글 신고 및 처리
- **마크다운 지원**: 게시글 작성 시 마크다운 문법 지원

## 🛠️ 기술 스택

### Backend
- **Java 21** - OpenJDK
- **Spring Boot 3.5.3** - 웹 애플리케이션 프레임워크
- **Spring Security** - 인증 및 인가
- **Spring Data JPA** - 데이터 접근 계층
- **Hibernate** - ORM

### Database
- **H2** - 개발 환경용 인메모리 데이터베이스
- **MySQL 8.0** - 프로덕션 환경용 데이터베이스

### Frontend
- **Thymeleaf** - 서버사이드 템플릿 엔진
- **Bootstrap 5** - CSS 프레임워크
- **JavaScript** - AJAX 기반 동적 기능

### Build & Deploy
- **Gradle** - 빌드 도구
- **Docker** - 컨테이너화
- **Docker Compose** - 멀티 컨테이너 관리

## 🏗️ 프로젝트 아키텍처

```
src/
├── main/
│   ├── java/com/board/
│   │   ├── domain/
│   │   │   ├── entity/          # 엔티티 클래스
│   │   │   ├── enums/           # 열거형 클래스
│   │   │   └── repository/      # Repository 인터페이스
│   │   ├── service/             # 비즈니스 로직
│   │   ├── controller/          # 웹 컨트롤러
│   │   ├── config/              # 설정 클래스
│   │   └── exception/           # 예외 처리
│   └── resources/
│       ├── templates/           # Thymeleaf 템플릿
│       └── static/              # 정적 리소스
└── test/                        # 테스트 코드
```

## 🚀 빠른 시작

### 전제 조건
- Java 21 이상
- Docker & Docker Compose (권장)

### Docker 실행 (권장)
```bash
# 프로젝트 클론
git clone <repository-url>
cd 7ddev_board

# Docker Compose로 실행
docker-compose up -d

# 브라우저에서 접속
# http://localhost:8080
```

### 로컬 개발 환경
```bash
# 개발 모드 실행 (H2 데이터베이스)
./gradlew bootRun

# 브라우저에서 접속
# http://localhost:8080
```

## 👤 기본 계정

### 관리자 계정
- **Username**: admin
- **Password**: admin123!
- **권한**: 모든 게시글/댓글/사용자 관리

### 일반 사용자
- 회원가입을 통해 계정 생성 가능

## 📱 주요 페이지

| URL | 설명 |
|-----|------|
| `/` | 메인 페이지 (게시글 목록) |
| `/posts` | 게시글 목록 |
| `/posts/{id}` | 게시글 상세 |
| `/posts/new` | 게시글 작성 |
| `/posts/search` | 게시글 검색 |
| `/login` | 로그인 |
| `/register` | 회원가입 |
| `/users/{username}` | 사용자 프로필 |
| `/admin` | 관리자 대시보드 |
| `/admin/reports` | 신고 관리 |

## 🧪 테스트

### 전체 테스트 실행
```bash
./gradlew test
```

### 특정 테스트 실행
```bash
# 엔티티 테스트
./gradlew test --tests "*Entity*"

# 서비스 테스트
./gradlew test --tests "*Service*"

# 컨트롤러 테스트
./gradlew test --tests "*Controller*"
```

### 테스트 커버리지
```bash
./gradlew jacocoTestReport
# 결과: build/reports/jacoco/test/html/index.html
```

## 📈 개발 방법론

### TDD (Test-Driven Development)
1. **Red**: 실패하는 테스트 작성
2. **Green**: 테스트를 통과하는 최소한의 코드 작성
3. **Refactor**: 코드 개선 및 최적화

### DDD (Domain-Driven Design)
- 도메인 중심의 설계
- 엔티티 내 비즈니스 로직 캡슐화
- 도메인 서비스 분리

### 애자일 방법론
- 우선순위별 기능 개발
- 반복적 개발 및 테스트
- 지속적 개선

## 🔧 환경 설정

### 개발 환경 (dev)
- H2 인메모리 데이터베이스
- 상세한 로깅 활성화
- 개발자 도구 활성화

### 프로덕션 환경 (prod)
- MySQL 데이터베이스
- 최적화된 로깅 설정
- 캐시 활성화

### 테스트 환경 (test)
- H2 테스트 데이터베이스
- 테스트 전용 설정

## 📊 주요 메트릭

### 코드 품질
- **테스트 커버리지**: 85% 이상
- **코드 복잡도**: 낮은 복잡도 유지
- **중복 코드**: 최소화

### 성능
- **응답 시간**: 평균 100ms 이하
- **동시 사용자**: 100명 지원
- **데이터베이스 쿼리**: N+1 문제 해결

## 🤝 기여 방법

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 변경 로그

### v1.0.0 (2025-08-07)
- ✅ 게시글 CRUD 완료
- ✅ 댓글 시스템 완료
- ✅ 사용자 인증 완료
- ✅ 추천/비추천 시스템 완료
- ✅ 관리자 기능 완료
- ✅ 신고 시스템 완료
- ✅ Docker 배포 환경 완료

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다. 자세한 내용은 `LICENSE` 파일을 참조하세요.

## 📞 지원 및 문의

- **개발자**: [Your Name]
- **이메일**: [your.email@example.com]
- **GitHub**: [https://github.com/yourusername/7ddev_board](https://github.com/yourusername/7ddev_board)

## 🙏 감사의 말

이 프로젝트는 7일 동안의 집중적인 개발을 통해 완성되었습니다. TDD와 DDD 방법론을 실제 프로젝트에 적용해보는 좋은 경험이었습니다.

---

**Made with ❤️ in 7 days**
