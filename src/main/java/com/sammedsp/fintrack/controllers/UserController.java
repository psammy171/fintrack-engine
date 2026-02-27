package com.sammedsp.fintrack.controllers;

import com.sammedsp.fintrack.dtos.ListResponse;
import com.sammedsp.fintrack.dtos.PublicUser;
import com.sammedsp.fintrack.dtos.UserContext;
import com.sammedsp.fintrack.security.Oauth2Service;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final Oauth2Service oauth2Service;

    UserController(Oauth2Service oauth2Service){
        this.oauth2Service = oauth2Service;
    }

    @GetMapping
    public ResponseEntity<ListResponse<PublicUser>> searchUsers(Authentication authentication, @RequestParam("search") String search) {
        UserContext userContext = (UserContext) authentication.getPrincipal();

        var users = this.oauth2Service.searchUserInfo(search);
        var usersExceptCurrentUser = users.stream().filter(user -> !user.userId().equals(userContext.userId())).toList();
        var publicUserResponse = new ListResponse<>(usersExceptCurrentUser);

        return ResponseEntity.ok(publicUserResponse);
    }
}
