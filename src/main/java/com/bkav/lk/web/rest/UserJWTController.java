package com.bkav.lk.web.rest;

import com.bkav.lk.domain.User;
import com.bkav.lk.dto.GoogleDTO;
import com.bkav.lk.dto.UserDTO;
import com.bkav.lk.dto.YBIAccessTokenDTO;
import com.bkav.lk.dto.YBIUserInfoDTO;
import com.bkav.lk.security.jwt.JWTFilter;
import com.bkav.lk.security.jwt.JWTToken;
import com.bkav.lk.security.jwt.TokenProvider;
import com.bkav.lk.service.UserService;
import com.bkav.lk.service.util.RestTemplateHelper;
import com.bkav.lk.service.util.Utils;
import com.bkav.lk.util.Constants;
import com.bkav.lk.util.StrUtil;
import com.bkav.lk.web.rest.vm.GoogleLoginVM;
import com.bkav.lk.web.rest.vm.LoginVM;
import com.bkav.lk.web.rest.vm.SocialLoginVM;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.json.JsonObject;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller to authenticate users.
 */
@RestController
@RequestMapping("/api")
public class UserJWTController {
    private final TokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final UserService userService;
    private final RestTemplateHelper restTemplateHelper;

    @Value("${ybi-sso.api-get-token}")
    private String API_GET_TOKEN;

    @Value("${ybi-sso.api-get-user-info}")
    private String API_GET_USER_INFO;

    @Value("${ybi-sso.client-id}")
    private String CLIENT_ID;

    @Value("${ybi-sso.client-secret}")
    private String CLIENT_SECRET;

    public UserJWTController(TokenProvider tokenProvider, AuthenticationManagerBuilder authenticationManagerBuilder,
                             UserService userService,
                             RestTemplateHelper restTemplateHelper) {
        this.tokenProvider = tokenProvider;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.userService = userService;
        this.restTemplateHelper = restTemplateHelper;
    }

    @PostMapping("/authenticate")
    public ResponseEntity<JWTToken> authorize(@Valid @RequestBody LoginVM loginVM) {
        Optional<User> optionalUser = this.userService.findByLogin(loginVM.getUsername().trim());
        if (!optionalUser.isPresent() || !Constants.ENTITY_STATUS.ACTIVE.equals(optionalUser.get().getStatus())) {
            throw new BadCredentialsException("Bad credentials");
        }
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginVM.getUsername().trim(), loginVM.getPassword().trim());
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        boolean rememberMe = (loginVM.isRememberMe() == null) ? false : loginVM.isRememberMe();
        String jwt = tokenProvider.createToken(authentication, rememberMe);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(JWTFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);
        return new ResponseEntity<>(new JWTToken(jwt), httpHeaders, HttpStatus.OK);
    }

    @PostMapping("/authenticate/sso")
    public ResponseEntity<JWTToken> authorizeSSO(@Valid @RequestBody LoginVM loginVM){
        MultiValueMap<String, String> authorizationParameters = generateAuthorizationParameters(loginVM.getUsername(), loginVM.getPassword());
        YBIAccessTokenDTO ybiAccessTokenDT = restTemplateHelper.execute(API_GET_TOKEN, HttpMethod.POST, authorizationParameters, YBIAccessTokenDTO.class);
        if(ybiAccessTokenDT == null){
            throw new BadCredentialsException("Bad credentials");
        }
        // call API to get user info
        HttpHeaders headers = generateHeaders(ybiAccessTokenDT.getAccessToken());
        YBIUserInfoDTO ybiUserInfoDTO = restTemplateHelper.executeWithPredefinedHeader(API_GET_USER_INFO, HttpMethod.GET, headers, null, YBIUserInfoDTO.class);
        if(ybiUserInfoDTO == null){
            throw new BadCredentialsException("Bad credentials");
        }
        Optional<User> optionalUser = this.userService.findByEmail(ybiUserInfoDTO.getEmail());
        if(!optionalUser.isPresent()){
            UserDTO userDTO = mapYbiUserInfoToUser(ybiUserInfoDTO, loginVM.getPassword());
            optionalUser = Optional.of(userService.createUser(userDTO));
        }
        Authentication authentication = new UsernamePasswordAuthenticationToken(optionalUser.get().getLogin(), null,
                AuthorityUtils.createAuthorityList(Utils.getAuthoritiesNames(optionalUser.get().getAuthorities())));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.createToken(authentication, false);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(JWTFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);
        return new ResponseEntity<>(new JWTToken(jwt), httpHeaders, HttpStatus.OK);
    }

    private MultiValueMap<String, String> generateAuthorizationParameters(String username, String password){
        MultiValueMap<String, String> authorizationParameters = new LinkedMultiValueMap<>();
        authorizationParameters.set("grant_type", "password");
        authorizationParameters.set("username", username);
        authorizationParameters.set("password", password);
        authorizationParameters.set("client_id", CLIENT_ID);
        authorizationParameters.set("client_secret", CLIENT_SECRET);
        authorizationParameters.set("scope", "openid");
        return authorizationParameters;
    }

    private HttpHeaders generateHeaders(String accessToken){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setBearerAuth(accessToken);
        return  headers;
    }

    private UserDTO mapYbiUserInfoToUser(YBIUserInfoDTO ybiUserInfoDTO, String password){
        UserDTO userDTO = new UserDTO();
        if (!StrUtil.isBlank(ybiUserInfoDTO.getEmail())) {
            userDTO.setLogin(ybiUserInfoDTO.getEmail().split("@")[0]);
            userDTO.setEmail(ybiUserInfoDTO.getEmail());
        }
        userDTO.setPassword(password);
        return userDTO;
    }

    @PostMapping("/authenticate/facebook")
    public ResponseEntity<JWTToken> authorizeByFacebookAccessToken(@Valid @RequestBody SocialLoginVM socialLoginVM) {
        FacebookClient facebookClient = new DefaultFacebookClient(socialLoginVM.getAccessToken(), Version.LATEST);
        UserDTO user = facebookClient.fetchObject("me", UserDTO.class, Parameter.with("fields", "id,name,email"));
        Optional<User> optionalUser = this.userService.findByLogin(user.getLogin());
        if (!optionalUser.isPresent()) {
            optionalUser = this.userService.findByEmail(user.getEmail());
            if (!optionalUser.isPresent()) {
                JsonObject jsonObject = facebookClient.fetchObject("/" + user.getLogin() + "/picture", JsonObject.class,
                        Parameter.with("type", "large"), Parameter.with("redirect", "false"));
                user.setAvatar(jsonObject.get("data").asObject().get("url").toString());
                user.setPassword(RandomStringUtils.randomAlphanumeric(10));
                optionalUser = Optional.of(userService.createUser(user));
            }
        }

        Authentication authentication = new UsernamePasswordAuthenticationToken(optionalUser.get().getLogin(), null,
                AuthorityUtils.createAuthorityList(Utils.getAuthoritiesNames(optionalUser.get().getAuthorities())));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        boolean rememberMe = (socialLoginVM.getRememberMe() == null) ? false : socialLoginVM.getRememberMe();
        String jwt = tokenProvider.createToken(authentication, rememberMe);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(JWTFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);
        return new ResponseEntity<>(new JWTToken(jwt), httpHeaders, HttpStatus.OK);
    }

    @PostMapping("/authenticate/google")
    public ResponseEntity<JWTToken> authorizeByGoogleAccessToken(@Valid @RequestBody GoogleLoginVM googleLoginVM) {
        String link = "https://oauth2.googleapis.com/tokeninfo?id_token=" + googleLoginVM.getIdToken();
        GoogleDTO googleDTO= restTemplateHelper.execute(
                link, HttpMethod.GET, null, GoogleDTO.class);
        if (googleDTO != null) {
            // Map from googleDTO to UserDTO
            UserDTO userDTO = mapperGoogleToDtoUserDto(googleDTO);
            return authenticate(userDTO, googleLoginVM.getRememberMe());
        }

        return null;
    }

    private ResponseEntity<JWTToken> authenticate(UserDTO userDTO, Boolean isRememberMe) {
        User user = userService.authenticationSocialUser(userDTO);
        List<GrantedAuthority> authorities = user.getAuthorities()
                .stream()
                .map(authority -> new SimpleGrantedAuthority(authority.getName()))
                .collect(Collectors.toList());
        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getLogin(), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        boolean rememberMe = isRememberMe != null && isRememberMe;
        String jwt = tokenProvider.createToken(authentication, rememberMe);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(JWTFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);
        return new ResponseEntity<>(new JWTToken(jwt), httpHeaders, HttpStatus.OK);
    }

    private UserDTO mapperGoogleToDtoUserDto(GoogleDTO dto) {
        UserDTO userDTO = new UserDTO();
        if (!StrUtil.isBlank(dto.getName())) {
            userDTO.setName(dto.getName());
        }
        if (!StrUtil.isBlank(dto.getEmail())) {
            userDTO.setLogin(dto.getEmail().split("@")[0]);
            userDTO.setEmail(dto.getEmail());
        } else if (!StrUtil.isBlank(userDTO.getName())) {
            String login = StrUtil.removeAccent(dto.getName());
            login = login.replace(" ", "_").toLowerCase();
            userDTO.setLogin(login);
        } else {
            userDTO.setLogin(String.format("%s_%s", Constants.SOCIAL_TYPE.GOOGLE, dto.getSub()));
        }

        if (!StrUtil.isBlank(dto.getPicture())) {
            userDTO.setAvatar(dto.getPicture());
        }

        userDTO.setSocialId(dto.getSub());
        userDTO.setSocialType(Constants.SOCIAL_TYPE.GOOGLE);
        return userDTO;
    }

}
