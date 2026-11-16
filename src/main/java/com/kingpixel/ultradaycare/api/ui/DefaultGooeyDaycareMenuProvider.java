package com.kingpixel.ultradaycare.api.ui;

import com.kingpixel.ultradaycare.UltraDaycare;
import com.kingpixel.ultradaycare.models.Plot;
import com.kingpixel.ultradaycare.models.User;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Default GooeyLibs chest inventory provider for UltraDaycare.
 */
public class DefaultGooeyDaycareMenuProvider implements DaycareMenuProvider {

  @Override
  public void openPrincipalMenu(ServerPlayerEntity player) {
    if (player == null) return;
    UltraDaycare.language.getPrincipalMenu().open(player);
  }

  @Override
  public void openPrincipalMenu(ServerPlayerEntity viewer, ServerPlayerEntity target) {
    if (viewer == null) return;
    ServerPlayerEntity actualTarget = target != null ? target : viewer;
    UltraDaycare.language.getPrincipalMenu().open(actualTarget);
  }

  @Override
  public void openPlotMenu(ServerPlayerEntity player, int plotIndex) {
    if (player == null || UltraDaycare.database == null) return;
    User user = UltraDaycare.database.getUser(player);
    if (user == null || user.getPlots() == null) return;
    if (plotIndex < 0 || plotIndex >= user.getPlots().size()) return;

    Plot plot = user.getPlots().get(plotIndex);
    if (plot == null) return;

    UltraDaycare.language.getPlotMenu().open(player, plot, user);
  }

  @Override
  public void openIncubatorsMenu(ServerPlayerEntity player) {
    if (player == null || UltraDaycare.database == null) return;
    User user = UltraDaycare.database.getUser(player);
    if (user != null) {
      UltraDaycare.language.getIncubatorsMenu().open(player, user);
    }
  }

  @Override
  public void openIncubatorsMenu(ServerPlayerEntity viewer, ServerPlayerEntity target) {
    if (viewer == null || UltraDaycare.database == null) return;
    ServerPlayerEntity actualTarget = target != null ? target : viewer;
    User user = UltraDaycare.database.getUser(actualTarget);
    if (user != null) {
      UltraDaycare.language.getIncubatorsMenu().open(viewer, user);
    }
  }

  @Override
  public void openProfileMenu(ServerPlayerEntity player) {
    if (player == null || UltraDaycare.database == null) return;
    User user = UltraDaycare.database.getUser(player);
    if (user != null) {
      UltraDaycare.language.getProfileMenu().open(player, user);
    }
  }
}
