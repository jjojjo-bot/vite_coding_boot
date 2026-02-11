# CHANGELOG

## [4.0.0] - 2026-02-11 — 4단계: 과제 시스템 리팩토링 (전체 사용자 등록/수정, 자동 상태, 최종결과 등록)

### 핵심 변경
- **과제 등록**: 조장만 → 모든 사용자 가능 (담당자 없이 등록)
- **과제 승인**: 조장이 승인 시 담당자 지정
- **진행 상태**: 시작/완료 수동 버튼 → 착수일 기반 자동 산출 (5일 유예 포함)
- **과제 완료**: 완료 버튼 → 담당자가 최종결과 텍스트 입력 후 자동 완료

### 추가
- **과제 수정 기능**
  - `GET /assignments/{id}/edit` — 수정 폼 (모든 사용자)
  - `POST /assignments/{id}/edit` — 수정 처리 (APPROVED 상태면 PENDING으로 리셋)
- **최종결과 입력 기능**
  - `GET /assignments/{id}/result` — 최종결과 입력 폼 (담당자만)
  - `POST /assignments/{id}/result` — 최종결과 제출 (APPROVED 검증 + 담당자 본인 검증)
- `Assignment.java` — `createdBy` (등록자), `startDate` (착수일), `finalResult` (최종결과), `resultRegisteredAt` (최종결과 등록 시점) 필드 추가
- `AssignmentRepository` — `findByCreatedByOrUser()` 메서드 추가 (등록자 또는 담당자 기준 조회)
- `AssignmentUseCase` — `updateAssignment()`, `submitFinalResult()`, `findAssignmentsByCreatorOrAssignee()` 메서드 추가
- `assignment-edit-form.html` — 과제 수정 폼 (기존 값 pre-fill)
- `assignment-result-form.html` — 최종결과 입력 폼

### 변경
- `Assignment.java` — 생성자 `(title, description, createdBy, startDate, dueDate)` 로 변경, `calculateProgressStatus()` 재작성 (미승인 → null, 5일 유예 기간 반영)
- `AssignmentUseCase.java` — `createAssignment` 시그니처 변경 (startDate 추가, assigneeUserId 제거), `approveAssignment(id, assigneeUserId)` 로 변경
- `AssignmentService.java` — 전면 수정 (승인 시 담당자 할당, 수정 시 APPROVED→PENDING 리셋)
- `AssignmentController.java` — 조장 제한 제거 (생성/수정), 승인 시 담당자 선택 파라미터 추가
- `DashboardController.java` — 조원 쿼리 `findAssignmentsByCreatorOrAssignee()` 로 변경
- `assignment-form.html` — 담당자 선택 드롭다운 제거, 착수일 입력 필드 추가
- `dashboard.html` — 등록자/착수일 컬럼 추가, 승인 시 담당자 선택 인라인 폼, 수정/최종결과 입력 버튼 추가, 시작/완료 버튼 제거

### 삭제
- `Assignment.java` — `started` (boolean), `completedDate` (LocalDate) 필드 제거
- `AssignmentUseCase.java` — `startAssignment()`, `completeAssignment()` 메서드 제거
- `AssignmentController.java` — `POST /{id}/start`, `POST /{id}/complete` 엔드포인트 제거

### 테스트
- `AssignmentTest.java` — `calculateProgressStatus()` 테스트 재작성 (9개 케이스: 미승인 null, 반려 null, 기한 내 완료, 유예 기간 내 완료, 유예 초과 지연완료, 지연, 착수일 후 진행중, 착수일 당일 진행중, 착수일 전 미진행)
- `AssignmentServiceTest.java` — 서비스 테스트 재작성 (19개 케이스: 생성/승인/반려/수정/최종결과/조회)

---

## [3.0.0] - 2026-02-11 15:37 — 3단계: 승인/반려 프로세스

### 추가
- **과제 승인 기능**
  - `POST /assignments/{id}/approve` — 조장이 PENDING 상태의 과제를 APPROVED로 변경
- **과제 반려 기능**
  - `POST /assignments/{id}/reject` — 조장이 PENDING 상태의 과제를 REJECTED로 변경 (반려 사유 필수)

### 변경
- `Assignment.java` — `rejectionReason` 필드 추가 (반려 사유 저장)
- `AssignmentUseCase.java` — `approveAssignment(Long)`, `rejectAssignment(Long, String)` 메서드 추가
- `AssignmentService.java` — 승인/반려 비즈니스 로직 구현 (PENDING 상태 검증 포함)
- `AssignmentController.java` — 승인/반려 엔드포인트 추가 (조장 권한 검증)
- `dashboard.html` — 결재상태 컬럼 추가 (PENDING/APPROVED/REJECTED 배지), 조장용 승인/반려 버튼, 조원은 APPROVED된 과제만 시작/완료 가능, 반려 사유 표시
- `AssignmentServiceTest.java` — 승인/반려 단위 테스트 6개 추가

---

## [2.0.0] - 2026-02-11 — 2단계: 세션 관리 & 과제 대시보드

### 추가
- **세션 기반 인증**
  - `LoginInterceptor.java` — 미인증 요청을 `/login`으로 리다이렉트하는 인터셉터
  - `WebConfig.java` — 인터셉터 등록 (`/login`, `/logout`, `/h2-console` 제외)
  - `LoginController.java` — HttpSession에 User 저장, 로그아웃, 루트 리다이렉트 추가

- **과제 CRUD**
  - `AssignmentUseCase.java` — 과제 관리 입력 포트 (생성, 조회, 삭제, 시작, 완료)
  - `AssignmentService.java` — 과제 유스케이스 구현 (트랜잭션 지원)
  - `AssignmentController.java` — 과제 생성/삭제/시작/완료 웹 컨트롤러

- **역할별 대시보드**
  - `DashboardController.java` — 조장: 전체 과제 + 조원 목록 / 조원: 본인 과제만
  - `dashboard.html` — 과제 목록 테이블, 상태 배지, 역할별 액션 버튼
  - `assignment-form.html` — 조장용 과제 생성 폼 (담당자 선택, 마감일)

- **포트 & 어댑터 확장**
  - `UserRepository.java` — `findById(Long)` 추가
  - `AssignmentRepository.java` — `findById(Long)`, `deleteById(Long)` 추가
  - `UserPersistenceAdapter.java` — `findById` 구현
  - `AssignmentPersistenceAdapter.java` — `findById`, `deleteById` 구현
  - `UserQueryUseCase.java` — `findAllMembers()`, `findById(Long)` 추가
  - `UserQueryService.java` — MEMBER 역할 필터링 조회 구현

- **테스트**
  - `AssignmentServiceTest.java` — Mockito 단위 테스트 (10개 케이스)

### 변경
- `User.java` — `implements Serializable` 추가 (HttpSession 저장용)
- `LoginController.java` — welcome 페이지 대신 세션 기반 대시보드 리다이렉트로 전환

### 삭제
- `welcome.html` — `dashboard.html`로 대체

---

## [1.0.0] - 2026-02-11 — 1단계: 데이터 모델 & JPA 엔티티 (헥사고날 아키텍처)

### 추가
- **헥사고날 아키텍처 패키지 구조 수립**
  - `domain/model/` — 순수 도메인 (엔티티, Enum)
  - `application/port/in/` — 입력 포트 (유스케이스 인터페이스)
  - `application/port/out/` — 출력 포트 (리포지토리 인터페이스)
  - `application/service/` — 애플리케이션 서비스
  - `adapter/in/web/` — 인바운드 어댑터 (컨트롤러)
  - `adapter/out/persistence/` — 아웃바운드 어댑터 (JPA 구현체)

- **Domain 레이어**
  - `Role.java` — 역할 enum (LEADER, MEMBER)
  - `ApprovalStatus.java` — 승인 상태 enum (PENDING, APPROVED, REJECTED)
  - `ProgressStatus.java` — 진행 상태 enum (NOT_STARTED, IN_PROGRESS, DELAYED, COMPLETED, DELAYED_COMPLETED)
  - `User.java` — 사용자 JPA 엔티티, `isLeader()` 도메인 메서드
  - `Assignment.java` — 과제 JPA 엔티티, `calculateProgressStatus()` 비즈니스 로직

- **Application 레이어**
  - `UserRepository.java` — 사용자 출력 포트 인터페이스
  - `AssignmentRepository.java` — 과제 출력 포트 인터페이스
  - `UserQueryUseCase.java` — 사용자 조회/인증 입력 포트
  - `UserQueryService.java` — 유스케이스 구현 (DB 기반 인증)

- **Adapter-Out 레이어**
  - `JpaUserRepository.java` — Spring Data JPA 리포지토리
  - `JpaAssignmentRepository.java` — Spring Data JPA 리포지토리
  - `UserPersistenceAdapter.java` — 사용자 출력 포트 구현체
  - `AssignmentPersistenceAdapter.java` — 과제 출력 포트 구현체
  - `DataInitializer.java` — 초기 데이터 (leader/member1/member2)

- **Adapter-In 레이어**
  - `LoginController.java` — 헥사고날 구조로 이동 및 DB 기반 인증으로 리팩토링

- **테스트**
  - `AssignmentTest.java` — 진행 상태 산출 단위 테스트 (6개 케이스)
  - `DataInitializerTest.java` — 초기 데이터 로딩 통합 테스트 (3개 케이스)

- **의존성**
  - `spring-boot-starter-data-jpa` 추가
  - `h2` (런타임) 추가

- **설정**
  - `application.properties` — H2 인메모리 DB, JPA, H2 콘솔 설정 추가

### 변경
- 로그인 인증 방식을 하드코딩(admin/1234)에서 DB 기반 인증으로 전환
- 로그인 성공 시 username 대신 사용자 이름(name)을 표시하도록 변경

### 삭제
- `controller/LoginController.java` — 헥사고날 구조(`adapter/in/web/`)로 이동
