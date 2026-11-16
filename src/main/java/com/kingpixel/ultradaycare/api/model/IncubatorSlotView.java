package com.kingpixel.ultradaycare.api.model;

import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Read-only view model representing an incubator slot.
 */
@Data
@Builder
public class IncubatorSlotView {

  private final int slotIndex;

  private final boolean unlocked;

  private final boolean hasIncubator;

  private final int remainingUses;

  @Nullable
  private final String caretakerName;

  @Nullable
  private final DaycarePokemonView caretaker;

  @Nullable
  private final DaycareEggView egg;

  private final int progressPercentage;

  private final boolean readyToHatch;

  private final double speedMultiplier;
}
