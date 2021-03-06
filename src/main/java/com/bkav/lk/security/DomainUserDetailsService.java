package com.bkav.lk.security;

import com.bkav.lk.domain.Group;
import com.bkav.lk.domain.User;
import com.bkav.lk.repository.UserRepository;
import com.bkav.lk.util.Constants;
import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Authenticate a user from the database.
 */
@Component("userDetailsService")
public class DomainUserDetailsService implements UserDetailsService {
    private final Logger log = LoggerFactory.getLogger(DomainUserDetailsService.class);
    private final UserRepository userRepository;

    public DomainUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(final String login) {
        log.debug("Authenticating {}", login);
        if (new EmailValidator().isValid(login, null)) {
            return userRepository.findOneWithAuthoritiesByEmailIgnoreCaseAndStatusIs(login,
                    Constants.ENTITY_STATUS.ACTIVE)
                    .map(user -> createSpringSecurityUser(login, user))
                    .orElseThrow(() -> new UsernameNotFoundException("User with email " + login + " was not found in " +
                            "the database"));
        }
        String lowercaseLogin = login.toLowerCase(Locale.ENGLISH);
        return userRepository.findOneWithAuthoritiesByLoginIgnoreCaseAndStatusIs(lowercaseLogin, Constants.ENTITY_STATUS.ACTIVE)
                .map(user -> createSpringSecurityUser(lowercaseLogin, user))
                .orElseThrow(() -> new UsernameNotFoundException("User " + lowercaseLogin + " was not found in the " +
                        "database"));
    }

    private org.springframework.security.core.userdetails.User createSpringSecurityUser(String lowercaseLogin,
                                                                                        User user) {
        if (!user.isActivated()) {
            throw new UserNotActivatedException("User " + lowercaseLogin + " was not activated");
        }
        List<GrantedAuthority> grantedAuthorities = user.getAuthorities().stream()
                .map(authority -> new SimpleGrantedAuthority(authority.getName()))
                .collect(Collectors.toList());
        //todo g??n th??m quy???n c???a nh??m ng?????i d??ng
        if(!user.getGroups().isEmpty()){
            List<GrantedAuthority> grantedAuthoritiesGroup = getListAuthorityByGroup(user.getGroups());
            if(!grantedAuthoritiesGroup.isEmpty()){
                grantedAuthorities.addAll(grantedAuthoritiesGroup);
            }
        }
        return new org.springframework.security.core.userdetails.User(user.getLogin(),
                user.getPassword(),
                grantedAuthorities);
    }

    private List<GrantedAuthority> getListAuthorityByGroup(Set<Group> groups){
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        for(Group g: groups){
            List<GrantedAuthority> authorities = g.getAuthorities().stream()
                    .map(authority -> new SimpleGrantedAuthority(authority.getName()))
                    .collect(Collectors.toList());
            grantedAuthorities.addAll(authorities);
        }
        return grantedAuthorities;
    }
}
