package com.sammedsp.fintrack.security;

import com.sammedsp.fintrack.dtos.PublicUser;

import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final Oauth2Service oauth2Service;

    AuthService(Oauth2Service oauth2Service){
        this.oauth2Service = oauth2Service;
    }

    public PublicUser authorizeCode(String code){
        return this.oauth2Service.authorizeCode(code);
    }
}
