package com.github.joselion.webfluxsecuritydemo.models.session;

import static com.github.joselion.webfluxsecuritydemo.helpers.Commons.CLOCK;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.github.joselion.maybe.Maybe;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.util.CastUtils;
import org.springframework.lang.Nullable;
import org.springframework.session.Session;
import org.springframework.util.Base64Utils;

import lombok.Builder;

@Table("session")
public class HStoreSession implements Session {

  public static final Long DEFAULT_INTERVAL = Duration.ofDays(7L).getSeconds();

  @Id
  private UUID id;

  private UUID originalId;

  private Map<String, Object> sessionAttrs;

  private Instant creationTime;

  private Instant lastAccessedTime;

  private Long maxInactiveInterval;

  @Version
  private @Nullable Long version;

  @Builder(toBuilder = true)
  private HStoreSession(
    final @Nullable UUID id,
    final @Nullable UUID originalId,
    final @Nullable Map<String, Object> sessionAttrs,
    final @Nullable Instant creationTime,
    final @Nullable Instant lastAccessedTime,
    final @Nullable Long maxInactiveInterval,
    final @Nullable Long version
  ) {
    final UUID newId = UUID.randomUUID();
    final Instant now = Instant.now(CLOCK);

    this.id = Optional.ofNullable(id).orElse(newId);
    this.originalId = Optional.ofNullable(originalId).orElse(newId);
    this.sessionAttrs = Optional.ofNullable(sessionAttrs)
      .map(HashMap::new)
      .orElse(new HashMap<>());
    this.creationTime = Optional.ofNullable(creationTime).orElse(now);
    this.lastAccessedTime = Optional.ofNullable(lastAccessedTime).orElse(now);
    this.maxInactiveInterval = Optional.ofNullable(maxInactiveInterval).orElse(DEFAULT_INTERVAL);
    this.version = version;
  }

  public HStoreSession toMatchingIds() {
    this.originalId = this.id;
    return this;
  }

  @Override
  public String getId() {
    return this.id.toString();
  }

  @Override
  public String changeSessionId() {
    final UUID newId = UUID.randomUUID();
    this.id = newId;
    this.version = null;

    return newId.toString();
  }

  @Nullable
  @Override
  public <T> T getAttribute(final String attributeName) {
    final var attr = this.sessionAttrs.get(attributeName);

    if (isBase64(attr)) {
      return Maybe.resolve(() -> {
        final var bytes = Base64Utils.decodeFromString((String) attr);

        try (
          ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
          ObjectInputStream ois = new ObjectInputStream(bais);
        ) {
          return CastUtils.<T>cast(ois.readObject());
        }
      })
      .orDefault(null);
    }

    return CastUtils.<T>cast(attr);
  }

  @Override
  public Set<String> getAttributeNames() {
    return this.sessionAttrs.keySet();
  }

  @Override
  public void setAttribute(final String attributeName, final @Nullable Object attributeValue) {
    if (attributeValue == null) {
      this.removeAttribute(attributeName);
      return;
    }

    if (attributeValue instanceof Serializable) {
      final var hash = Maybe.resolve(() -> {
        try (
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          ObjectOutputStream oos = new ObjectOutputStream(baos);
        ) {
          oos.writeObject(attributeValue);
          return Base64Utils.encodeToString(baos.toByteArray());
        }
      })
      .orDefault("");

      this.sessionAttrs.put(attributeName, hash);
      return;
    }

    this.sessionAttrs.put(attributeName, attributeValue);
  }

  @Override
  public void removeAttribute(final String attributeName) {
    this.sessionAttrs.remove(attributeName);
  }

  @Override
  public Instant getCreationTime() {
    return this.creationTime;
  }

  @Override
  public void setLastAccessedTime(final Instant lastAccessedTime) {
    this.lastAccessedTime = lastAccessedTime;
  }

  @Override
  public Instant getLastAccessedTime() {
    return this.lastAccessedTime;
  }

  @Override
  public void setMaxInactiveInterval(final Duration interval) {
    this.maxInactiveInterval = interval.toSeconds();
  }

  @Override
  public Duration getMaxInactiveInterval() {
    return Duration.ofSeconds(this.maxInactiveInterval);
  }

  @Override
  public boolean isExpired() {
    final Duration interval = this.getMaxInactiveInterval();

    if (interval.isNegative()) {
      return false;
    }

    final var inactiveDiff = Instant.now(CLOCK)
      .minus(interval)
      .compareTo(this.lastAccessedTime);

    return inactiveDiff >= 0;
  }

  public String getOriginalId() {
    return this.originalId.toString();
  }

  public Long getVersion() {
    return this.version;
  }

  private boolean isBase64(final Object data) {
    if (data instanceof String) {
      return Maybe.resolve(() -> {
        final var decoded = Base64Utils.decodeFromString((String) data);
        return decoded.length > 0;
      })
      .orDefault(false);
    }

    return false;
  }
}
