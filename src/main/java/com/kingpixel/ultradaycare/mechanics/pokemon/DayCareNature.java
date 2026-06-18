package com.kingpixel.ultradaycare.mechanics.pokemon;

import com.kingpixel.ultradaycare.mechanics.Mechanics;

import com.cobblemon.mod.common.CobblemonItems;
import com.cobblemon.mod.common.api.pokemon.Natures;
import com.cobblemon.mod.common.pokemon.Nature;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.util.PokemonUtils;
import com.kingpixel.cobbleutils.util.Utils;
import com.kingpixel.ultradaycare.UltraDaycare;
import com.kingpixel.ultradaycare.models.EggBuilder;
import com.kingpixel.ultradaycare.models.HatchBuilder;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.List;

/**
 * @author Carlos Varas Alonso - 31/01/2025 0:57
 */
@lombok.Getter
@lombok.Setter
@lombok.EqualsAndHashCode(callSuper = true)
public class DayCareNature extends Mechanics {
  public static final String TAG = "nature";
  private float percentageEverstone;

  public DayCareNature() {
    this.percentageEverstone = 100F;
  }

  private Pokemon getEverstoneParent(List<Pokemon> parents) {
    for (Pokemon parent : parents) {
      if (parent != null && parent.heldItem().getItem().equals(CobblemonItems.EVERSTONE)) {
        return parent;
      }
    }
    return null;
  }

  @Override
  public void applyEgg(EggBuilder builder) {
    Pokemon egg = builder.getEgg();
    List<Pokemon> parents = builder.getParents();
    Pokemon everstoneParent = getEverstoneParent(parents);

    if (everstoneParent != null && Utils.getRandom().nextFloat() * 100 < percentageEverstone) {
      applyNature(everstoneParent.getNature(), egg);
    } else {
      applyNature(Natures.getRandomNature(), egg);
    }
  }

  @Override
  public void applyHatch(HatchBuilder builder) {
    Pokemon egg = builder.getEgg();
    Pokemon pokemon = builder.getPokemon();
    String s = egg.getPersistentData().getString(TAG);
    if (s.isEmpty()) {
      s = egg.getPersistentData().getString("Nature");
    }
    if (!s.isEmpty()) {
      Nature nature = getNature(s);
      if (nature == null) {
        UltraDaycare.LOGGER.error("Invalid nature: " + s);
        nature = Natures.getRandomNature();
      }
      pokemon.setNature(nature);
    } else {
      pokemon.setNature(Natures.getRandomNature());
    }
    egg.getPersistentData().remove(TAG);
    egg.getPersistentData().remove("Nature");
  }

  @Override
  public void createEgg(ServerPlayerEntity player, Pokemon pokemon, Pokemon egg) {
    applyNature(pokemon.getNature(), egg);
  }

  @Override
  public String getEggInfo(String s, NbtCompound nbt) {
    String natureStr = nbt.getString(TAG);
    if (natureStr.isEmpty()) {
      natureStr = nbt.getString("Nature");
    }
    if (natureStr.isEmpty()) {
      natureStr = nbt.getString(TAG);
    }
    Nature nature = getNature(natureStr);
    if (nature == null)
      return s;
    return s.replace("%nature%", PokemonUtils.getNatureTranslate(nature));
  }

  @Override
  public void validateData() {
    // TODO document why this method is empty
  }

  @Override
  public String fileName() {
    return "nature";
  }

  @Override
  public String replace(String text, ServerPlayerEntity player) {
    return text
      .replace("%everstone%", String.format("%.2f", percentageEverstone));
  }

  private void applyNature(Nature nature, Pokemon egg) {
    egg.setNature(nature);
    egg.getPersistentData().putString(TAG, nature.getName().toString());
  }

  private Nature getNature(String name) {
    if (name.isEmpty()) {
      if (UltraDaycare.config.isDebug()) {
        UltraDaycare.LOGGER.error("Nature name is empty");
      }
      return null;
    }
    Nature nature = Natures.getNature(Identifier.of(name));
    if (nature == null) {

      nature = Natures.getNature(Identifier.of(name.toLowerCase()));
    }
    if (nature == null) {
      nature = Natures.getRandomNature();
    }
    return nature;
  }
}
