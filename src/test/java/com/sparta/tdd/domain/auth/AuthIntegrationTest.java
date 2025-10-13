package com.sparta.tdd.domain.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sparta.tdd.common.template.IntegrationTest;
import com.sparta.tdd.domain.auth.dto.request.LoginRequestDto;
import com.sparta.tdd.domain.auth.dto.request.SignUpRequestDto;
import com.sparta.tdd.domain.user.enums.UserAuthority;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

class AuthIntegrationTest extends IntegrationTest {

    @Test
    @DisplayName("회원가입 후 발급받은 토큰으로 UserDetailsImpl에 올바른 데이터가 담기는지 확인")
    void signUpAndVerifyTokenData() throws Exception {
        // given
        SignUpRequestDto signUpRequest = new SignUpRequestDto(
            "testuser",
            "Password1!",
            "테스트유저",
            UserAuthority.CUSTOMER
        );

        // when
        MvcResult signUpResult = mockMvc.perform(post("/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(signUpRequest)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(header().exists("Authorization"))
            .andReturn();

        String accessToken = signUpResult.getResponse().getHeader("Authorization");
        String bearerToken = accessToken.replace("Bearer ", "");

        // then
        mockMvc.perform(get("/v1/test/token/info")
                .header("Authorization", "Bearer " + bearerToken))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("testuser"))
            .andExpect(jsonPath("$.authority").value("CUSTOMER"))
            .andExpect(jsonPath("$.userId").isNumber());
    }

    @Test
    @DisplayName("로그인 후 발급받은 토큰으로 UserDetailsImpl에 올바른 데이터가 담기는지 확인")
    void loginAndVerifyTokenData() throws Exception {
        // given
        SignUpRequestDto signUpRequest = new SignUpRequestDto(
            "loginuser",
            "Password1!",
            "로그인테스트",
            UserAuthority.MASTER
        );

        mockMvc.perform(post("/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(signUpRequest)))
            .andExpect(status().isOk());

        LoginRequestDto loginRequest = new LoginRequestDto(
            "loginuser",
            "Password1!"
        );

        // when
        MvcResult loginResult = mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(loginRequest)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(header().exists("Authorization"))
            .andReturn();

        String accessToken = loginResult.getResponse().getHeader("Authorization");
        String bearerToken = accessToken.replace("Bearer ", "");

        // then
        mockMvc.perform(get("/v1/test/token/info")
                .header("Authorization", "Bearer " + bearerToken))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("loginuser"))
            .andExpect(jsonPath("$.authority").value("MASTER"))
            .andExpect(jsonPath("$.userId").isNumber());
    }
}
