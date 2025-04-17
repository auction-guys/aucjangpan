package com.fifteen.auction.domain.user.service;

import com.fifteen.auction.domain.user.auth.entity.AuthUser;
import com.fifteen.auction.domain.user.dto.request.SetPasswordRequest;
import com.fifteen.auction.domain.user.dto.request.UserUpdatePasswordRequest;
import com.fifteen.auction.domain.user.dto.request.UserUpdateRequest;
import com.fifteen.auction.domain.user.dto.response.UserResponse;
import com.fifteen.auction.domain.user.dto.response.UserUpdateResponse;
import com.fifteen.auction.domain.user.entity.User;
import com.fifteen.auction.domain.user.repository.UserRepository;
import com.fifteen.auction.global.dto.error.ErrorCode;
import com.fifteen.auction.global.dto.exception.ClientException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserResponse findUser(Long userId) {

        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new ClientException(ErrorCode.USER_NOT_FOUND));

        return new UserResponse(user.getId(), user.getEmail(), user.getNickname(), user.getPreferCategory());
    }

    @Transactional
    public UserUpdateResponse updateProfile(AuthUser authUser, UserUpdateRequest request) {

        User user = userRepository.findById(authUser.getId())
                .orElseThrow(() -> new ClientException(ErrorCode.UNAUTHORIZED_PROFILE_UPDATE));

        //이미 존재하는 닉네임.
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new ClientException(ErrorCode.DUPLICATE_NICKNAME);
        }

        //유저의 프로필 정보(이메일, 이름) 수정.
        user.updateProfile(request.getEmail(), request.getNickname(), request.getAddress());

        return new UserUpdateResponse(user.getId(),
                                      user.getEmail(),
                                      user.getNickname(),
                                      user.getName(),
                                      user.getGender(),
                                      user.getAgeGroup(),
                                      user.getAddress(),
                                      user.getContactNumber(),
                                      user.getPreferCategory(),
                                      user.getAccountNumber()
        );
    }

    @Transactional
    public void updatePassword(AuthUser authUser, UserUpdatePasswordRequest request) {

        User user = userRepository.findById(authUser.getId())
                .orElseThrow(() -> new ClientException(ErrorCode.UNAUTHORIZED_PASSWORD_UPDATE));

        //기존의 비밀번호와 같은 경우에만 수정 가능.
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new ClientException(ErrorCode.INVALID_OLD_PASSWORD);
        }

        //기존의 비밀번호와 새 비밀번호가 같은 경우.
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new ClientException(ErrorCode.PASSWORD_NOT_CHANGED);
        }

        //비밀번호 변경.
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());  //비밀번호 변경 후, 인코딩!!
        user.updatePassword(encodedPassword);
    }

    @Transactional
    public void setPassword(Long userId, SetPasswordRequest request) {

        // 1. 비밀번호, 비밀번호 확인이 일치하는지 검증
        if (!request.getPassword().equals(request.getPasswordCheck())) {
            throw new ClientException(ErrorCode.PASSWORDS_DO_NOT_MATCH);
        }

        // 2. 사용자 조회
        User user = userRepository.findByIdAndDeletedFalse(userId).orElseThrow(
                () -> new ClientException(ErrorCode.USER_NOT_FOUND)
        );

        System.out.println("현재 비밀번호 상태: " + user.getPassword());

        // 3. 기존에 비밀번호가 이미 설정되어 있다면 거부 (중복 설정 방지)
        if (user.getPassword() != null) {
            throw new ClientException(ErrorCode.PASSWORD_ALREADY_SET);
        }

        // 4. 비밀번호 인코딩 후 저장
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        user.setPassword(encodedPassword);
    }

}
