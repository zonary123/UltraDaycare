package com.kingpixel.ultradaycare.mechanics.pokemon;

import com.cobblemon.mod.common.api.pokeball.PokeBalls;
import com.cobblemon.mod.common.api.pokemon.egg.EggGroup;
import com.cobblemon.mod.common.pokeball.PokeBall;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.ultradaycare.mechanics.Mechanics;
import com.kingpixel.ultradaycare.models.EggBuilder;
import com.kingpixel.ultradaycare.models.HatchBuilder;
import com.kingpixel.ultradaycare.UltraDaycare;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Carlos Varas Alonso - 11/03/2025 9:09
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DayCarePokeBall extends Mechanics {
  public static final String TAG = "pokeball";
  private static final String DEFAULT_POKEBALL = "cobblemon:poke_ball";
  private boolean viewPokeball = true;
  private Set<String> blacklistedBalls = new HashSet<>(List.of("cobblemon:master_ball", "cobblemon:cherish_ball"));

  private boolean isDitto(Pokemon pokemon) {
    return pokemon != null && pokemon.getForm() != null && pokemon.getForm().getEggGroups().contains(EggGroup.DITTO);
  }

  private boolean isNonInheritableBall(Identifier id) {
    if (id == null) return true;
    if (blacklistedBalls == null) return false;
    return blacklistedBalls.contains(id.toString().toLowerCase()) || blacklistedBalls.contains(id.getPath().toLowerCase());
  }

  private Identifier getInheritedBall(Pokemon male, Pokemon female) {
    Identifier defaultBall = Identifier.of(DEFAULT_POKEBALL);
    if (female == null && male == null) return defaultBall;
    if (female == null) return getValidBall(male, defaultBall);
    if (male == null) return getValidBall(female, defaultBall);

    boolean femaleIsDitto = isDitto(female);
    boolean maleIsDitto = isDitto(male);

    if (femaleIsDitto && !maleIsDitto) {
      return getValidBall(male, defaultBall);
    }
    if (maleIsDitto && !femaleIsDitto) {
      return getValidBall(female, defaultBall);
    }

    if (!femaleIsDitto && !maleIsDitto) {
      boolean sameSpecies = female.getSpecies().showdownId().equalsIgnoreCase(male.getSpecies().showdownId());
      if (sameSpecies && Math.random() < 0.5) {
        return getValidBall(male, defaultBall);
      }
    }

    return getValidBall(female, defaultBall);
  }

  private Identifier getValidBall(Pokemon pokemon, Identifier defaultBall) {
    if (pokemon == null || pokemon.getCaughtBall() == null) return defaultBall;
    Identifier ballId = pokemon.getCaughtBall().getName();
    return isNonInheritableBall(ballId) ? defaultBall : ballId;
  }

  @Override
  public void applyEgg(EggBuilder builder) {
    if (builder == null || builder.getEgg() == null) return;
    Identifier id = getInheritedBall(builder.getMale(), builder.getFemale());
    PokeBall pokeBall = PokeBalls.getPokeBall(id);
    if (pokeBall != null && viewPokeball) {
      builder.getEgg().setCaughtBall(pokeBall);
    }
    builder.getEgg().getPersistentData().putString(TAG, id.toString());
  }

  @Override
  public String replace(String text, ServerPlayerEntity player) {
    if (text == null) return "";
    return text.replace("%pokeball%", isActive() ? CobbleUtils.language.getYes() : CobbleUtils.language.getNo());
  }

  @Override
  public void applyHatch(HatchBuilder builder) {
    if (builder == null || builder.getEgg() == null || builder.getPokemon() == null) return;
    Pokemon egg = builder.getEgg();
    Pokemon pokemon = builder.getPokemon();
    String ball = egg.getPersistentData().getString(TAG);
    if (ball != null && !ball.isEmpty()) {
      try {
        PokeBall pokeBall = PokeBalls.getPokeBall(Identifier.of(ball));
        if (pokeBall != null) {
          pokemon.setCaughtBall(pokeBall);
        }
      } catch (Exception e) {
        UltraDaycare.LOGGER.error("Error setting caught ball on hatch: ", e);
      }
    }
    egg.getPersistentData().remove(TAG);
  }

  @Override
  public void createEgg(ServerPlayerEntity player, Pokemon pokemon, Pokemon egg) {
    if (egg == null) return;
    Identifier defaultBall = Identifier.of(DEFAULT_POKEBALL);
    Identifier id = (pokemon == null) ? defaultBall : getValidBall(pokemon, defaultBall);
    PokeBall pokeBall = PokeBalls.getPokeBall(id);
    if (pokeBall != null && viewPokeball) {
      egg.setCaughtBall(pokeBall);
    }
    egg.getPersistentData().putString(TAG, id.toString());
  }

  @Override
  public String getEggInfo(String s, NbtCompound nbt) {
    if (s == null) return "";
    if (nbt == null) return s;
    return s.replace("%pokeball%", nbt.getString(TAG));
  }

  @Override
  public void validateData() {
    if (blacklistedBalls == null) {
      blacklistedBalls = new HashSet<>(List.of("cobblemon:master_ball", "cobblemon:cherish_ball"));
    } else {
      Set<String> normalized = new HashSet<>();
      for (String ball : blacklistedBalls) {
        if (ball != null) {
          normalized.add(ball.toLowerCase());
        }
      }
      blacklistedBalls = normalized;
    }
  }

  @Override
  public String fileName() {
    return TAG;
  }
}
