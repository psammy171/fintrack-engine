package com.sammedsp.fintrack.security;

import com.sammedsp.fintrack.dtos.UserContext;
import com.sammedsp.fintrack.services.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

@Component
public class AuthCookieFilter extends OncePerRequestFilter {

    UserService userService;
    CookieService cookieService;

    AuthCookieFilter(CookieService cookieService, UserService userService){
        this.userService = userService;
        this.cookieService = cookieService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        Cookie[] cookies = request.getCookies();
        Optional<Cookie> authCookie = this.getAuthCookie(cookies);

        if(authCookie.isPresent()){
            String cookieValue = authCookie.get().getValue();
            String userId = this.cookieService.extractUserId(cookieValue);

            Optional<UserContext> userContext = this.userService.getUserInfo(userId);

            if(userContext.isPresent()) {
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = getUsernamePasswordAuthenticationToken(userContext.get());

                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }

        filterChain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken getUsernamePasswordAuthenticationToken(UserContext userContext) {

        return new UsernamePasswordAuthenticationToken(
                userContext,
                null,
                Collections.emptyList()
        );
    }

    private Optional<Cookie> getAuthCookie(Cookie[] cookies)  {
        if (cookies == null || cookies.length == 0) {
            return Optional.empty();
        }

        return Arrays.stream(cookies)
                .filter(cookie -> "fintrack-auth-token".equals(cookie.getName()))
                .findFirst();
    }
}
