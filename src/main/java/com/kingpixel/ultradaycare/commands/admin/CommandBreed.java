package com.kingpixel.ultradaycare.commands.admin;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.command.argument.PartySlotArgumentType;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.api.PermissionApi;
import com.kingpixel.cobbleutils.util.PlayerUtils;
import com.kingpixel.cobbleutils.util.PokemonUtils;
import com.kingpixel.cobbleutils.util.TypeMessage;
import com.kingpixel.ultradaycare.UltraDaycare;
import com.kingpixel.ultradaycare.models.Plot;
import com.kingpixel.ultradaycare.models.SelectGender;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

/**
 * @author Carlos Varas Alonso - 05/04/2025 2:09
 */
public class CommandBreed {
  public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                              LiteralArgumentBuilder<ServerCommandSource> base) {
    dispatcher.register(
      base
        .requires(source -> PermissionApi.hasPermission(source, List.of("cobbledaycare.breed.base", "cobbledaycare" +
            ".admin"),
          4))
        .then(
          CommandManager.argument("male", PartySlotArgumentType.Companion.partySlot())
            .requires(source -> PermissionApi.hasPermission(source, List.of("cobbledaycare.breed.base", "cobbledaycare" +
                ".admin"),
              4))
            .then(
              CommandManager.argument("female", PartySlotArgumentType.Companion.partySlot())
                .requires(source -> PermissionApi.hasPermission(source, List.of("cobbledaycare.breed.base", "cobbledaycare" +
                    ".admin"),
                  4))
                .executes(context -> {
                    var player = context.getSource().getPlayer();
                    return breed(context, player);
                  }
                ).then(
                  CommandManager.argument("player", EntityArgumentType.players())
                    .requires(source -> PermissionApi.hasPermission(source, List.of("cobbledaycare.breed.other",
                      "cobbledaycare.admin"), 4))
                    .executes(context -> {
                      var players = EntityArgumentType.getPlayers(context, "player");
                      for (ServerPlayerEntity player : players) {
                        breed(context, player);
                      }
                      return 1;
                    })
                )
            )
        )
    );
  }

  private static int breed(CommandContext<ServerCommandSource> context, ServerPlayerEntity player) {
    if (player == null) return 0;
    var user = UltraDaycare.database.getUser(player);
    if (user == null) return 0;
    if (user.hasCooldownBreed(player)) {
      PlayerUtils.sendMessage(
        player,
        UltraDaycare.language.getMessageCooldownBreed()
          .replace("%cooldown%", PlayerUtils.getCooldown(user.getCooldownBreed())),
        UltraDaycare.language.getPrefix(),
        TypeMessage.CHAT
      );
      return 0;
    }
    Plot plot = new Plot();
    Pokemon male = PartySlotArgumentType.Companion.getPokemon(context, "male");
    UltraDaycare.fixBreedable(male);
    boolean maleCanBreed = plot.canBreed(male, SelectGender.MALE);
    plot.setMale(male);
    Pokemon female = PartySlotArgumentType.Companion.getPokemon(context, "female");
    UltraDaycare.fixBreedable(female);
    boolean femaleCanBreed = plot.canBreed(female, SelectGender.FEMALE);
    plot.setFemale(female);

    if (UltraDaycare.config.isDebug()) {
      CobbleUtils.LOGGER.info("maleCanBreed: " + maleCanBreed);
      CobbleUtils.LOGGER.info("femaleCanBreed: " + femaleCanBreed);
    }
    if (maleCanBreed && femaleCanBreed) {
      Cobblemon.INSTANCE.getStorage().getParty(player).add(plot.createEgg(player));
      user.setCooldownBreed(player);
      user.markDirty();
    } else {
      PlayerUtils.sendMessage(
        player,
        PokemonUtils.replace(UltraDaycare.language.getMessageCannotBreed(), List.of(male, female)),
        UltraDaycare.language.getPrefix(),
        TypeMessage.CHAT
      );
    }
    return 1;
  }
}
