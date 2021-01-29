package com.github.joselion.webfluxsecuritydemo.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
public class AuthManager implements ReactiveAuthenticationManager {

  @Autowired
  private UserDetailsService userDetailsService;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Override
  public Mono<Authentication> authenticate(final Authentication authentication) {
    if (authentication.isAuthenticated()) {
      return Mono.just(authentication);
    }

    return ReactiveSecurityContextHolder.getContext()
      .map(SecurityContext::getAuthentication)
      .flatMap(auth -> auth.isAuthenticated()
        ? Mono.just(auth)
        : Mono.empty()
      )
      .switchIfEmpty(
        userDetailsService.findByUsername(authentication.getName())
          .switchIfEmpty(raiseBadCredentials("Username does not exist!"))
          .filter(userDetails ->
            passwordEncoder.matches(
              (String) authentication.getCredentials(),
              userDetails.getPassword()
            )
          )
          .switchIfEmpty(raiseBadCredentials("Incorrect password!"))
          .map(userDetails ->
            new UsernamePasswordAuthenticationToken(
              userDetails.getUsername(),
              userDetails.getPassword(),
              userDetails.getAuthorities()
            )
          )
      );
  }

  private <T> Mono<T> raiseBadCredentials(final String message) {
    return Mono.error(new BadCredentialsException(message));
  }
}
