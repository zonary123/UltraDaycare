package com.kingpixel.ultradaycare.api.action;

import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Result model for player daycare operations executed programmatically.
 */
@Data
@Builder
public class DaycareActionResult {

  public enum Status {
    SUCCESS,
    PLOT_NOT_FOUND,
    PLOT_LOCKED,
    NO_PARENTS,
    NO_EGGS,
    POKEMON_NOT_FOUND,
    POKEMON_IN_BATTLE,
    NOT_BREEDABLE,
    INVALID_GENDER,
    INSUFFICIENT_FUNDS,
    MAX_LEVEL_REACHED,
    INCUBATOR_NOT_FOUND,
    INCUBATOR_LOCKED,
    INCUBATOR_DAILY_LIMIT_REACHED,
    INCUBATOR_SLOT_EMPTY,
    EGG_NOT_READY,
    PARTY_FULL,
    COOLDOWN,
    PERMISSION_DENIED,
    ERROR
  }

  @NotNull
  private final Status status;
  @NotNull
  private final String message;
  private final int plotIndex;
  private final int slotIndex;
  private final int count;
  private final double cost;

  public boolean isSuccess() {
    return status == Status.SUCCESS;
  }

  public static DaycareActionResult success(@NotNull String message) {
    return DaycareActionResult.builder()
      .status(Status.SUCCESS)
      .message(message)
      .plotIndex(-1)
      .slotIndex(-1)
      .count(0)
      .cost(0.0)
      .build();
  }

  public static DaycareActionResult successPlot(@NotNull String message, int plotIndex) {
    return DaycareActionResult.builder()
      .status(Status.SUCCESS)
      .message(message)
      .plotIndex(plotIndex)
      .slotIndex(-1)
      .count(0)
      .cost(0.0)
      .build();
  }

  public static DaycareActionResult successIncubator(@NotNull String message, int slotIndex) {
    return DaycareActionResult.builder()
      .status(Status.SUCCESS)
      .message(message)
      .plotIndex(-1)
      .slotIndex(slotIndex)
      .count(0)
      .cost(0.0)
      .build();
  }

  public static DaycareActionResult successCount(@NotNull String message, int count) {
    return DaycareActionResult.builder()
      .status(Status.SUCCESS)
      .message(message)
      .plotIndex(-1)
      .slotIndex(-1)
      .count(count)
      .cost(0.0)
      .build();
  }

  public static DaycareActionResult failure(@NotNull Status status, @NotNull String message) {
    return DaycareActionResult.builder()
      .status(status)
      .message(message)
      .plotIndex(-1)
      .slotIndex(-1)
      .count(0)
      .cost(0.0)
      .build();
  }

  public static DaycareActionResult failure(@NotNull Status status, @NotNull String message, int plotIndex) {
    return DaycareActionResult.builder()
      .status(status)
      .message(message)
      .plotIndex(plotIndex)
      .slotIndex(-1)
      .count(0)
      .cost(0.0)
      .build();
  }
}
