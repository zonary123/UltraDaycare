package com.kingpixel.ultradaycare.api;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.util.PlayerUtils;
import com.kingpixel.cobbleutils.util.TypeMessage;
import com.kingpixel.ultradaycare.UltraDaycare;
import com.kingpixel.ultradaycare.models.Plot;
import com.kingpixel.ultradaycare.models.User;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * PokeMMO style daycare: breeding is an active trigger that consumes both parents to instantly produce an egg.
 */
public class PokeMMODaycareMode implements DaycareMode {
  public static final String ID = "pokemmo";

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public boolean isPassiveBreedingEnabled() {
    return false;
  }

  @Override
  public boolean consumeParents() {
    return true;
  }

  @Override
  public void onBreed(ServerPlayerEntity player, Plot plot, User user) {
    if (!plot.hasTwoParents()) {
      PlayerUtils.sendMessage(
        player,
        "&cYou need both parents in the plot to breed!",
        UltraDaycare.language.getPrefix(),
        TypeMessage.CHAT
      );
      return;
    }

    // Generate egg based on current parents
    Pokemon egg = plot.createEgg(player);

    // Sacrifice/consume parents
    plot.setMale(null);
    plot.setFemale(null);

    // Add egg to plot
    plot.getEggs().add(egg);
    plot.setEggProducedSincePayment(true);
    plot.setBreedingPaid(false);

    PlayerUtils.sendMessage(
      player,
      "&aBreeding succeeded! Both parents were consumed to produce the egg.",
      UltraDaycare.language.getPrefix(),
      TypeMessage.CHAT
    );

    user.markDirty();
    user.save();
  }
}
