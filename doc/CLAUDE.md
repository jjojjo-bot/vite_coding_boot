# 조별과제 관리 시스템 (Group Assignment Management System)

## 프로젝트 개요

회의에서 도출된 일정 관리가 필요한 과제들을 등록/수정/삭제/승인/반려할 수 있는 시스템.
조원 전체가 과제를 등록하고 수정할 수 있으며, 조장만이 승인/반려 권한을 가진다.

## 기술 스택

- **Backend**: Spring Boot 4.0.2, Java 25
- **Template Engine**: Thymeleaf
- **Build Tool**: Gradle 9.3.0
- **Database**: H2 (개발), 추후 MySQL/PostgreSQL 전환 가능
- **ORM**: Spring Data JPA

---

## 사용자 역할

| 역할 | 설명 | 권한 |
|------|------|------|
| 조장 (LEADER) | 그룹 리더 | 등록, 수정, 삭제, 승인, 반려 |
| 조원 (MEMBER) | 일반 구성원 | 등록, 수정, 삭제 |

- 삭제는 본인이 등록한 과제만 가능
- 승인된 과제의 삭제는 조장만 가능

---

## 과제 상태 관리

### 승인 상태 (Approval Status)

| 상태 | 코드 | 설명 |
|------|------|------|
| 승인대기 | `PENDING` | 등록 직후 또는 수정 후 재승인 대기 |
| 승인 | `APPROVED` | 조장이 승인 |
| 반려 | `REJECTED` | 조장이 반려 (반려 사유 포함) |

### 진행 상태 (Progress Status)

승인된 과제에 한하여 날짜 기반으로 자동 산출된다.

| 상태 | 코드 | 조건 |
|------|------|------|
| 미진행 | `NOT_STARTED` | 현재 날짜 < 착수일 |
| 진행중 | `IN_PROGRESS` | 착수일 <= 현재 날짜 <= 완료예정일 + 5일 |
| 지연 | `DELAYED` | 현재 날짜 > 완료예정일 + 5일 AND 최종결과 미등록 |
| 완료 | `COMPLETED` | 최종결과 등록 AND 등록 시점 <= 완료예정일 + 5일 |
| 지연완료 | `DELAYED_COMPLETED` | 최종결과 등록 AND 등록 시점 > 완료예정일 + 5일 |

### 상태 흐름도

```
[등록] → PENDING(승인대기)
           ├─ 승인 → APPROVED → 날짜 기반 자동 산출
           │                     ├─ NOT_STARTED (착수일 전)
           │                     ├─ IN_PROGRESS (착수일 ~ 완료예정일+5일)
           │                     ├─ DELAYED (완료예정일+5일 초과, 미완료)
           │                     ├─ COMPLETED (결과 등록, 기한 내)
           │                     └─ DELAYED_COMPLETED (결과 등록, 기한 초과)
           │
           ├─ 반려 → REJECTED → 수정 후 재등록 → PENDING
           │
           └─ 수정 → PENDING (재승인 필요)
```

---

## 핵심 기능

### 1. 과제 등록
- 모든 구성원이 과제를 등록할 수 있다
- 등록 시 승인 상태는 `PENDING`
- 필수 입력: 제목, 내용, 착수일, 완료예정일
- 선택 입력: 담당자

### 2. 과제 수정
- 모든 구성원이 과제를 수정할 수 있다
- 승인된 과제를 수정하면 승인 상태가 `PENDING`으로 변경되어 재승인 프로세스를 탄다
- 수정 이력이 기록된다

### 3. 과제 삭제
- `PENDING`, `REJECTED` 상태의 과제: 등록자 본인이 삭제 가능
- `APPROVED` 상태의 과제: 조장만 삭제 가능

### 4. 과제 승인
- 조장만 가능
- `PENDING` 상태의 과제를 `APPROVED`로 변경

### 5. 과제 반려
- 조장만 가능
- `PENDING` 상태의 과제를 `REJECTED`로 변경
- 반려 사유를 필수로 입력

### 6. 최종결과 등록
- 승인된 과제에 최종결과(텍스트)를 등록하면 자동으로 완료 처리
- 완료예정일 + 5일 이내 등록: `COMPLETED`
- 완료예정일 + 5일 초과 등록: `DELAYED_COMPLETED`

---

## 데이터 모델

### User (사용자)

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | PK |
| username | String | 로그인 아이디 |
| password | String | 비밀번호 |
| name | String | 이름 |
| role | Enum | LEADER, MEMBER |
| createdAt | LocalDateTime | 생성일시 |

### Assignment (과제)

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | PK |
| title | String | 제목 |
| content | String | 내용 |
| startDate | LocalDate | 착수일 |
| dueDate | LocalDate | 완료예정일 |
| approvalStatus | Enum | PENDING, APPROVED, REJECTED |
| rejectionReason | String | 반려 사유 |
| finalResult | String | 최종결과 텍스트 |
| resultRegisteredAt | LocalDateTime | 최종결과 등록일시 |
| createdBy | User (FK) | 등록자 |
| createdAt | LocalDateTime | 생성일시 |
| updatedAt | LocalDateTime | 수정일시 |

> 진행 상태(progressStatus)는 DB에 저장하지 않고, 조회 시 날짜 기반으로 실시간 산출한다.

---

## API 설계

### 페이지

| Method | URL | 설명 | 접근 권한 |
|--------|-----|------|-----------|
| GET | `/login` | 로그인 페이지 | ALL |
| POST | `/login` | 로그인 처리 | ALL |
| GET | `/assignments` | 과제 목록 | 인증 사용자 |
| GET | `/assignments/new` | 과제 등록 폼 | 인증 사용자 |
| GET | `/assignments/{id}` | 과제 상세 | 인증 사용자 |
| GET | `/assignments/{id}/edit` | 과제 수정 폼 | 인증 사용자 |

### API

| Method | URL | 설명 | 접근 권한 |
|--------|-----|------|-----------|
| POST | `/assignments` | 과제 등록 | 인증 사용자 |
| PUT | `/assignments/{id}` | 과제 수정 | 인증 사용자 |
| DELETE | `/assignments/{id}` | 과제 삭제 | 등록자 / 조장 |
| POST | `/assignments/{id}/approve` | 과제 승인 | LEADER |
| POST | `/assignments/{id}/reject` | 과제 반려 | LEADER |
| POST | `/assignments/{id}/result` | 최종결과 등록 | 인증 사용자 |

---

## 패키지 구조

```
com.example.vite_coding_boot
├── ViteCodingBootApplication.java
├── controller/
│   ├── LoginController.java
│   └── AssignmentController.java
├── domain/
│   ├── User.java
│   ├── Assignment.java
│   ├── Role.java                  (enum: LEADER, MEMBER)
│   ├── ApprovalStatus.java        (enum: PENDING, APPROVED, REJECTED)
│   └── ProgressStatus.java        (enum: NOT_STARTED, IN_PROGRESS, DELAYED, COMPLETED, DELAYED_COMPLETED)
├── repository/
│   ├── UserRepository.java
│   └── AssignmentRepository.java
└── service/
    ├── UserService.java
    └── AssignmentService.java
```

---

## 구현 우선순위

1. **1단계**: 데이터 모델 및 JPA 엔티티 구성
2. **2단계**: 과제 CRUD (등록/수정/삭제/조회)
3. **3단계**: 승인/반려 프로세스
4. **4단계**: 최종결과 등록 및 자동 완료 처리
5. **5단계**: 진행 상태 자동 산출 로직
6. **6단계**: 권한 체크 (조장/조원 분기)
7. **7단계**: UI 화면 (Thymeleaf)
