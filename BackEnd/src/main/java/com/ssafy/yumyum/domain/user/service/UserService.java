package com.ssafy.yumyum.domain.user.service;

import java.sql.Date;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.yumyum.domain.user.dao.UserDao;
import com.ssafy.yumyum.domain.user.dto.UserOnboardingRequest;
import com.ssafy.yumyum.domain.user.dto.UserOnboardingResponse;
import com.ssafy.yumyum.domain.user.dto.UserProfileResponse;
import com.ssafy.yumyum.domain.user.dto.UserProfileUpdateRequest;
import com.ssafy.yumyum.domain.user.entity.User;
import com.ssafy.yumyum.domain.user.entity.UserRole;
import com.ssafy.yumyum.global.exception.BusinessException;
import com.ssafy.yumyum.global.exception.ExceptionType;
import com.ssafy.yumyum.global.security.jwt.TokenProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserDao userDao;
    private final TokenProvider tokenProvider;

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        return toProfileResponse(findUser(userId));
    }

    @Transactional
    public UserProfileResponse updateProfile(Long userId, UserProfileUpdateRequest request) {
        User user = findUser(userId);
        user.setNickname(request.nickname().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setGender(request.gender());
        user.setBirthDate(Date.valueOf(request.birthDate()));
        user.setHeightCm(request.heightCm());
        user.setWeightKg(request.weightKg());
        user.setActivityLevel(request.activityLevel());

        if (!userDao.updateProfile(user)) {
            throw new BusinessException(ExceptionType.USER_NOT_FOUND);
        }
        return toProfileResponse(user);
    }

    @Transactional
    public UserOnboardingResponse completeOnboarding(Long userId, UserOnboardingRequest request) {
        User user = findUser(userId);
        if (user.getRole() != UserRole.GUEST) {
            throw new BusinessException(ExceptionType.USER_PROFILE_ALREADY_COMPLETED);
        }

        user.setNickname(request.nickname().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setGender(request.gender());
        user.setBirthDate(Date.valueOf(request.birthDate()));
        user.setHeightCm(request.heightCm());
        user.setWeightKg(request.weightKg());
        user.setActivityLevel(request.activityLevel());

        if (!userDao.completeProfile(user)) {
            throw new BusinessException(ExceptionType.USER_PROFILE_ALREADY_COMPLETED);
        }

        user.setRole(UserRole.USER);
        return new UserOnboardingResponse(
                tokenProvider.generateAccessToken(user),
                tokenProvider.generateRefreshToken(user),
                user.getRole().name()
        );
    }

    private User findUser(Long userId) {
        return userDao.findById(userId)
                .orElseThrow(() -> new BusinessException(ExceptionType.USER_NOT_FOUND));
    }

    private UserProfileResponse toProfileResponse(User user) {
        return new UserProfileResponse(
                user.getId(), user.getNickname(), user.getEmail(), user.getProvider(),
                user.getProfileImageUrl(), user.getGender(),
                user.getBirthDate() == null ? null : new java.sql.Date(user.getBirthDate().getTime()).toLocalDate(),
                user.getHeightCm(), user.getWeightKg(), user.getActivityLevel()
        );
    }
}
