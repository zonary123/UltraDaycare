package com.kingpixel.ultradaycare.api.ui;

import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Thread-safe registry and dispatcher for {@link DaycareMenuProvider}.
 */
public final class DaycareMenuRegistry {

  private static final DaycareMenuProvider DEFAULT_PROVIDER = new DefaultGooeyDaycareMenuProvider();
  private static volatile DaycareMenuProvider activeProvider = DEFAULT_PROVIDER;

  private DaycareMenuRegistry() {
  }

  /**
   * Registers a custom menu provider, overriding default chest GUIs.
   */
  public static void registerProvider(@NotNull DaycareMenuProvider provider) {
    activeProvider = Objects.requireNonNull(provider, "DaycareMenuProvider cannot be null");
  }

  /**
   * Unregisters any custom menu provider and reverts to default GooeyLibs chest menus.
   */
  public static void unregisterProvider() {
    activeProvider = DEFAULT_PROVIDER;
  }

  /**
   * Returns true if a custom menu provider is currently active.
   */
  public static boolean hasCustomProvider() {
    return activeProvider != DEFAULT_PROVIDER;
  }

  /**
   * Returns the currently active menu provider.
   */
  @NotNull
  public static DaycareMenuProvider getProvider() {
    return activeProvider;
  }

  /**
   * Opens the main daycare plot overview menu for a player.
   */
  public static void openPrincipalMenu(@Nullable ServerPlayerEntity player) {
    if (player == null) return;
    activeProvider.openPrincipalMenu(player);
  }

  /**
   * Opens the main daycare menu where viewer is inspecting target player's daycare plots.
   */
  public static void openPrincipalMenu(@Nullable ServerPlayerEntity viewer, @Nullable ServerPlayerEntity target) {
    if (viewer == null || target == null) return;
    activeProvider.openPrincipalMenu(viewer, target);
  }

  /**
   * Opens the detail menu for a specific daycare plot.
   */
  public static void openPlotMenu(@Nullable ServerPlayerEntity player, int plotIndex) {
    if (player == null) return;
    activeProvider.openPlotMenu(player, plotIndex);
  }

  /**
   * Opens the incubators hub menu for a player.
   */
  public static void openIncubatorsMenu(@Nullable ServerPlayerEntity player) {
    if (player == null) return;
    activeProvider.openIncubatorsMenu(player);
  }

  /**
   * Opens the incubators hub where viewer is inspecting target player's incubators.
   */
  public static void openIncubatorsMenu(@Nullable ServerPlayerEntity viewer, @Nullable ServerPlayerEntity target) {
    if (viewer == null || target == null) return;
    activeProvider.openIncubatorsMenu(viewer, target);
  }

  /**
   * Opens the player profile and daycare notification options menu.
   */
  public static void openProfileMenu(@Nullable ServerPlayerEntity player) {
    if (player == null) return;
    activeProvider.openProfileMenu(player);
  }
}
