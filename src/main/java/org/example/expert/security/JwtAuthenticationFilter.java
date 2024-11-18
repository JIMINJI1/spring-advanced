package org.example.expert.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.config.JwtUtil;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@Slf4j(topic = "로그인 및 토큰 생성")
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(final JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
        setFilterProcessesUrl("/auth/signin");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            SigninRequest requestDto = new ObjectMapper().readValue(request.getInputStream(), SigninRequest.class);

            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(
                            requestDto.getEmail(),
                            requestDto.getPassword(),
                            null
                    )
            );
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    // 인증 성공
    // UserDetailsImpl에 getUsername()에서 이메일 받아 옴
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException {
        String email = ((UserDetailsImpl) authResult.getPrincipal()).getUsername();
        UserRole role = ((UserDetailsImpl) authResult.getPrincipal()).getUser().getUserRole();
        String nickname = ((UserDetailsImpl) authResult.getPrincipal()).getUser().getNickname();
        Long id = ((UserDetailsImpl) authResult.getPrincipal()).getUser().getId();

        String token = jwtUtil.createToken(id, email, role, nickname);

        final String AUTHORIZATION_HEADER = "Authorization";
        response.addHeader(AUTHORIZATION_HEADER, token);

        log.info("인증 성공: 이메일 = {}, 역할 = {}, JWT 토큰 생성됨", email, role);
    }

    // 인증 실패
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        response.setContentType("application/json; charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        int status = HttpServletResponse.SC_UNAUTHORIZED;
        String errorMessage = "이메일 또는 비밀번호를 잘못 입력하였습니다.";

        String jsonResponse = String.format("{\"status\": %d, \"errorMessage\": \"%s\"}", status, errorMessage);
        response.getWriter().write(jsonResponse);
        response.getWriter().flush(); // 버퍼 비우기

    }
}
