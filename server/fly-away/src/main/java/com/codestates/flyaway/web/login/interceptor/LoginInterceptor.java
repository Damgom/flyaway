package com.codestates.flyaway.web.login.interceptor;

import com.auth0.jwt.JWT;
import com.codestates.flyaway.domain.member.entity.Member;
import com.codestates.flyaway.domain.member.repository.MemberRepository;
import com.codestates.flyaway.global.exception.BusinessLogicException;
import com.codestates.flyaway.global.exception.ExceptionCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

import static com.auth0.jwt.algorithms.Algorithm.*;
import static com.codestates.flyaway.domain.login.util.JwtProperties.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    private final MemberRepository memberRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        String header = request.getHeader(HEADER);

        if (header == null || !header.startsWith(PREFIX)) {
            errorResponse(response);
            return false;
        }

        String token = request.getHeader(HEADER).replace(PREFIX, "");
        String email = JWT.require(HMAC512(SECRET)).build()
                .verify(token)
                .getClaim("email").asString();

        if (email == null || !memberRepository.existsByEmail(email)) {
            errorResponse(response);
            return false;
        }

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.EMAIL_NOT_EXISTS));

        if (!member.getIsLoggedIn().equals("Y")) {
            errorResponse(response);
            return false;
        }

        if (request.getRequestURI().equals("/logout")) {
            log.info("email ={}", email);
            request.setAttribute("email", email);
        }

        return true;
    }

    private boolean isPreflightRequest(HttpServletRequest request) {
        return isOptions(request) && hasHeaders(request) && hasMethod(request) && hasOrigin(request);
    }

    private boolean isOptions(HttpServletRequest request) {
        return request.getMethod().equalsIgnoreCase(HttpMethod.OPTIONS.toString());
    }

    private boolean hasHeaders(HttpServletRequest request) {
        return Objects.nonNull(request.getHeader("Access-Control-Request-Headers"));
    }

    private boolean hasMethod(HttpServletRequest request) {
        return Objects.nonNull(request.getHeader("Access-Control-Request-Method"));
    }

    private boolean hasOrigin(HttpServletRequest request) {
        return Objects.nonNull(request.getHeader("Origin"));
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        if (ex != null) {
            log.info("exception occurred={}", ex.getMessage());
        }
    }

    private void errorResponse(HttpServletResponse response) throws IOException {
        response.setStatus(401);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("???????????? ????????? ??????????????????.");
    }
}
