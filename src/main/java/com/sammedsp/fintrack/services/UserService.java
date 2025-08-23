package com.sammedsp.fintrack.services;

import com.sammedsp.fintrack.dtos.UserContext;
import com.sammedsp.fintrack.security.Oauth2Service;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final Oauth2Service oauth2Service;

    UserService(Oauth2Service oauth2Service){
        this.oauth2Service = oauth2Service;
    }


    public Optional<UserContext> getUserInfo(String userId) {
        return this.oauth2Service.getUserInfo(userId);
    }
}
