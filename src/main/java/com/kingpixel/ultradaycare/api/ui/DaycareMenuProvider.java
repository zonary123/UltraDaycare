package com.kingpixel.ultradaycare.api.ui;

import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Service Provider Interface (SPI) for presenting Daycare menus.
 * Companion mods (such as Rotom Phone or custom GUI engines) can implement this
 * interface to replace default GooeyLibs chest menus with custom screens.
 */
public interface DaycareMenuProvider {

  /**
   * Opens the main daycare plot overview menu for a player.
   */
  void openPrincipalMenu(ServerPlayerEntity player);

  /**
   * Opens the main daycare menu where viewer is inspecting target player's daycare plots.
   */
  void openPrincipalMenu(ServerPlayerEntity viewer, ServerPlayerEntity target);

  /**
   * Opens the detail menu for a specific daycare plot.
   *
   * @param player    the player opening the menu
   * @param plotIndex 0-indexed plot position
   */
  void openPlotMenu(ServerPlayerEntity player, int plotIndex);

  /**
   * Opens the incubators hub menu for a player.
   */
  void openIncubatorsMenu(ServerPlayerEntity player);

  /**
   * Opens the incubators hub where viewer is inspecting target player's incubators.
   */
  void openIncubatorsMenu(ServerPlayerEntity viewer, ServerPlayerEntity target);

  /**
   * Opens the player profile and daycare notification options menu.
   */
  void openProfileMenu(ServerPlayerEntity player);
}
