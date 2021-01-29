package com.github.joselion.webfluxsecuritydemo.config;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class Router {

  @Bean
  public RouterFunction<ServerResponse> apiRouter() {
    return nest(
      path("/api"),
      route(GET("/greet"), request -> ok().bodyValue("👋 Quito Lambda!"))
    );
  }

  @Bean
  public RouterFunction<ServerResponse> publicRouter() {
    return nest(
      path("/public"),
      route(GET("/version"), request -> ok().bodyValue("v0.0.1"))
    );
  }
}
