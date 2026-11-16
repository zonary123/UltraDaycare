package com.kingpixel.ultradaycare;

import com.kingpixel.ultradaycare.api.DaycareApi;
import com.kingpixel.ultradaycare.api.action.DaycareActionResult;
import com.kingpixel.ultradaycare.api.action.DaycareActionResult.Status;
import com.kingpixel.ultradaycare.api.model.DaycareEggView;
import com.kingpixel.ultradaycare.api.model.DaycarePokemonView;
import com.kingpixel.ultradaycare.api.model.DaycareProfileContext;
import com.kingpixel.ultradaycare.api.model.IncubatorSlotView;
import com.kingpixel.ultradaycare.api.model.PlotView;
import com.kingpixel.ultradaycare.api.ui.DaycareMenuProvider;
import com.kingpixel.ultradaycare.api.ui.DaycareMenuRegistry;
import com.kingpixel.ultradaycare.api.ui.DefaultGooeyDaycareMenuProvider;
import com.kingpixel.ultradaycare.models.SelectGender;
import net.minecraft.server.network.ServerPlayerEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestDaycareApi {

  @BeforeEach
  @AfterEach
  void resetRegistry() {
    DaycareApi.unregisterMenuProvider();
  }

  @Test
  void testActionResultSuccess() {
    DaycareActionResult result = DaycareActionResult.success("Success");
    assertTrue(result.isSuccess());
    assertEquals(Status.SUCCESS, result.getStatus());
    assertEquals("Success", result.getMessage());

    DaycareActionResult plotResult = DaycareActionResult.successPlot("Plot 1 ready", 0);
    assertTrue(plotResult.isSuccess());
    assertEquals(0, plotResult.getPlotIndex());

    DaycareActionResult incResult = DaycareActionResult.successIncubator("Incubator 2 active", 1);
    assertTrue(incResult.isSuccess());
    assertEquals(1, incResult.getSlotIndex());

    DaycareActionResult countResult = DaycareActionResult.successCount("Collected eggs", 3);
    assertTrue(countResult.isSuccess());
    assertEquals(3, countResult.getCount());
  }

  @Test
  void testActionResultFailure() {
    DaycareActionResult failure = DaycareActionResult.failure(Status.PLOT_LOCKED, "Plot locked", 2);
    assertFalse(failure.isSuccess());
    assertEquals(Status.PLOT_LOCKED, failure.getStatus());
    assertEquals("Plot locked", failure.getMessage());
    assertEquals(2, failure.getPlotIndex());
  }

  @Test
  void testMenuProviderRegistryLifecycle() {
    assertFalse(DaycareApi.hasCustomMenuProvider());
    assertTrue(DaycareMenuRegistry.getProvider() instanceof DefaultGooeyDaycareMenuProvider);

    TestCustomDaycareMenuProvider custom = new TestCustomDaycareMenuProvider();
    DaycareApi.registerMenuProvider(custom);

    assertTrue(DaycareApi.hasCustomMenuProvider());
    assertEquals(custom, DaycareMenuRegistry.getProvider());

    DaycareApi.openPrincipalMenu((ServerPlayerEntity) null);
    DaycareApi.openPrincipalMenu(null, null);
    DaycareApi.openPlotMenu(null, 0);
    DaycareApi.openIncubatorsMenu((ServerPlayerEntity) null);
    DaycareApi.openIncubatorsMenu(null, null);
    DaycareApi.openProfileMenu(null);

    DaycareApi.unregisterMenuProvider();
    assertFalse(DaycareApi.hasCustomMenuProvider());
    assertTrue(DaycareMenuRegistry.getProvider() instanceof DefaultGooeyDaycareMenuProvider);
  }

  @Test
  void testViewModels() {
    UUID pUuid = UUID.randomUUID();
    DaycarePokemonView pokemon = DaycarePokemonView.builder()
      .pokemonUuid(pUuid)
      .species("Ditto")
      .displayName("Ditto")
      .form("default")
      .gender("GENDERLESS")
      .level(50)
      .shiny(true)
      .holdingItem("Destiny Knot")
      .nature("Modest")
      .ability("Imposter")
      .lore(List.of("Shiny Ditto"))
      .build();

    assertEquals(pUuid, pokemon.getPokemonUuid());
    assertEquals("Ditto", pokemon.getSpecies());
    assertEquals(50, pokemon.getLevel());
    assertTrue(pokemon.isShiny());
    assertEquals("Destiny Knot", pokemon.getHoldingItem());

    UUID eUuid = UUID.randomUUID();
    DaycareEggView egg = DaycareEggView.builder()
      .eggUuid(eUuid)
      .species("Charmander")
      .shiny(false)
      .currentSteps(500)
      .targetSteps(1000)
      .progressPercentage(50.0)
      .timeToHatchMillis(3600000L)
      .formattedRemainingTime("500/1000 steps")
      .build();

    assertEquals(eUuid, egg.getEggUuid());
    assertEquals("Charmander", egg.getSpecies());
    assertEquals(500, egg.getCurrentSteps());
    assertEquals(1000, egg.getTargetSteps());
    assertEquals(50.0, egg.getProgressPercentage(), 0.001);

    PlotView plot = PlotView.builder()
      .plotIndex(0)
      .unlocked(true)
      .male(pokemon)
      .female(null)
      .malePendingXp(120.0)
      .femalePendingXp(0.0)
      .hasEggs(true)
      .eggsCount(1)
      .maxEggs(3)
      .breedingPaid(true)
      .timeToHatchMillis(60000L)
      .formattedCooldown("1m")
      .eggs(List.of(egg))
      .build();

    assertEquals(0, plot.getPlotIndex());
    assertTrue(plot.isUnlocked());
    assertNotNull(plot.getMale());
    assertNull(plot.getFemale());
    assertEquals(120.0, plot.getMalePendingXp(), 0.001);
    assertTrue(plot.isHasEggs());
    assertEquals(1, plot.getEggsCount());
    assertEquals(3, plot.getMaxEggs());
    assertTrue(plot.isBreedingPaid());

    IncubatorSlotView incSlot = IncubatorSlotView.builder()
      .slotIndex(0)
      .unlocked(true)
      .hasIncubator(true)
      .remainingUses(5)
      .caretakerName("Magmar")
      .caretaker(pokemon)
      .egg(egg)
      .progressPercentage(50)
      .readyToHatch(false)
      .speedMultiplier(1.5)
      .build();

    assertEquals(0, incSlot.getSlotIndex());
    assertTrue(incSlot.isUnlocked());
    assertTrue(incSlot.isHasIncubator());
    assertEquals(5, incSlot.getRemainingUses());
    assertEquals("Magmar", incSlot.getCaretakerName());
    assertEquals(50, incSlot.getProgressPercentage());
    assertFalse(incSlot.isReadyToHatch());
    assertEquals(1.5, incSlot.getSpeedMultiplier(), 0.001);

    UUID playerUuid = UUID.randomUUID();
    DaycareProfileContext profile = DaycareProfileContext.builder()
      .playerUuid(playerUuid)
      .playerName("TrainerRed")
      .totalPlots(1)
      .unlockedPlotsCount(1)
      .activeEggsInPlots(1)
      .totalIncubatorsCount(1)
      .incubatorsReadyToHatch(0)
      .plots(List.of(plot))
      .incubatorSlots(List.of(incSlot))
      .storedEggs(List.of(egg))
      .build();

    assertEquals(playerUuid, profile.getPlayerUuid());
    assertEquals("TrainerRed", profile.getPlayerName());
    assertEquals(1, profile.getTotalPlots());
    assertEquals(1, profile.getUnlockedPlotsCount());
    assertEquals(1, profile.getActiveEggsInPlots());
    assertEquals(1, profile.getTotalIncubatorsCount());
    assertEquals(0, profile.getIncubatorsReadyToHatch());
    assertEquals(1, profile.getPlots().size());
    assertEquals(1, profile.getIncubatorSlots().size());
    assertEquals(1, profile.getStoredEggs().size());
  }

  @Test
  void testHeadlessActionsNullSafety() {
    DaycareActionResult deposit = DaycareApi.depositParent(null, 0, UUID.randomUUID(), SelectGender.MALE);
    assertFalse(deposit.isSuccess());
    assertEquals(Status.ERROR, deposit.getStatus());

    DaycareActionResult withdraw = DaycareApi.withdrawParent(null, 0, SelectGender.MALE);
    assertFalse(withdraw.isSuccess());
    assertEquals(Status.ERROR, withdraw.getStatus());

    DaycareActionResult collect = DaycareApi.collectEggs(null, 0);
    assertFalse(collect.isSuccess());
    assertEquals(Status.ERROR, collect.getStatus());

    DaycareActionResult claimXp = DaycareApi.claimPendingXp(null, 0, SelectGender.MALE);
    assertFalse(claimXp.isSuccess());

    DaycareActionResult fee = DaycareApi.payBreedingFee(null, 0);
    assertFalse(fee.isSuccess());

    DaycareActionResult buy = DaycareApi.buyIncubator(null);
    assertFalse(buy.isSuccess());

    DaycareActionResult hatch = DaycareApi.hatchIncubatorEgg(null, 0);
    assertFalse(hatch.isSuccess());

    DaycareActionResult caretaker = DaycareApi.removeIncubatorCaretaker(null, 0);
    assertFalse(caretaker.isSuccess());
  }

  @Test
  void testHeadlessContextNullSafety() {
    assertNull(DaycareApi.getPlayerProfile(null));
    assertNotNull(DaycareApi.getPlotViews(null));
    assertTrue(DaycareApi.getPlotViews(null).isEmpty());
    assertNull(DaycareApi.getPlotView(null, 0));
    assertNotNull(DaycareApi.getIncubatorViews(null));
    assertTrue(DaycareApi.getIncubatorViews(null).isEmpty());
    assertNull(DaycareApi.getIncubatorView(null, 0));
    assertNotNull(DaycareApi.getStoredEggs(null));
    assertTrue(DaycareApi.getStoredEggs(null).isEmpty());
  }

  @Test
  void testServiceGetters() {
    assertNotNull(DaycareApi.getActionService());
    assertNotNull(DaycareApi.getContextService());
  }

  private static class TestCustomDaycareMenuProvider implements DaycareMenuProvider {
    @Override
    public void openPrincipalMenu(ServerPlayerEntity player) {}

    @Override
    public void openPrincipalMenu(ServerPlayerEntity viewer, ServerPlayerEntity target) {}

    @Override
    public void openPlotMenu(ServerPlayerEntity player, int plotIndex) {}

    @Override
    public void openIncubatorsMenu(ServerPlayerEntity player) {}

    @Override
    public void openIncubatorsMenu(ServerPlayerEntity viewer, ServerPlayerEntity target) {}

    @Override
    public void openProfileMenu(ServerPlayerEntity player) {}
  }
}
