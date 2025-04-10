package com.fifteen.auction.domain.user.controller;

import com.fifteen.auction.domain.user.auth.entity.AuthUser;
import com.fifteen.auction.domain.user.dto.request.UserUpdatePasswordRequest;
import com.fifteen.auction.domain.user.dto.request.UserUpdateRequest;
import com.fifteen.auction.domain.user.dto.response.UserResponse;
import com.fifteen.auction.domain.user.dto.response.UserUpdateResponse;
import com.fifteen.auction.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> findUser(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(userService.findUser(userId));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserUpdateResponse> updateProfile(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody @Valid UserUpdateRequest request
    ) {
        return ResponseEntity.ok(userService.updateProfile(authUser, request));
    }

    @PutMapping("/password")
    public void updatePassword(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody @Valid UserUpdatePasswordRequest request
    ) {
        userService.updatePassword(authUser, request);
    }
}
