## 팀 개발 규칙 및 가이드

### 팀 규칙

- 코드 리뷰를 적극적으로 하자
- PR 작성 시에, 자신이 왜 이런 코드를 작성했는지 간단하게라도 의사를 담아 작성
- 설정파일은 .properties 대신 .yml으로 구성
- 환경변수 내용 업데이트를 잊지 말자
- Slack은 깔끔하게 쓰레드 단위로 관리
- 다른 사람이 작성한 코드를 수정하는 경우에는 꼭 알려주기
- 테스트 코드는 작성하되, 커버리지에 강제성을 크게 두지는 않음

### **Code Convention 및 팀 내 규칙**

- DTO 클래스 생성 시 **Java Record** 이용
- Java Google Code Convention 적용

#### DTO 네임 컨벤션

```
[도메인][용도][Request/Response]Dto

- SignUpRequestDto
- UserResponseDto
- OrderCreateRequestDto
- StoreDetailResponseDto
```

---

### **Git Branch 전략**

#### Git Flow 이용

- `main` : 서비스 최신 배포 브랜치
- `dev` : `feature` 브랜치에서 개발된 내용이 합쳐지는 브랜치
- `feat` : `develop` 브랜치에서 분기되어 기능 별 개발 브랜치

#### 브랜치명

```
feat/[도메인]/[기능이름]

예시:
- feat/auth/login
- feat/order/payment-integration
- feat/store/menu-management
```

---

### 브랜치 관리 규칙

1. dev 브랜치로 병합 시 PR 필수
2. feat 브랜치는 dev에 병합 후 삭제
3. main 브랜치는 직접 커밋 금지

### **깃헙 커밋 규칙**

| 커밋 유형      | 의미                                       |
|------------|------------------------------------------|
| `Feat`     | 새로운 기능 추가                                |
| `Fix`      | 버그 수정 (기능에 수정사항 있을경우)                    |
| `Docs`     | 문서 수정                                    |
| `Style`    | 코드 formatting, 세미콜론 누락, 코드 자체의 변경이 없는 경우 |
| `Refactor` | 코드 리팩토링 (기능에 수정 없음)                      |
| `Test`     | 테스트 코드, 리팩토링 테스트 코드 추가                   |
| `Chore`    | 패키지 매니저 수정, 그 외 기타 수정 ex) `.gitignore`   |
| `Design`   | CSS 등 사용자 UI 디자인 변경                      |
| `Comment`  | 필요한 주석 추가 및 변경                           |
| `Rename`   | 파일 또는 폴더 명을 수정하거나 옮기는 작업만인 경우            |
| `Remove`   | 파일을 삭제하는 작업만 수행한 경우                      |
| `!HOTFIX`  | 급하게 치명적인 버그를 고쳐야 하는 경우                   |
| `Config`   | 프로젝트 공통 설정 추가                            |
| `Init`     | 프로젝트 초기화 설정                              |
| `WIP`      | 코드 수정중(Work In Progress)                 |

**커밋 메시지 형식**

```
[타입]([도메인]): [메시지]

예시:
- feat(auth): 회원가입 API 구현
- fix(order): 결제 금액 계산 오류 수정
- refactor(user): 회원 조회 로직 개선
- test(store): 매장 생성 테스트 추가
```

### Pull Request 규칙

#### PR 생성 시

- 제목: [타입] 작업 내용 요약
- 설명: 변경 사항, 테스트 결과, 특이사항 작성
- 리뷰어: 최소 5명 지정

#### PR 승인 조건

- 모든 테스트 통과
- 코드 리뷰 승인 2개 이상
- 컨벤션 준수 확인

### **프로젝트 구조**

```
src
├── main
│   ├── java
│   │   └── com
│   │       └── sparta
│   │           └── tdd
│   │               ├── TddApplication.java
│   │               ├── domain
│   │               │   ├── ai
│   │               │   ├── auth
│   │               │   ├── cart
│   │               │   ├── coupon
│   │               │   ├── menu
│   │               │   ├── order
│   │               │   ├── payment
│   │               │   ├── point
│   │               │   ├── review
│   │               │   ├── store
│   │               │   └── user
│   │               └── global
│   │                   ├── aop
│   │                   ├── config
│   │                   ├── exception
│   │                   └── jwt
│   └── resources
│       ├── application.yml
│       └── env.properties
└── test
    └── java
        └── com
            └── sparta
                └── tdd
                    ├── common
                    └── domain
```

### 디렉토리 설명

#### `domain`

애플리케이션의 핵심 비즈니스 로직을 도메인별로 관리합니다.

- 각 도메인은 `controller`, `service`, `repository`, `entity`, `dto` 계층으로 구성
- 도메인 간 의존성 최소화 원칙

#### `global`

도메인에 종속되지 않는 전역 공통 기능을 관리합니다.

- **`config`**: Spring Security, JPA, Swagger 등 설정
- **`exception`**: 전역 예외 처리 및 커스텀 예외 정의
- **`jwt`**: JWT 생성, 검증, 파싱 로직
- **`aop`**: 로깅, 트랜잭션 등 횡단 관심사 관리

#### `test`

테스트 코드 관리 디렉토리

- **`common/helper`**: 테스트용 커스텀 어노테이션 및 헬퍼 클래스
- **`common/template`**: 테스트 베이스 클래스 (`IntegrationTest`, `RepositoryTest`)
- **`domain`**: 도메인별 테스트 코드 (main 구조와 동일)