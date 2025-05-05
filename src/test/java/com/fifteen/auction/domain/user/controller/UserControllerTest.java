package com.fifteen.auction.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fifteen.auction.domain.recommend.enums.AgeGroup;
import com.fifteen.auction.domain.recommend.enums.Gender;
import com.fifteen.auction.domain.recommend.enums.Region;
import com.fifteen.auction.domain.user.auth.entity.AuthUser;
import com.fifteen.auction.domain.user.auth.util.JwtUtil;
import com.fifteen.auction.domain.user.dto.request.SetPasswordRequest;
import com.fifteen.auction.domain.user.dto.request.UserUpdatePasswordRequest;
import com.fifteen.auction.domain.user.dto.request.UserUpdateRequest;
import com.fifteen.auction.domain.user.dto.response.UserResponse;
import com.fifteen.auction.domain.user.dto.response.UserUpdateResponse;
import com.fifteen.auction.domain.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.mockito.ArgumentMatchers.eq;
import static org.junit.jupiter.api.Assertions.assertEquals;

@WebMvcTest(
        controllers = UserController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @InjectMocks
    private UserController userController;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void 사용자_조회_성공() throws Exception {
        // given
        UserResponse response = new UserResponse(
                1L,
                "test@example.com",
                "nickname",
                "디지털기기"
        );

        when(userService.findUser(1L)).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.nickname").value("nickname"))
                .andExpect(jsonPath("$.preferCategory").value("디지털기기"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void 사용자_프로필_수정_성공() throws Exception {
        // given
        UserUpdateRequest request = new UserUpdateRequest("test@example.com", "newNickname", Region.SEOUL.name());
        UserUpdateResponse response = new UserUpdateResponse(
                1L,
                "test@example.com",
                "newNickname",
                "Test User",
                Gender.MALE.name(),
                AgeGroup.TWENTIES.name(),
                Region.SEOUL.name(),
                "123-456-7890",
                "디지털기기",
                "123-456-789"
        );

        when(userService.updateProfile(any(), any(UserUpdateRequest.class))).thenReturn(response);

        // when & then
        mockMvc.perform(put("/api/v1/users/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.nickname").value("newNickname"))
                .andExpect(jsonPath("$.gender").value("MALE"))
                .andExpect(jsonPath("$.ageGroup").value("TWENTIES"))
                .andExpect(jsonPath("$.address").value("SEOUL"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void 비밀번호_수정_성공() throws Exception {
        // given
        UserUpdatePasswordRequest request = new UserUpdatePasswordRequest("oldPw123", "newPw1234");

        doNothing().when(userService).updatePassword(any(), any(UserUpdatePasswordRequest.class));

        // when & then
        mockMvc.perform(put("/api/v1/users/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(userService, times(1)).updatePassword(any(), any(UserUpdatePasswordRequest.class));
    }
}