# API 명세서

## 도메인 개요

- **Address**: 가게와 사용자 주소를 등록 및 관리합니다.
- **AI**: Google GenAI 를 활용한 글 생성을 제공합니다.
- **Auth**:사용자의 회원가입, 로그인, 토큰 발급 및 검증을 포함한 인증과 인가 로직을 담당합니다.
- **Cart**:장바구니는 주문 전에 사용자가 선택한 메뉴 아이템을 임시로 저장하고 관리합니다.
- **Coupon**: MASTER 및 STORE 쿠폰 발행, 수정, 삭제, 조회와 사용자의 쿠폰 발급 및 상태를 관리합니다.
- **Menu**: 가게별 메뉴의 등록, 수정, 삭제, 조회를 관리하며 메뉴의 노출상태와 가격 등의 정보를 제공합니다.
- **Order**: 사용자의 주문 요청부터 상태 변경까지의 주문 처리 전 과정을 제어합니다.
- **OrderMenu**: 개별 주문에 포함된 메뉴 항목을 관리하며, 주문-메뉴 간 다대다 관계를 매핑하는 역할을 수행합니다.
- **Payment**: 주문에 대한 결제 정보를 관리하며, 결제 요청/승인/취소 등의 프로세스를 처리합니다.
- **Review**: 고객의 리뷰 작성, 수정, 삭제와 점주의 답글 기능을 지원하여 양방향 피드백 시스템을 제공합니다.
- **Store**: 가게의 등록, 수정, 검색, 상세조회 기능을 담당하며 점주와 고객 간의 매장 정보 접근 제어를 수행합니다.
- **User**: 사용자의 권한(Role) 및 프로필 정보를 관리하며 이용자 유형에 따른 접근 가능 리소스를 제한합니다.

![GET](https://img.shields.io/badge/GET-2196F3?style=flat)
![POST](https://img.shields.io/badge/POST-4CAF50?style=flat)
![PATCH](https://img.shields.io/badge/PATCH-FFC107?style=flat)
![DELETE](https://img.shields.io/badge/DELETE-F44336?style=flat)

## API 명세서

<table>
<tr>
<th>도메인</th><th>기능</th><th>메서드</th><th>URI</th><th>Request</th><th>Response</th>
</tr>
<tr>
<td>Address</td><td>가게 주소 등록</td><td>메서드</td><td>/v1/address/store</td>
<td><pre><code>{  
"roadAddress": "경기도 성남시 분당구 불정로 6 NAVER그린팩토리",
"jibunAddress": "경기도 성남시 분당구 정자동 178-1 NAVER그린팩토리",
“detailAddress”: “101동”,
"latitude": "127.1052160",
"longitude": "37.3595033"
}
</code></pre></td>
</tr>

<tr>
<td>Address</td><td>가게 주소 수정</td><td>메서드</td><td>/v1/address/store/{storeId}</td>
<td><pre><code>{  
"roadAddress": "경기도 성남시 분당구 불정로 6 NAVER그린팩토리",
"jibunAddress": "경기도 성남시 분당구 정자동 178-1 NAVER그린팩토리",
“detailAddress”: “101동”,
"latitude": "127.1052160",
"longitude": "37.3595033"
}
</code></pre></td>
</tr>


</table>
