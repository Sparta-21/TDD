package com.sparta.tdd.domain.payment.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.tdd.common.config.TestSecurityConfig;
import com.sparta.tdd.common.helper.CustomWithMockUser;
import com.sparta.tdd.domain.payment.dto.UpdatePaymentStatusRequest;
import com.sparta.tdd.domain.payment.enums.PaymentStatus;
import com.sparta.tdd.domain.payment.service.PaymentService;
import com.sparta.tdd.domain.user.enums.UserAuthority;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = PaymentController.class)
@Import(TestSecurityConfig.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaymentService paymentService;

    private UUID paymentId;
    private UUID storeId;

    @BeforeEach
    void setUp() {
        paymentId = UUID.randomUUID();
        storeId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("고객 결제 내역 조회")
    class GetCustomerPaymentHistoryTest {

        @Test
        @CustomWithMockUser(userId = 2L, authority = UserAuthority.OWNER)
        @DisplayName("CUSTOMER 권한이 아니면 접근 불가")
        void getCustomerPaymentHistory_forbidden_notCustomer() throws Exception {
            // when & then
            mockMvc.perform(get("/v1/payments"))
                .andDo(print())
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("인증 없이는 접근 불가")
        void getCustomerPaymentHistory_forbidden_noAuth() throws Exception {
            // when & then
            mockMvc.perform(get("/v1/payments"))
                .andDo(print())
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("가게 결제 내역 조회")
    class GetStorePaymentHistoryTest {

        @Test
        @CustomWithMockUser(userId = 1L, authority = UserAuthority.CUSTOMER)
        @DisplayName("CUSTOMER 권한으로는 가게 결제 내역 조회 불가")
        void getStorePaymentHistory_forbidden_customer() throws Exception {
            // when & then
            mockMvc.perform(get("/v1/payments/store/{storeId}", storeId))
                .andDo(print())
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("인증 없이는 접근 불가")
        void getStorePaymentHistory_forbidden_noAuth() throws Exception {
            // when & then
            mockMvc.perform(get("/v1/payments/store/{storeId}", storeId))
                .andDo(print())
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("결제 상태 변경")
    class ChangePaymentStatusTest {

        @Test
        @CustomWithMockUser(userId = 2L, authority = UserAuthority.MANAGER)
        @DisplayName("MANAGER 권한으로 결제 상태 변경 성공")
        void changePaymentStatus_owner_success() throws Exception {
            // given
            UpdatePaymentStatusRequest request = new UpdatePaymentStatusRequest(
                PaymentStatus.COMPLETED);
            doNothing().when(paymentService).changePaymentStatus(paymentId, request);

            // when & then
            mockMvc.perform(patch("/v1/payments/status/{paymentId}", paymentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());

            verify(paymentService).changePaymentStatus(eq(paymentId), any(
                UpdatePaymentStatusRequest.class));
        }

        @Test
        @CustomWithMockUser(userId = 3L, authority = UserAuthority.MASTER)
        @DisplayName("MASTER 권한으로 결제 상태 변경 성공")
        void changePaymentStatus_manager_success() throws Exception {
            // given
            UpdatePaymentStatusRequest request = new UpdatePaymentStatusRequest(
                PaymentStatus.CANCELLED);
            doNothing().when(paymentService).changePaymentStatus(paymentId, request);

            // when & then
            mockMvc.perform(patch("/v1/payments/status/{paymentId}", paymentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());

            verify(paymentService).changePaymentStatus(eq(paymentId), any(
                UpdatePaymentStatusRequest.class));
        }

        @Test
        @CustomWithMockUser(userId = 1L, authority = UserAuthority.CUSTOMER)
        @DisplayName("CUSTOMER 권한으로는 결제 상태 변경 불가")
        void changePaymentStatus_forbidden_customer() throws Exception {
            // given
            UpdatePaymentStatusRequest request = new UpdatePaymentStatusRequest(
                PaymentStatus.COMPLETED);

            // when & then
            mockMvc.perform(patch("/v1/payments/status/{paymentId}", paymentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("인증 없이는 결제 상태 변경 불가")
        void changePaymentStatus_forbidden_noAuth() throws Exception {
            // given
            UpdatePaymentStatusRequest request = new UpdatePaymentStatusRequest(
                PaymentStatus.COMPLETED);

            // when & then
            mockMvc.perform(patch("/v1/payments/status/{paymentId}", paymentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden());
        }

        @Test
        @CustomWithMockUser(userId = 2L, authority = UserAuthority.OWNER)
        @DisplayName("결제 상태가 null이면 요청 실패")
        void changePaymentStatus_badRequest_nullStatus() throws Exception {
            // given
            String invalidRequest = "{\"status\": null}";

            // when & then
            mockMvc.perform(patch("/v1/payments/status/{paymentId}", paymentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidRequest))
                .andDo(print())
                .andExpect(status().isBadRequest());
        }
    }
}
