# Gamma 발표자료 생성 프롬프트

아래 내용을 바탕으로 SSAFY 프로젝트 최종 발표용 PPT를 만들어 주세요.

## 발표 기본 조건

- 발표 언어: 한국어
- 발표 시간: 약 20분
- 슬라이드 수: 14장 내외, 최소 10장 이상
- 발표 목적: YumYum 프로젝트의 기획 배경, 설계, 구현 결과, 시연 흐름, 기대 효과, 개발 후기를 설득력 있게 전달
- 디자인 톤: Toss Design System처럼 밝고 정돈된 느낌. 흰색/연회색 배경, 선명한 포인트 컬러, 넓은 여백, 짧은 문장, 카드형 정보 배치
- 피해야 할 표현: 과도한 장식, 긴 문단, 추상적인 AI 이미지, 비밀키/API 키 노출
- 표지 정보:
  - 프로젝트명: YumYum
  - 부제: 냉장고 재고 기반 식단 기록 및 레시피 추천 서비스
  - 팀명: `[팀명 입력]`
  - 팀원: `[팀원 이름/역할 입력]`

## 발표 대상과 평가 포인트

- 대상: SSAFY 프로젝트 평가자, 팀원, 동료 교육생
- 평가자가 빠르게 봐야 하는 것:
  - 문제 정의가 명확한가
  - 화면과 기능이 실제 구현되어 있는가
  - DB/시스템 구조가 기능과 연결되는가
  - 외부 API/OCR/AI 사용이 데모 안정성까지 고려했는가
  - 팀이 어떤 시행착오를 겪고 어떻게 해결했는가

## 프로젝트 요약

YumYum은 냉장고 속 식재료를 등록하고, 유통기한 알림과 식단 기록, 보유 재료 기반 레시피 추천을 제공하는 웹 서비스입니다. 사용자는 OAuth 로그인 후 온보딩을 완료하고, 재고를 수기/OCR/바코드 방식으로 등록합니다. 이후 재고 목록, 식단 기록, 일일 영양 요약, 레시피 추천, 알림을 사용할 수 있습니다. 관리자는 `/admin`에서 서비스 현황, API 사용량, 유통기한 배치, 공지, 감사 로그를 확인합니다.

## 핵심 차별점

- OCR/바코드/수기 입력을 모두 지원하는 냉장고 등록 흐름
- 냉장고 재고와 식단 기록을 연결한 사용자 중심 식생활 관리
- 유통기한 임박 재료와 보유 재료를 활용한 레시피 추천
- 외부 API 장애에도 데모가 멈추지 않도록 `mock/live/fallback` 모드를 지원
- 관리자 화면에서 API 사용량, 배치, 공지, 운영 로그를 확인 가능

## 기술 스택

- FrontEnd: Vue 3, Vite, Pinia
- BackEnd: Spring Boot, MyBatis, MySQL
- OCR: FastAPI, Pororo OCR
- Infra: Docker Compose
- 외부 연동: OAuth Provider, GMS/GPT, 식품안전나라 API

## 추천 슬라이드 구성

1. 표지
2. 목차
3. 기획 배경과 문제 정의
4. 서비스 목표와 핵심 사용자 흐름
5. 경쟁 서비스 비교와 차별화 전략
6. 전체 시스템 구조
7. DB/ERD 및 도메인 설계
8. 냉장고 등록: 수기/OCR/바코드
9. 식단 기록과 일일 영양 요약
10. 레시피 추천 구조와 mock/live/fallback 전략
11. 알림과 관리자 기능
12. 화면 흐름 및 시연 시나리오
13. 기대 효과와 향후 개선
14. 개발 후기
15. AI 사용 보고 요약

## 사용할 이미지 자료

아래 파일 경로를 슬라이드에 적극적으로 배치해 주세요. 파일이 아직 없으면 이미지 자리표시자를 만들고, 파일명 기준으로 나중에 교체할 수 있게 해 주세요.

- `../04-assets/screenshots/01-login.png`
- `../04-assets/screenshots/02-onboarding.png`
- `../04-assets/screenshots/03-dashboard.png`
- `../04-assets/screenshots/04-inventory-list.png`
- `../04-assets/screenshots/05-inventory-add.png`
- `../04-assets/screenshots/06-inventory-detail.png`
- `../04-assets/screenshots/07-meal-log.png`
- `../04-assets/screenshots/08-recipe-recommendation.png`
- `../04-assets/screenshots/09-notifications.png`
- `../04-assets/screenshots/10-admin-login.png`
- `../04-assets/screenshots/11-admin-dashboard.png`
- `../04-assets/screenshots/12-admin-operations.png`
- `../04-assets/diagrams/system-architecture.mmd`
- `../04-assets/diagrams/class-diagram.mmd`
- `../04-assets/diagrams/class-diagram-integration-detail.mmd`
- `../04-assets/diagrams/erd.mmd`
- `../04-assets/diagrams/user-flow.mmd`

## 슬라이드별 이미지 매핑

| 슬라이드 | 권장 이미지 |
| --- | --- |
| 표지 | `03-dashboard.png` 또는 `08-recipe-recommendation.png` |
| 서비스 흐름 | `user-flow.mmd` |
| 시스템 구조 | `system-architecture.mmd` |
| DB/도메인 | `erd.mmd`, `class-diagram.mmd` |
| 냉장고 등록 | `04-inventory-list.png`, `05-inventory-add.png`, `06-inventory-detail.png` |
| 식단 기록 | `07-meal-log.png` |
| 레시피 추천 | `08-recipe-recommendation.png`, `class-diagram-integration-detail.mmd` |
| 알림/관리자 | `09-notifications.png`, `11-admin-dashboard.png`, `12-admin-operations.png` |
| 개발 후기 | `../04-assets/photos/team-photo.jpg` 자리표시자 |

## 발표 스토리라인

문제는 “냉장고 속 식재료를 기억하지 못해 유통기한이 지나고, 오늘 무엇을 먹을지 결정하는 데 시간이 든다”입니다. YumYum은 이 문제를 재고 등록, 알림, 식단 기록, 추천으로 연결해 해결합니다. 발표는 기능 나열이 아니라 사용자 흐름 중심으로 진행합니다. 특히 시연에서는 로그인 후 냉장고 재고를 등록하고, 식단을 기록한 뒤, 레시피 추천과 알림/관리자 화면까지 이어지는 흐름을 보여줍니다.

## 발표 톤 가이드

- PM/QA 관점에서 “왜 이 기능이 필요한가”를 먼저 말하고, 그 다음 구현 구조를 설명한다.
- 기술 용어는 한 번에 하나씩만 소개하고, 화면 캡처와 연결해 설명한다.
- 레시피 추천은 “AI가 똑똑하다”보다 “외부 API 장애에도 시연이 멈추지 않는 구조”를 강조한다.
- 관리자 기능은 부가 기능이 아니라 운영 안정성의 증거로 설명한다.
- 개발 후기는 과장하지 말고, 실제로 겪은 일정 압박, 외부 API, 인증, 문서화 이슈를 중심으로 구성한다.

## 슬라이드 작성 규칙

- 한 슬라이드에는 핵심 메시지 1개만 둔다.
- 표는 최대 4행 내외로 압축한다.
- 코드 상세보다 구조와 의사결정 이유를 강조한다.
- “AI 추천”은 실제 외부 API만 강조하지 말고, mock/live/fallback 전략으로 데모 안정성을 확보한 점을 설명한다.
- 마지막 개발 후기는 팀 사진과 개인별 한 줄 회고를 넣을 수 있는 레이아웃으로 만든다.
- 슬라이드 하단에 작은 글씨로 파일명이나 구현 근거를 넣지 않는다. 발표자 노트에만 남긴다.
- 비밀값이나 실제 계정 정보처럼 보이는 텍스트는 만들지 않는다.

## AI 사용 보고 슬라이드 요약

AI는 기획 정리, 코드 안정화, 외부 API provider 패턴 설계, 문서 초안 작성, 발표자료 프롬프트 구성에 사용했습니다. 사용한 프롬프트 유형은 “코드베이스 분석”, “데모 MVP 안정화”, “문서 산출물 생성”, “QA 체크리스트 작성”, “발표자료 구성”입니다. AI 산출물은 사람이 검토하고 실제 코드/빌드/화면 기준으로 검증했습니다.

## 생성 후 검수 기준

- 슬라이드 수가 10장 미만이면 실패다.
- 실제 구현되지 않은 모바일 앱, 실시간 운영 대시보드, 공개 배포 URL을 임의로 추가하면 안 된다.
- 스크린샷이 없는 슬라이드는 다이어그램 또는 표로 근거를 보강한다.
- 발표자가 읽을 수 있도록 각 슬라이드의 핵심 문장은 2줄 이내로 제한한다.
- “YumYum”과 “냠냠” 표기는 혼용될 수 있으나, 표지와 제목은 `YumYum`으로 통일한다.
