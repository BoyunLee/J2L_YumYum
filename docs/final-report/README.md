# YumYum Final Report Package

이 폴더는 최종 완료 보고서와 설계 문서, 발표자료 생성을 위해 외부 AI(Gamma 등)에 전달할 원본 자료 패키지입니다.

## 기준 코드

- 기준 커밋: `a963b9d feat: stabilize demo mvp flows`
- 프로젝트명: `YumYum`
- 팀 정보: `[팀명]`, `[팀원 이름/역할]`, `[팀 사진]`은 제출 전 교체
- 기술 스택: Vue 3/Vite/Pinia, Spring Boot/MyBatis/MySQL, FastAPI Pororo OCR, Docker Compose

## 폴더 구조

- `01-design-doc/`: 설계 문서 제출 항목 원본
- `02-presentation/`: Gamma 발표자료 생성 프롬프트, 슬라이드 구성, 발표 멘트
- `03-ai-report/`: AI 사용 보고서와 프롬프트 정리
- `04-assets/`: 화면캡처, 사진 플레이스홀더, Mermaid 다이어그램 원본

## 문서별 역할

| 경로 | 역할 | 외부 AI 입력 여부 |
| --- | --- | --- |
| `01-design-doc/requirements.md` | 요구사항, 품질 기준, 데모 성공 기준 | 설계문서 생성 시 제공 |
| `01-design-doc/use-case.md` | 사용자/관리자 use-case와 상세 흐름 | 설계문서 생성 시 제공 |
| `01-design-doc/class-diagram.md` | 백엔드 계층과 provider 구조 설명 | 설계문서 생성 시 제공 |
| `01-design-doc/erd.md` | DB 테이블 역할과 데이터 흐름 | 설계문서 생성 시 제공 |
| `01-design-doc/wbs-gantt.md` | 개발 일정, 역할, 리스크 | 발표자료와 설계문서 모두 제공 |
| `01-design-doc/screen-design.md` | 화면 목록, 발표 메시지, 캡처 기준 | 발표자료 생성 시 제공 |
| `02-presentation/gamma-prompt.md` | Gamma 입력용 메인 프롬프트 | 필수 제공 |
| `02-presentation/slide-outline.md` | 슬라이드별 구성과 시간 배분 | 필수 제공 |
| `02-presentation/speaker-notes.md` | 발표 멘트와 Q&A 대비 | 발표자 참고 |
| `03-ai-report/ai-usage-report.md` | AI 사용 보고서 본문 | 별도 문서 또는 PPT 부록 |
| `03-ai-report/ai-prompts.md` | 재사용 가능한 프롬프트 원문 | AI 사용 보고서 근거 |

## 외부 AI 사용 순서

1. `02-presentation/gamma-prompt.md`를 Gamma에 입력한다.
2. `02-presentation/slide-outline.md`와 `02-presentation/speaker-notes.md`를 참고자료로 함께 제공한다.
3. `04-assets/screenshots/`의 화면 이미지를 슬라이드별로 배치한다.
4. 설계 문서는 `01-design-doc/`의 Markdown과 `04-assets/diagrams/`의 Mermaid 원본을 Word/PDF로 변환한다.
5. AI 사용 보고서는 `03-ai-report/ai-usage-report.md`와 `03-ai-report/ai-prompts.md`를 별도 문서 또는 발표 부록으로 변환한다.

## 제출 전 체크

- 팀명, 팀원, 역할, 팀 사진 플레이스홀더 교체
- OAuth client secret, API key, 개인 토큰이 캡처/문서에 포함되지 않았는지 확인
- 발표자료는 최소 10장 이상, 권장 14~15장 구성 유지
- 발표 시간은 20분 기준으로 제품 배경 4분, 설계/구현 8분, 시연 5분, 회고/AI 사용 3분 배분

## 빠른 작업 순서

1. `photos/`에 팀 사진과 팀원 사진을 넣는다.
2. `[팀명]`, `[팀원 이름/역할]`, `[개인 회고]` 플레이스홀더를 실제 정보로 바꾼다.
3. Gamma에 `gamma-prompt.md`와 주요 캡처 12장을 넣어 PPT 초안을 생성한다.
4. 생성된 PPT를 `slide-outline.md`와 대조해 누락 슬라이드를 보완한다.
5. AI 사용 보고서는 `ai-usage-report.md`를 기반으로 별도 PDF 또는 PPT 부록으로 넣는다.
6. 최종 제출 전 비밀값 검색과 화면 시각 검수를 다시 수행한다.

## 품질 목표

- 평가자가 “무엇을 만들었고, 왜 만들었고, 실제로 어디까지 동작하는지”를 3분 안에 파악할 수 있어야 한다.
- 발표자는 이 폴더만 보고 20분 발표의 흐름과 시연 순서를 복원할 수 있어야 한다.
- 외부 AI는 이 폴더만으로 PPT 초안을 만들 수 있어야 하며, 임의의 기능을 추가하지 않아야 한다.
