package com.sparta.tdd.common.helper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sparta.tdd.common.config.TestSecurityConfig;
import com.sparta.tdd.domain.auth.TestAuthController;
import com.sparta.tdd.domain.user.enums.UserAuthority;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TestAuthController.class)
@Import(TestSecurityConfig.class)
class CustomWithMockUserExampleTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("인증이 필요한 컨트롤러에 인증객체가 없다면 통과하지 못한다")
    void doesNotPass_requireAuthenticationController() throws Exception {
        mockMvc.perform(get("/v1/test/mockUser/test"))
            .andExpect(status().isForbidden());
    }

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

    @Test
    @CustomWithMockUser(userId = 5L, username = "testUser2", authority = UserAuthority.MASTER)
    @DisplayName("@CustomWithMockUser의 옵션을 통해 인증객체의 정보를 변경할 수 있다.")
    void withMockUser_optionCustom() throws Exception {
        mockMvc.perform(get("/v1/test/mockUser/test"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(5L))
            .andExpect(jsonPath("$.username").value("testUser2"))
            .andExpect(jsonPath("$.authority").value("MASTER"));
    }

    @Test
    @CustomWithMockUser
    @DisplayName("Master 권한이 필요한 곳에 CUSTOMER 권한은 접근할 수 없다.")
    void customer_cannotAccess_masterOnly() throws Exception {
        mockMvc.perform(get("/v1/test/master-only"))
            .andDo(print())
            .andExpect(status().isForbidden());
    }

    @Test
    @CustomWithMockUser(authority = UserAuthority.MASTER)
    @DisplayName("Master 권한이 필요한 곳에 MASTER 권한은 접근할 수 있다.")
    void master_canAccess_masterOnly() throws Exception {
        mockMvc.perform(get("/v1/test/master-only"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(1L))
            .andExpect(jsonPath("$.username").value("testUser"))
            .andExpect(jsonPath("$.authority").value("MASTER"));
    }

}
