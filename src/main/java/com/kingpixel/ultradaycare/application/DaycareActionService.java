package com.kingpixel.ultradaycare.application;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.pokemon.experience.SidemodExperienceSource;
import com.cobblemon.mod.common.battles.BattleRegistry;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.api.EconomyApi;
import com.kingpixel.ultradaycare.UltraDaycare;
import com.kingpixel.ultradaycare.api.action.DaycareActionResult;
import com.kingpixel.ultradaycare.api.action.DaycareActionResult.Status;
import com.kingpixel.ultradaycare.config.IncubatorConfig;
import com.kingpixel.ultradaycare.config.IncubatorHatchMode;
import com.kingpixel.ultradaycare.models.EggData;
import com.kingpixel.ultradaycare.models.IncubatorSlot;
import com.kingpixel.ultradaycare.models.Plot;
import com.kingpixel.ultradaycare.models.SelectGender;
import com.kingpixel.ultradaycare.models.User;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Application service providing atomic, programmatical actions for UltraDaycare.
 * Executes daycare operations without requiring chest GUIs.
 */
public class DaycareActionService {

  /**
   * Deposits a Pokémon from the player's party into a daycare plot as a parent.
   */
  public DaycareActionResult depositParent(@Nullable ServerPlayerEntity player,
                                          int plotIndex,
                                          @Nullable UUID pokemonUuid,
                                          @Nullable SelectGender gender) {
    if (player == null || pokemonUuid == null || gender == null) {
      return DaycareActionResult.failure(Status.ERROR, "Parameters cannot be null");
    }

    if (UltraDaycare.database == null) {
      return DaycareActionResult.failure(Status.ERROR, "Database is not initialized");
    }

    User user = UltraDaycare.database.getUser(player);
    if (user == null || user.getPlots() == null) {
      return DaycareActionResult.failure(Status.ERROR, "User data not found");
    }

    String perm = Plot.plotPermission(plotIndex);
    if (!UltraDaycare.hasPermission(player, perm, 2)) {
      return DaycareActionResult.failure(Status.PLOT_LOCKED, "Plot " + (plotIndex + 1) + " is locked", plotIndex);
    }

    if (plotIndex < 0 || plotIndex >= user.getPlots().size()) {
      return DaycareActionResult.failure(Status.PLOT_NOT_FOUND, "Plot index out of range: " + plotIndex, plotIndex);
    }

    Plot plot = user.getPlots().get(plotIndex);
    if (plot == null) {
      return DaycareActionResult.failure(Status.PLOT_NOT_FOUND, "Plot not found at index: " + plotIndex, plotIndex);
    }

    if (BattleRegistry.getBattleByParticipatingPlayer(player) != null) {
      return DaycareActionResult.failure(Status.POKEMON_IN_BATTLE, "Cannot deposit Pokemon while in battle", plotIndex);
    }

    var party = Cobblemon.INSTANCE.getStorage().getParty(player);
    Pokemon targetPokemon = null;
    for (Pokemon p : party) {
      if (p != null && p.getUuid().equals(pokemonUuid)) {
        targetPokemon = p;
        break;
      }
    }

    if (targetPokemon == null) {
      return DaycareActionResult.failure(Status.POKEMON_NOT_FOUND, "Pokemon not found in party: " + pokemonUuid, plotIndex);
    }

    if (!plot.canBreed(targetPokemon, gender)) {
      return DaycareActionResult.failure(Status.NOT_BREEDABLE, "Pokemon cannot breed in this plot", plotIndex);
    }

    party.remove(targetPokemon);

    if (gender == SelectGender.MALE) {
      if (plot.getMale() != null) {
        party.add(plot.getMale());
      }
      plot.addMale(player, targetPokemon);
    } else {
      if (plot.getFemale() != null) {
        party.add(plot.getFemale());
      }
      plot.addFemale(player, targetPokemon);
    }

    user.markDirty();
    user.save();

    return DaycareActionResult.successPlot(
      "Deposited " + targetPokemon.getSpecies().getName() + " into plot " + (plotIndex + 1),
      plotIndex
    );
  }

  /**
   * Withdraws a parent Pokémon from a daycare plot back to the player's party.
   */
  public DaycareActionResult withdrawParent(@Nullable ServerPlayerEntity player,
                                           int plotIndex,
                                           @Nullable SelectGender gender) {
    if (player == null || gender == null) {
      return DaycareActionResult.failure(Status.ERROR, "Parameters cannot be null");
    }

    if (UltraDaycare.database == null) {
      return DaycareActionResult.failure(Status.ERROR, "Database is not initialized");
    }

    User user = UltraDaycare.database.getUser(player);
    if (user == null || user.getPlots() == null) {
      return DaycareActionResult.failure(Status.ERROR, "User data not found");
    }

    if (plotIndex < 0 || plotIndex >= user.getPlots().size()) {
      return DaycareActionResult.failure(Status.PLOT_NOT_FOUND, "Plot index out of range: " + plotIndex, plotIndex);
    }

    Plot plot = user.getPlots().get(plotIndex);
    if (plot == null) {
      return DaycareActionResult.failure(Status.PLOT_NOT_FOUND, "Plot not found at index: " + plotIndex, plotIndex);
    }

    Pokemon parent = gender == SelectGender.MALE ? plot.getMale() : plot.getFemale();
    if (parent == null) {
      return DaycareActionResult.failure(Status.POKEMON_NOT_FOUND, "No parent in that slot", plotIndex);
    }

    var party = Cobblemon.INSTANCE.getStorage().getParty(player);
    int occupied = 0;
    for (Pokemon p : party) {
      if (p != null) occupied++;
    }
    if (occupied >= 6) {
      return DaycareActionResult.failure(Status.PARTY_FULL, "Party is full", plotIndex);
    }

    if (gender == SelectGender.MALE) {
      plot.setMale(null);
    } else {
      plot.setFemale(null);
    }

    plot.setTime(player);
    plot.checkRefund(player);
    party.add(parent);

    user.markDirty();
    user.save();

    return DaycareActionResult.successPlot(
      "Withdrew " + parent.getSpecies().getName() + " from plot " + (plotIndex + 1),
      plotIndex
    );
  }

  /**
   * Collects all accumulated eggs from a plot into the player's party or incubator storage.
   */
  public DaycareActionResult collectEggs(@Nullable ServerPlayerEntity player, int plotIndex) {
    if (player == null) {
      return DaycareActionResult.failure(Status.ERROR, "Player cannot be null");
    }

    if (UltraDaycare.database == null) {
      return DaycareActionResult.failure(Status.ERROR, "Database is not initialized");
    }

    User user = UltraDaycare.database.getUser(player);
    if (user == null || user.getPlots() == null) {
      return DaycareActionResult.failure(Status.ERROR, "User data not found");
    }

    if (plotIndex < 0 || plotIndex >= user.getPlots().size()) {
      return DaycareActionResult.failure(Status.PLOT_NOT_FOUND, "Plot index out of range: " + plotIndex, plotIndex);
    }

    Plot plot = user.getPlots().get(plotIndex);
    if (plot == null) {
      return DaycareActionResult.failure(Status.PLOT_NOT_FOUND, "Plot not found at index: " + plotIndex, plotIndex);
    }

    if (!plot.hasEggs()) {
      return DaycareActionResult.failure(Status.NO_EGGS, "No eggs available to collect in plot " + (plotIndex + 1), plotIndex);
    }

    int eggCount = plot.getEggs().size();
    boolean given = plot.giveEggs(player);

    user.markDirty();
    user.save();

    if (given) {
      return DaycareActionResult.successCount(
        "Collected " + eggCount + " egg(s) from plot " + (plotIndex + 1),
        eggCount
      );
    } else {
      return DaycareActionResult.failure(Status.ERROR, "Failed to collect eggs from plot " + (plotIndex + 1), plotIndex);
    }
  }

  /**
   * Claims pending training experience for a parent Pokémon in a plot.
   */
  public DaycareActionResult claimPendingXp(@Nullable ServerPlayerEntity player, int plotIndex, @Nullable SelectGender gender) {
    if (player == null || gender == null) {
      return DaycareActionResult.failure(Status.ERROR, "Parameters cannot be null");
    }

    if (!UltraDaycare.config.isEnablePaidExperience()) {
      return DaycareActionResult.failure(Status.ERROR, "Paid experience training is disabled");
    }

    if (UltraDaycare.database == null) {
      return DaycareActionResult.failure(Status.ERROR, "Database is not initialized");
    }

    User user = UltraDaycare.database.getUser(player);
    if (user == null || user.getPlots() == null) {
      return DaycareActionResult.failure(Status.ERROR, "User data not found");
    }

    if (plotIndex < 0 || plotIndex >= user.getPlots().size()) {
      return DaycareActionResult.failure(Status.PLOT_NOT_FOUND, "Plot index out of range: " + plotIndex, plotIndex);
    }

    Plot plot = user.getPlots().get(plotIndex);
    if (plot == null) {
      return DaycareActionResult.failure(Status.PLOT_NOT_FOUND, "Plot not found at index: " + plotIndex, plotIndex);
    }

    Pokemon parent = gender == SelectGender.MALE ? plot.getMale() : plot.getFemale();
    if (parent == null) {
      return DaycareActionResult.failure(Status.POKEMON_NOT_FOUND, "No parent in slot", plotIndex);
    }

    double pendingXp = gender == SelectGender.MALE ? plot.getMalePendingXp() : plot.getFemalePendingXp();
    if (pendingXp <= 0) {
      return DaycareActionResult.failure(Status.NO_PARENTS, "No pending XP to claim", plotIndex);
    }

    if (parent.getLevel() >= UltraDaycare.config.getMaxLevelTraining()) {
      return DaycareActionResult.failure(Status.MAX_LEVEL_REACHED, "Max training level reached", plotIndex);
    }

    double cost = pendingXp * UltraDaycare.config.getPricePerXp();
    if (!EconomyApi.hasEnoughMoney(player.getUuid(), BigDecimal.valueOf(cost), UltraDaycare.config.getEconomyUse(), true)) {
      return DaycareActionResult.failure(Status.INSUFFICIENT_FUNDS, "Insufficient funds to claim XP: requires " + cost, plotIndex);
    }

    EconomyApi.removeMoney(player.getUuid(), BigDecimal.valueOf(cost), UltraDaycare.config.getEconomyUse());
    parent.addExperience(new SidemodExperienceSource(UltraDaycare.MOD_ID), (int) pendingXp);

    if (gender == SelectGender.MALE) {
      plot.setMalePendingXp(0);
    } else {
      plot.setFemalePendingXp(0);
    }

    user.markDirty();
    user.save();

    return DaycareActionResult.builder()
      .status(Status.SUCCESS)
      .message("Claimed " + (int) pendingXp + " XP for " + parent.getSpecies().getName())
      .plotIndex(plotIndex)
      .cost(cost)
      .build();
  }

  /**
   * Pays the breeding entrance fee for a plot if required by configuration.
   */
  public DaycareActionResult payBreedingFee(@Nullable ServerPlayerEntity player, int plotIndex) {
    if (player == null) {
      return DaycareActionResult.failure(Status.ERROR, "Player cannot be null");
    }

    if (!UltraDaycare.config.isEnableBreedingFee()) {
      return DaycareActionResult.failure(Status.ERROR, "Breeding fee is disabled");
    }

    if (UltraDaycare.database == null) {
      return DaycareActionResult.failure(Status.ERROR, "Database is not initialized");
    }

    User user = UltraDaycare.database.getUser(player);
    if (user == null || user.getPlots() == null) {
      return DaycareActionResult.failure(Status.ERROR, "User data not found");
    }

    if (plotIndex < 0 || plotIndex >= user.getPlots().size()) {
      return DaycareActionResult.failure(Status.PLOT_NOT_FOUND, "Plot index out of range: " + plotIndex, plotIndex);
    }

    Plot plot = user.getPlots().get(plotIndex);
    if (plot == null) {
      return DaycareActionResult.failure(Status.PLOT_NOT_FOUND, "Plot not found at index: " + plotIndex, plotIndex);
    }

    if (plot.isBreedingPaid()) {
      return DaycareActionResult.failure(Status.ERROR, "Breeding fee is already paid for plot " + (plotIndex + 1), plotIndex);
    }

    if (!plot.hasTwoParents()) {
      return DaycareActionResult.failure(Status.NO_PARENTS, "Plot requires two parents before paying fee", plotIndex);
    }

    double price = UltraDaycare.config.getBreedingFeePrice();
    if (!EconomyApi.hasEnoughMoney(player.getUuid(), BigDecimal.valueOf(price), UltraDaycare.config.getEconomyUse(), true)) {
      return DaycareActionResult.failure(Status.INSUFFICIENT_FUNDS, "Insufficient funds to pay breeding fee: requires " + price, plotIndex);
    }

    EconomyApi.removeMoney(player.getUuid(), BigDecimal.valueOf(price), UltraDaycare.config.getEconomyUse());
    plot.setBreedingPaid(true);

    user.markDirty();
    user.save();

    return DaycareActionResult.builder()
      .status(Status.SUCCESS)
      .message("Breeding fee paid for plot " + (plotIndex + 1))
      .plotIndex(plotIndex)
      .cost(price)
      .build();
  }

  /**
   * Purchases a new incubator and installs it in the first available slot.
   */
  public DaycareActionResult buyIncubator(@Nullable ServerPlayerEntity player) {
    if (player == null) {
      return DaycareActionResult.failure(Status.ERROR, "Player cannot be null");
    }

    if (!UltraDaycare.config.getIncubators().isEnabled()) {
      return DaycareActionResult.failure(Status.ERROR, "Incubators module is disabled");
    }

    if (UltraDaycare.database == null) {
      return DaycareActionResult.failure(Status.ERROR, "Database is not initialized");
    }

    User user = UltraDaycare.database.getUser(player);
    if (user == null || user.getIncubatorSlots() == null) {
      return DaycareActionResult.failure(Status.ERROR, "User data not found");
    }

    user.checkDailyReset();
    if (!user.canBuyIncubator(player)) {
      return DaycareActionResult.failure(Status.INCUBATOR_DAILY_LIMIT_REACHED, "Daily incubator purchase limit reached");
    }

    IncubatorConfig config = UltraDaycare.config.getIncubators();
    IncubatorSlot targetSlot = null;
    int foundIndex = -1;

    for (int i = 0; i < config.getSlots().size(); i++) {
      if (UltraDaycare.hasPermission(player, IncubatorConfig.incubatorSlotPermission(i), 2)
        && i < user.getIncubatorSlots().size()) {
        IncubatorSlot s = user.getIncubatorSlots().get(i);
        if (!s.isHasIncubator()) {
          targetSlot = s;
          foundIndex = i;
          break;
        }
      }
    }

    if (targetSlot == null) {
      return DaycareActionResult.failure(Status.INCUBATOR_NOT_FOUND, "No available slot to install incubator");
    }

    double price = config.getPrice();
    if (!EconomyApi.hasEnoughMoney(player.getUuid(), BigDecimal.valueOf(price), config.getEconomyUse(), true)) {
      return DaycareActionResult.failure(Status.INSUFFICIENT_FUNDS, "Insufficient funds to buy incubator: requires " + price);
    }

    EconomyApi.removeMoney(player.getUuid(), BigDecimal.valueOf(price), config.getEconomyUse());
    targetSlot.setHasIncubator(true);
    targetSlot.setRemainingUses(config.getDefaultIncubatorUses());
    user.recordIncubatorPurchase();

    user.markDirty();
    user.save();

    return DaycareActionResult.builder()
      .status(Status.SUCCESS)
      .message("Purchased incubator in slot " + (foundIndex + 1))
      .slotIndex(foundIndex)
      .cost(price)
      .build();
  }

  /**
   * Hatches a ready egg from an incubator slot.
   */
  public DaycareActionResult hatchIncubatorEgg(@Nullable ServerPlayerEntity player, int slotIndex) {
    if (player == null) {
      return DaycareActionResult.failure(Status.ERROR, "Player cannot be null");
    }

    if (!UltraDaycare.config.getIncubators().isEnabled()) {
      return DaycareActionResult.failure(Status.ERROR, "Incubators module is disabled");
    }

    if (UltraDaycare.database == null) {
      return DaycareActionResult.failure(Status.ERROR, "Database is not initialized");
    }

    User user = UltraDaycare.database.getUser(player);
    if (user == null || user.getIncubatorSlots() == null) {
      return DaycareActionResult.failure(Status.ERROR, "User data not found");
    }

    if (slotIndex < 0 || slotIndex >= user.getIncubatorSlots().size()) {
      return DaycareActionResult.failure(Status.INCUBATOR_NOT_FOUND, "Incubator slot index out of range: " + slotIndex);
    }

    IncubatorSlot slot = user.getIncubatorSlots().get(slotIndex);
    if (slot == null || !slot.isHasIncubator()) {
      return DaycareActionResult.failure(Status.INCUBATOR_SLOT_EMPTY, "No incubator installed in slot " + (slotIndex + 1));
    }

    IncubatorHatchMode mode = UltraDaycare.config.getIncubators().getHatchMode();
    if (!slot.isHatchReady(mode)) {
      return DaycareActionResult.failure(Status.EGG_NOT_READY, "Egg is not ready to hatch yet in slot " + (slotIndex + 1));
    }

    Pokemon eggToHatch = slot.getEgg();
    slot.setEgg(null);
    slot.resetProgress();
    slot.setRemainingUses(slot.getRemainingUses() - 1);

    if (slot.getRemainingUses() <= 0) {
      slot.setHasIncubator(false);
    }

    user.markDirty();
    user.save();

    if (eggToHatch != null) {
      EggData.hatch(player, eggToHatch);
    }

    return DaycareActionResult.successIncubator("Egg hatched successfully from incubator slot " + (slotIndex + 1), slotIndex);
  }

  /**
   * Retrieves an assigned caretaker from an incubator slot back to the player's party.
   */
  public DaycareActionResult removeIncubatorCaretaker(@Nullable ServerPlayerEntity player, int slotIndex) {
    if (player == null) {
      return DaycareActionResult.failure(Status.ERROR, "Player cannot be null");
    }

    if (UltraDaycare.database == null) {
      return DaycareActionResult.failure(Status.ERROR, "Database is not initialized");
    }

    User user = UltraDaycare.database.getUser(player);
    if (user == null || user.getIncubatorSlots() == null) {
      return DaycareActionResult.failure(Status.ERROR, "User data not found");
    }

    if (slotIndex < 0 || slotIndex >= user.getIncubatorSlots().size()) {
      return DaycareActionResult.failure(Status.INCUBATOR_NOT_FOUND, "Incubator slot index out of range: " + slotIndex);
    }

    IncubatorSlot slot = user.getIncubatorSlots().get(slotIndex);
    if (slot == null || !slot.hasCaretaker()) {
      return DaycareActionResult.failure(Status.POKEMON_NOT_FOUND, "No caretaker assigned to slot " + (slotIndex + 1));
    }

    var party = Cobblemon.INSTANCE.getStorage().getParty(player);
    Pokemon caretaker = slot.getCaretaker();
    slot.setCaretaker(null);
    slot.resetProgress();

    party.add(caretaker);
    user.markDirty();
    user.save();

    return DaycareActionResult.successIncubator("Caretaker retrieved from incubator slot " + (slotIndex + 1), slotIndex);
  }
}
