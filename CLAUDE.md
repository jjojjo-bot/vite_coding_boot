# 프로젝트 아키텍처 규칙

## 기술 스택
- **Backend**: Spring Boot 4.0.2 + Java 25 + H2 (dev) + JPA/Hibernate
- **Frontend**: Vite + React 18 + TypeScript
- **인증**: JWT (httpOnly cookie, HS256, 8시간 만료)
- **상태관리**: TanStack Query (서버 상태) + React Context (인증)
- **UI**: AG-Grid Community + react-chartjs-2
- **빌드**: Gradle (FE 빌드 → static/ 복사, 단일 JAR 배포)

## 아키텍처
헥사고날 아키텍처 (Ports & Adapters) 적용

## 패키지 구조 규칙
- `domain`: 외부 의존성 절대 금지
- `application/port/in`: UseCase 인터페이스만
- `application/port/out`: Repository 인터페이스만
- `application/service`: UseCase 구현체
- `adapter/in/web`: Controller만
- `adapter/in/web/dto`: Request/Response DTO
- `adapter/in/web/security`: JWT 관련 (JwtUtil, JwtAuthFilter)
- `adapter/out/persistence`: JPA 구현체만

## 코딩 규칙
- Lombok 사용
- 생성자 주입 방식 사용 (`@RequiredArgsConstructor`)
- 인터페이스 먼저 작성 후 구현체 작성
- DTO는 Java `record`로 작성
- REST API 경로: `/api/` 접두사 필수

## 프론트엔드 구조
```
frontend/src/
  api/         → Axios API 모듈
  components/  → layout, common 컴포넌트
  context/     → AuthContext
  pages/       → 라우트 페이지 컴포넌트
  types/       → TypeScript 인터페이스
```

## 빌드 & 실행
- 백엔드: `./gradlew bootRun`
- 프론트엔드 (개발): `cd frontend && npm run dev`
- 전체 빌드: `./gradlew build`
- 테스트: `./gradlew test`
