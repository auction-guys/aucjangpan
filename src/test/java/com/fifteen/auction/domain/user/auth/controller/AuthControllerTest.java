package com.fifteen.auction.domain.user.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fifteen.auction.domain.recommend.enums.AgeGroup;
import com.fifteen.auction.domain.recommend.enums.Gender;
import com.fifteen.auction.domain.recommend.enums.Region;
import com.fifteen.auction.domain.user.auth.dto.request.SigninRequest;
import com.fifteen.auction.domain.user.auth.dto.request.SignupRequest;
import com.fifteen.auction.domain.user.auth.dto.request.WithdrawRequest;
import com.fifteen.auction.domain.user.auth.dto.response.AccessTokenResponse;
import com.fifteen.auction.domain.user.auth.dto.response.SigninResponse;
import com.fifteen.auction.domain.user.auth.service.AuthService;
import com.fifteen.auction.domain.user.auth.service.OAuthService;
import com.fifteen.auction.domain.user.auth.util.JwtUtil;
import com.fifteen.auction.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AuthController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@SpringBootTest
@ActiveProfiles("prod")
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private OAuthService oAuthService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    void 회원가입_성공() throws Exception {

        SignupRequest request = new SignupRequest(
                "test@example.com",
                "닉네임",
                "홍길동",
                Gender.MALE.name(),
                AgeGroup.TWENTIES.name(),
                "Password123!",
                Region.SEOUL.name(),
                "010-1234-5678",
                "전자기기",
                "1234-5678-9012-3456",
                UserRole.ROLE_USER.name()
        );

        String jsonContent = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent)
                        .with(csrf()))
                .andExpect(status().isCreated());
    }

    @Test
    void 로그인_성공() throws Exception {

        SignupRequest request = new SignupRequest(
                "test@example.com",
                "닉네임",
                "홍길동",
                Gender.MALE.name(),
                AgeGroup.TWENTIES.name(),
                "Password123!",
                Region.SEOUL.name(),
                "010-1234-5678",
                "전자기기",
                "1234-5678-9012-3456",
                UserRole.ROLE_USER.name()
        );

        SigninResponse response = new SigninResponse("jwt-token", "refresh-token");

        Mockito.when(authService.login(any(SigninRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string("Authorization", "jwt-token"))
                .andExpect(header().string("Refresh-Token", "refresh-token"));
    }

    @Test
    void 로그아웃_성공() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isNoContent());
    }

    @Test
    void 회원탈퇴_성공() throws Exception {
        WithdrawRequest request = new WithdrawRequest("test@example.com", "password123");

        mockMvc.perform(delete("/api/v1/auth/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void 구글_로그인_성공() throws Exception {
        SigninResponse response = new SigninResponse("jwt-token", "refresh-token");

        Mockito.when(oAuthService.loginWithGoogle("code123")).thenReturn(response);

        mockMvc.perform(get("/api/v1/auth/google/callback")
                        .param("code", "code123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwt").value("jwt-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void 토큰_재발급_성공() throws Exception {
        AccessTokenResponse response = new AccessTokenResponse("new-jwt-token", "refresh-token");

        Mockito.when(authService.reissue("refresh-token")).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/reissue")
                        .header("Refresh-Token", "refresh-token"))
                .andExpect(status().isOk())
                .andExpect(header().string("Authorization", "new-jwt-token"));
    }
}