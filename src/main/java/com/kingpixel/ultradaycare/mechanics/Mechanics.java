package com.kingpixel.ultradaycare.mechanics;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.util.UtilsFile;
import com.kingpixel.ultradaycare.UltraDaycare;
import com.kingpixel.ultradaycare.models.EggBuilder;
import com.kingpixel.ultradaycare.models.HatchBuilder;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;

import java.nio.file.Path;

/**
 * Author: Carlos Varas Alonso - 31/01/2025 0:25
 */
@lombok.Data
public abstract class Mechanics {
  private Boolean enabled = true;

  public boolean isActive() {
    return enabled == null || enabled;
  }

  private String modeId;

  public Mechanics getInstance() {
    return getInstance("pokemon");
  }

  public Mechanics getInstance(String modeId) {
    this.modeId = modeId;
    return readFromFile(this.getClass(), modeId);
  }

  /*
   * This method is used to validate the data of the class that extends Mechanics.
   * It checks if the data is valid and throws an exception if it is not.
   */
  public abstract void validateData();

  /*
   * This method is used to get the file name of the class that extends Mechanics.
   * It returns the file name without the .json extension.
   */
  public abstract String fileName();

  /*
   * This method is used to get the name of the class that extends Mechanics.
   * It returns the class name without the package name.
   */
  public abstract String replace(String text, ServerPlayerEntity player);

  /*
   * This method is used to apply the egg mechanics to the egg builder.
   * It takes an EggBuilder object as a parameter and applies the mechanics to it.
   */
  public abstract void applyEgg(EggBuilder eggBuilder);

  /*
   * This method is used to apply the hatch mechanics to the egg.
   * It takes a ServerPlayerEntity object and a Pokemon object as parameters and applies the mechanics to it.
   */
  public abstract void applyHatch(HatchBuilder builder);

  /*
   * This method is used to create an egg for the player.
   * It takes a ServerPlayerEntity object, a Pokemon object, and an egg Pokemon object as parameters and creates the egg.
   */
  public abstract void createEgg(ServerPlayerEntity player, Pokemon pokemon, Pokemon egg);

  /*
   * This method is used to get the egg info.
   * It takes a String and an NbtCompound object as parameters and returns the egg info as a String.
   */
  public abstract String getEggInfo(String s, NbtCompound nbt);

  public <T> T readFromFile(Class<T> clazz) {
    return readFromFile(clazz, "pokemon");
  }

  public <T> T readFromFile(Class<T> clazz, String modeId) {
    try {
      Path path = UltraDaycare.getPath().resolve("modules").resolve(modeId).resolve(fileName() + ".json");
      T instance = UtilsFile.read(path, clazz);
      if (instance == null) {
        instance = clazz.getDeclaredConstructor().newInstance();
      }
      ((Mechanics) instance).modeId = modeId;
      ((Mechanics) instance).validateData();
      UtilsFile.write(path, instance);
      return instance;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }


}