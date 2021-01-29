package com.github.joselion.webfluxsecuritydemo.config.security;

import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.security.config.web.server.SecurityWebFiltersOrder.AUTHENTICATION;
import static org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.web.server.WebSession;

import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

  @Autowired
  private AuthManager authManager;

  @Bean
  public SecurityWebFilterChain securityWebFilterChain(final ServerHttpSecurity http) {
    return http
      .authorizeExchange()
        .pathMatchers("/public/**", "/login", "/logout")
          .permitAll()
        .anyExchange()
          .authenticated()
        .and()
      .csrf()
        .disable()
      .httpBasic()
        .disable()
      .formLogin()
        .disable()
      .logout()
        .requiresLogout(pathMatchers(POST, "/logout"))
        .logoutHandler((filterExchange, authentication) ->
          filterExchange.getExchange().getSession()
            .flatMap(WebSession::invalidate)
        )
        .logoutSuccessHandler((filterExchange, authentication) -> {
          final var response = filterExchange.getExchange().getResponse();
          response.setStatusCode(NO_CONTENT);

          return response.setComplete();
        })
        .and()
      .exceptionHandling()
        .authenticationEntryPoint((exchange, ex) -> {
          final var response = exchange.getResponse();
          response.setStatusCode(UNAUTHORIZED);

          return response.setComplete();
        })
        .and()
      .addFilterAt(this.authenticationWebFilter(), AUTHENTICATION)
      .build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(10);
  }

  private AuthenticationWebFilter authenticationWebFilter() {
    final var filter = new AuthenticationWebFilter(authManager);
    filter.setSecurityContextRepository(new WebSessionServerSecurityContextRepository());
    filter.setServerAuthenticationConverter(new AuthConverter());
    filter.setRequiresAuthenticationMatcher(pathMatchers(POST, "/login"));
    filter.setAuthenticationSuccessHandler((filterExchange, authentication) -> {
      final var response = filterExchange.getExchange().getResponse();
      response.setStatusCode(NO_CONTENT);

      return filterExchange.getExchange().getSession()
        .doOnNext(session -> response.getHeaders().add("Session", session.getId()))
        .flatMap(it -> response.setComplete());
    });
    filter.setAuthenticationFailureHandler((filterExchange, denied) -> {
      final var response = filterExchange.getExchange().getResponse();
      response.setStatusCode(UNAUTHORIZED);

      return response.writeWith(
        Mono.just(denied)
          .map(AuthenticationException::getMessage)
          .map(String::getBytes)
          .map(response.bufferFactory()::wrap)
      );
    });

    return filter;
  }
}
