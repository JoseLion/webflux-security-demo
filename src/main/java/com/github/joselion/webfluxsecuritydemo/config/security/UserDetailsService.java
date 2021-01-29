package com.github.joselion.webfluxsecuritydemo.config.security;

import com.github.joselion.webfluxsecuritydemo.models.account.AccountRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
public class UserDetailsService implements ReactiveUserDetailsService {

  @Autowired
  private AccountRepository accountRepo;

  @Override
  public Mono<UserDetails> findByUsername(final String username) {
    return accountRepo.findByUsername(username)
      .map(account ->
        User.builder()
          .accountExpired(account.isExpired())
          .accountLocked(account.isLocked())
          .authorities("ROLE_ADMIN")
          .credentialsExpired(false)
          .disabled(false)
          .password(account.password())
          .username(account.username())
          .build()
      );
  }
}
