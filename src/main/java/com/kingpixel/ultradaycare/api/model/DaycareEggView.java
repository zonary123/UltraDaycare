package com.kingpixel.ultradaycare.api.model;

import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Read-only view model representing a Pokémon egg in a plot, incubator, or player storage.
 */
@Data
@Builder
public class DaycareEggView {

  @NotNull
  private final UUID eggUuid;

  @NotNull
  private final String species;

  private final boolean shiny;

  private final int currentSteps;

  private final int targetSteps;

  private final double progressPercentage;

  private final long timeToHatchMillis;

  @NotNull
  private final String formattedRemainingTime;
}
