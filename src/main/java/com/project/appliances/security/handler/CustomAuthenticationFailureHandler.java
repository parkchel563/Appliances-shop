package com.project.appliances.security.handler;

import com.project.appliances.service.interfaces.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final UserService userService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        String email = request.getParameter("username");

        if (exception instanceof LockedException) {
            response.sendRedirect("/login?error=locked");
            return;
        }

        userService.registerFailedAttempt(email);

        boolean lockedNow = userService.findUserByEmail(email)
                .map(user -> !Boolean.TRUE.equals(user.getAccountNonLocked()))
                .orElse(false);

        if (lockedNow) {
            response.sendRedirect("/login?error=locked");
            return;
        }

        response.sendRedirect("/login?error=invalid");
    }
}