package com.github.joselion.webfluxsecuritydemo.models.account;

import static com.github.joselion.webfluxsecuritydemo.helpers.Commons.CLOCK;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.Nullable;

import lombok.Builder;
import lombok.Getter;

@Table
@Getter
public class Account {

  @Id
  private final @Nullable Long id;

  private final OffsetDateTime createdAt;

  private final String username;

  private final @Nullable String password;

  private final Boolean isLocked;

  private final Boolean isExpired;

  @Builder(toBuilder = true)
  public Account(
    final @Nullable Long id,
    final @Nullable OffsetDateTime createdAt,
    final @Nullable String username,
    final @Nullable String password,
    final @Nullable Boolean isLocked,
    final @Nullable Boolean isExpired
  ) {
    this.id = id;
    this.createdAt = Optional.ofNullable(createdAt).orElse(OffsetDateTime.now(CLOCK));
    this.username = Optional.ofNullable(username).orElse("");
    this.password = password;
    this.isLocked = Optional.ofNullable(isLocked).orElse(false);
    this.isExpired = Optional.ofNullable(isExpired).orElse(false);
  }
}
