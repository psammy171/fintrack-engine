package com.sammedsp.fintrack.controllers;

import com.sammedsp.fintrack.dtos.UserContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.nio.file.attribute.UserPrincipalNotFoundException;

@RestController
@RequestMapping("api/profile")
public class ProfileController {

    @GetMapping
    public ResponseEntity<UserContext> getUserDetails(Authentication authentication) throws UserPrincipalNotFoundException {
        UserContext userContext = (UserContext) authentication.getPrincipal();
        return ResponseEntity.ok(userContext);
    }
}
