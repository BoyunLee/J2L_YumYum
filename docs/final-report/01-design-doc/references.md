# 기타 참고 문서

## 개발 일지 기반 프로젝트 흐름

| 날짜 | 핵심 내용 |
| --- | --- |
| 2026-05-22 | ERD와 DDL 초안 작성. 냉장고, 식단, 추천, 알림, 관리자 도메인 정의 |
| 2026-05-29 | 기능/화면 기획. OCR/바코드/수기 등록, 추천 기준, 유통기한 배치 범위 결정 |
| 2026-06-05 | Vue/Vite FrontEnd와 Spring Boot BackEnd 프로젝트 구조 착수 |
| 2026-06-22 | Spring Security, OAuth2, JWT, 공통 응답/예외, 사용자 도메인 구현 |
| 2026-06-23 | OAuth/온보딩, 냉장고 CRUD, 알림, 레시피 추천 이력, FE 연동 |
| 2026-06-24 | OCR/바코드/GPT 구조화, FastAPI OCR 서비스, batch 등록, Docker 환경 구성 |
| 2026-06-25 | 데모 MVP 안정화. mock/live/fallback 통합 모드, 서버 추천 생성 API, 설정 정리 |

## 실행 환경

- FrontEnd: Vue 3, Vite, Pinia
- BackEnd: Spring Boot, MyBatis, MySQL
- OCR Service: FastAPI, Pororo OCR
- Infra: Docker Compose
- 외부 연동: OAuth Provider, GMS/GPT, 식품안전나라 API

## 주요 API

| 영역 | API |
| --- | --- |
| 사용자 | `GET/PATCH /api/users/me`, `PATCH /api/users/me/onboarding`, `PATCH /api/users/token/refresh` |
| 냉장고 | `GET /api/refrigerator/items`, `POST /api/refrigerator/items/manual`, `POST /api/refrigerator/items/analyze/{analysisType}` |
| 식단 | `GET /api/meal-logs/foods/search`, `POST /api/meal-logs`, `GET /api/meal-logs/summary`, `DELETE /api/meal-logs/{id}` |
| 추천 | `POST /api/meal-logs/recommendations/generate`, `GET /api/meal-logs/recommendations/latest` |
| 알림 | `GET /api/notifications`, `PUT /api/notifications/{id}/read`, `PUT /api/notifications/read-all` |
| 관리자 | `POST /api/admin/auth/login`, `GET /api/admin/dashboard`, `GET /api/admin/api-usage`, `POST /api/admin/batches/expiration`, `POST /api/admin/notices` |

## 제출 파일 구성 제안

- 설계 문서: `01-design-doc/` 원본을 Word 또는 PDF로 변환
- 소스 코드: `BackEnd`, `FrontEnd`, `OcrService`, `docker-compose.yml`, SQL 파일을 zip으로 제출
- 발표자료: Gamma에서 생성한 PPT/PDF
- AI 사용 보고서: `03-ai-report/` 원본을 별도 PDF 또는 발표 부록으로 제출

## 검증 명령 기록

| 검증 | 명령 | 상태 |
| --- | --- | --- |
| FrontEnd build | `npm run build` | 통과 |
| BackEnd compile | `mvn -q -DskipTests compile` | 통과 |
| Compose config | `docker compose config --quiet` | 통과 |
| 문서 whitespace | `git diff --check` | 통과 |
| 비밀값 패턴 검색 | `rg` 기반 token/secret/API key 검색 | 매칭 없음 |

## 외부 AI에 전달할 때 주의할 점

- `gamma-prompt.md`만 단독으로 넣기보다 `slide-outline.md`, `speaker-notes.md`, `screenshots/README.md`를 함께 제공한다.
- Mermaid 원본은 Gamma가 직접 렌더링하지 못할 수 있으므로, 필요하면 별도 Mermaid renderer에서 PNG로 변환한다.
- 팀명, 팀원, 팀 사진, 개인 회고는 실제 정보로 교체하기 전까지 플레이스홀더로 유지한다.
- 외부 AI가 실제 구현과 다른 기능을 추가하면 삭제한다. 특히 “모바일 앱”, “실시간 푸시 운영”, “정식 배포 URL”은 이번 제출 범위 밖이다.

## 제출 전 마지막 체크리스트

- [ ] 설계문서 6개 항목이 모두 Word/PDF에 포함됨
- [ ] PPT가 최소 10장 이상이며 20분 발표 흐름을 가짐
- [ ] AI 사용 보고서가 별도 파일 또는 PPT 부록에 포함됨
- [ ] 화면캡처 12장이 발표자료에 연결됨
- [ ] 팀 정보와 사진 플레이스홀더가 실제 정보로 교체됨
- [ ] 문서와 이미지에 secret/token/API key가 없음
