package com.sammedsp.fintrack.security;

import com.sammedsp.fintrack.dtos.AuthorizeCodeDto;
import com.sammedsp.fintrack.dtos.AuthorizeCodeResponseDto;
import com.sammedsp.fintrack.dtos.UserContext;
import com.sammedsp.fintrack.dtos.UserProfileRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
public class Oauth2Service {
    @Value("${client.id}")
    private String clientId;

    @Value("${client.secret}")
    private String clientSecret;

    @Value("${accounts.url}")
    private String accountsUrl;

    public Optional<UserContext> getUserInfo(String userId) {
        try {

            String profileUrl = accountsUrl + "/profile/client";

            HttpHeaders headers = this.getHttpHeaders();

            UserProfileRequest userProfileRequest = new UserProfileRequest(userId);
            HttpEntity<UserProfileRequest> entity = new HttpEntity<>(userProfileRequest, headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<UserContext> response = restTemplate.exchange(profileUrl, HttpMethod.POST, entity, UserContext.class);

            UserContext userContext = response.getBody();

            if(userContext != null)
                return Optional.of(userContext);
        }catch (Exception e){
            return Optional.empty();
        }
        return Optional.empty();
    }

    public AuthorizeCodeResponseDto authorizeCode(String code){
        String authorizeUrl = accountsUrl + "/auth/authorize/code";

        AuthorizeCodeDto authorizeCodeDto = new AuthorizeCodeDto(this.clientId, code );

        HttpHeaders headers = this.getHttpHeaders();
        HttpEntity<AuthorizeCodeDto> entity = new HttpEntity<>(authorizeCodeDto, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<AuthorizeCodeResponseDto> response = restTemplate.exchange(authorizeUrl, HttpMethod.POST, entity, AuthorizeCodeResponseDto.class);

        return response.getBody();
    }

    private HttpHeaders getHttpHeaders(){
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(this.clientId, this.clientSecret);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
