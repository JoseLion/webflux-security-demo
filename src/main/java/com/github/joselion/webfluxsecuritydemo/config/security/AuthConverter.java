package com.github.joselion.webfluxsecuritydemo.config.security;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.function.Predicate.not;

import java.util.Optional;

import com.github.joselion.maybe.Maybe;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.util.Base64Utils;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

public class AuthConverter implements ServerAuthenticationConverter {

  protected static final String AUTH_HEADER = "Authentication";

  protected static final String BEARER = "Bearer";

  @Override
  public Mono<Authentication> convert(final ServerWebExchange exchange) {
    final Optional<String[]> headerParts = Optional.ofNullable(exchange)
      .map(ServerWebExchange::getRequest)
      .map(ServerHttpRequest::getHeaders)
      .map(it -> it.getFirst(AUTH_HEADER))
      .map(it -> it.split(" "))
      .filter(it -> it.length == 2);
    final String bearer = headerParts
      .map(it -> it[0])
      .orElse("");
    final String credentials = headerParts
      .map(it -> it[1])
      .orElse("");

    if (!bearer.equals(BEARER)) {
      return this.raiseBadCredentials("Invalid credentials bearer!");
    }

    return Mono.just(credentials)
      .filter(not(String::isBlank))
      .switchIfEmpty(this.raiseBadCredentials("Missing credentials!"))
      .map(it ->
        Maybe.resolve(() -> Base64Utils.decodeFromString(it))
          .orDefault("".getBytes())
      )
      .map(it -> new String(it, UTF_8))
      .filter(not(String::isBlank))
      .switchIfEmpty(this.raiseBadCredentials("Invalid credentials format!"))
      .map(it -> it.split(":"))
      .filter(it -> it.length == 2)
      .map(it -> Tuples.of(it[0], it[1]))
      .filter(it -> !it.getT1().isBlank() || !it.getT2().isBlank())
      .switchIfEmpty(this.raiseBadCredentials("Invalid credentials!"))
      .map(it -> new UsernamePasswordAuthenticationToken(it.getT1(), it.getT2()));
  }

  private <T> Mono<T> raiseBadCredentials(final String message) {
    return Mono.error(new BadCredentialsException(message));
  }
}
