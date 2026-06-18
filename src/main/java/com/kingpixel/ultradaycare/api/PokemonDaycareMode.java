package com.kingpixel.ultradaycare.api;

import com.kingpixel.ultradaycare.models.Plot;
import com.kingpixel.ultradaycare.models.User;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Standard Pokémon style daycare: breeding happens passively over time/steps, and parents remain.
 */
public class PokemonDaycareMode implements DaycareMode {
  public static final String ID = "pokemon";
  
  @Override
  public String getId() {
    return ID;
  }

  @Override
  public boolean isPassiveBreedingEnabled() {
    return true;
  }

  @Override
  public boolean consumeParents() {
    return false;
  }

  @Override
  public void onBreed(ServerPlayerEntity player, Plot plot, User user) {
    // Passive breeding handles creation based on steps/time, no active action needed.
  }
}
