package com.kingpixel.ultradaycare.mechanics.pokemon;

import com.kingpixel.ultradaycare.mechanics.Mechanics;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.api.ItemsMod;
import com.kingpixel.cobbleutils.util.Utils;
import com.kingpixel.cobbleutils.util.UtilsFile;
import com.kingpixel.ultradaycare.UltraDaycare;
import com.kingpixel.ultradaycare.models.EggBuilder;
import com.kingpixel.ultradaycare.models.HatchBuilder;
import com.kingpixel.ultradaycare.models.Incense;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Carlos Varas Alonso - 15/03/2025 20:23
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DayCareInciense extends Mechanics {
  public static String TAG_INCENSE = "incense";
  private List<Incense> incenses = new ArrayList<>();
  private static String path = UltraDaycare.PATH_MODULES + "incense/";
  private static DayCareInciense instance;

  public DayCareInciense() {
    setEnabled(true);
  }

  public static DayCareInciense INSTANCE() {
    DayCareInciense active = UltraDaycare.getActiveMechanic(DayCareInciense.class);
    if (active != null) return active;
    if (instance == null) {
      instance = new DayCareInciense();
    }
    return instance;
  }

  @Override
  public DayCareInciense getInstance() {
    return getInstance("pokemon");
  }

  @Override
  public DayCareInciense getInstance(String modeId) {
    DayCareInciense loaded = this.readFromFile(getClass(), modeId);
    if (loaded != null) {
      loaded.init(modeId);
      for (Incense incense : loaded.getIncenses()) {
        ItemsMod.addItem(UltraDaycare.MOD_ID, incense.getId(), incense.getItemStackIncense(1));
      }
      return loaded;
    }
    return null;
  }


  public Incense getIncense(String id) {
    for (Incense incense : incenses) {
      if (incense.getId().equals(id)) {
        return incense;
      }
    }
    return null;
  }

  public void init(String modeId) {
    File folder = Utils.getAbsolutePath(UltraDaycare.PATH_MODULES + modeId + "/incense/");
    if (!folder.exists()) {
      folder.mkdirs();
    }

    File[] files = folder.listFiles();
    if (files == null || files.length == 0) {
      createDefaultIncense(folder, Incense.defaultIncenses());
      files = folder.listFiles(); // Re-read the files after creating defaults
    }
    incenses.clear();
    for (File file : files) {
      if (file.getName().endsWith(".json")) {
        try {
          Incense incense = UtilsFile.read(file.toPath(), Incense.class);
          if (incense != null) {
            incense.setId(file.getName().replace(".json", ""));
            incenses.add(incense);
            UtilsFile.writeAsync(file.toPath(), incense);
          }
        } catch (Exception e) {
          UltraDaycare.LOGGER.error("Error reading incense file: ", e);
        }
      }
    }

  }

  private void createDefaultIncense(File folder, List<Incense> incenses) {
    for (Incense incense : incenses) {
      UtilsFile.writeAsync(new File(folder.getAbsolutePath() + "/" + incense.getId() + ".json").toPath(), incense).join();
    }
  }

  public Pokemon applyIncense(Pokemon pokemon) {
    if (!isActive()) return null;
    String species;
    for (Incense incense : incenses) {
      species = incense.getChild(pokemon);
      if (species != null) return PokemonProperties.Companion.parse(species).create();
    }
    return null;
  }

  @Override
  public void validateData() {
  }

  @Override
  public String fileName() {
    return "incense";
  }

  @Override
  public String replace(String text, ServerPlayerEntity player) {
    return text
      .replace("%incense%", isActive() ? CobbleUtils.language.getYes() : CobbleUtils.language.getNo());
  }

  @Override
  public void applyEgg(EggBuilder builder) {
  }

  @Override
  public void applyHatch(HatchBuilder builder) {
  }

  @Override
  public void createEgg(ServerPlayerEntity player, Pokemon pokemon, Pokemon egg) {
  }

  @Override
  public String getEggInfo(String s, NbtCompound nbt) {
    return s;
  }
}
