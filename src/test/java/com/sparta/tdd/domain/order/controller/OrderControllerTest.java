package com.sparta.tdd.domain.order.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.tdd.common.config.TestSecurityConfig;
import com.sparta.tdd.common.helper.CustomWithMockUser;
import com.sparta.tdd.domain.order.dto.*;
import com.sparta.tdd.domain.order.enums.OrderStatus;
import com.sparta.tdd.domain.order.service.OrderService;
import com.sparta.tdd.domain.orderMenu.dto.OrderMenuRequestDto;
import com.sparta.tdd.domain.user.enums.UserAuthority;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = OrderController.class)
@Import(TestSecurityConfig.class)

class OrderControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean OrderService orderService;

    UUID orderId;
    UUID storeId;

    @BeforeEach
    void init() {
        orderId = UUID.randomUUID();
        storeId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("주문 목록 조회")
    class GetOrders {

        @Test
        @CustomWithMockUser(userId = 1L, authority = UserAuthority.CUSTOMER)
        @DisplayName("페이지 요청 시 사이즈 제한이 적용되고 200 반환")
        void getOrders_success() throws Exception {
            OrderResponseDto dto = new OrderResponseDto(
                orderId,
                "홍길동",
                "가게명",
                10000,
                "서울시 강남구",
                List.of(),
                LocalDateTime.now(),
                OrderStatus.PENDING
            );
            Page<OrderResponseDto> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);
            given(orderService.getOrders(any(), any(), any())).willReturn(page);

            mockMvc.perform(get("/v1/orders?page=0&size=20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(orderId.toString()))
                .andExpect(jsonPath("$.content[0].orderStatus").value(OrderStatus.PENDING.name()));

            verify(orderService).getOrders(any(), any(), any());
        }

        @Test
        @DisplayName("인증 없으면 403 반환")
        void getOrders_forbidden_noAuth() throws Exception {
            mockMvc.perform(get("/v1/orders"))
                .andDo(print())
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("주문 단건 조회")
    class GetOrder {

        @Test
        @CustomWithMockUser(userId = 1L, authority = UserAuthority.CUSTOMER)
        @DisplayName("성공적으로 단건 조회")
        void getOrder_success() throws Exception {
            OrderResponseDto res = new OrderResponseDto(
                orderId,
                "홍길동",
                "테스트가게",
                10000,
                "서울시 강남구",
                List.of(),
                LocalDateTime.now(),
                OrderStatus.PENDING
            );
            given(orderService.getOrder(any(), eq(orderId))).willReturn(res);

            mockMvc.perform(get("/v1/orders/{orderId}", orderId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.customerName").value("홍길동"))
                .andExpect(jsonPath("$.orderStatus").value(OrderStatus.PENDING.name()));

            verify(orderService).getOrder(any(), eq(orderId));
        }

        @Test
        @DisplayName("인증 없으면 403")
        void getOrder_forbidden_noAuth() throws Exception {
            mockMvc.perform(get("/v1/orders/{orderId}", orderId))
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("주문 생성")
    class CreateOrder {

        @Test
        @CustomWithMockUser(userId = 1L, authority = UserAuthority.CUSTOMER)
        @DisplayName("CUSTOMER 권한으로 주문 생성 성공")
        void createOrder_success() throws Exception {
            List<OrderMenuRequestDto> menu = List.of(new OrderMenuRequestDto(
                UUID.randomUUID(),
                "불고기버거",
                5000,
                2
            ));
            OrderRequestDto req = new OrderRequestDto(
                "서울시 강남구",
                "홍길동",
                storeId,
                "테스트가게",
                10000,
                menu
            );
            OrderResponseDto res = new OrderResponseDto(
                orderId,
                "홍길동",
                "테스트가게",
                10000,
                "서울시 강남구",
                List.of(),
                LocalDateTime.now(),
                OrderStatus.PENDING
            );
            given(orderService.createOrder(any(), any())).willReturn(res);

            mockMvc.perform(post("/v1/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.customerName").value("홍길동"))
                .andExpect(jsonPath("$.orderStatus").value(OrderStatus.PENDING.name()));

            verify(orderService).createOrder(any(), any());
        }

        @Test
        @CustomWithMockUser(userId = 2L, authority = UserAuthority.MANAGER)
        @DisplayName("CUSTOMER가 아니면 403 반환")
        void createOrder_forbidden_notCustomer() throws Exception {
            OrderRequestDto req = new OrderRequestDto(
                "서울시 강남구",
                "홍길동",
                storeId,
                "테스트가게",
                10000,
                List.of(new OrderMenuRequestDto(UUID.randomUUID(), "불고기버거", 5000, 1))
            );

            mockMvc.perform(post("/v1/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                .andDo(print())
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("주문 상태 변경")
    class UpdateStatus {

        @Test
        @CustomWithMockUser(userId = 1L, authority = UserAuthority.MANAGER)
        @DisplayName("MANAGER 권한으로 상태 변경 성공")
        void updateOrderStatus_success() throws Exception {
            OrderStatusRequestDto req = new OrderStatusRequestDto(OrderStatus.PENDING);
            OrderResponseDto res = new OrderResponseDto(
                orderId,
                "홍길동",
                "테스트가게",
                10000,
                "서울시 강남구",
                List.of(),
                LocalDateTime.now(),
                OrderStatus.PENDING
            );
            given(orderService.changeOrderStatus(eq(orderId), any())).willReturn(res);

            mockMvc.perform(patch("/v1/orders/{orderId}", orderId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderStatus").value(OrderStatus.PENDING.name()));

            verify(orderService).changeOrderStatus(eq(orderId), any());
        }

        @Test
        @CustomWithMockUser(userId = 2L, authority = UserAuthority.CUSTOMER)
        @DisplayName("CUSTOMER 권한은 403 반환")
        void updateOrderStatus_forbidden_customer() throws Exception {
            OrderStatusRequestDto req = new OrderStatusRequestDto(OrderStatus.PENDING);

            mockMvc.perform(patch("/v1/orders/{orderId}", orderId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                .andDo(print())
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("인증 없으면 403 반환")
        void updateOrderStatus_forbidden_noAuth() throws Exception {
            OrderStatusRequestDto req = new OrderStatusRequestDto(OrderStatus.PENDING);

            mockMvc.perform(patch("/v1/orders/{orderId}", orderId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                .andDo(print())
                .andExpect(status().isForbidden());
        }

        @Test
        @CustomWithMockUser(userId = 1L, authority = UserAuthority.MANAGER)
        @DisplayName("요청 status가 null이면 400 반환")
        void updateOrderStatus_badRequest_null() throws Exception {
            String invalidJson = "{\"orderStatus\": null}";

            mockMvc.perform(patch("/v1/orders/{orderId}", orderId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson))
                .andDo(print())
                .andExpect(status().isBadRequest());
        }
    }
}
