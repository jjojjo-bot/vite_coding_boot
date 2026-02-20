package com.example.vite_coding_boot.adapter.out.persistence;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.vite_coding_boot.application.port.out.AssignmentRepository;
import com.example.vite_coding_boot.application.port.out.TeamRepository;
import com.example.vite_coding_boot.application.port.out.UserRepository;
import com.example.vite_coding_boot.domain.model.ApprovalStatus;
import com.example.vite_coding_boot.domain.model.Assignment;
import com.example.vite_coding_boot.domain.model.Role;
import com.example.vite_coding_boot.domain.model.Team;
import com.example.vite_coding_boot.domain.model.User;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final AssignmentRepository assignmentRepository;

    public DataInitializer(UserRepository userRepository, TeamRepository teamRepository,
                           AssignmentRepository assignmentRepository) {
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            Team 인사팀 = teamRepository.save(new Team("경영본부", "경영지원실", "인사팀"));
            Team 재무팀 = teamRepository.save(new Team("경영본부", "경영지원실", "재무팀"));
            Team 백엔드팀 = teamRepository.save(new Team("기술본부", "개발실", "백엔드팀"));
            Team 프론트엔드팀 = teamRepository.save(new Team("기술본부", "개발실", "프론트엔드팀"));
            Team 클라우드팀 = teamRepository.save(new Team("기술본부", "인프라실", "클라우드팀"));

            userRepository.save(new User("leader", "1234", "조장", Role.LEADER, 인사팀));
            userRepository.save(new User("member1", "1234", "조원1", Role.MEMBER, 백엔드팀));
            userRepository.save(new User("member2", "1234", "조원2", Role.MEMBER, 프론트엔드팀));

            userRepository.save(new User("user01", "1234", "김인사", Role.MEMBER, 인사팀));
            userRepository.save(new User("user02", "1234", "이인사", Role.MEMBER, 인사팀));
            userRepository.save(new User("user03", "1234", "박재무", Role.MEMBER, 재무팀));
            userRepository.save(new User("user04", "1234", "최재무", Role.MEMBER, 재무팀));
            userRepository.save(new User("user05", "1234", "정백엔", Role.MEMBER, 백엔드팀));
            userRepository.save(new User("user06", "1234", "강백엔", Role.MEMBER, 백엔드팀));
            userRepository.save(new User("user07", "1234", "조프론", Role.MEMBER, 프론트엔드팀));
            userRepository.save(new User("user08", "1234", "윤프론", Role.MEMBER, 프론트엔드팀));
            userRepository.save(new User("user09", "1234", "임클라", Role.MEMBER, 클라우드팀));
            userRepository.save(new User("user10", "1234", "한클라", Role.MEMBER, 클라우드팀));

            createSampleAssignments();
        }
    }

    private void createSampleAssignments() {
        List<User> allUsers = userRepository.findAll();
        Random rng = new Random(42);

        String[] titles = {
            "1분기 경영보고서 작성", "API 성능 최적화", "신규 채용 공고 등록",
            "클라우드 인프라 마이그레이션", "프론트엔드 UI 리뉴얼", "보안 취약점 점검",
            "연간 예산안 수립", "CI/CD 파이프라인 구축", "사내 교육 프로그램 기획",
            "데이터베이스 백업 자동화", "모바일 앱 성능 개선", "고객 만족도 조사 분석",
            "내부 감사 보고서 작성", "마이크로서비스 전환 설계", "인사 평가 시스템 개선",
            "서버 모니터링 대시보드 구축", "결제 시스템 리팩토링", "신입사원 온보딩 매뉴얼",
            "2분기 매출 분석 보고", "REST API 문서화", "네트워크 장비 교체 계획",
            "코드 리뷰 가이드라인 수립", "복리후생 제도 개선안", "로그 수집 시스템 구축",
            "프로젝트 관리 도구 도입", "개인정보 처리방침 갱신", "성능 테스트 자동화",
            "사내 메신저 도입 검토", "재무제표 분석 리포트", "컨테이너 오케스트레이션 구축",
            "ERP 시스템 업그레이드", "모바일 푸시 알림 구현", "사내 위키 플랫폼 구축",
            "월간 KPI 리포트 자동화", "OAuth 2.0 인증 도입", "직원 건강검진 일정 관리",
            "GraphQL API 전환", "데이터 파이프라인 설계", "사내 보안 교육 진행",
            "캐시 레이어 최적화", "AWS 비용 절감 분석", "채용 면접 프로세스 개선",
            "메시지 큐 시스템 도입", "프론트엔드 빌드 최적화", "급여 정산 시스템 개선",
            "API 게이트웨이 구축", "UI 컴포넌트 라이브러리 개발", "퇴직연금 제도 검토",
            "실시간 알림 시스템 구축", "코드 정적 분석 도입", "사내 동호회 지원 체계 수립",
            "데이터 마이그레이션 계획", "WebSocket 채팅 구현", "연차 관리 시스템 자동화",
            "서비스 장애 대응 매뉴얼", "검색 엔진 최적화", "법인카드 관리 시스템 구축",
            "Blue/Green 배포 전환", "접근성(A11y) 개선", "2분기 인력 운영 계획",
            "Redis 클러스터 구성", "다국어 지원 시스템 구축", "사내 설문조사 플랫폼 개발",
            "gRPC 서비스 전환", "이미지 CDN 최적화", "직무 역량 평가 도구 개발",
            "Terraform IaC 전환", "모바일 반응형 개선", "상반기 교육 성과 분석",
            "로드밸런서 이중화", "디자인 시스템 v2 구축", "사내 포인트 제도 기획",
            "서비스 메시 도입", "E2E 테스트 자동화", "하반기 채용 계획 수립",
            "데이터 레이크 구축", "PWA 전환 프로젝트", "조직문화 개선 TF 운영",
            "API 버전 관리 체계 수립", "성능 모니터링 알림 고도화", "사내 멘토링 프로그램 기획",
            "DB 샤딩 전략 수립", "SSR 렌더링 도입", "상반기 복지 만족도 조사",
            "CI 파이프라인 병렬화", "마이크로 프론트엔드 전환", "임직원 역량 개발 로드맵",
            "서버리스 아키텍처 PoC", "웹 성능 Core Vitals 개선", "글로벌 인재 채용 전략",
            "Feature Flag 시스템 구축", "크로스 브라우저 테스트 자동화",
            "사내 SSO 통합 인증 구축", "배치 작업 스케줄링 시스템", "오픈소스 라이선스 점검",
            "모바일 앱 접근성 개선", "장기 미사용 계정 정리 자동화", "사내 기술 블로그 플랫폼 구축",
            "API Rate Limiting 도입", "클라우드 보안 감사 체계 수립"
        };

        String[] descriptions = {
            "1분기 실적을 정리하여 경영진에 보고", "주요 API 응답시간 50% 개선", "개발팀 신규 인력 3명 채용",
            "온프레미스에서 AWS로 전환", "사용자 경험 개선을 위한 전면 리디자인", "OWASP Top 10 기반 점검",
            "다음 회계연도 부서별 예산 편성", "자동 빌드/배포 환경 구성", "분기별 직무 교육 과정 설계",
            "일일 자동 백업 및 복구 테스트", "앱 로딩 속도 2초 이내 달성", "NPS 설문 결과 분석 및 개선안",
            "분기 내부 감사 결과 정리", "모놀리스를 MSA로 분리하는 아키텍처 설계", "MBO 기반 평가 체계 도입",
            "Grafana 기반 실시간 모니터링", "레거시 결제 모듈 현대화", "입사 첫 주 가이드 문서 작성",
            "2분기 사업부별 매출 현황 정리", "Swagger/OpenAPI 기반 문서 자동화", "노후 스위치 및 라우터 교체",
            "팀 내 코드 품질 기준 정립", "직원 설문 기반 복지 개선", "ELK 스택 기반 중앙 로그 관리",
            "Jira/Notion 등 도구 비교 분석", "개정된 법률에 맞게 방침 업데이트", "JMeter 기반 부하 테스트 자동화",
            "Slack 대안 검토 및 PoC", "월별 재무 현황 시각화", "Kubernetes 클러스터 구성 및 운영",
            "SAP ERP 최신 버전으로 업그레이드", "FCM/APNs 기반 푸시 알림 구현", "Confluence 대체 위키 구축",
            "Power BI 기반 KPI 대시보드 자동화", "소셜 로그인 및 SSO 통합 인증", "협력 병원 연계 건강검진 관리",
            "REST에서 GraphQL로 점진적 전환", "Spark 기반 실시간 데이터 처리", "피싱 대응 및 정보보호 교육",
            "Redis/Memcached 캐시 전략 수립", "월별 AWS 리소스 사용량 분석", "구조화 면접 및 평가표 개선",
            "RabbitMQ/Kafka 도입 비교 분석", "Webpack → Vite 전환 및 번들 최적화", "급여 명세서 자동 생성 시스템",
            "Kong/Envoy 기반 API 게이트웨이", "Storybook 기반 공통 UI 컴포넌트", "DC/DB형 퇴직연금 비교 분석",
            "SSE/WebSocket 기반 실시간 알림", "SonarQube 기반 코드 품질 자동 점검", "분기별 동호회 활동 지원금 관리",
            "legacy DB → 신규 DB 무중단 이전", "Socket.IO 기반 실시간 채팅", "연차 자동 부여 및 잔여 일수 관리",
            "장애 등급별 대응 절차서 작성", "Elasticsearch 기반 통합 검색", "법인카드 사용 내역 자동 정산",
            "무중단 Blue/Green 배포 파이프라인", "WCAG 2.1 AA 기준 접근성 개선", "부서별 인력 수급 계획서 작성",
            "Redis Sentinel → Cluster 전환", "i18n 프레임워크 기반 다국어 대응", "Google Forms 대체 사내 설문 도구",
            "HTTP/2 기반 gRPC 마이그레이션", "CloudFront + S3 이미지 최적화", "역량 모델 기반 평가 도구 개발",
            "AWS 인프라 Terraform 코드화", "모바일 퍼스트 반응형 레이아웃", "교육 이수율 및 만족도 분석 보고",
            "ALB 이중화 및 헬스체크 구성", "Design Token 기반 시스템 v2", "활동 포인트 적립/사용 시스템",
            "Istio 기반 서비스 메시 구축", "Cypress/Playwright E2E 테스트", "하반기 직무별 채용 인원 산정",
            "S3 + Athena 기반 데이터 레이크", "오프라인 대응 PWA 전환", "조직문화 진단 및 개선 활동",
            "URL 버전/헤더 버전 관리 정책", "PagerDuty 연동 알림 고도화", "시니어-주니어 멘토링 매칭",
            "Vitess/Citus 기반 수평 샤딩", "Next.js SSR 렌더링 적용", "복지 항목별 만족도 설문 분석",
            "GitHub Actions 병렬 빌드", "Module Federation 마이크로 FE", "직급별 역량 개발 로드맵 수립",
            "Lambda + API GW 서버리스 PoC", "LCP/FID/CLS 성능 지표 개선", "해외 개발자 채용 채널 발굴",
            "LaunchDarkly 기반 Feature Flag", "Selenium Grid 크로스 브라우저 자동화",
            "SAML/OIDC 기반 SSO 통합", "Spring Batch 기반 야간 배치 스케줄링", "SBOM 기반 오픈소스 라이선스 분석",
            "iOS/Android 접근성 가이드라인 적용", "90일 미접속 계정 자동 잠금 처리", "Hugo/Jekyll 기반 기술 블로그 운영",
            "Token Bucket 기반 API 트래픽 제어", "CIS Benchmark 기반 클라우드 보안 감사"
        };

        String[] rejectionReasons = {
            "요구사항이 불명확합니다. 구체적인 목표를 재정의해주세요.",
            "예산 범위를 초과합니다. 비용 절감 방안을 포함해주세요.",
            "일정이 비현실적입니다. 마일스톤을 재조정해주세요.",
            "기술적 타당성 검토가 부족합니다.",
            "다른 프로젝트와 일정이 충돌합니다.",
            "담당 인력이 부족합니다. 인력 계획을 수정해주세요.",
            "선행 과제가 완료되지 않았습니다.",
            "보안 검토가 필요합니다."
        };

        String[] finalResults = {
            "목표 대비 120% 달성. 상세 보고서 첨부.",
            "예정대로 완료. 테스트 결과 정상 확인.",
            "일부 지연이 있었으나 핵심 기능 모두 구현 완료.",
            "성능 목표치 달성. 응답시간 40% 개선.",
            "파일럿 테스트 완료. 전사 적용 승인 대기.",
            "문서화 및 인수인계 완료.",
            "1차 배포 완료. 모니터링 중.",
            "분석 보고서 제출 완료. 경영진 승인 완료.",
            "시스템 안정화 확인. 운영 이관 완료.",
            "교육 완료. 수료율 95% 달성."
        };

        // 2026-01-01 ~ 2026-06-01 범위의 착수일
        LocalDate rangeStart = LocalDate.of(2026, 1, 1);
        int rangeDays = 151; // Jan 1 ~ May 31

        for (int i = 0; i < 100; i++) {
            User creator = allUsers.get(rng.nextInt(allUsers.size()));
            LocalDate startDate = rangeStart.plusDays(rng.nextInt(rangeDays));
            LocalDate dueDate = startDate.plusDays(7 + rng.nextInt(21));

            Assignment a = new Assignment(
                    titles[i], descriptions[i], creator, startDate, dueDate);

            // 분포: ~30% PENDING, ~50% APPROVED, ~20% REJECTED
            int roll = rng.nextInt(100);
            if (roll < 30) {
                // PENDING — 그대로
            } else if (roll < 80) {
                // APPROVED
                a.setApprovalStatus(ApprovalStatus.APPROVED);
                User assignee = allUsers.get(rng.nextInt(allUsers.size()));
                a.setUser(assignee);

                // 승인된 과제 중 60%는 최종결과 입력 완료
                if (rng.nextInt(100) < 60) {
                    a.setFinalResult(finalResults[rng.nextInt(finalResults.length)]);
                    // 결과 등록일: 마감일 전후 -3 ~ +10일
                    int resultOffset = -3 + rng.nextInt(14);
                    a.setResultRegisteredAt(
                            dueDate.plusDays(resultOffset).atTime(9 + rng.nextInt(9), rng.nextInt(60)));
                }
            } else {
                // REJECTED
                a.setApprovalStatus(ApprovalStatus.REJECTED);
                a.setRejectionReason(rejectionReasons[rng.nextInt(rejectionReasons.length)]);
            }

            assignmentRepository.save(a);
        }
    }
}
