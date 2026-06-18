package com.kingpixel.ultradaycare.api;

import com.kingpixel.ultradaycare.models.Plot;
import com.kingpixel.ultradaycare.models.User;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Defines the contract for different daycare gameplay modes (e.g. Pokémon style, PokeMMO style).
 * Allows third-party mods to implement their own custom breeding behaviors.
 */
public interface DaycareMode {
  /**
   * @return The unique identifier of this daycare mode.
   */
  String getId();

  /**
   * @return True if eggs should be generated passively over time/steps.
   */
  boolean isPassiveBreedingEnabled();

  /**
   * @return True if parents should be consumed (deleted) when breeding occurs.
   */
  boolean consumeParents();

  /**
   * Callback triggered when a player triggers the breed action in this mode.
   */
  void onBreed(ServerPlayerEntity player, Plot plot, User user);
}
