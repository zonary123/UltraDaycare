package com.kingpixel.ultradaycare.tasks;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.util.PlayerUtils;
import com.kingpixel.cobbleutils.util.TypeMessage;
import com.kingpixel.ultradaycare.UltraDaycare;
import com.kingpixel.ultradaycare.models.EggData;
import com.kingpixel.ultradaycare.models.Plot;
import com.kingpixel.ultradaycare.models.Position;
import com.kingpixel.ultradaycare.models.User;
import lombok.Data;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Unique;

import java.util.Collections;
import java.util.concurrent.*;

/**
 * @author Carlos Varas Alonso - 24/11/2025 8:05
 */
@Data
public class TaskDayCare implements Runnable {
  public static final ConcurrentMap<ServerPlayerEntity, Integer> cobbleDaycare$playerTeleport = new ConcurrentHashMap<>();
  private static final long TICKS_TO_MILLISECONDS = 50;
  private static final ConcurrentMap<ServerPlayerEntity, Position> cobbleDaycare$playerPositions =
    new ConcurrentHashMap<>();
  private static final ScheduledExecutorService cobbleDaycare$scheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
    .setNameFormat("CobbleDaycare-walk-breeding-%d")
    .setDaemon(true)
    .build());

  public TaskDayCare() {
    cobbleDaycare$scheduler.scheduleAtFixedRate(this, 5, 1, TimeUnit.SECONDS);
  }

  private static void sendMessageMultiplierSteps(User user, ServerPlayerEntity player) {
    boolean activeMultiplier = UltraDaycare.config.isGlobalMultiplierSteps();
    float multiplier = user.getActualMultiplier(player);
    boolean hasMultiplier = multiplier >= UltraDaycare.config.getMultiplierSteps();

    if (activeMultiplier || hasMultiplier) {
      long cooldown = user.getTimeMultiplierSteps() * TICKS_TO_MILLISECONDS;
      String cooldownMessage = PlayerUtils.getCooldown(System.currentTimeMillis() + cooldown);

      PlayerUtils.sendMessage(
        player,
        UltraDaycare.language.getMessageActiveStepsMultiplier()
          .replace("%multiplier%", String.format("%.2f", multiplier))
          .replace("%cooldown%", cooldownMessage)
          .replace("%time%", cooldownMessage),
        UltraDaycare.language.getPrefix(),
        TypeMessage.ACTIONBAR
      );
    }
  }

  @Unique
  private static boolean isVehiclePermitted(ServerPlayerEntity player) {
    String vehicleId = player.getVehicle() == null ? "" : player.getVehicle().getSavedEntityId();
    if (vehicleId == null) vehicleId = "";
    return UltraDaycare.config.getPermittedVehicles().contains(vehicleId) || vehicleId.isEmpty();
  }

  // -------------------- Mismos métodos existentes pero sin CompletableFuture --------------------

  private boolean isPlayerEligibleForStepUpdate(ServerPlayerEntity player) {
    return (UltraDaycare.config.isAllowElytra() || !player.isInPose(EntityPose.FALL_FLYING))
      && !player.getAbilities().flying
      && isVehiclePermitted(player)
      && (!player.isTouchingWater() || player.isInPose(EntityPose.SWIMMING));
  }

  private Entity getEffectiveEntity(ServerPlayerEntity player) {
    return player.getVehicle() != null ? player.getVehicle() : player;
  }

  private void updateEggSteps(PlayerPartyStore party, double deltaMovement, ServerPlayerEntity player) {
    User user = UltraDaycare.database.getUser(player);
    if (user == null) return;
    for (Pokemon pokemon : party) {
      if (pokemon != null && "egg".equals(pokemon.showdownId())) {
        EggData.steps(player, pokemon, deltaMovement, user);
      }
    }
  }

  private void updatePlotsXp(ServerPlayerEntity player, double deltaMovement) {
    User user = UltraDaycare.database.getUser(player);
    if (user == null || user.getPlots().isEmpty()) return;
    double xpAccumulated = deltaMovement * UltraDaycare.config.getXpPerStep();
    int maxLevel = UltraDaycare.config.getMaxLevelTraining();

    for (Plot plot : user.getPlots()) {
      if (plot.getMale() != null && plot.getMale().getLevel() < maxLevel) {
        plot.setMalePendingXp(plot.getMalePendingXp() + xpAccumulated);
      }
      if (plot.getFemale() != null && plot.getFemale().getLevel() < maxLevel) {
        plot.setFemalePendingXp(plot.getFemalePendingXp() + xpAccumulated);
      }
    }
  }

  private void updatePlotsBreeding(ServerPlayerEntity player, User user) {
    if (user == null || user.getPlots().isEmpty()) return;
    for (Plot plot : user.getPlots()) {
      // Preliminary quick checks before calling the heavier logic in Plot.checkEgg
      if (plot.hasTwoParents()) {
        plot.checkEgg(player, user);
      }
    }
  }

  private void sendMessage(ServerPlayerEntity player) {
    User user = UltraDaycare.database.getUser(player);
    if (user == null) return;
    long timeMultiplierSteps = user.getTimeMultiplierSteps();

    if (timeMultiplierSteps > 0) {
      timeMultiplierSteps -= UltraDaycare.config.getTicksToWalking();
      user.setTimeMultiplierSteps(timeMultiplierSteps);

      if (timeMultiplierSteps <= 0) {
        user.setMultiplierSteps(UltraDaycare.config.getMultiplierSteps());
        user.setTimeMultiplierSteps(0);
      }

      if (user.isActionBar()) {
        sendMessageMultiplierSteps(user, player);
      }
    }
  }

  private void processPlayers() {
    if (CobbleUtils.server == null) return;
    long currentTime = System.currentTimeMillis();
    var playerManager = CobbleUtils.server.getPlayerManager();
    if (playerManager == null || playerManager.getPlayerList().isEmpty()) return;

    for (ServerPlayerEntity player : Collections.synchronizedList(playerManager.getPlayerList())) {
      if (player == null || !player.isAlive() || player.isRemoved()) continue;
      try {
        User user = UltraDaycare.database.getUser(player);
        if (user == null || !isPlayerEligibleForStepUpdate(player)) continue;

        Entity entity = getEffectiveEntity(player);
        if (entity == null) continue;

        Position pos = cobbleDaycare$playerPositions.computeIfAbsent(player, p -> new Position(entity.getX(), entity.getZ(), currentTime));

        double deltaX = entity.getX() - pos.getX();
        double deltaZ = entity.getZ() - pos.getZ();
        double deltaMovement = Math.hypot(deltaX, deltaZ);

        int teleportCount = cobbleDaycare$playerTeleport.getOrDefault(player, 0);
        if (deltaMovement <= 0 || teleportCount > 0) {
          if (teleportCount > 0) cobbleDaycare$playerTeleport.put(player, teleportCount - 1);
          pos.setX(entity.getX());
          pos.setZ(entity.getZ());
          pos.setLastUpdate(currentTime);
          continue;
        }

        PlayerPartyStore party = Cobblemon.INSTANCE.getStorage().getParty(player);
        updateEggSteps(party, deltaMovement, player);
        updatePlotsXp(player, deltaMovement);
        updatePlotsBreeding(player, user);
        sendMessage(player);

        pos.setX(entity.getX());
        pos.setZ(entity.getZ());
        pos.setLastUpdate(currentTime);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public void run() {
    try {
      processPlayers();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
