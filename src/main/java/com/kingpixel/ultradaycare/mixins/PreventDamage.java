package com.kingpixel.ultradaycare.mixins;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.ultradaycare.UltraDaycare;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author Carlos Varas Alonso - 15/04/2025 22:35
 */
@Mixin(PokemonEntity.class)
public abstract class PreventDamage {

  @Inject(method = "offerHeldItem", at = @At("HEAD"), cancellable = true)
  private void offerHeldItem(PlayerEntity player, ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
    try {
      PokemonEntity pokemonEntity = (PokemonEntity) (Object) this;
      if (pokemonEntity == null) return;
      var pokemon = pokemonEntity.getPokemon();
      if (pokemon == null) return;
      if (cobbleDaycare$isEgg(pokemon)) {
        cir.setReturnValue(false);
      }
    } catch (Exception e) {
      UltraDaycare.LOGGER.error("Error offering held item: ", e);
    }
  }

  @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
  private void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
    try {
      PokemonEntity pokemonEntity = (PokemonEntity) (Object) this;
      if (pokemonEntity == null) return;
      var pokemon = pokemonEntity.getPokemon();
      if (pokemon == null) return;
      var attacker = source.getAttacker();
      if (attacker == null) return;
      if (attacker instanceof ServerPlayerEntity player) {
        if (cobbleDaycare$isEgg(pokemon)) {
          cobbleDaycare$givePokemon(player, pokemon, pokemonEntity);
          cir.cancel();
        }
      }
    } catch (Exception e) {
      UltraDaycare.LOGGER.error("Error processing damage: ", e);
    }
  }

  @Inject(method = "interactMob", at = @At("HEAD"), cancellable = true)
  private void interact(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
    try {
      PokemonEntity pokemonEntity = (PokemonEntity) (Object) this;
      if (pokemonEntity == null) return;
      var pokemon = pokemonEntity.getPokemon();
      if (pokemon == null) return;
      if (pokemon.isPlayerOwned()) return;
      if (player == null) return;
      if (cobbleDaycare$isEgg(pokemon)) {
        cobbleDaycare$givePokemon((ServerPlayerEntity) player, pokemon, pokemonEntity);
        cir.cancel();
      }
    } catch (Exception e) {
      UltraDaycare.LOGGER.error("Error processing mob interaction: ", e);
    }
  }


  @Unique
  private void cobbleDaycare$givePokemon(ServerPlayerEntity player, Pokemon pokemon, PokemonEntity pokemonEntity) {
    if (UltraDaycare.config.isDebug()) {
      UltraDaycare.LOGGER.info(
        " Persistent data: " + pokemon.getPersistentData().getBoolean(UltraDaycare.TAG_SPAWNED) + " " +
          " Owner: " + pokemon.isPlayerOwned());
    }
    if (pokemon.getPersistentData().getBoolean(UltraDaycare.TAG_SPAWNED) && !pokemon.isPlayerOwned()) {
      pokemon.getPersistentData().remove(UltraDaycare.TAG_SPAWNED);
      var party = Cobblemon.INSTANCE.getStorage().getParty(player);
      party.add(pokemon);
      for (Pokemon p : party) {
        p.getPersistentData().remove(UltraDaycare.TAG_SPAWNED);
      }
      pokemonEntity.remove(Entity.RemovalReason.DISCARDED);
    }
  }

  @Unique
  private boolean cobbleDaycare$isEgg(Pokemon pokemon) {
    return pokemon.getSpecies().showdownId().equals("egg");
  }
}
