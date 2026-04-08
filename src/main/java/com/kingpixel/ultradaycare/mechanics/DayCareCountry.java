package com.kingpixel.ultradaycare.mechanics;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.ultradaycare.UltraDaycare;
import com.kingpixel.ultradaycare.models.EggBuilder;
import com.kingpixel.ultradaycare.models.HatchBuilder;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * @author Carlos Varas Alonso - 31/01/2025 0:25
 */
public class DayCareCountry extends Mechanics {
  public static final String TAG = "country";

  @Override
  public void applyEgg(EggBuilder builder) {
    var user = UltraDaycare.database.getUser(builder.getPlayer());
    if (user == null) return;
    var country = user.getCountry();
    if (country == null) return;
    builder.getEgg().getPersistentData().putString(TAG, country);
  }

  @Override
  public void applyHatch(HatchBuilder builder) {
  }

  @Override
  public void createEgg(ServerPlayerEntity player, Pokemon pokemon, Pokemon egg) {

  }

  @Override
  public String getEggInfo(String s, NbtCompound nbt) {
    return s.replace("%country%", nbt.getString(TAG));
  }

  @Override
  public void validateData() {
  }

  @Override
  public String fileName() {
    return "country";
  }

  @Override
  public String replace(String text, ServerPlayerEntity player) {
    return text;
  }
}
