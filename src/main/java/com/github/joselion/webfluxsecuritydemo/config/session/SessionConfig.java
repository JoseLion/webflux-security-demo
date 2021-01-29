package com.github.joselion.webfluxsecuritydemo.config.session;

import com.github.joselion.webfluxsecuritydemo.models.session.HStoreSession;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.ReactiveSessionRepository;
import org.springframework.session.config.annotation.web.server.EnableSpringWebSession;
import org.springframework.web.server.session.HeaderWebSessionIdResolver;
import org.springframework.web.server.session.WebSessionIdResolver;

@Configuration
@EnableSpringWebSession
public class SessionConfig {

  @Bean
  public WebSessionIdResolver webSessionIdResolver() {
    return new HeaderWebSessionIdResolver();
  }

  @Bean
  public ReactiveSessionRepository<HStoreSession> reactiveSessionRepository() {
    return new SessionRepository();
  }
}
