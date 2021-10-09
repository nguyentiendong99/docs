package com.bkav.lk.web.rest;

import com.bkav.lk.dto.UserDTO;
import com.bkav.lk.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * REST controller for managing the current user's account.
 */
@RestController
@RequestMapping("/api")
public class AccountResource {
    private static class AccountResourceException extends RuntimeException {
        private AccountResourceException(String message) {
            super(message);
        }
    }

    private final Logger log = LoggerFactory.getLogger(AccountResource.class);
    private final UserService userService;

    public AccountResource(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/authenticate")
    public String isAuthenticated(HttpServletRequest request) {
        log.debug("REST request to check if the current user is authenticated");
        return request.getRemoteUser();
    }

    @GetMapping("/account")
    public UserDTO getAccount() {
        return userService.getUserWithAuthorities().map(UserDTO::new).orElseThrow(() -> new AccountResourceException(
                "User could not be found"));
    }

}
