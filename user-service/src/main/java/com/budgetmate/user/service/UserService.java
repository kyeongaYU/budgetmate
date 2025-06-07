package com.budgetmate.user.service;

import com.budgetmate.user.dto.*;
import com.budgetmate.user.entity.LoginType;
import com.budgetmate.user.entity.SignupCode;
import com.budgetmate.user.entity.User;
import com.budgetmate.user.repository.SignupCodeRepository;
import com.budgetmate.user.repository.UserRepository;
import com.budgetmate.user.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository         userRepository;
    private final PasswordEncoder        passwordEncoder;
    private final JwtTokenProvider       jwtTokenProvider;
    private final JavaMailSender         mailSender;
    private final SignupCodeRepository   signupCodeRepository;

    private static final long SIGNUP_CODE_VALIDITY = 5 * 60 * 1000;  // 5분
    private static final long RESET_CODE_VALIDITY  = 5 * 60 * 1000;  // 5분

    @Value("${kakao.client-id}")
    private String kakaoClientId;
    @Value("${kakao.redirect-uri}")
    private String kakaoRedirectUri;
    @Value("${google.client-id}")
    private String googleClientId;
    @Value("${google.client-secret}")
    private String googleClientSecret;
    @Value("${google.redirect-uri}")
    private String googleRedirectUri;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // ──────────────────────────────────────────────────────────────────────────
    //  인증코드 생성
    // ──────────────────────────────────────────────────────────────────────────
    @Transactional
    public String generateAndSaveSignupCode(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("이미 가입된 이메일입니다.");
        }

        int randomNum = new java.util.Random().nextInt(900000) + 100000;
        String code = String.valueOf(randomNum);

        Date now      = new Date();
        Date expireAt = new Date(now.getTime() + SIGNUP_CODE_VALIDITY);


        signupCodeRepository.findByEmailAndUsedFalse(email)
                .ifPresentOrElse(existing -> {
                    existing.setCode(code);
                    existing.setExpireAt(expireAt);
                    existing.setUsed(false);
                    signupCodeRepository.save(existing);
                }, () -> {
                    signupCodeRepository.findByEmailAndUsedTrue(email)
                            .ifPresentOrElse(oldUsed -> {
                                oldUsed.setCode(code);
                                oldUsed.setExpireAt(expireAt);
                                oldUsed.setUsed(false);
                                signupCodeRepository.save(oldUsed);
                            }, () -> {
                                SignupCode newSignupCode = SignupCode.builder()
                                        .email(email)
                                        .code(code)
                                        .expireAt(expireAt)
                                        .used(false)
                                        .build();
                                signupCodeRepository.save(newSignupCode);
                            });
                });

        return code;
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  인증코드 검증
    // ──────────────────────────────────────────────────────────────────────────
    @Transactional
    public boolean verifySignupCode(String email, String code) {
        SignupCode signupCode = signupCodeRepository.findByEmailAndUsedFalse(email)
                .orElseThrow(() -> new RuntimeException("유효한 인증 요청이 없습니다."));

        if (!signupCode.getCode().equals(code)) {
            return false;
        }

        Date now = new Date();
        if (signupCode.getExpireAt() == null || signupCode.getExpireAt().before(now)) {
            signupCodeRepository.delete(signupCode);
            return false;
        }

        signupCode.setUsed(true);
        signupCodeRepository.save(signupCode);
        return true;
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  회원가입
    // ──────────────────────────────────────────────────────────────────────────
    @Transactional
    public User signup(SignupRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("이미 등록된 이메일입니다.");
        }

        // used=false 혹은 used=true 상태 둘 다 조회
        SignupCode sc = signupCodeRepository.findByEmailAndUsedFalse(email)
                .orElseGet(() -> signupCodeRepository.findByEmailAndUsedTrue(email).orElse(null));
        if (sc == null || !sc.isUsed()) {
            throw new RuntimeException("인증을 완료하지 않았습니다.");
        }

        // 실제 User 저장
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .userName(request.getUserName())
                .roles(List.of("ROLE_USER"))
                .build();
        User savedUser = userRepository.save(user);

        // 인증코드 레코드 삭제
        signupCodeRepository.delete(sc);

        return savedUser;
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  로그인
    // ──────────────────────────────────────────────────────────────────────────
    public String login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }
        return jwtTokenProvider.createToken(user.getEmail(), user.getRoles());
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public FindIdResponse findIdByUserName(FindIdRequest request) {
        String userName = request.getUserName().trim();
        List<User> matchedUsers = userRepository.findAllByUserName(userName);

        if (matchedUsers.isEmpty()) {
            throw new RuntimeException("해당 이름으로 가입된 계정이 없습니다.");
        }

        FindIdResponse resp = new FindIdResponse();
        if (matchedUsers.size() == 1) {
            resp.setEmail(matchedUsers.get(0).getEmail());
            resp.setMultiple(false);
        } else {
            resp.setMultiple(true);
            List<String> emailList = matchedUsers.stream()
                    .map(User::getEmail)
                    .toList();
            resp.setEmailList(emailList);
        }
        return resp;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 비밀번호 재설정용 코드 생성
    // ──────────────────────────────────────────────────────────────────────────
    @Transactional
    public String generateAndSaveResetCode(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("등록된 이메일이 아닙니다."));

        int randomNum = new java.util.Random().nextInt(900000) + 100000;
        String code = String.valueOf(randomNum);

        Date now      = new Date();
        Date expireAt = new Date(now.getTime() + RESET_CODE_VALIDITY);

        user.setResetCode(code);
        user.setResetCodeExpireAt(expireAt);
        userRepository.save(user);

        return code;
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  비밀번호 재설정용 코드 검증
    // ──────────────────────────────────────────────────────────────────────────
    @Transactional
    public boolean verifyResetCode(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("등록된 이메일이 아닙니다."));

        if (user.getResetCode() == null || !user.getResetCode().equals(code)) {
            return false;
        }

        Date now = new Date();
        if (user.getResetCodeExpireAt() == null || user.getResetCodeExpireAt().before(now)) {
            user.setResetCode(null);
            user.setResetCodeExpireAt(null);
            userRepository.save(user);
            return false;
        }

        return true;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 비밀번호 재설정
    // ──────────────────────────────────────────────────────────────────────────
    @Transactional
    public boolean resetPasswordWithCode(String email, String code, String newPassword) {
        if (!verifyResetCode(email, code)) {
            throw new RuntimeException("유효하지 않거나 만료된 인증 코드입니다.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("등록된 이메일이 아닙니다."));
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetCode(null);
        user.setResetCodeExpireAt(null);
        userRepository.save(user);

        return true;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 카카오 로그인 처리
    // ──────────────────────────────────────────────────────────────────────────
    @Transactional
    public SocialLoginResult kakaoLoginAndGetUser(String code) {
        String token = getAccessToken(code);
        KakaoUserInfo k = getKakaoUserInfo(token);
        return processSocialLogin(toSocialUserInfo(k), LoginType.KAKAO);
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  구글 로그인 처리
    // ──────────────────────────────────────────────────────────────────────────
    @Transactional
    public SocialLoginResult googleLoginAndGetUser(String code) {
        String token = getGoogleAccessToken(code);
        GoogleUserInfo g = getGoogleUserInfo(token);
        return processSocialLogin(toSocialUserInfo(g), LoginType.GOOGLE);
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  카카오 소셜링크 확인 (소셜 전환)
    // ──────────────────────────────────────────────────────────────────────────
    @Transactional
    public User confirmKakaoLink(String code) {
        String token = getAccessToken(code);
        KakaoUserInfo k = getKakaoUserInfo(token);
        return confirmSocialLink(toSocialUserInfo(k), LoginType.KAKAO);
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  구글 소셜링크 확인 (소셜 전환)
    // ──────────────────────────────────────────────────────────────────────────
    @Transactional
    public User confirmGoogleLink(String code) {
        String token = getGoogleAccessToken(code);
        GoogleUserInfo g = getGoogleUserInfo(token);
        return confirmSocialLink(toSocialUserInfo(g), LoginType.GOOGLE);
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  소셜 로그인/가입 공통 처리
    // ──────────────────────────────────────────────────────────────────────────
    @Transactional
    public SocialLoginResult processSocialLogin(SocialUserInfo info, LoginType loginType) {
        Optional<User> userOpt = userRepository.findBySocialIdAndLoginType(info.getId(), loginType);
        if (userOpt.isPresent()) {
            return SocialLoginResult.builder()
                    .user(userOpt.get())
                    .requiresConsent(false)
                    .build();
        }

        Optional<User> existingEmailUser = userRepository.findByEmail(info.getEmail());
        if (existingEmailUser.isPresent()) {
            User user = existingEmailUser.get();
            if (user.getLoginType() == LoginType.LOCAL) {
                return SocialLoginResult.builder()
                        .user(user)
                        .requiresConsent(true)
                        .build();
            } else {
                return SocialLoginResult.builder()
                        .user(user)
                        .requiresConsent(false)
                        .build();
            }
        }

        User newUser = User.builder()
                .email(info.getEmail())
                .userName(info.getName())
                .loginType(loginType)
                .socialId(info.getId())
                .roles(List.of("ROLE_USER"))
                .build();

        return SocialLoginResult.builder()
                .user(userRepository.save(newUser))
                .requiresConsent(false)
                .build();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 소셜 계정 연동(로컬 → 소셜) 최종 처리
    // ──────────────────────────────────────────────────────────────────────────
    @Transactional
    public User confirmSocialLink(SocialUserInfo info, LoginType loginType) {
        User user = userRepository.findByEmail(info.getEmail())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 이메일입니다."));

        if (user.getLoginType() != LoginType.LOCAL) {
            throw new RuntimeException("이미 소셜 계정으로 등록된 사용자입니다.");
        }

        user.setLoginType(loginType);
        user.setSocialId(info.getId());
        return userRepository.save(user);
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  이메일로 유저 조회
    // ──────────────────────────────────────────────────────────────────────────
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("해당 이메일의 사용자가 존재하지 않습니다."));
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  카카오 액세스 토큰 요청
    // ──────────────────────────────────────────────────────────────────────────
    private String getAccessToken(String code) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", kakaoClientId);
        body.add("redirect_uri", kakaoRedirectUri);
        body.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "https://kauth.kakao.com/oauth/token", request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody().get("access_token") != null) {
                return (String) response.getBody().get("access_token");
            } else {
                throw new RuntimeException("카카오 access_token 발급 실패: " + response.getBody());
            }

        } catch (HttpClientErrorException.BadRequest e) {
            throw new RuntimeException("이미 사용된 인가 코드입니다.");
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 구글 액세스 토큰 요청
    // ──────────────────────────────────────────────────────────────────────────
    private String getGoogleAccessToken(String code) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", googleClientId);
        body.add("client_secret", googleClientSecret);
        body.add("redirect_uri", googleRedirectUri);
        body.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://oauth2.googleapis.com/token", request, Map.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody().get("access_token") != null) {
            return (String) response.getBody().get("access_token");
        } else {
            throw new RuntimeException("구글 access_token 발급 실패: " + response.getBody());
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  구글 사용자 정보 조회
    // ──────────────────────────────────────────────────────────────────────────
    private GoogleUserInfo getGoogleUserInfo(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<?> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://www.googleapis.com/oauth2/v2/userinfo", HttpMethod.GET, request, Map.class);

        Map<String, Object> body = response.getBody();
        if (body == null) {
            throw new RuntimeException("구글 사용자 정보 조회 실패");
        }

        return GoogleUserInfo.builder()
                .id((String) body.get("id"))
                .email((String) body.get("email"))
                .name((String) body.get("name"))
                .build();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 카카오 사용자 정보 조회
    // ──────────────────────────────────────────────────────────────────────────
    private KakaoUserInfo getKakaoUserInfo(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<?> request = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me", HttpMethod.GET, request, Map.class);

        Map<String, Object> body = response.getBody();
        if (body == null) {
            throw new RuntimeException("카카오 사용자 정보 조회 실패");
        }

        Map<String, Object> kakaoAccount = (Map<String, Object>) body.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        return KakaoUserInfo.builder()
                .id(String.valueOf(body.get("id")))
                .email((String) kakaoAccount.get("email"))
                .nickname((String) profile.get("nickname"))
                .build();
    }

    private SocialUserInfo toSocialUserInfo(GoogleUserInfo g) {
        return SocialUserInfo.builder()
                .id(g.getId())
                .email(g.getEmail())
                .name(g.getName())
                .build();
    }

    private SocialUserInfo toSocialUserInfo(KakaoUserInfo k) {
        return SocialUserInfo.builder()
                .id(k.getId())
                .email(k.getEmail())
                .name(k.getNickname())
                .build();
    }

    @Transactional
    public User authenticate(String email, String password) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElse(null);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            return user;
        }
        return null;
    }

}
