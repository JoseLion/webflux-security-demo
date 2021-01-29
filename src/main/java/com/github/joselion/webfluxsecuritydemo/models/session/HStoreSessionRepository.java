package com.github.joselion.webfluxsecuritydemo.models.session;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface HStoreSessionRepository extends ReactiveCrudRepository<HStoreSession, UUID> {

}
