package com.github.joselion.webfluxsecuritydemo.helpers;

import static java.time.temporal.ChronoUnit.MICROS;
import static lombok.AccessLevel.PRIVATE;

import java.time.Clock;
import java.time.Duration;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public class Commons {

  public static final Clock CLOCK = Clock.tick(
    Clock.systemDefaultZone(),
    Duration.of(1, MICROS)
  );
}
