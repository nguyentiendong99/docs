package com.bkav.lk.web.rest;

import com.bkav.lk.domain.User;
import com.bkav.lk.dto.Oauth2DTO;
import com.bkav.lk.dto.UserDTO;
import com.bkav.lk.dto.YBIAccessTokenDTO;
import com.bkav.lk.dto.YBIUserInfoDTO;
import com.bkav.lk.security.jwt.JWTFilter;
import com.bkav.lk.security.jwt.JWTToken;
import com.bkav.lk.security.jwt.TokenProvider;
import com.bkav.lk.service.UserService;
import com.bkav.lk.service.util.RestTemplateHelper;
import com.bkav.lk.service.util.Utils;
import com.bkav.lk.util.StrUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Controller for oauth authentication
 */
@RestController
@RequestMapping("/api")
public class OAuth2Controller {
    private final Logger log = LoggerFactory.getLogger(OAuth2Controller.class);

    @Value("${ybi-sso.api-get-token}")
    private String API_GET_TOKEN;

    @Value("${ybi-sso.api-get-user-info}")
    private String API_GET_USER_INFO;

    @Value("${ybi-sso.api-revoke-token}")
    private String API_REVOKE_TOKEN;

    @Value("${ybi-sso.api-logout-oidc}")
    private String API_LOGOUT_OIDC;

    @Value("${ybi-sso.redirect-uri}")
    private String REDIRECT_URI;

    @Value("${ybi-sso.redirect-header-value}")
    private String REDIRECT_HEADER_VALUE;

    @Value("${ybi-sso.client-id}")
    private String CLIENT_ID;

    @Value("${ybi-sso.client-secret}")
    private String CLIENT_SECRET;

    private final RestTemplateHelper restTemplateHelper;

    private final UserService userService;

    private final TokenProvider tokenProvider;


    public OAuth2Controller(RestTemplateHelper restTemplateHelper, UserService userService, TokenProvider tokenProvider) {
        this.restTemplateHelper = restTemplateHelper;
        this.userService = userService;
        this.tokenProvider = tokenProvider;
    }

    @GetMapping("/public/oauth2/request")
    public ResponseEntity<Oauth2DTO> requestSSOUrl(){
        String url = "https://login.yenbai.gov.vn/oauth2/authorize?response_type=code&client_id=" + CLIENT_ID
                + "&redirect_uri=" + REDIRECT_URI + "&scope=openid";
        Oauth2DTO oauth2DTO = new Oauth2DTO();
        oauth2DTO.setUrl(url);
        return ResponseEntity.ok().body(oauth2DTO);
    }

    @GetMapping("/oauth2/ybi/callback")
    public ResponseEntity<Object> YBICallback(@RequestParam MultiValueMap<String, String> parameters) {
        log.info("GET /ybi/callback: {}", parameters);

        // TODO: Implement callback verification here
        // Get authorization code from YBI login page
        String authorizationCode = parameters.get("code").get(0);
        if(authorizationCode.isEmpty()){
            return ResponseEntity.badRequest().body(null);
        }
        // call API to get Access Token
        MultiValueMap<String, String> authorizationParameters = generateAuthorizationParameters(authorizationCode);
        YBIAccessTokenDTO ybiAccessTokenDT = restTemplateHelper.execute(API_GET_TOKEN, HttpMethod.POST, authorizationParameters, YBIAccessTokenDTO.class);
        if(ybiAccessTokenDT == null){
            return ResponseEntity.badRequest().body(null);
        }
        // call API to get user info
        HttpHeaders headers = generateHeaders(ybiAccessTokenDT.getAccessToken());
        YBIUserInfoDTO ybiUserInfoDTO = restTemplateHelper.executeWithPredefinedHeader(API_GET_USER_INFO, HttpMethod.GET, headers, null, YBIUserInfoDTO.class);
        if(ybiUserInfoDTO == null){
            return ResponseEntity.badRequest().body(null);
        }
        // check if user with the email existed in LK database
        Optional<User> optionalUser = this.userService.findByEmail(ybiUserInfoDTO.getEmail());
        if(!optionalUser.isPresent()){
            UserDTO userDTO = mapYbiUserInfoToUser(ybiUserInfoDTO);
            optionalUser = Optional.of(userService.createUser(userDTO));
        }
        Authentication authentication = new UsernamePasswordAuthenticationToken(optionalUser.get().getLogin(), null,
                AuthorityUtils.createAuthorityList(Utils.getAuthoritiesNames(optionalUser.get().getAuthorities())));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.createToken(authentication, false);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Location",REDIRECT_HEADER_VALUE + "jwt="+jwt+"&ybiaccesstoken="+ ybiAccessTokenDT.getAccessToken()
        + "&ybiidtoken=" + ybiAccessTokenDT.getIdToken());
        httpHeaders.add(JWTFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);
        return new ResponseEntity<>(new JWTToken(jwt), httpHeaders, HttpStatus.MOVED_PERMANENTLY);
    }

    private MultiValueMap<String, String> generateAuthorizationParameters(String authorizationCode){
        MultiValueMap<String, String> authorizationParameters = new LinkedMultiValueMap<>();
        authorizationParameters.set("grant_type", "authorization_code");
        authorizationParameters.set("code", authorizationCode);
        authorizationParameters.set("redirect_uri", REDIRECT_URI);
        authorizationParameters.set("client_id", CLIENT_ID);
        authorizationParameters.set("client_secret", CLIENT_SECRET);
        return authorizationParameters;
    }

    private HttpHeaders generateHeaders(String accessToken){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setBearerAuth(accessToken);
        return  headers;
    }

    private UserDTO mapYbiUserInfoToUser(YBIUserInfoDTO ybiUserInfoDTO){
        UserDTO userDTO = new UserDTO();
        if (!StrUtil.isBlank(ybiUserInfoDTO.getEmail())) {
            userDTO.setLogin(ybiUserInfoDTO.getEmail().split("@")[0]);
            userDTO.setEmail(ybiUserInfoDTO.getEmail());
        }
        userDTO.setPassword(RandomStringUtils.randomAlphanumeric(10));
        return userDTO;
    }

    @PostMapping("/oauth2/revoke/{token}")
    public ResponseEntity<Object> revokeYBIToken(@PathVariable String token){
        if(token == null || token.isEmpty()){
            return ResponseEntity.noContent().build();
        }
        MultiValueMap<String, String> revokeParameters = generateRevokeTokenBody(token);
        restTemplateHelper.execute(API_REVOKE_TOKEN, HttpMethod.POST, revokeParameters, null);
        return ResponseEntity.ok().build();
    }

    private MultiValueMap<String, String> generateRevokeTokenBody(String token){
        MultiValueMap<String, String> revokeParameters = new LinkedMultiValueMap<>();
        revokeParameters.set("token", token);
        revokeParameters.set("token_type_hint", "access_token");
        revokeParameters.set("client_id", CLIENT_ID);
        revokeParameters.set("client_secret", CLIENT_SECRET);
        return revokeParameters;
    }

    @GetMapping("/oauth2/logout/{idToken}")
    public ResponseEntity<Object> logoutSSO(@PathVariable String idToken){
        if(idToken == null || idToken.isEmpty()){
            return ResponseEntity.noContent().build();
        }
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.set("id_token_hint", idToken);
        try{
            restTemplateHelper.execute(API_LOGOUT_OIDC, HttpMethod.GET, parameters, null);
        }catch (Exception ex){

        }
        return ResponseEntity.ok().build();
    }
}
