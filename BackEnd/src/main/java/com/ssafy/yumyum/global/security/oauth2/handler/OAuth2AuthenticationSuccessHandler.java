package com.ssafy.yumyum.global.security.oauth2.handler;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.ssafy.yumyum.domain.user.dao.UserDao;
import com.ssafy.yumyum.domain.user.entity.User;
import com.ssafy.yumyum.domain.user.entity.UserRole;
import com.ssafy.yumyum.global.exception.BusinessException;
import com.ssafy.yumyum.global.exception.ExceptionType;
import com.ssafy.yumyum.global.security.jwt.TokenProvider;
import com.ssafy.yumyum.global.security.oauth2.repository.HttpCookieOAuth2AuthorizationRequestRepository;
import com.ssafy.yumyum.global.security.oauth2.service.OAuth2UserPrincipal;
import com.ssafy.yumyum.global.security.oauth2.user.OAuth2Provider;
import com.ssafy.yumyum.global.security.oauth2.user.unlink.OAuth2UserUnlinkManager;
import com.ssafy.yumyum.global.util.CookieUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.ssafy.yumyum.global.security.oauth2.repository.HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME;
import static com.ssafy.yumyum.global.security.oauth2.repository.HttpCookieOAuth2AuthorizationRequestRepository.MODE_PARAM_COOKIE_NAME;

@Slf4j
@RequiredArgsConstructor
@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;
    private final OAuth2UserUnlinkManager oAuth2UserUnlinkManager;
    private final TokenProvider tokenProvider;
    private final UserDao userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        String targetUrl = determineTargetUrl(request, response, authentication);

        log.info("determineTargetUrl={}", targetUrl);

        if (response.isCommitted()) {
            log.debug("Response has already been committed. Unable to redirect to {}", targetUrl);
            return;
        }

        clearAuthenticationAttributes(request, response);

        log.info("after clear Authentication: targetUrl={}", targetUrl);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String targetUrl = CookieUtils.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)
                .map(Cookie::getValue)
                .orElse(getDefaultTargetUrl());

        String mode = CookieUtils.getCookie(request, MODE_PARAM_COOKIE_NAME)
                .map(Cookie::getValue)
                .orElse("");

        OAuth2UserPrincipal principal = getOAuth2UserPrincipal(authentication);

        if (principal == null) {
            return UriComponentsBuilder.fromUriString(targetUrl)
                    .queryParam("error", "Login failed")
                    .build().toUriString();
        }

        if (mode.equalsIgnoreCase("login")) {
            String providerUserId = principal.getUserInfo().getId();
            OAuth2Provider provider = principal.getUserInfo().getProvider();

            log.info("providerUserId={}, provider={}", providerUserId, provider);

            AtomicReference<Boolean> isNewUser = new AtomicReference<>(false);

            User user = userRepository.findByProviderAndProviderUserId(provider, providerUserId)
                    .orElseGet(() -> {
                        isNewUser.set(true);
                        return createAndSaveNewUser(
                                provider,
                                providerUserId,
                                principal.getUserInfo().getNickname(),
                                principal.getUserInfo().getEmail(),
                                principal.getUserInfo().getProfileImageUrl()
                        );
                    });

            if (user.getRole() == UserRole.GUEST) {
                isNewUser.set(true);
            }

            userRepository.updateLastLoginAt(user.getId());

            String accessToken = tokenProvider.generateAccessToken(user);
            String refreshToken = tokenProvider.generateRefreshToken(user);

            // TODO: 리프레시 토큰 저장

            log.info("userId={}, provider={}, providerId={}", user.getId(), user.getProvider(), user.getProviderUserId());

            log.info("email={}, nickname={}, accessToken={}",
                    principal.getUserInfo().getEmail(),
                    principal.getUserInfo().getNickname(),
                    principal.getUserInfo().getAccessToken()
            );

            return UriComponentsBuilder.fromUriString(targetUrl)
                    .queryParam("is_new_user", isNewUser)
                    .queryParam("access_token", accessToken)
                    .queryParam("refresh_token", refreshToken)
                    .build().toUriString();
        } else if (mode.equalsIgnoreCase("unlink")) {
            String accessToken = principal.getUserInfo().getAccessToken();
            String providerId = principal.getUserInfo().getId();
            OAuth2Provider provider = principal.getUserInfo().getProvider();

            User user = userRepository.findByProviderAndProviderUserId(provider, providerId)
                    .orElseThrow(() -> new BusinessException(ExceptionType.USER_NOT_FOUND));

            oAuth2UserUnlinkManager.unlink(provider, accessToken);

            if (!userRepository.deactivate(user.getId())) {
                throw new BusinessException(ExceptionType.USER_NOT_FOUND);
            }

            return UriComponentsBuilder.fromUriString(targetUrl)
                    .queryParam("unlinked", true)
                    .build().toUriString();
        }
        return UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("error", "Login failed")
                .build().toUriString();
    }

    private User createAndSaveNewUser(OAuth2Provider provider, String providerId, String nickname,
                                      String email, String profileImageUrl) {
        User user = User.builder()
                .provider(provider)
                .providerUserId(providerId)
                .nickname(resolveNickname(nickname))
                .email(email)
                .profileImageUrl(profileImageUrl)
                // TODO: 기본 프로필 아이콘 주소(String) 넣기
                .role(UserRole.GUEST)
                .build();

        userRepository.insert(user);
        return user;
    }

    private String resolveNickname(String nickname) {
        if (nickname != null && !nickname.isBlank()) {
            return nickname;
        }

        return "유저_" + UUID.randomUUID().toString().substring(0, 8);
    }

    private OAuth2UserPrincipal getOAuth2UserPrincipal(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof OAuth2UserPrincipal) {
            return (OAuth2UserPrincipal) principal;
        }
        return null;
    }

    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }
}
