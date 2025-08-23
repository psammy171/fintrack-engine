package com.sammedsp.fintrack.security;

import com.sammedsp.fintrack.dtos.AuthorizeCodeResponseDto;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final Oauth2Service oauth2Service;

    AuthService(Oauth2Service oauth2Service){
        this.oauth2Service = oauth2Service;
    }

    public AuthorizeCodeResponseDto authorizeCode(String code){
        return this.oauth2Service.authorizeCode(code);
    }
}
