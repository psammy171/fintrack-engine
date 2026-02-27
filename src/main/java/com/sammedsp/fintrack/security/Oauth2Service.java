package com.sammedsp.fintrack.security;

import com.sammedsp.fintrack.dtos.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
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

    public List<PublicUser> searchUserInfo(String search) {
        try {

            String profileUrl = UriComponentsBuilder.fromUriString(accountsUrl).path("/users")
                    .queryParam("search", search)
                    .build()
                    .toUriString();

            HttpHeaders headers = this.getHttpHeaders();

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<ListResponse<PublicUser>> response = restTemplate.exchange(profileUrl, HttpMethod.GET, entity, new ParameterizedTypeReference<ListResponse<PublicUser>>() {});

            ListResponse<PublicUser> publicUserResponse = response.getBody();

            if(publicUserResponse != null )
                return publicUserResponse.data();
        }catch (Exception e){
            return List.of();
        }

        return List.of();
    }

    public List<PublicUser> getUserInfoByUserIds(String[] userIds) {
        try {

            String profileUrl = accountsUrl + "/users";

            HttpHeaders headers = this.getHttpHeaders();

            PublicUserProfilesRequest publicUserProfilesRequest = new PublicUserProfilesRequest(userIds);
            HttpEntity<PublicUserProfilesRequest> entity = new HttpEntity<>(publicUserProfilesRequest, headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<ListResponse<PublicUser>> response = restTemplate.exchange(profileUrl, HttpMethod.POST, entity, new ParameterizedTypeReference<ListResponse<PublicUser>>() {});

            ListResponse<PublicUser> publicUserResponse = response.getBody();

            if(publicUserResponse != null )
                return publicUserResponse.data();
        }catch (Exception e){
            return List.of();
        }

        return List.of();
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
