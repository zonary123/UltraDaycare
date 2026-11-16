package com.kingpixel.ultradaycare.api.model;

import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Read-only view model representing a breeding plot in the Daycare.
 */
@Data
@Builder
public class PlotView {

  private final int plotIndex;

  private final boolean unlocked;

  @Nullable
  private final DaycarePokemonView male;

  @Nullable
  private final DaycarePokemonView female;

  private final double malePendingXp;

  private final double femalePendingXp;

  private final boolean hasEggs;

  private final int eggsCount;

  private final int maxEggs;

  private final boolean breedingPaid;

  private final long timeToHatchMillis;

  @NotNull
  private final String formattedCooldown;

  @NotNull
  private final List<DaycareEggView> eggs;
}
