# Screenshot Manifest

로컬 앱 기준 캡처 파일 목록입니다. Gamma 프롬프트와 화면 설계서에서 같은 파일명을 참조합니다.

| 파일명 | 화면 | 상태 |
| --- | --- | --- |
| `01-login.png` | 로그인 | 캡처 완료 |
| `02-onboarding.png` | 온보딩 | 캡처 완료 |
| `03-dashboard.png` | 대시보드 | 캡처 완료 |
| `04-inventory-list.png` | 냉장고 목록 | 캡처 완료 |
| `05-inventory-add.png` | 재고 등록 | 캡처 완료 |
| `06-inventory-detail.png` | 재고 상세 | 캡처 완료 |
| `07-meal-log.png` | 식단 기록 | 캡처 완료 |
| `08-recipe-recommendation.png` | 레시피 추천 | 캡처 완료 |
| `09-notifications.png` | 알림 목록 | 캡처 완료 |
| `10-admin-login.png` | 관리자 로그인 | 캡처 완료 |
| `11-admin-dashboard.png` | 관리자 대시보드 | 캡처 완료 |
| `12-admin-operations.png` | 관리자 API 사용량 | 캡처 완료 |

## 캡처 원칙

- 해상도: 1440x1000 이상 권장
- 브라우저 주소창/개인 계정 정보는 포함하지 않음
- OAuth client secret, API key, JWT token, 개인 이메일은 노출하지 않음
- 실제 데이터가 부족하면 데모용 mock 응답을 사용해 화면 상태를 만든 뒤 캡처

## 캡처 메모

- 캡처 일자: 2026-06-25
- 캡처 기준: 로컬 Vue/Vite 앱 `http://127.0.0.1:5173`
- API 데이터: 개인정보와 비밀값 노출을 막기 위해 로컬 임시 mock API 응답 사용
- OAuth/관리자 토큰: 실제 토큰이 아닌 캡처 전용 더미 토큰 사용

## 슬라이드 활용 가이드

| 파일명 | 추천 슬라이드 | 사용 의도 |
| --- | --- | --- |
| `01-login.png` | 로그인/서비스 시작 | OAuth 기반 진입을 보여줌 |
| `02-onboarding.png` | 사용자 흐름 | 신규 사용자 정보 입력 단계 설명 |
| `03-dashboard.png` | 표지/대시보드 | 서비스 전체 요약 화면 |
| `04-inventory-list.png` | 냉장고 기능 | 재고 상태와 유통기한 관리 |
| `05-inventory-add.png` | 냉장고 등록 | 수기/OCR/바코드 입력 구조 |
| `06-inventory-detail.png` | 냉장고 상세 | 수정/삭제 가능한 상세 정보 |
| `07-meal-log.png` | 식단 기록 | 저장된 기록과 영양 요약 |
| `08-recipe-recommendation.png` | 레시피 추천 | 재고 기반 추천 결과 |
| `09-notifications.png` | 알림 | 유통기한/공지 알림 처리 |
| `10-admin-login.png` | 관리자 | 운영자 인증 |
| `11-admin-dashboard.png` | 관리자 | 서비스 현황과 배치 요약 |
| `12-admin-operations.png` | 관리자 | API 사용량 모니터링 |

## 재캡처가 필요한 경우

- 팀명/브랜드명 UI가 바뀐 경우
- 실제 OAuth 리허설 화면을 사용하기로 한 경우
- 발표자료에서 특정 화면을 확대해야 하는 경우
- mock 데이터 문구를 실제 시연 데이터로 바꿔야 하는 경우
