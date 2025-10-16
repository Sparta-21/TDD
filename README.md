# TDD(Today Delicious Delivery)

## 프로젝트 소개

> Spring boot 모놀리식 어플리케이션 개발 학습을 위해 주문 관리 플랫폼 프로젝트를 진행하였습니다.

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

### 2. 도메인 다이어그램

### 3. API 명세서

[설계 도메인 개요 및 API 명세서](api-docs)

### 4. 인프라 설계도

### 5. Conventions

-
-
- Package Structure

```angular2html
src/main/java/com/sparta/tdd
│
├── domain
│   └── order
│       ├── controller
│       │   └── OrderController.java
│       ├── dto
│       │   ├── OrderRequestDto.java
│       │   └── OrderResponseDto.java
│       ├── entity
│       │   └── Order.java
│       ├── enums
│       │   └── OrderStatus.java
│       ├── repository
│       │   └── OrderRepository.java
│       └── service
│           └── OrderService.java
│
├── global
│   ├── config
│   │   ├── AuditConfig.java
│   │   ├── CacheConfig.java
│   │   ├── JwtFilterConfig.java
│   │   ├── QueryDSLConfig.java
│   │   ├── SecurityConfig.java
│   │   └── SwaggerConfig.java
│   │
│   ├── exception
│   │   ├── BusinessException.java
│   │   ├── ErrorCode.java
│   │   └── GlobalExceptionHandler.java
│   │
│   ├── jwt
│   │
│   └── model
│       └── BaseEntity.java
│
│
└── AppApplication.java
```

## 개발 산출물

### 1. 도메인별 핵심 기능 상세 구현

### 2. 트러블슈팅

### 3. 공통 관심사항

### 4. 테스트 코드

## 회고

### 1. 개발 및 협업 측면에서 잘한 부분

-

### 2. 현재 시스템의 한계와 이를 발전시키기 위한 계획

-

### 3. 협업 시 아쉽거나 부족했던 부분

-

## 팀원 소개

| 팀원  | 깃허브                                        | 역할 |
|-----|--------------------------------------------|----|
| 박성민 | [@dnjsals45](https://github.com/dnjsals45) |    |
| 김민수 | [@Doritosch](https://github.com/Doritosch) |    |
| 김채연 | [@yeon-22k](https://github.com/yeon-22k)   |    |
| 박주찬 | [@p990805](https://github.com/p990805)     |    |
| 변영재 | [@bbangjae](https://github.com/bbangjae)   |    |
| 송의현 | [@yawning5](https://github.com/yawning5)   |    |