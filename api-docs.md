# API 명세서

## 도메인 개요

| 도메인           | 설명                                                          |
|---------------|-------------------------------------------------------------|
| **Address**   | 가게와 사용자 주소를 등록 및 관리합니다.                                     |
| **AI**        | Google GenAI 를 활용한 글 생성을 제공합니다.                             |
| **Auth**      | 사용자의 회원가입, 로그인, 토큰 발급 및 검증을 포함한 인증과 인가 로직을 담당합니다.           |
| **Cart**      | 장바구니는 주문 전에 사용자가 선택한 메뉴 아이템을 임시로 저장하고 관리합니다.                |
| **Coupon**    | MASTER 및 STORE 쿠폰 발행, 수정, 삭제, 조회와 사용자의 쿠폰 발급 및 상태를 관리합니다.   |
| **Menu**      | 가게별 메뉴의 등록, 수정, 삭제, 조회를 관리하며 메뉴의 노출상태와 가격 등의 정보를 제공합니다.     |
| **Order**     | 사용자의 주문 요청부터 상태 변경까지의 주문 처리 전 과정을 제어합니다.                    |
| **OrderMenu** | 개별 주문에 포함된 메뉴 항목을 관리하며, 주문-메뉴 간 다대다 관계를 매핑하는 역할을 수행합니다.     |
| **Payment**   | 주문에 대한 결제 정보를 관리하며, 결제 요청/승인/취소 등의 프로세스를 처리합니다.             |
| **Review**    | 고객의 리뷰 작성, 수정, 삭제와 점주의 답글 기능을 지원하여 양방향 피드백 시스템을 제공합니다.      |
| **Store**     | 가게의 등록, 수정, 검색, 상세조회 기능을 담당하며 점주와 고객 간의 매장 정보 접근 제어를 수행합니다. |
| **User**      | 사용자의 권한(Role) 및 프로필 정보를 관리하며 이용자 유형에 따른 접근 가능 리소스를 제한합니다.   |

## API 명세서

![GET](https://img.shields.io/badge/GET-2196F3?style=flat)
![POST](https://img.shields.io/badge/POST-4CAF50?style=flat)
![PATCH](https://img.shields.io/badge/PATCH-FFC107?style=flat)
![DELETE](https://img.shields.io/badge/DELETE-F44336?style=flat)

<table>
  <tr>
    <th>도메인</th><th>기능</th><th>메서드</th><th>URI</th><th>Request</th><th>Response</th>
  </tr>
  <tr>
    <td>Address</td><td>가게 주소 등록</td><td>POST</td><td>/v1/address/store</td>
    <td><pre><code>{
  "roadAddress": "경기도 성남시 분당구 불정로 6 NAVER그린팩토리",
  "jibunAddress": "경기도 성남시 분당구 정자동 178-1 NAVER그린팩토리",
  "detailAddress": "101동",
  "latitude": "127.1052160",
  "longitude": "37.3595033"
}</code></pre></td>
    <td></td>
  </tr>

  <tr>
    <td>Address</td><td>가게 주소 수정</td><td>PATCH</td><td>/v1/address/store/{storeId}</td>
    <td></td>
    <td></td>
  </tr>

  <tr>
    <td>Address</td><td>가게 주소 삭제</td><td>DELETE</td><td>/v1/address/store/{storeId}</td>
    <td></td>
    <td></td>
  </tr>

  <tr>
    <td>Address</td><td>회원 주소 등록</td><td>POST</td><td>/v1/address/user</td>
    <td><pre><code>{
  "roadAddress": "경기도 성남시 분당구 불정로 6 NAVER그린팩토리",
  "jibunAddress": "경기도 성남시 분당구 정자동 178-1 NAVER그린팩토리",
  "detailAddress": "101동",
  "alias": "집",
  "latitude": "127.1052160",
  "longitude": "37.3595033"
}</code></pre></td>
    <td></td>
  </tr>

  <tr>
    <td>Address</td><td>회원 주소 조회</td><td>GET</td><td>/v1/address/user</td>
    <td><pre><code>{}</code></pre></td>
    <td>여기 넣으세요</td>
  </tr>

  <tr>
    <td>Address</td><td>회원 주소 수정</td><td>PATCH</td><td>/v1/address/user/{addressId}</td>
    <td><pre><code>{
  "roadAddress": "경기도 성남시 분당구 불정로 6 NAVER그린팩토리",
  "jibunAddress": "경기도 성남시 분당구 정자동 178-1 NAVER그린팩토리",
  "detailAddress": "102동",
  "alias": "회사",
  "latitude": "127.1052160",
  "longitude": "37.3595033"
}</code></pre></td>
    <td>여기 넣으세요</td>
  </tr>

  <tr>
    <td>Address</td><td>회원 주소 삭제</td><td>DELETE</td><td>/v1/address/user/{addressId}</td>
    <td><pre><code>{}</code></pre></td>
    <td>여기 넣으세요</td>
  </tr>

  <!-- New AI Domain Added -->
  <tr>
    <td>AI</td><td>음식 소개 작성</td><td>POST</td><td>/v1/ai/req</td>
    <td><pre><code>{
  "content": "요청내용"
}</code></pre></td>
    <td><pre><code>{
  "id": "id",
  "inputText": "만두",
  "outputText": "오감만족, 인생만두!",
  "createdAt": "2025-10-02T12:57:43.983978"
}</code></pre></td>
  </tr>
  <!-- Auth Domain Added -->

 <tr>
    <td>Auth</td><td>회원가입</td><td>POST</td><td>/v1/auth/signup</td>
    <td><pre><code>{
  "username": "username",
  "password": "password"
}</code></pre></td>
    <td><pre><code>{
  "userId": 1
}</code></pre></td>
  </tr>

  <tr>
    <td>Auth</td><td>유저네임 중복확인</td><td>GET</td><td>/v1/auth/exists?username={username}</td>
    <td><pre><code>{}</code></pre></td>
    <td><pre><code>{
  "exists": true
}</code></pre></td>
  </tr>

  <tr>
    <td>Auth</td><td>로그인</td><td>POST</td><td>/v1/auth/login</td>
    <td><pre><code>{
  "username": "username",
  "password": "password"
}</code></pre></td>
    <td><pre><code>{
  "userId": 1
}</code></pre></td>
  </tr>

  <tr>
    <td>Auth</td><td>로그아웃</td><td>POST</td><td>/v1/auth/logout</td>
    <td><pre><code>{}</code></pre></td>
    <td><pre><code>{}</code></pre></td>
  </tr>

  <tr>
    <td>Auth</td><td>회원 탈퇴</td><td>DELETE</td><td>/v1/auth/withdrawal</td>
    <td><pre><code>{}</code></pre></td>
    <td><pre><code>{}</code></pre></td>
  </tr>

  <tr>
    <td>Auth</td><td>토큰 재발급</td><td>POST</td><td>/v1/auth/token/reissue</td>
    <td><pre><code>{}</code></pre></td>
    <td><pre><code>AT: 헤더, RT: 쿠키</code></pre></td>
  </tr>
  <!-- New Cart Domain Added -->
  <tr>
    <td>Cart</td><td>장바구니 조회</td><td>GET</td><td>/v1/cart</td>
    <td><pre><code>{}</code></pre></td>
    <td><pre><code>{
  "cartId": "UUID",
  "userId": 1,
  "items": [
    {
      "cartItemId": "UUID",
      "menuId": "UUID",
      "menuName": "김치찌개",
      "price": 8000,
      "quantity": 2,
      "totalPrice": 16000,
      "storeId": "UUID",
      "storeName": "맛있는식당"
    }
  ],
  "totalPrice": 16000,
  "storeId": "UUID"
}</code></pre></td>
  </tr>

  <tr>
    <td>Cart</td><td>장바구니에 아이템 추가</td><td>POST</td><td>/v1/cart/items</td>
    <td><pre><code>{
  "menuId": "UUID",
  "quantity": 2
}</code></pre></td>
    <td><pre><code>{
  "cartId": "UUID",
  "userId": 1,
  "items": [
    {
      "cartItemId": "UUID",
      "menuId": "UUID",
      "menuName": "김치찌개",
      "price": 8000,
      "quantity": 2,
      "totalPrice": 16000,
      "storeId": "UUID",
      "storeName": "맛있는식당"
    }
  ],
  "totalPrice": 16000,
  "storeId": "UUID"
}</code></pre></td>
  </tr>

  <tr>
    <td>Cart</td><td>장바구니 아이템 수량 수정</td><td>PATCH</td><td>/v1/cart/items/{cartItemId}?quantity={quantity}</td>
    <td><pre><code>{}</code></pre></td>
    <td><pre><code>{
  "cartId": "UUID",
  "userId": 1,
  "items": [],
  "totalPrice": 0,
  "storeId": null
}</code></pre></td>
  </tr>

  <tr>
    <td>Cart</td><td>장바구니 아이템 삭제</td><td>DELETE</td><td>/v1/cart/items/{cartItemId}</td>
    <td><pre><code>{}</code></pre></td>
    <td><pre><code>{
  "cartId": "UUID",
  "userId": 1,
  "items": [],
  "totalPrice": 0,
  "storeId": null
}</code></pre></td>
  </tr>

  <tr>
    <td>Cart</td><td>장바구니 비우기</td><td>DELETE</td><td>/v1/cart</td>
    <td><pre><code>{}</code></pre></td>
    <td><pre><code>{
  "cartId": "UUID",
  "userId": 1,
  "items": [],
  "totalPrice": 0,
  "storeId": null
}</code></pre></td>
  </tr>
  <!-- New Coupon Domain Added -->

  <tr>
    <td>Coupon</td><td>매장 쿠폰 목록 조회</td><td>GET</td><td>/v1/coupon/list/{storeId}</td>
    <td><pre><code>{}</code></pre></td>
    <td><pre><code>{}</code></pre></td>
  </tr>

  <tr>
    <td>Coupon</td><td>내 쿠폰 목록 조회</td><td>GET</td><td>/v1/coupon/my/list</td>
    <td><pre><code>{}</code></pre></td>
    <td><pre><code>{}</code></pre></td>
  </tr>

  <tr>
    <td>Coupon</td><td>Store 쿠폰 등록</td><td>POST</td><td>/v1/coupon/{storeId}</td>
    <td><pre><code>{}</code></pre></td>
    <td><pre><code>{}</code></pre></td>
  </tr>

  <tr>
    <td>Coupon</td><td>Master 쿠폰 등록</td><td>POST</td><td>/v1/coupon/{storeId}</td>
    <td><pre><code>{}</code></pre></td>
    <td><pre><code>{}</code></pre></td>
  </tr>

  <tr>
    <td>Coupon</td><td>쿠폰 발급</td><td>POST</td><td>/v1/coupon/{couponId}</td>
    <td><pre><code>{}</code></pre></td>
    <td><pre><code>{}</code></pre></td>
  </tr>

  <tr>
    <td>Coupon</td><td>쿠폰 수정</td><td>PATCH</td><td>/v1/coupon/{couponId}</td>
    <td><pre><code>{}</code></pre></td>
    <td><pre><code>{}</code></pre></td>
  </tr>

  <tr>
    <td>Coupon</td><td>쿠폰 사용</td><td>POST</td><td>/v1/coupon/{couponId}</td>
    <td><pre><code>{}</code></pre></td>
    <td><pre><code>{}</code></pre></td>
  </tr>

  <tr>
    <td>Coupon</td><td>쿠폰 삭제</td><td>DELETE</td><td>/v1/coupon/{couponId}</td>
    <td><pre><code>{}</code></pre></td>
    <td><pre><code>{}</code></pre></td>
  </tr>

  <tr>
    <td>Coupon</td><td>쿠폰 만료</td><td>DELETE</td><td>/v1/coupon/expire/{couponId}</td>
    <td><pre><code>{}</code></pre></td>
    <td><pre><code>{}</code></pre></td>
  </tr>
  <!-- New Menu Domain Added -->

 <tr>
    <td>Menu</td><td>메뉴 조회(목록)</td><td>GET</td><td>/v1/store/{storeId}/menu</td>
    <td><pre><code>{}</code></pre></td>
    <td><pre><code>[
  {
    "menuId": "550e8400-e29b-41d4-a716-446655440000",
    "name": "치킨버거",
    "description": "바삭한 치킨과 신선한 야채가 들어간 버거",
    "price": 8900,
    "imageUrl": "https://example.com/images/chicken-burger.jpg",
    "isHidden": false
  },
  {
    "menuId": "551e8400-e29b-41d4-a716-446655440001",
    "name": "새우버거",
    "description": "통통한 새우와 타르타르소스의 조화",
    "price": 9500,
    "imageUrl": "https://example.com/images/shrimp-burger.jpg",
    "isHidden": false
  }
]</code></pre></td>
  </tr>

  <tr>
    <td>Menu</td><td>메뉴 조회(개별)</td><td>GET</td><td>/v1/store/{storeId}/menu/{menuId}</td>
    <td><pre><code>{}</code></pre></td>
    <td><pre><code>{
  "menuId": "550e8400-e29b-41d4-a716-446655440000",
  "storeId": "660e8400-e29b-41d4-a716-446655440000",
  "name": "치킨버거",
  "description": "바삭한 치킨과 신선한 야채가 들어간 버거",
  "price": 8900,
  "imageUrl": "https://example.com/images/chicken-burger.jpg",
  "isHidden": false
}</code></pre></td>
  </tr>

  <tr>
    <td>Menu</td><td>메뉴 등록</td><td>POST</td><td>/v1/store/{storeId}/menu</td>
    <td><pre><code>{
  "name": "치킨버거",
  "description": "바삭한 치킨과 신선한 야채가 들어간 버거",
  "price": 8900,
  "imageUrl": "https://example.com/images/chicken-burger.jpg",
  "isHidden": false
}</code></pre></td>
    <td><pre><code>{
  "menuId": "550e8400-e29b-41d4-a716-446655440000",
  "restaurantId": "660e8400-e29b-41d4-a716-446655440000",
  "name": "치킨버거",
  "description": "바삭한 치킨과 신선한 야채가 들어간 버거",
  "price": 8900,
  "imageUrl": "https://example.com/images/chicken-burger.jpg",
  "isHidden": false,
  "createdAt": "2025-09-29T12:00:00.000000000"
}</code></pre></td>
  </tr>

  <tr>
    <td>Menu</td><td>메뉴 수정</td><td>PATCH</td><td>/v1/store/{storeId}/menu/{menuId}</td>
    <td><pre><code>{ "name": "수정버거" }</code></pre></td>
    <td><pre><code>{}</code></pre></td>
  </tr>

  <tr>
    <td>Menu</td><td>메뉴 상태 변경</td><td>PATCH</td><td>/v1/store/{storeId}/menu/{menuId}/status</td>
    <td><pre><code>{ "isHidden": true }</code></pre></td>
    <td><pre><code>{}</code></pre></td>
  </tr>

  <tr>
    <td>Menu</td><td>메뉴 삭제</td><td>DELETE</td><td>/v1/store/{storeId}/menu/{menuId}</td>
    <td><pre><code>{}</code></pre></td>
    <td><pre><code>{}</code></pre></td>
  </tr>
  <!-- New Order Domain Added -->

  <tr>
    <td>Order</td><td>주문 목록 조회</td><td>GET</td><td>/v1/orders?from={startDate}&to={endDate}&status={status}&userId={userId}&storeId={storeId}&sort={sort}&page={page}&size={size}</td>
    <td><pre><code>{}</code></pre></td>
    <td><pre><code>{ 
  "totalElements": 0,
  "totalPages": 0,
  "size": 0,
  "content": [
    {
      "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "customerName": "string",
      "storeName": "string",
      "price": 0,
      "address": "string",
      "orderMenuList": [
        {
          "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
          "name": "string",
          "price": 0,
          "quantity": 0
        }
      ],
      "createdAt": "2025-10-17T08:14:04.380Z",
      "orderStatus": "CANCELLED"
    }
  ],
  "number": 0,
  "sort": {
    "empty": true,
    "sorted": true,
    "unsorted": true
  },
  "first": true,
  "last": true,
  "numberOfElements": 0,
  "pageable": {
    "offset": 0,
    "sort": {
      "empty": true,
      "sorted": true,
      "unsorted": true
    }
  }
}</code></pre></td>
  </tr>

  <tr>
    <td>Order</td><td>주문 조회</td><td>GET</td><td>/v1/orders/{orderId}</td>
    <td><pre><code>{}</code></pre></td>
    <td><pre><code>{ 
  "id": 1002,
  "customerName": "c",
  "storeName": "d",
  "price": 45000,
  "address": "부산시 어떤가 456",
  "orderMenuList": [
    { 
      "id": "UUID", 
      "name": "탕수육", 
      "price": 20000, 
      "quantity": 1 
    },
    { 
      "id": "UUID", 
      "name": "깐풍기", 
      "price": 25000, 
      "quantity": 2 
    }
  ],
  "createdAt": "2025-09-29T12:34:56",
  "orderStatus": "DELIVERED"
}</code></pre></td>
  </tr>

  <tr>
    <td>Order</td><td>주문 등록</td><td>POST</td><td>/v1/orders</td>
    <td><pre><code>{
  "address": "부산시 어떤가 456",
  "customerName": "c",
  "storeId": "UUID",
  "storeName": "d",
  "price": 45000,
  "menu": [
    { 
      "id": "UUID", 
      "name": "탕수육", 
      "price": 20000, 
      "quantity": 1 
    },
    { 
      "id": "UUID", 
      "name": "깐풍기", 
      "price": 25000, 
      "quantity": 2 
    }
  ]
}</code></pre></td>
    <td><pre><code>{
  "id": 1002,
  "customerName": "c",
  "storeName": "d",
  "price": 45000,
  "address": "부산시 어떤가 456",
  "orderMenuList": [
    { 
      "id": "UUID", 
      "name": "탕수육", 
      "price": 20000, 
      "quantity": 1 
    },
    { 
      "id": "UUID", 
      "name": "깐풍기", 
      "price": 25000, 
      "quantity": 2 
    }
  ],
  "createdAt": "2025-09-29T12:34:56",
  "orderStatus": "DELIVERED"
}</code></pre></td>
  </tr>

  <tr>
    <td>Order</td><td>주문 상태 변경(관리자)</td><td>PATCH</td><td>/v1/orders/{orderId}</td>
    <td><pre><code>{ "status": "DELIVERED" }</code></pre></td>
    <td><pre><code>{}</code></pre></td>
  </tr>

  <tr>
    <td>Order</td><td>주문 상태 변경(가게주인, 관리자)</td><td>PATCH</td><td>/orderId/next-status</td>
    <td><pre><code>{}</code></pre></td>
    <td><pre><code>{}</code></pre></td>
  </tr>

  <tr>
    <td>Order</td><td>주문 취소</td><td>PATCH</td><td>/cancel/{orderId}</td>
    <td><pre><code>{}</code></pre></td>
    <td><pre><code>{ 
  "id": 1002,
  "customerName": "c",
  "storeName": "d",
  "price": 45000,
  "address": "부산시 어떤가 456",
  "orderMenuList": [
    { 
      "id": "UUID", 
      "name": "탕수육", 
      "price": 20000, 
      "quantity": 1 
    },
    { 
      "id": "UUID", 
      "name": "깐풍기", 
      "price": 25000, 
      "quantity": 2 
    }
  ],
  "createdAt": "2025-09-29T12:34:56",
  "orderStatus": "CANCELLED"
}</code></pre></td>
  </tr>
  <!-- New Payment Domain Added -->

  <tr>
    <td>Payment</td><td>결제내역 조회</td><td>GET</td><td>/v1/payments?page=1&size=10&orderBy={정렬조건}&keyword={검색조건}</td>
    <td><pre><code>{}</code></pre></td>
    <td><pre><code>{
  "payments": [
    {
      "id": 1,
      "paymentNumber": "12312312",
      "status": "결제 성공",
      "storeName": "음식점1",
      "price": 10000
    }
  ]
}</code></pre></td>
  </tr>

  <tr>
    <td>Payment</td><td>결제내역 상세 조회</td><td>GET</td><td>/v1/payments/{paymentId}</td>
    <td><pre><code>{}</code></pre></td>
    <td><pre><code>{
  "paymentNumber": "1q2w3e4r!",
  "price": 10000,
  "cardCompany": "XXX카드",
  "cardNumber": "1111 2222 3333 4444",
  "approvedAt": "2025-09-29T10:15:00",
  "restaurant": {
    "id": 1,
    "storeName": "음식점",
    "phone": "02-1234-1234",
    "address": "서울 어떤가"
  },
  "orderItem": [
    {
      "id": 5,
      "storeName": "음식1",
      "quantity": 1,
      "price": 5000,
      "totalPrice": 5000
    }
  ]
}</code></pre></td>
  </tr>

  <tr>
    <td>Payment</td><td>결제 상태 변경</td><td>PATCH</td><td>/v1/payments/status/{paymentId}</td>
    <td><pre><code>{ "status": "결제 완료" }</code></pre></td>
    <td><pre><code>{}</code></pre></td>
  </tr>

  <tr>
    <td>Payment</td><td>결제 요청</td><td>POST</td><td>/v1/payments</td>
    <td><pre><code>{
  "cardCompany": "삼성",
  "cardNumber": "123123"
}</code></pre></td>
    <td><pre><code>{ "status": "approve" }</code></pre></td>
  </tr>

  <!-- Review Domain Added -->
  <tr>
    <td>Review</td><td>리뷰 등록</td><td>POST</td><td>/v1/reviews/order/{orderId}</td>
    <td><pre><code>{
  "content": "리뷰 내용",
  "storeId": 1,
  "rating": 3,
  "photos": "abc.png"
}</code></pre></td>
    <td><pre><code>{
  "reviewId": 123,
  "storeId": 1,
  "content": "너무 맛있어요!",
  "rating": 3,
  "userId": "user01",
  "photos": "abc.png",
  "createdAt": "2025-09-29T10:15:00",
  "modifiedAt": "2025-09-29T10:20:00"
}</code></pre></td>
  </tr>

  <tr>
    <td>Review</td><td>리뷰 수정</td><td>PATCH</td><td>/v1/reviews/{reviewId}</td>
    <td><pre><code>{
  "content": "리뷰 내용",
  "rating": 3,
  "photos": "abc.png"
}</code></pre></td>
    <td><pre><code>{
  "reviewId": 123,
  "storeId": 1,
  "content": "너무 맛있어요!",
  "rating": 3,
  "userId": "user01",
  "photos": "abc.png",
  "createdAt": "2025-09-29T10:15:00",
  "modifiedAt": "2025-09-29T10:20:00"
}</code></pre></td>
  </tr>

  <tr>
    <td>Review</td><td>리뷰 개별 조회</td><td>GET</td><td>/v1/reviews/{reviewId}</td>
    <td><pre><code>{}</code></pre></td>
    <td><pre><code>{
  "reviewId": 123,
  "storeId": 1,
  "content": "너무 맛있어요!",
  "rating": 3,
  "userId": "user01",
  "photos": "abc.png",
  "createdAt": "2025-09-29T10:15:00",
  "modifiedAt": "2025-09-29T10:20:00"
}</code></pre></td>
  </tr>

  <tr>
    <td>Review</td><td>리뷰 목록 조회</td><td>GET</td><td>/v1/reviews/{storeId}?page=1&size=10</td>
    <td><pre><code>{}</code></pre></td>
    <td><pre><code>{
  "reviews": [
    {
      "reviewId": 101,
      "storeId": 1,
      "content": "음식이 맛있어요!",
      "rating": 3,
      "userId": "user01",
      "photos": "abc.png",
      "createdAt": "2025-09-25T14:30:00",
      "modifiedAt": "2025-09-29T10:20:00",
      "reply": {
        "content": "이용해주셔서 감사합니다"
      }
    },
    {
      "reviewId": 102,
      "storeId": 1,
      "content": "서비스가 친절했어요.",
      "rating": 3,
      "userId": "user02",
      "photos": "abc.png",
      "createdAt": "2025-09-26T16:45:00",
      "modifiedAt": "2025-09-29T10:20:00",
      "reply": {
        "content": "다음에도 오세요"
      }
    }
  ],
  "pageInfo": {
    "page": 1,
    "size": 10,
    "totalElements": 52,
    "totalPages": 6,
    "hasNext": true
  }
}</code></pre></td>
  </tr>

  <tr>
    <td>Review</td><td>리뷰 삭제</td><td>DELETE</td><td>/v1/reviews/{reviewId}</td>
    <td><pre><code>{}</code></pre></td>
    <td><pre><code>{}</code></pre></td>
  </tr>

  <tr>
    <td>Review</td><td>리뷰 답글 등록</td><td>POST</td><td>/v1/reviews/{reviewId}/reply</td>
    <td><pre><code>{ "content": "사장님 답글 내용" }</code></pre></td>
    <td><pre><code>{
  "replyId": 88,
  "reviewId": 123,
  "storeId": 1,
  "ownerId": 1,
  "content": "리뷰 감사합니다! 또 이용해주세요",
  "createdAt": "2025-09-29T10:15:00",
  "modifiedAt": "2025-09-29T10:20:00"
}</code></pre></td>
  </tr>

  <tr>
    <td>Review</td><td>리뷰 답글 수정</td><td>PATCH</td><td>/v1/reviews/{reviewId}/reply</td>
    <td><pre><code>{ "content": "사장님 수정 답글 내용" }</code></pre></td>
    <td><pre><code>{
  "replyId": 88,
  "reviewId": 123,
  "storeId": 1,
  "ownerId": 1,
  "content": "리뷰 감사합니다! 또 이용해주세요",
  "createdAt": "2025-09-29T10:15:00",
  "modifiedAt": "2025-09-29T10:20:00"
}</code></pre></td>
  </tr>

  <tr>
    <td>Review</td><td>리뷰 답글 삭제</td><td>DELETE</td><td>/v1/reviews/{reviewId}/reply</td>
    <td><pre><code>{}</code></pre></td>
    <td><pre><code>{}</code></pre></td>
  </tr>
  <tr>
    <td>Review</td><td>리뷰 목록 조회(사장님 답글 추가 시)</td><td>GET</td><td>/v1/reviews/{storeId}?page=1&size=10</td>
    <td><pre><code>{}</code></pre></td>
    <td><pre><code>{
  "reviews": [
  {
    "reviewId": 101,
    “storeId”: 1,
    "content": "음식이 맛있어요!",
    “rating”: 3,
    "userId": "user01",
    “photos”: “abc.png”,
    "createdAt": "2025-09-25T14:30:00",
    "modifiedAt": "2025-09-29T10:20:00”,
    “reply”: { 
      “conetent”: 이용해주셔서 감사합니다
    }

},
{
"reviewId": 102,
“storeId”: 1,
"content": "서비스가 친절했어요.",
“rating”: 3,
"userId": "user02",
“photos”: “abc.png”,
"createdAt": "2025-09-26T16:45:00",
"modifiedAt": "2025-09-29T10:20:00”
“reply”: {
“conetent”: 다음에 또 오세요
}

}
],
"pageInfo": {
"page": 1,
"size": 10,
"totalElements": 52,
"totalPages": 6,
"hasNext": true
}
}</code></pre></td>
</tr>
  <!-- Store Domain Added -->

  <tr>
    <td>Store</td><td>음식점 검색</td><td>GET</td><td>/v1/search?name={keyword}&category={keyword}&description={keyword}&sort=createdAt,desc&page=2&size=30</td>
    <td><pre><code>{ "String": "keyword" }</code></pre></td>
    <td><pre><code>{
  "content": [
    {
      "id": "d9756dfd-99e9-480d-a7f7-4e38b00d5163",
      "name": "맛스터치 강남점",
      "ownerName": "jae",
      "category": "CHICKEN",
      "description": "강남 최고 치킨버거",
      "imageUrl": "https://example.com/images/chicken-burger.jpg",
      "avgRating": 0,
      "reviewCount": 0,
      "orderCount": null,
      "menus": [
        {
          "menuId": "bf03b171-1ccb-4173-80b6-e890a176f69b2",
          "name": "싸이버거",
          "description": "맘스터치 대표 메뉴",
          "price": 5500,
          "imageUrl": null,
          "isHidden": null
        },
        {
          "menuId": "704c5850-d379-454d-8f66-6fdba7e5e22",
          "name": "감자튀김",
          "description": "바삭한 감자튀김",
          "price": 2500,
          "imageUrl": null,
          "isHidden": null
        }
      ]
    }
  ]
}</code></pre></td>
  </tr>

  <tr>
    <td>Store</td><td>음식점 상세 조회</td><td>GET</td><td>/v1/store/{storeId}</td>
    <td><pre><code>{ "storeId": "storeId" }</code></pre></td>
    <td><pre><code>{
  "status": 200,
  "message": "음식점 상세 조회에 성공했습니다.",
  "data": {
    "name": "name",
    "category": "category",
    "description": "description",
    "address": "address",
    "imageUrl": "imageUrl",
    "avgRating": 4.5,
    "reviewCount": 500
  }
}</code></pre></td>
  </tr>

  <tr>
    <td>Store</td><td>음식점 등록</td><td>POST</td><td>/v1/store</td>
    <td><pre><code>{
  "name": "name",
  "category": "category",
  "description": "description",
  "address": "address",
  "imageUrl": "imageUrl"
}</code></pre></td>
    <td><pre><code>{
  "status": 201,
  "message": "음식점 등록에 성공했습니다.",
  "data": {
    "storeId": "storeId",
    "name": "name",
    "category": "category",
    "description": "description",
    "address": "address",
    "imageUrl": "imageUrl",
    "avgRating": 0,
    "reviewCount": 0,
    "createdAt": "",
    "createdBy": ""
  }
}</code></pre></td>
  </tr>

  <tr>
    <td>Store</td><td>음식점 수정</td><td>PATCH</td><td>/v1/store/{storeId}</td>
    <td><pre><code>{
  "storeId": "storeId",
  "name": "newName",
  "category": "newCategory",
  "description": "newDescription",
  "address": "newAddress",
  "imageUrl": "newImageUrl"
}</code></pre></td>
    <td><pre><code>{
  "status": 200,
  "message": "음식점 수정에 성공했습니다.",
  "data": {
    "storeId": "storeId",
    "name": "newName",
    "category": "newCategory",
    "description": "newDescription",
    "address": "newAddress",
    "imageUrl": "newImageUrl",
    "avgRating": 4.5,
    "reviewCount": 500,
    "updatedAt": "",
    "updatedBy": ""
  }
}</code></pre></td>
  </tr>

  <tr>
    <td>Store</td><td>음식점 삭제</td><td>DELETE</td><td>/v1/store/{storeId}</td>
    <td><pre><code>{}</code></pre></td>
    <td><pre><code>{
  "status": 200,
  "message": "음식점 삭제에 성공했습니다.",
  "data": {
    "storeId": "storeId",
    "deletedAt": "",
    "deletedBy": ""
  }
}</code></pre></td>
  </tr>
  <!-- User Domain Added -->


<tr>
    <td>User</td><td>회원 정보 조회</td><td>GET</td><td>/v1/users/{userId}</td>
    <td><pre><code>{}</code></pre></td>
    <td><pre><code>{
  "userId": "userId",
  "username": "username"
}</code></pre></td>
  </tr>
  <tr>
    <td>User</td><td>회원 닉네임 수정</td><td>PATCH</td><td>/v1/users/{userId}/nickname</td>
    <td><pre><code>{
  "nickname": "nickname"
}</code></pre></td>
    <td><pre><code>{}</code></pre></td>
  </tr>
  <tr>
    <td>User</td><td>회원 비밀번호 수정</td><td>PATCH</td><td>/v1/users/{userId}/password</td>
    <td><pre><code>{
  "password": "password"
}</code></pre></td>
    <td><pre><code>{}</code></pre></td>
  </tr>
  <tr>
    <td>User</td><td>회원 리뷰 목록 조회</td><td>GET</td><td>/v1/users/{userId}/reviews</td>
    <td><pre><code>정렬조건</code></pre></td>
    <td><pre><code>{
  "data": [
    {
      "page": 1,
      "size": 1,
      "reviews": {
        "reviewId": "reviewId",
        "content": "content",
        "createdAt": "2025-09-29T10:15:00",
        "storeName": "storeName"
      }
    }
  ]
}</code></pre></td>
  </tr>
  <tr>
    <td>User</td><td>회원 목록 조회</td><td>GET</td><td>/v1/users</td>
    <td><pre><code>{ page=1&size=10&sort=createdAt,desc }</code></pre></td>
    <td><pre><code>{
  "data": [
    {
      "user1": {
        "userId": "userId",
        "username": "username"
      }
    }
  ]
}</code></pre></td>
  </tr>
  <tr>
    <td>User</td><td>회원 주문 목록 조회</td><td>GET</td><td>/v1/users/{userId}/orders</td>
    <td><pre><code>{ page=1&size=10&sort=createdAt,desc }</code></pre></td>
    <td><pre><code>{
  "data": [
    {
      "orderId": "orderId",
      "storeName": "storeName",
      "price": "price",
      "address": "address",
      "createdAt": "2025-09-29T12:34:56",
      "menu": [
        { "name": "탕수육", "price": 20000, "quantity": 1 },
        { "name": "깐풍기", "price": 25000, "quantity": 2 }
      ]
    }
  ]
}</code></pre></td>
  </tr>
  <tr>
    <td>User</td><td>매니저 권한 부여</td><td>PATCH</td><td>/v1/users/{userId}/authority</td>
    <td><pre><code>{}</code></pre></td>
    <td><pre><code>{}</code></pre></td>
  </tr>
</table>
