package com.kingpixel.ultradaycare.api.model;

import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Read-only view model representing a Pokémon deposited in the Daycare or assigned as caretaker.
 */
@Data
@Builder
public class DaycarePokemonView {

  @NotNull
  private final UUID pokemonUuid;

  @NotNull
  private final String species;

  @NotNull
  private final String displayName;

  @NotNull
  private final String form;

  @NotNull
  private final String gender;

  private final int level;

  private final boolean shiny;

  @NotNull
  private final String holdingItem;

  @NotNull
  private final String nature;

  @NotNull
  private final String ability;

  @NotNull
  private final List<String> lore;
}
