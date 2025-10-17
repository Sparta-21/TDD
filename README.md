# TDD(Today Delicious Delivery)

## 프로젝트 소개

> Spring boot 모놀리식 어플리케이션 개발 학습을 위해 주문 관리 플랫폼 프로젝트를 진행하였습니다.

TDD(Today Delicious Delivery)는 이름 그대로 '오늘의 맛있는 배달' 이라는 서비스 컨셉을 가져가는 동시에 Test-driven development 철학을
실천하려는 의도를 담고 있습니다.
프로젝트 전반으로는 핵심 기능(MVP)을 먼저 수립하고, 이후 부가적인 도메인 기능을 확장해 나가는 방향으로 개발을 진행하였습니다.

비즈니스 측면에서는 사용자의 주문 과정 전반에서 실제 서비스와 유사한 경험을 제공하도록 다양한 시나리오를 설계했습니다.
특히 권한별 접근 제어(CUSTOM, OWNER, MANAGER, MASTER)에 중점을 두어, 각 역할에 따라 가능한 시나리오를 설계하고 요청 처리 및 검증 로직을 정교하게
구현하였습니다.

기술적으로는 클린 코드와 일관된 컨벤션을 중요한 목표로 삼았습니다.
각 클래스의 역할과 책임을 명확히 구분하면서도 중복되거나 누락된 로직을 최소화하고 더 좋은 방향성을 찾기 위해 위해 팀원 간의 코드 리뷰(PR)를 적극적으로 활용하였습니다.
개발 후기 단계에서는 도메인 구현만을 넘어서 어떤 방식이 더 나을지 근거를 기반으로 의논하며 기술적 판단력과 설계 역량을 함께 성장시키는 데 집중했습니다.

## 개발 환경 소개

| 분류                  | 상세                                                                         |
|---------------------|----------------------------------------------------------------------------|
| **Back-End**        | Java 21, Spring Boot 3.5.6, Spring Data JPA, Querydsl 7.0, Spring Security |
| **Database**        | PostgreSQL 18.0                                                            |
| **Build Tool**      | Gradle                                                                     |
| **Infra**           | Docker compose, Github Actions(CI)                                         |
| **open API**        | Google GenAI API, Naver Map API                                            |
| **Testing**         | JUnit5, Mockito, TestContainers                                            
| **Version Control** | Git                                                                        |
| **API Docs**        | Swagger                                                                    |

## 프로젝트 실행 가이드

### 1. 환경변수 설정

아래 값 환경 변수 등록 후, application.yml 에서 .env.properties 사용

```
DB_PASSWORD=password
DB_URL=
DB_USERNAME=test
JWT_ACCESS_EXPIRED=
JWT_ACCESS_SECRET=
JWT_REFRESH_EXPIRED=
JWT_REFRESH_SECRET=
GOOGLE_API_KEY=
NAVER_CLIENT_ID=
NAVER_CLIENT_SECRET=
```

```yml

spring:
  config:
    import: classpath:.env.properties
```

### 2. Docker compose 로 DB 실행

프로젝트 디렉토리로 이동 후,

```
docker-compose up -d
docker exec -it tdd-db psql -U test -d tdd-db
```

### 3. 어플리케이션 실행

필요 시, ```ddl-auto: create```로 변경 후 실행

```
./gradlew bootRun
```

## 설계 산출물

### 1. ERD

![erd](https://github.com/user-attachments/assets/96efb039-16e5-4f0d-95e6-b0b7e0a40cbb)

### 2. 도메인 다이어그램

![domain_diagram](https://github.com/user-attachments/assets/0ab9c29e-c9e0-47f7-995a-1ece5c84a1a2)

### 3. API 명세서

[설계 도메인 개요 및 API 명세서](api-docs)

### 4. 인프라 설계도

![infra_architecture](https://github.com/user-attachments/assets/32c655a9-7567-4149-8ef9-2c55f5d50d08)

### 5. Conventions

[팀 개발 규칙 및 가이드](team-convention.md)

## 개발 산출물

### 1. 도메인별 핵심 기능 상세 구현

### 2. 트러블슈팅

### 3. 공통 관심사항

- **BaseEntity**: JPA Auditing 기능을 통해 모든 엔티티에 추적 및 soft delete 기능 공통화
    - `createdAt`, `createdBy`, `updatedAt`, `updatedBy`, `deletedAt`, `deletedBy` 컬럼 공통화
    - `delete()` 메소드를 통해 soft delete 패턴을 모든 엔티티에서 공통적으로 사용하도록 설정
    - 모든 도메인 엔티티는 `BaseEntity`를 상속받은 형태로 작성

- **JWT 인증**: `jjwt` 라이브러리를 통해 액세스 토큰과 리프레쉬 토큰을 JWT로 관리
    - `JwtAuthenticationFilter`: 요청에서 토큰 존재여부를 파악하고, 토큰 정보를 바탕으로 인증 객체 생성. 토큰이 존재하지 않을 경우 public
      url로 판단한다.
    - `JwtExceptionFilter`: Jwt 필터에서 발생하는 예외를 처리하는 필터
    - `JwtTokenProvider`: 토큰 발급, 검증, 디코딩을 담당하는 인터페이스

- **공통 예외 처리**: `GlobalExceptionHandler`를 통해 필터를 제외한 프로젝트 전역에서 발생하는 예외를 한 곳에서 관리하도록 설정
    - `BusinessException`: 일반적인 Spring 예외가 아닌 비즈니스 로직에서 발생하는 예외들을 처리하는 Exception
    - `ErrorCode`: 비즈니스 로직과 관련된 에러 관리 포인트. Http 상태코드 및 에러 메시지를 관리
    - `ErrorResponse`: 예외 발생 시 일관된 응답을 전달하기 위한 클래스

### 4. 테스트 코드

현재 작성된 테스트 케이스는 총 **211개**이며, 각 계층의 역할과 책임에 따라 다음과 같은 전략으로 테스트를 구성합니다.

### 테스트 원칙

- 중요 신규 기능은 테스트 코드 작성 필수
- Given-When-Then 패턴 사용 권장
- 계층별 테스트 전략

### (1) **Controller (API End-to-End Test)**

**목표**: API 요청부터 응답까지 전체 흐름 검증

**전략**:

- `@SpringBootTest` + `MockMvc` 사용
- `IntegrationTest` 템플릿 상속

**인증/인가 테스트**:

```java

@Test
@CustomWithMockUser
@DisplayName("@CustomWithMockUser을 이용했을 때 정상적으로 인증이 통과된다.")
void withMockUser_canPassAuthentication() throws Exception {
    mockMvc.perform(get("/v1/test/mockUser/test"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(1L))
        .andExpect(jsonPath("$.username").value("testUser"))
        .andExpect(jsonPath("$.authority").value("CUSTOMER"));
}
```

### (2) **Service (Unit Test & Integration Test)**

**목표**: 비즈니스 로직의 정확성 검증
**전략**:

- `@ExtendWith(MockitoExtension.class)` 사용
- Repository 및 외부 의존성 Mocking
- Given-When-Then 패턴 적용

**예시**:

```java

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void 주문_생성_성공() {
        // given
        given(orderRepository.save(any())).willReturn(mockOrder);

        // when
        OrderResponseDto result = orderService.createOrder(request);

        // then
        assertThat(result.getOrderId()).isNotNull();
        verify(orderRepository, times(1)).save(any());
    }
}
```

---

### (3) Repository Layer (Data Access Test)

**목표**: 데이터베이스 연동 및 쿼리 정확성 검증

**전략**:

- `@DataJpaTest` + TestContainer 사용
- `RepositoryTest` 템플릿 상속
- 실제 DB와 유사한 환경에서 테스트

**템플릿**:

```java

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestContainerConfig.class, CleanUp.class, AuditConfig.class, QueryDSLConfig.class})
public abstract class RepositoryTest {

    @Autowired
    protected CleanUp cleanUp;

    @Autowired
    protected EntityManager em;

    @AfterEach
    protected void tearDown() {
        cleanUp.tearDown();
    }
}
```

**사용 예시**:

```java

@DisplayName("StoreRepository 테스트")
class StoreRepositoryTest extends RepositoryTest {

    @Autowired
    private StoreRepository storeRepository;

    @Test
    @DisplayName("키워드와 카테고리로 검색")
    void searchWithKeywordAndCategory() {
        // given
        pageable = PageRequest.of(0, 10);
        String keyword = "김밥";
        StoreCategory category = StoreCategory.KOREAN;

        //when
        List<UUID> stores = storeRepository.findPagedStoreIdsByKeyword(pageable, keyword,
            category);

        //then
        assertThat(stores).hasSize(2);
    }
}
```

**검증 항목**:

- CRUD 동작 정확성
- QueryDSL 복잡 쿼리 결과
- 연관 관계 매핑

## 회고

### 1. 개발 및 협업 측면에서 잘한 부분

- 적극적인 PR 리뷰 참여 및 피드백을 통한 코드 완성도 증가
- 테스트 컨테이너를 활용한 실제 DB 환경에서의 각 도메인 별 단위 테스트 코드 작성
- 공통된 에러코드 관리 및 예외 핸들러를 통해 프로젝트 내에서 통일된 예외처리 진행
- JPA Auditing을 적용해 데이터를 추적 및 관리하는 테이블 설계
- 근거를 바탕으로 한 기술적인 토론 및 의견 통합

### 2. 현재 시스템의 한계와 이를 발전시키기 위한 계획

- 로컬 캐시 기반 단일 서버 환경에 적합한 기술을 분산 환경에서 적용 가능한 Redis 등으로 마이그레이션
- 다중 서버 환경에서 동시에 스케쥴러에 접근할 수 있는 문제를 Lock을 통해 해결
- AI / 네이버 지도 등의 외부 API 호출 시 장애를 고려한 시스템 설계
- 로깅 및 모니터링 시스템 구축을 통한 서버 자원 관리 및 추적
- 비즈니스 상황에서 발생할 수 있는 검증 및 예외 처리 로직 세분화
- 특정 기술에 종속되지 않고 기술 교체가 가능하도록 확장성 있는 아키텍쳐로의 전환
- 가상 시나리오로 작성된 결제 시스템을 실제 PG사 연동을 통한 시스템으로 전환
- 주문부터 고객에게 음식 전달까지의 시나리오 확장
- 데이터 조회 인덱스를 적용해 조회 성능 개선 및 DB 부하 최소화

### 3. 협업 시 아쉽거나 부족했던 부분

- 프로젝트 초반부에 기술적인 논의를 하기 위한 코어 타임의 부재
- 업무 분담의 효율성이 떨어져 추가적인 시간 비용 발생
    - Entity 작성 등의 공통 작업을 각자 진행함에 있어, 코드를 합치는 과정에서 많은 시간 소요 및 충돌 발생
- 프로젝트명이 TDD 인데 TDD 방법론을 적용하지 못한 부분이 아쉬움
- PR 리뷰 시간이 길어짐에 따라, 브랜치 최신화가 더딘 부분이 존재

## 팀원 소개

| 팀원  | 깃허브                                        | 역할 |
|-----|--------------------------------------------|----|
| 박성민 | [@dnjsals45](https://github.com/dnjsals45) |    |
| 김민수 | [@Doritosch](https://github.com/Doritosch) |    |
| 김채연 | [@yeon-22k](https://github.com/yeon-22k)   |    |
| 박주찬 | [@p990805](https://github.com/p990805)     |    |
| 변영재 | [@bbangjae](https://github.com/bbangjae)   |    |
| 송의현 | [@yawning5](https://github.com/yawning5)   |    |