package com.kingpixel.ultradaycare.commands.base;

import com.cobblemon.mod.common.command.argument.PartySlotArgumentType;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.api.PermissionApi;
import com.kingpixel.cobbleutils.util.PlayerUtils;
import com.kingpixel.cobbleutils.util.TypeMessage;
import com.kingpixel.ultradaycare.UltraDaycare;
import com.kingpixel.ultradaycare.mechanics.Mechanics;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

/**
 * @author Carlos Varas Alonso - 05/04/2025 2:07
 */
public class CommandEggInfo {
  public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
      LiteralArgumentBuilder<ServerCommandSource> base) {
    dispatcher.register(
        base
            .requires(source -> PermissionApi.hasPermission(source,
                List.of("cobbledaycare.egginfo.base", "cobbledaycare" +
                    ".admin"),
                4))
            .then(
                CommandManager.argument("slot", PartySlotArgumentType.Companion.partySlot())
                    .executes(context -> {

                      if (context.getSource().isExecutedByPlayer()) {
                        ServerPlayerEntity player = context.getSource().getPlayer();
                        Pokemon egg = PartySlotArgumentType.Companion.getPokemon(context, "slot");
                        if (egg.getSpecies().showdownId().equals("egg")) {
                          String message = UltraDaycare.language.getEggInfo();
                          var nbt = egg.getPersistentData();
                          if (UltraDaycare.config.isDebug()) {
                            UltraDaycare.LOGGER.info("Egg NBT keys: " + String.join(", ", nbt.getKeys()));
                          }
                          for (Mechanics mechanic : UltraDaycare.mechanics) {
                            try {
                              message = mechanic.getEggInfo(message, nbt);
                            } catch (Exception e) {
                              UltraDaycare.LOGGER.error(
                                  "Error in egg info " + mechanic.getClass().getSimpleName() + ": ", e);
                            }
                          }

                          // Clean up unreplaced placeholders and their corresponding lines
                          String[] lines = message.split("\n");
                          StringBuilder cleanedMessage = new StringBuilder();
                          for (String line : lines) {
                            if (line.contains("%nature%") || line.contains("%ability%")
                                || line.contains("%eggmoves%")) {
                              continue;
                            }
                            cleanedMessage.append(line).append("\n");
                          }
                          if (!cleanedMessage.isEmpty()) {
                            cleanedMessage.setLength(cleanedMessage.length() - 1);
                          }
                          message = cleanedMessage.toString();

                          PlayerUtils.sendMessage(
                              player,
                              message,
                              UltraDaycare.language.getPrefix(),
                              TypeMessage.CHAT);
                        } else {
                          PlayerUtils.sendMessage(
                              player,
                              UltraDaycare.language.getMessageItNotEgg(),
                              UltraDaycare.language.getPrefix(),
                              TypeMessage.CHAT);
                        }
                      }
                      return 1;

                    })));
  }
}
