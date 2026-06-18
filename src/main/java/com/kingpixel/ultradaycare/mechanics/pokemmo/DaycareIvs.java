package com.kingpixel.ultradaycare.mechanics.pokemmo;

import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.item.CobblemonItem;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.util.Utils;
import com.kingpixel.ultradaycare.UltraDaycare;
import com.kingpixel.ultradaycare.mechanics.Mechanics;
import com.kingpixel.ultradaycare.models.EggBuilder;
import com.kingpixel.ultradaycare.models.HatchBuilder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.cobblemon.mod.common.CobblemonItems.*;

/**
 * @author Carlos Varas Alonso - 31/01/2025 0:25
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DaycareIvs extends Mechanics {
  public static final List<Stats> stats =
    Arrays.stream(Stats.values()).filter(stats1 -> stats1 != Stats.ACCURACY && stats1 != Stats.EVASION).toList();
  private static final Map<Stats, String> oldStats = Map.of(
    Stats.HP, "HP",
    Stats.ATTACK, "Attack",
    Stats.DEFENCE, "Defense",
    Stats.SPECIAL_ATTACK, "SpecialAttack",
    Stats.SPECIAL_DEFENCE, "SpecialDefense",
    Stats.SPEED, "Speed"
  );
  private static Map<CobblemonItem, Stats> bracelets;
  private float percentagePowerItem;

  public DaycareIvs() {
    this.percentagePowerItem = 100f;
  }

  @Override
  public String replace(String text, ServerPlayerEntity player) {
    return text
      .replace("%destinyknot%", CobbleUtils.language.getNo())
      .replace("%poweritem%", String.format("%.2f", percentagePowerItem))
      .replace("%maxivs%", CobbleUtils.language.getNo())
      .replace("%defaultIvsTransfer%", CobbleUtils.language.getNo())
      .replace("%destinyKnotIvsTransfer%", CobbleUtils.language.getNo())
      .replace("%maxIvsRandom%", CobbleUtils.language.getNo());
  }

  @Override
  public void applyEgg(EggBuilder builder) {
    Pokemon male = builder.getMale();
    Pokemon female = builder.getFemale();
    Pokemon egg = builder.getEgg();

    if (bracelets == null) {
      bracelets = Map.of(
        POWER_WEIGHT, Stats.HP,
        POWER_BRACER, Stats.ATTACK,
        POWER_BELT, Stats.DEFENCE,
        POWER_ANKLET, Stats.SPEED,
        POWER_LENS, Stats.SPECIAL_ATTACK,
        POWER_BAND, Stats.SPECIAL_DEFENCE
      );
    }

    // Check held braces
    Stats maleLockedStat = null;
    if (male.heldItem().getItem() instanceof CobblemonItem item) {
      if (Utils.getRandom().nextFloat() * 100 < getPercentagePowerItem()) {
        maleLockedStat = bracelets.get(item);
      }
    }
    Stats femaleLockedStat = null;
    if (female.heldItem().getItem() instanceof CobblemonItem item) {
      if (Utils.getRandom().nextFloat() * 100 < getPercentagePowerItem()) {
        femaleLockedStat = bracelets.get(item);
      }
    }

    for (Stats stat : stats) {
      int iv;
      boolean maleLocked = (maleLockedStat == stat);
      boolean femaleLocked = (femaleLockedStat == stat);

      if (maleLocked && femaleLocked) {
        iv = Utils.getRandom().nextBoolean() ? male.getIvs().getOrDefault(stat) : female.getIvs().getOrDefault(stat);
      } else if (maleLocked) {
        iv = male.getIvs().getOrDefault(stat);
      } else if (femaleLocked) {
        iv = female.getIvs().getOrDefault(stat);
      } else {
        int maleIv = male.getIvs().getOrDefault(stat);
        int femaleIv = female.getIvs().getOrDefault(stat);

        if (maleIv == femaleIv) {
          iv = maleIv;
        } else {
          int max = Math.max(maleIv, femaleIv);
          int min = Math.min(maleIv, femaleIv);
          int avg = (maleIv + femaleIv) / 2;

          float roll = Utils.getRandom().nextFloat() * 100;
          if (roll < 40f) {
            iv = max;
          } else if (roll < 80f) {
            iv = avg;
          } else {
            iv = min;
          }
        }
      }
      applyData(egg, stat, iv);
    }
  }

  @Override
  public void applyHatch(HatchBuilder builder) {
    Pokemon egg = builder.getEgg();
    Pokemon pokemon = builder.getPokemon();
    stats.forEach(stat -> {
      int iv;
      String oldStat = oldStats.get(stat).intern();

      if (egg.getPersistentData().contains(oldStat)) {
        iv = egg.getPersistentData().getInt(oldStat);
      } else if (egg.getPersistentData().contains(stat.getShowdownId())) {
        iv = egg.getPersistentData().getInt(stat.getShowdownId());
      } else {
        iv = Utils.getRandom().nextInt(32);
      }

      pokemon.getIvs().set(stat, iv);

      egg.getPersistentData().remove(stat.getShowdownId());
      egg.getPersistentData().remove(oldStat);
    });
  }

  @Override
  public void createEgg(ServerPlayerEntity player, Pokemon pokemon, Pokemon egg) {
    stats.forEach(stat -> {
      int iv = pokemon.getIvs().getOrDefault(stat);
      applyData(egg, stat, iv);
    });
  }

  @Override
  public String getEggInfo(String s, NbtCompound nbt) {
    for (Stats stat : stats) {
      s = s.replace("%iv_" + stat.getShowdownId() + "%",
        String.valueOf(nbt.getInt(stat.getShowdownId())));
    }
    return s;
  }

  @Override
  public void validateData() {
  }

  @Override
  public String fileName() {
    return "ivs";
  }

  private void applyData(Pokemon egg, Stats stat, int iv) {
    if (UltraDaycare.config.isShowIvs()) {
      egg.getIvs().set(stat, iv);
    } else {
      egg.getIvs().set(stat, 0);
    }
    egg.getPersistentData().putInt(stat.getShowdownId(), iv);
  }
}
