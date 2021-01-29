package com.github.joselion.webfluxsecuritydemo.models.account;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Mono;

public interface AccountRepository extends ReactiveCrudRepository<Account, Long> {

  Mono<Account> findByUsername(String username);
}
