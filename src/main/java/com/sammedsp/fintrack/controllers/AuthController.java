package com.sammedsp.fintrack.controllers;

import com.sammedsp.fintrack.dtos.PublicUser;
import com.sammedsp.fintrack.dtos.UserContext;
import com.sammedsp.fintrack.security.CookieService;
import com.sammedsp.fintrack.security.AuthService;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/oauth2")
public class AuthController {

    private final AuthService authService;
    private final CookieService cookieService;

    AuthController(AuthService authService, CookieService cookieService){
        this.authService = authService;
        this.cookieService = cookieService;
    }

    @GetMapping("/authorize")
    public ResponseEntity<UserContext> authorize(Authentication authentication){
        UserContext userContext = (UserContext) authentication.getPrincipal();
        return ResponseEntity.ok(userContext);
    }

    @GetMapping("/callback")
    public ResponseEntity<PublicUser> callback(@RequestParam("code") String code){
        PublicUser publicUser = this.authService.authorizeCode(code);
        ResponseCookie cookie = this.cookieService.getResponseCookie(publicUser.id());
        return ResponseEntity
                .ok()
                .header("Set-Cookie", cookie.toString())
                .body(publicUser);
    }

    @GetMapping("/logout")
    public ResponseEntity<Void> logout(){
        ResponseCookie cookie = this.cookieService.getResponseCookieForLogout();
        return ResponseEntity.ok().header("Set-Cookie", cookie.toString()).build();
    }
}
