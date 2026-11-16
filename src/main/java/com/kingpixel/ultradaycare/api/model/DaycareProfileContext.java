package com.kingpixel.ultradaycare.api.model;

import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * Read-only profile context representing a player's daycare overview, plots, incubators, and stored eggs.
 */
@Data
@Builder
public class DaycareProfileContext {

  @NotNull
  private final UUID playerUuid;

  @NotNull
  private final String playerName;

  private final int totalPlots;

  private final int unlockedPlotsCount;

  private final int activeEggsInPlots;

  private final int totalIncubatorsCount;

  private final int incubatorsReadyToHatch;

  @NotNull
  private final List<PlotView> plots;

  @NotNull
  private final List<IncubatorSlotView> incubatorSlots;

  @NotNull
  private final List<DaycareEggView> storedEggs;
}
