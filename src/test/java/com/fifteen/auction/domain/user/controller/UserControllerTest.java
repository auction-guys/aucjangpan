package com.fifteen.auction.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fifteen.auction.domain.recommend.entity.RecommendGroup;
import com.fifteen.auction.domain.recommend.enums.AgeGroup;
import com.fifteen.auction.domain.recommend.enums.Gender;
import com.fifteen.auction.domain.recommend.enums.Region;
import com.fifteen.auction.domain.user.auth.entity.AuthUser;
import com.fifteen.auction.domain.user.dto.request.SetPasswordRequest;
import com.fifteen.auction.domain.user.dto.request.UserUpdatePasswordRequest;
import com.fifteen.auction.domain.user.dto.request.UserUpdateRequest;
import com.fifteen.auction.domain.user.dto.response.UserResponse;
import com.fifteen.auction.domain.user.dto.response.UserUpdateResponse;
import com.fifteen.auction.domain.user.entity.User;
import com.fifteen.auction.domain.user.enums.UserRole;
import com.fifteen.auction.domain.user.repository.UserRepository;
import com.fifteen.auction.domain.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
        // 모의 사용자 설정
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
        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.nickname").value("nickname"));
    }

    @Test
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

        // AuthUser 설정
        AuthUser authUser = new AuthUser(1L, "test@example.com", "oldNickname");
        Authentication authentication = new UsernamePasswordAuthenticationToken(authUser, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        RecommendGroup dummyGroup = RecommendGroup.create(Gender.MALE, AgeGroup.TWENTIES, Region.SEOUL); // 임시 RecommendGroup 객체

        // userRepository 모킹
        User user = new User(
                "test@example.com",
                "닉네임",
                "홍길동",
                Gender.MALE,
                AgeGroup.TWENTIES,
                "encodedPassword",
                Region.SEOUL,
                "010-1234-5678",
                "전자기기",
                "1234567890",
                dummyGroup,
                UserRole.ROLE_USER
        );
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByNickname("newNickname")).thenReturn(false);

        // userService 모킹
        when(userService.updateProfile(any(AuthUser.class), any(UserUpdateRequest.class)))
                .thenReturn(response);

        // when
        MvcResult result = mockMvc.perform(put("/api/v1/users/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        // then
        mockMvc.perform(put("/api/v1/users/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.nickname").value("newNickname"))
                .andExpect(jsonPath("$.gender").value("MALE"))
                .andExpect(jsonPath("$.ageGroup").value("TWENTIES"))
                .andExpect(jsonPath("$.address").value("SEOUL"));
    }

    @Test
    void 비밀번호_수정_성공() throws Exception {

        // given
        UserUpdatePasswordRequest request = new UserUpdatePasswordRequest("oldPw123", "newPw1234");

        // AuthUser 설정
        AuthUser authUser = new AuthUser(1L, "test@example.com", "testNickname");
        Authentication authentication = new UsernamePasswordAuthenticationToken(authUser, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        RecommendGroup dummyGroup = RecommendGroup.create(Gender.MALE, AgeGroup.TWENTIES, Region.SEOUL); // 임시 RecommendGroup 객체

        // userRepository 모킹
        User user = new User(
                "test@example.com",
                "닉네임",
                "홍길동",
                Gender.MALE,
                AgeGroup.TWENTIES,
                "encodedPassword",
                Region.SEOUL,
                "010-1234-5678",
                "전자기기",
                "1234567890",
                dummyGroup,
                UserRole.ROLE_USER
        );

        // userService 모킹 (void 메서드라 실제 호출 허용)
        doNothing().when(userService).updatePassword(any(AuthUser.class), any(UserUpdatePasswordRequest.class));

        // when
        MvcResult result = mockMvc.perform(put("/api/v1/users/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())  // 여기서 바로 검증
                .andReturn();

        // then
        verify(userService).updatePassword(any(AuthUser.class), any(UserUpdatePasswordRequest.class));

    }

    @Test
    void 비밀번호_설정_성공() throws Exception {
        // given
        SetPasswordRequest request = new SetPasswordRequest("myNewPw123", "myNewPw123");
        AuthUser authUser = new AuthUser(1L, "test@email.com", UserRole.ROLE_USER.name());

        // 권한 설정 포함된 토큰 생성
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                authUser,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // when & then
        mockMvc.perform(post("/api/v1/users/password/set")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        Mockito.verify(userService).setPassword(any(Long.class), any(SetPasswordRequest.class));
    }

}
