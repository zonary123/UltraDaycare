package com.kingpixel.ultradaycare.application;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.util.PlayerUtils;
import com.kingpixel.ultradaycare.UltraDaycare;
import com.kingpixel.ultradaycare.api.model.DaycareEggView;
import com.kingpixel.ultradaycare.api.model.DaycarePokemonView;
import com.kingpixel.ultradaycare.api.model.DaycareProfileContext;
import com.kingpixel.ultradaycare.api.model.IncubatorSlotView;
import com.kingpixel.ultradaycare.api.model.PlotView;
import com.kingpixel.ultradaycare.config.IncubatorConfig;
import com.kingpixel.ultradaycare.config.IncubatorHatchMode;
import com.kingpixel.ultradaycare.mechanics.pokemon.DayCarePokemon;
import com.kingpixel.ultradaycare.models.IncubatorSlot;
import com.kingpixel.ultradaycare.models.Plot;
import com.kingpixel.ultradaycare.models.User;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Application service providing query and context projection for UltraDaycare.
 * Constructs read-only ViewModels and player profiles for headless UI integrations.
 */
public class DaycareContextService {

  /**
   * Retrieves the comprehensive daycare profile context for a player.
   */
  @Nullable
  public DaycareProfileContext getPlayerProfile(@Nullable ServerPlayerEntity player) {
    if (player == null || UltraDaycare.database == null) {
      return null;
    }

    User user = UltraDaycare.database.getUser(player);
    if (user == null) {
      return null;
    }

    List<PlotView> plotViews = getPlotViews(player);
    List<IncubatorSlotView> incubatorViews = getIncubatorViews(player);
    List<DaycareEggView> storedEggs = getStoredEggs(player);

    int unlockedPlots = (int) plotViews.stream().filter(PlotView::isUnlocked).count();
    int activeEggsInPlots = plotViews.stream().mapToInt(PlotView::getEggsCount).sum();
    int totalIncubators = (int) incubatorViews.stream().filter(IncubatorSlotView::isHasIncubator).count();
    int readyToHatch = (int) incubatorViews.stream().filter(IncubatorSlotView::isReadyToHatch).count();

    return DaycareProfileContext.builder()
      .playerUuid(player.getUuid())
      .playerName(player.getGameProfile().getName())
      .totalPlots(plotViews.size())
      .unlockedPlotsCount(unlockedPlots)
      .activeEggsInPlots(activeEggsInPlots)
      .totalIncubatorsCount(totalIncubators)
      .incubatorsReadyToHatch(readyToHatch)
      .plots(plotViews)
      .incubatorSlots(incubatorViews)
      .storedEggs(storedEggs)
      .build();
  }

  /**
   * Retrieves all plot view models for a player.
   */
  @NotNull
  public List<PlotView> getPlotViews(@Nullable ServerPlayerEntity player) {
    if (player == null || UltraDaycare.database == null) {
      return List.of();
    }

    User user = UltraDaycare.database.getUser(player);
    if (user == null || user.getPlots() == null) {
      return List.of();
    }

    int slotSize = UltraDaycare.config != null && UltraDaycare.config.getSlotPlots() != null
      ? UltraDaycare.config.getSlotPlots().size()
      : user.getPlots().size();

    List<PlotView> views = new ArrayList<>(slotSize);
    for (int i = 0; i < slotSize; i++) {
      views.add(buildPlotView(player, user, i));
    }
    return views;
  }

  /**
   * Retrieves a single plot view model.
   */
  @Nullable
  public PlotView getPlotView(@Nullable ServerPlayerEntity player, int plotIndex) {
    if (player == null || UltraDaycare.database == null || plotIndex < 0) {
      return null;
    }

    User user = UltraDaycare.database.getUser(player);
    if (user == null || user.getPlots() == null || plotIndex >= user.getPlots().size()) {
      return null;
    }

    return buildPlotView(player, user, plotIndex);
  }

  /**
   * Retrieves all incubator slot view models for a player.
   */
  @NotNull
  public List<IncubatorSlotView> getIncubatorViews(@Nullable ServerPlayerEntity player) {
    if (player == null || UltraDaycare.database == null) {
      return List.of();
    }

    User user = UltraDaycare.database.getUser(player);
    if (user == null || user.getIncubatorSlots() == null) {
      return List.of();
    }

    IncubatorConfig config = UltraDaycare.config != null ? UltraDaycare.config.getIncubators() : null;
    int slotSize = (config != null && config.getSlots() != null)
      ? config.getSlots().size()
      : user.getIncubatorSlots().size();

    List<IncubatorSlotView> views = new ArrayList<>(slotSize);
    for (int i = 0; i < slotSize; i++) {
      views.add(buildIncubatorView(player, user, i, config));
    }
    return views;
  }

  /**
   * Retrieves a single incubator slot view model.
   */
  @Nullable
  public IncubatorSlotView getIncubatorView(@Nullable ServerPlayerEntity player, int slotIndex) {
    if (player == null || UltraDaycare.database == null || slotIndex < 0) {
      return null;
    }

    User user = UltraDaycare.database.getUser(player);
    if (user == null || user.getIncubatorSlots() == null || slotIndex >= user.getIncubatorSlots().size()) {
      return null;
    }

    IncubatorConfig config = UltraDaycare.config != null ? UltraDaycare.config.getIncubators() : null;
    return buildIncubatorView(player, user, slotIndex, config);
  }

  /**
   * Retrieves stored eggs for a player.
   */
  @NotNull
  public List<DaycareEggView> getStoredEggs(@Nullable ServerPlayerEntity player) {
    if (player == null || UltraDaycare.database == null) {
      return List.of();
    }

    User user = UltraDaycare.database.getUser(player);
    if (user == null || user.getStoredEggs() == null) {
      return List.of();
    }

    List<DaycareEggView> list = new ArrayList<>(user.getStoredEggs().size());
    for (Pokemon egg : user.getStoredEggs()) {
      DaycareEggView view = toEggView(egg);
      if (view != null) {
        list.add(view);
      }
    }
    return list;
  }

  @NotNull
  private PlotView buildPlotView(@NotNull ServerPlayerEntity player, @NotNull User user, int plotIndex) {
    boolean unlocked = UltraDaycare.hasPermission(player, Plot.plotPermission(plotIndex), 2)
      && plotIndex < user.getPlots().size();

    Plot plot = plotIndex < user.getPlots().size() ? user.getPlots().get(plotIndex) : null;
    if (plot == null) {
      return PlotView.builder()
        .plotIndex(plotIndex)
        .unlocked(unlocked)
        .male(null)
        .female(null)
        .malePendingXp(0.0)
        .femalePendingXp(0.0)
        .hasEggs(false)
        .eggsCount(0)
        .maxEggs(0)
        .breedingPaid(false)
        .timeToHatchMillis(0L)
        .formattedCooldown("Locked")
        .eggs(List.of())
        .build();
    }

    DaycarePokemonView maleView = toPokemonView(plot.getMale());
    DaycarePokemonView femaleView = toPokemonView(plot.getFemale());

    List<DaycareEggView> eggViews = new ArrayList<>();
    if (plot.getEggs() != null) {
      for (Pokemon egg : plot.getEggs()) {
        DaycareEggView ev = toEggView(egg);
        if (ev != null) {
          eggViews.add(ev);
        }
      }
    }

    String cooldown = PlayerUtils.getCooldown(plot.getTimeToHatch());
    int maxEggs = plot.limitEggs(player);

    return PlotView.builder()
      .plotIndex(plotIndex)
      .unlocked(unlocked)
      .male(maleView)
      .female(femaleView)
      .malePendingXp(plot.getMalePendingXp())
      .femalePendingXp(plot.getFemalePendingXp())
      .hasEggs(plot.hasEggs())
      .eggsCount(plot.getEggs().size())
      .maxEggs(maxEggs)
      .breedingPaid(plot.isBreedingPaid())
      .timeToHatchMillis(plot.getTimeToHatch())
      .formattedCooldown(cooldown != null ? cooldown : "")
      .eggs(eggViews)
      .build();
  }

  @NotNull
  private IncubatorSlotView buildIncubatorView(@NotNull ServerPlayerEntity player,
                                              @NotNull User user,
                                              int slotIndex,
                                              @Nullable IncubatorConfig config) {
    String perm = IncubatorConfig.incubatorSlotPermission(slotIndex);
    boolean unlocked = UltraDaycare.hasPermission(player, perm, 2)
      && slotIndex < user.getIncubatorSlots().size();

    IncubatorSlot slot = slotIndex < user.getIncubatorSlots().size()
      ? user.getIncubatorSlots().get(slotIndex)
      : null;

    if (slot == null) {
      return IncubatorSlotView.builder()
        .slotIndex(slotIndex)
        .unlocked(unlocked)
        .hasIncubator(false)
        .remainingUses(0)
        .caretakerName(null)
        .caretaker(null)
        .egg(null)
        .progressPercentage(0)
        .readyToHatch(false)
        .speedMultiplier(1.0)
        .build();
    }

    IncubatorHatchMode mode = config != null ? config.getHatchMode() : IncubatorHatchMode.TIME;
    boolean ready = slot.isHatchReady(mode);
    int progress = slot.getProgressPercentage(mode);
    double speedMult = slot.getSpeedMultiplier();

    String caretakerName = slot.getCaretaker() != null
      ? slot.getCaretaker().getDisplayName(false).getString()
      : null;

    DaycarePokemonView caretakerView = toPokemonView(slot.getCaretaker());
    DaycareEggView eggView = toEggView(slot.getEgg());

    return IncubatorSlotView.builder()
      .slotIndex(slotIndex)
      .unlocked(unlocked)
      .hasIncubator(slot.isHasIncubator())
      .remainingUses(slot.getRemainingUses())
      .caretakerName(caretakerName)
      .caretaker(caretakerView)
      .egg(eggView)
      .progressPercentage(progress)
      .readyToHatch(ready)
      .speedMultiplier(speedMult)
      .build();
  }

  @Nullable
  public static DaycarePokemonView toPokemonView(@Nullable Pokemon pokemon) {
    if (pokemon == null) return null;

    String species = pokemon.getSpecies() != null ? pokemon.getSpecies().getName() : "Unknown";
    String displayName = pokemon.getDisplayName(false).getString();
    String form = pokemon.getForm() != null ? pokemon.getForm().getName() : "default";
    String gender = pokemon.getGender() != null ? pokemon.getGender().name() : "GENDERLESS";
    int level = pokemon.getLevel();
    boolean shiny = pokemon.getShiny();
    String heldItem = (pokemon.heldItem() != null && !pokemon.heldItem().isEmpty())
      ? pokemon.heldItem().getName().getString()
      : "None";
    String nature = pokemon.getNature() != null ? pokemon.getNature().getDisplayName() : "Unknown";
    String ability = pokemon.getAbility() != null ? pokemon.getAbility().getDisplayName() : "Unknown";

    return DaycarePokemonView.builder()
      .pokemonUuid(pokemon.getUuid())
      .species(species)
      .displayName(displayName)
      .form(form)
      .gender(gender)
      .level(level)
      .shiny(shiny)
      .holdingItem(heldItem)
      .nature(nature)
      .ability(ability)
      .lore(List.of())
      .build();
  }

  @Nullable
  public static DaycareEggView toEggView(@Nullable Pokemon egg) {
    if (egg == null) return null;

    String species = egg.getPersistentData().getString(DayCarePokemon.TAG_POKEMON);
    if (species == null || species.isEmpty()) {
      species = egg.getSpecies() != null ? egg.getSpecies().getName() : "Egg";
    }

    double remainingSteps = egg.getPersistentData().getDouble(DayCarePokemon.TAG_STEPS);
    double referenceSteps = egg.getPersistentData().getDouble(DayCarePokemon.TAG_REFERENCE_STEPS);
    int targetSteps = (int) referenceSteps;
    int currentSteps = (int) Math.max(0, referenceSteps - remainingSteps);
    double pct = targetSteps > 0 ? Math.min(100.0, ((double) currentSteps * 100.0) / targetSteps) : 0.0;

    return DaycareEggView.builder()
      .eggUuid(egg.getUuid())
      .species(species)
      .shiny(egg.getShiny())
      .currentSteps(currentSteps)
      .targetSteps(targetSteps)
      .progressPercentage(pct)
      .timeToHatchMillis(0L)
      .formattedRemainingTime(currentSteps + "/" + targetSteps + " steps")
      .build();
  }
}
