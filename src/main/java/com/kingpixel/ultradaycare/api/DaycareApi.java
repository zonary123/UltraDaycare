package com.kingpixel.ultradaycare.api;

import com.kingpixel.ultradaycare.api.action.DaycareActionResult;
import com.kingpixel.ultradaycare.api.model.DaycareEggView;
import com.kingpixel.ultradaycare.api.model.DaycareProfileContext;
import com.kingpixel.ultradaycare.api.model.IncubatorSlotView;
import com.kingpixel.ultradaycare.api.model.PlotView;
import com.kingpixel.ultradaycare.api.ui.DaycareMenuProvider;
import com.kingpixel.ultradaycare.api.ui.DaycareMenuRegistry;
import com.kingpixel.ultradaycare.application.DaycareActionService;
import com.kingpixel.ultradaycare.application.DaycareContextService;
import com.kingpixel.ultradaycare.models.SelectGender;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Public API entry point for UltraDaycare.
 * <p>
 * Provides pluggable UI registration via {@link DaycareMenuProvider},
 * headless programmatic actions via {@link DaycareActionService},
 * and view model queries via {@link DaycareContextService}.
 */
public final class DaycareApi {

  private static final DaycareActionService ACTION_SERVICE = new DaycareActionService();
  private static final DaycareContextService CONTEXT_SERVICE = new DaycareContextService();

  private DaycareApi() {
  }

  // ═══════════════════════════════════════════════════════════════════════════
  // Services
  // ═══════════════════════════════════════════════════════════════════════════

  @NotNull
  public static DaycareActionService getActionService() {
    return ACTION_SERVICE;
  }

  @NotNull
  public static DaycareContextService getContextService() {
    return CONTEXT_SERVICE;
  }

  // ═══════════════════════════════════════════════════════════════════════════
  // Menu Provider SPI
  // ═══════════════════════════════════════════════════════════════════════════

  /**
   * Registers a custom menu provider to replace default chest GUIs.
   */
  public static void registerMenuProvider(@NotNull DaycareMenuProvider provider) {
    DaycareMenuRegistry.registerProvider(provider);
  }

  /**
   * Unregisters custom menu providers and restores default GooeyLibs chest menus.
   */
  public static void unregisterMenuProvider() {
    DaycareMenuRegistry.unregisterProvider();
  }

  /**
   * Checks whether a custom menu provider is currently registered.
   */
  public static boolean hasCustomMenuProvider() {
    return DaycareMenuRegistry.hasCustomProvider();
  }

  /**
   * Opens the main daycare plot overview menu for a player.
   */
  public static void openPrincipalMenu(@Nullable ServerPlayerEntity player) {
    DaycareMenuRegistry.openPrincipalMenu(player);
  }

  /**
   * Opens the main daycare menu where viewer is inspecting target player's daycare plots.
   */
  public static void openPrincipalMenu(@Nullable ServerPlayerEntity viewer, @Nullable ServerPlayerEntity target) {
    DaycareMenuRegistry.openPrincipalMenu(viewer, target);
  }

  /**
   * Opens the detail menu for a specific daycare plot.
   */
  public static void openPlotMenu(@Nullable ServerPlayerEntity player, int plotIndex) {
    DaycareMenuRegistry.openPlotMenu(player, plotIndex);
  }

  /**
   * Opens the incubators hub menu for a player.
   */
  public static void openIncubatorsMenu(@Nullable ServerPlayerEntity player) {
    DaycareMenuRegistry.openIncubatorsMenu(player);
  }

  /**
   * Opens the incubators hub where viewer is inspecting target player's incubators.
   */
  public static void openIncubatorsMenu(@Nullable ServerPlayerEntity viewer, @Nullable ServerPlayerEntity target) {
    DaycareMenuRegistry.openIncubatorsMenu(viewer, target);
  }

  /**
   * Opens the player profile and daycare notification options menu.
   */
  public static void openProfileMenu(@Nullable ServerPlayerEntity player) {
    DaycareMenuRegistry.openProfileMenu(player);
  }

  // ═══════════════════════════════════════════════════════════════════════════
  // Headless Context Queries
  // ═══════════════════════════════════════════════════════════════════════════

  @Nullable
  public static DaycareProfileContext getPlayerProfile(@Nullable ServerPlayerEntity player) {
    return CONTEXT_SERVICE.getPlayerProfile(player);
  }

  @NotNull
  public static List<PlotView> getPlotViews(@Nullable ServerPlayerEntity player) {
    return CONTEXT_SERVICE.getPlotViews(player);
  }

  @Nullable
  public static PlotView getPlotView(@Nullable ServerPlayerEntity player, int plotIndex) {
    return CONTEXT_SERVICE.getPlotView(player, plotIndex);
  }

  @NotNull
  public static List<IncubatorSlotView> getIncubatorViews(@Nullable ServerPlayerEntity player) {
    return CONTEXT_SERVICE.getIncubatorViews(player);
  }

  @Nullable
  public static IncubatorSlotView getIncubatorView(@Nullable ServerPlayerEntity player, int slotIndex) {
    return CONTEXT_SERVICE.getIncubatorView(player, slotIndex);
  }

  @NotNull
  public static List<DaycareEggView> getStoredEggs(@Nullable ServerPlayerEntity player) {
    return CONTEXT_SERVICE.getStoredEggs(player);
  }

  // ═══════════════════════════════════════════════════════════════════════════
  // Headless Actions
  // ═══════════════════════════════════════════════════════════════════════════

  @NotNull
  public static DaycareActionResult depositParent(@Nullable ServerPlayerEntity player,
                                                  int plotIndex,
                                                  @Nullable UUID pokemonUuid,
                                                  @Nullable SelectGender gender) {
    return ACTION_SERVICE.depositParent(player, plotIndex, pokemonUuid, gender);
  }

  @NotNull
  public static DaycareActionResult withdrawParent(@Nullable ServerPlayerEntity player,
                                                   int plotIndex,
                                                   @Nullable SelectGender gender) {
    return ACTION_SERVICE.withdrawParent(player, plotIndex, gender);
  }

  @NotNull
  public static DaycareActionResult collectEggs(@Nullable ServerPlayerEntity player, int plotIndex) {
    return ACTION_SERVICE.collectEggs(player, plotIndex);
  }

  @NotNull
  public static DaycareActionResult claimPendingXp(@Nullable ServerPlayerEntity player,
                                                   int plotIndex,
                                                   @Nullable SelectGender gender) {
    return ACTION_SERVICE.claimPendingXp(player, plotIndex, gender);
  }

  @NotNull
  public static DaycareActionResult payBreedingFee(@Nullable ServerPlayerEntity player, int plotIndex) {
    return ACTION_SERVICE.payBreedingFee(player, plotIndex);
  }

  @NotNull
  public static DaycareActionResult buyIncubator(@Nullable ServerPlayerEntity player) {
    return ACTION_SERVICE.buyIncubator(player);
  }

  @NotNull
  public static DaycareActionResult hatchIncubatorEgg(@Nullable ServerPlayerEntity player, int slotIndex) {
    return ACTION_SERVICE.hatchIncubatorEgg(player, slotIndex);
  }

  @NotNull
  public static DaycareActionResult removeIncubatorCaretaker(@Nullable ServerPlayerEntity player, int slotIndex) {
    return ACTION_SERVICE.removeIncubatorCaretaker(player, slotIndex);
  }
}
