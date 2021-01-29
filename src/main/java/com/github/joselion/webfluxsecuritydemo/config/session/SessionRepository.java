package com.github.joselion.webfluxsecuritydemo.config.session;

import static java.util.function.Predicate.not;

import java.util.UUID;
import java.util.function.Predicate;

import com.github.joselion.maybe.Maybe;
import com.github.joselion.webfluxsecuritydemo.models.session.HStoreSession;
import com.github.joselion.webfluxsecuritydemo.models.session.HStoreSessionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.session.ReactiveSessionRepository;
import org.springframework.session.Session;

import reactor.core.publisher.Mono;

public class SessionRepository implements ReactiveSessionRepository<HStoreSession> {

  @Autowired
  private HStoreSessionRepository hstoreSessionRepo;

  @Override
  public Mono<HStoreSession> createSession() {
    return Mono.just(HStoreSession.builder().build());
  }

  @Override
  public Mono<Void> save(final HStoreSession session) {
    return Mono.just(session.getOriginalId())
      .filter(not(session.getId()::equals))
      .flatMap(this::deleteById)
      .then(hstoreSessionRepo.save(session.toMatchingIds()))
      .then();
  }

  @Override
  public Mono<HStoreSession> findById(final String id) {
    final var uuid = Maybe.resolve(() -> UUID.fromString(id))
      .and()
      .toOptional();

    return uuid.isEmpty()
      ? Mono.empty()
      : hstoreSessionRepo.findById(uuid.get())
        .filter(Predicate.not(Session::isExpired))
        .switchIfEmpty(
          this.deleteById(id).then(Mono.empty())
        );
  }

  @Override
  public Mono<Void> deleteById(final String id) {
    final var uuid = Maybe.resolve(() -> UUID.fromString(id))
      .and()
      .toOptional();

    return uuid.isEmpty()
      ? Mono.empty()
      : hstoreSessionRepo.deleteById(uuid.get());
  }
}
