package com.kingpixel.ultradaycare.ui;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.page.GooeyPage;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.pokemon.experience.SidemodExperienceSource;
import com.cobblemon.mod.common.item.PokemonItem;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.ItemModel;
import com.kingpixel.cobbleutils.api.EconomyApi;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.kingpixel.cobbleutils.util.PlayerUtils;
import com.kingpixel.cobbleutils.util.PokemonUtils;
import com.kingpixel.cobbleutils.util.TypeMessage;
import com.kingpixel.ultradaycare.UltraDaycare;
import com.kingpixel.ultradaycare.models.Plot;
import com.kingpixel.ultradaycare.models.SelectGender;
import com.kingpixel.ultradaycare.models.User;
import lombok.Data;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Carlos Varas Alonso - 11/03/2025 5:09
 */
@Data
public class PlotMenu {
  private static final String PRICE_LORE = "&7Price: &6%price%";
  private int rows;
  private String title;
  private ItemModel male;
  private ItemModel female;
  private ItemModel egg;
  private ItemModel close;
  private ItemModel claimXp;
  private ItemModel breedingEntrance;
  private ItemModel breedButton;

  public PlotMenu() {
    this.rows = 3;
    this.title = "&6Plot Menu";
    this.male = new ItemModel(10, "minecraft:light_blue_wool", "&6Male", List.of(""), 1);
    this.female = new ItemModel(16, "minecraft:pink_wool", "&6Female", List.of(""), 1);
    this.egg = new ItemModel(13, "minecraft:dragon_egg", "&6Egg", List.of(""), 1);
    this.close = new ItemModel(22, "minecraft:barrier", "&cClose", List.of(""), 1);
    this.claimXp = new ItemModel(2, "minecraft:experience_bottle", "&aClaim Pending XP",
      List.of("&7Pending XP: &6%xp%", PRICE_LORE, "", "&eClick to claim!"), 1);
    this.breedingEntrance = new ItemModel(13, "minecraft:gold_ingot", "&6Breeding Entrance Fee",
      List.of("&7You must pay to start breeding", PRICE_LORE, "", "&eClick to pay and start!"), 1);
    this.breedButton = new ItemModel(13, "minecraft:blaze_powder", "&e&lBreed Parents!",
      List.of("&7Click to breed the parents.", "&7Both parents will be &c&lCONSUMED &7permanently!", "", "&aClick to breed!"), 1);
  }

  public void open(ServerPlayerEntity player, Plot plot, User user) {
    open(player, plot, user, false);
  }

  public void open(ServerPlayerEntity player, Plot plot, User user, boolean force) {
    if (!force && UltraDaycare.config.hasOpenCooldown(player))
      return;
    UltraDaycare.getAsyncContext().runAsync(() -> {
      ChestTemplate template = ChestTemplate.builder(rows).build();

      if (plot.checkEgg(player, user))
        user.save();

      addParentButtons(template, plot, user);
      addEggButton(template, player, plot, user);
      addClaimXpButtons(template, player, plot, user);
      addCloseButton(template, player);

      GooeyPage page = GooeyPage.builder()
        .template(template)
        .title(AdventureTranslator.toNative(title))
        .build();

      CobbleUtils.server.execute(() -> UIManager.openUIForcefully(player, page));
    });
  }

  private void addParentButtons(ChestTemplate template, Plot plot, User user) {
    // Male
    GooeyButton mButton = GooeyButton.builder()
      .display(plot.getMale() != null ? PokemonItem.from(plot.getMale()) : male.getItemStack())
      .with(DataComponentTypes.LORE, new LoreComponent(AdventureTranslator.toNativeL(PokemonUtils.replaceLore(plot.getMale()))))
      .onClick(action -> handleParentClick(plot, user, SelectGender.MALE, action.getPlayer()))
      .build();
    male.applyTemplate(template, mButton);

    // Female
    GooeyButton fButton = GooeyButton.builder()
      .display(plot.getFemale() != null ? PokemonItem.from(plot.getFemale()) : female.getItemStack())
      .with(DataComponentTypes.LORE, new LoreComponent(AdventureTranslator.toNativeL(PokemonUtils.replaceLore(plot.getFemale()))))
      .onClick(action -> handleParentClick(plot, user, SelectGender.FEMALE, action.getPlayer()))
      .build();
    female.applyTemplate(template, fButton);
  }

  private void handleParentClick(Plot plot, User user, SelectGender gender, ServerPlayerEntity player) {
    Pokemon slotPokemon = gender == SelectGender.MALE ? plot.getMale() : plot.getFemale();

    if (slotPokemon != null) {
      String msg = gender == SelectGender.MALE ? UltraDaycare.language.getMessageRemovedMale() : UltraDaycare.language.getMessageRemovedFemale();
      PlayerUtils.sendMessage(player, PokemonUtils.replace(msg, slotPokemon).replace("%plot%", user.getIndexPlot(plot) + ""),
        UltraDaycare.language.getPrefix(), TypeMessage.CHAT);

      Pokemon toAdd = slotPokemon.clone(false, CobbleUtils.server.getRegistryManager());
      if (gender == SelectGender.MALE) plot.setMale(null);
      else plot.setFemale(null);

      plot.checkRefund(player);
      CobbleUtils.server.submit(() -> Cobblemon.INSTANCE.getStorage().getParty(player).add(toAdd));
      user.markDirty();
      open(player, plot, user, true);
    } else {
      UltraDaycare.language.getSelectPokemonMenu().open(player, plot, user, gender, 0);
    }
  }

  private void addEggButton(ChestTemplate template, ServerPlayerEntity player, Plot plot, User user) {
    if (plot.hasEggs()) {
      ItemStack displayEgg = PokemonItem.from(plot.getEggs().getFirst());
      GooeyButton eggButton = GooeyButton.builder()
        .display(displayEgg)
        .onClick(action -> {
          if (plot.giveEggs(player)) {
            if (plot.getEggs().size() >= plot.limitEggs(player)) plot.setTime(player);
            user.markDirty();
            open(player, plot, user, true);
          }
        })
        .build();
      egg.applyTemplate(template, eggButton);
    } else if (UltraDaycare.config.isEnableBreedingFee() && !plot.isBreedingPaid() && plot.hasTwoParents()) {
      double price = UltraDaycare.config.getBreedingFeePrice();
      List<String> lore = breedingEntrance.getLore().stream()
        .map(line -> line.replace("%price%", String.format("%.2f", price)))
        .toList();

      GooeyButton feeButton = GooeyButton.builder()
        .display(breedingEntrance.getItemStack())
        .with(DataComponentTypes.LORE, new LoreComponent(AdventureTranslator.toNativeL(lore)))
        .onClick(action -> {
          if (EconomyApi.hasEnoughMoney(player.getUuid(), BigDecimal.valueOf(price), UltraDaycare.config.getEconomyUse(), true)) {
            EconomyApi.removeMoney(player.getUuid(), BigDecimal.valueOf(price), UltraDaycare.config.getEconomyUse());
            plot.setBreedingPaid(true);
            user.markDirty();
            PlayerUtils.sendMessage(player, UltraDaycare.language.getMessageBreedingEntrancePaid().replace("%price%", String.valueOf(price)),
              UltraDaycare.language.getPrefix(), TypeMessage.CHAT);
            open(player, plot, user, true);
          }
        }).build();
      breedingEntrance.applyTemplate(template, feeButton);
    } else if (UltraDaycare.getActiveMode().consumeParents() && plot.hasTwoParents()) {
      GooeyButton breedBtn = GooeyButton.builder()
        .display(breedButton.getItemStack())
        .onClick(action -> {
          UltraDaycare.getActiveMode().onBreed(player, plot, user);
          open(player, plot, user, true);
        })
        .build();
      breedButton.applyTemplate(template, breedBtn);
    } else {
      GooeyButton eggButton = GooeyButton.builder()
        .display(egg.getItemStack())
        .onClick(action -> {
          // Do nothing or default passive plot display
        })
        .build();
      egg.applyTemplate(template, eggButton);
    }
  }


  private void addClaimXpButtons(ChestTemplate template, ServerPlayerEntity player, Plot plot, User user) {
    if (!UltraDaycare.config.isEnablePaidExperience()) return;
    // Progressive XP Buttons
    if (plot.getMale() != null && plot.getMalePendingXp() > 0 && plot.getMale().getLevel() < UltraDaycare.config.getMaxLevelTraining()) {
      template.set(male.getSlot() - 9, getClaimXpButton(player, plot, user, SelectGender.MALE));
    }
    if (plot.getFemale() != null && plot.getFemalePendingXp() > 0 && plot.getFemale().getLevel() < UltraDaycare.config.getMaxLevelTraining()) {
      template.set(female.getSlot() - 9, getClaimXpButton(player, plot, user, SelectGender.FEMALE));
    }
  }

  private GooeyButton getClaimXpButton(ServerPlayerEntity player, Plot plot, User user, SelectGender gender) {
    double pendingXp = gender == SelectGender.MALE ? plot.getMalePendingXp() : plot.getFemalePendingXp();

    // Paid XP claim
    double cost = pendingXp * UltraDaycare.config.getPricePerXp();

    List<String> lore = claimXp.getLore().stream()
      .map(line -> line.replace("%xp%", String.format("%.0f", pendingXp)).replace("%price%", String.format("%.2f", cost)))
      .toList();

    return GooeyButton.builder()
      .display(claimXp.getItemStack())
      .with(DataComponentTypes.LORE, new LoreComponent(AdventureTranslator.toNativeL(lore)))
      .onClick(action -> {
        Pokemon p = gender == SelectGender.MALE ? plot.getMale() : plot.getFemale();
        if (p == null) return;
        if (p.getLevel() >= UltraDaycare.config.getMaxLevelTraining()) {
          PlayerUtils.sendMessage(player, UltraDaycare.language.getMessageMaxLevelReached()
              .replace("%level%", String.valueOf(UltraDaycare.config.getMaxLevelTraining())),
            UltraDaycare.language.getPrefix(), TypeMessage.CHAT);
          return;
        }

        if (EconomyApi.hasEnoughMoney(player.getUuid(), BigDecimal.valueOf(cost), UltraDaycare.config.getEconomyUse(), true)) {
          EconomyApi.removeMoney(player.getUuid(), BigDecimal.valueOf(cost), UltraDaycare.config.getEconomyUse());
          p.addExperience(new SidemodExperienceSource(UltraDaycare.MOD_ID), (int) pendingXp);
          if (gender == SelectGender.MALE) plot.setMalePendingXp(0);
          else plot.setFemalePendingXp(0);
          user.markDirty();
          PlayerUtils.sendMessage(player, UltraDaycare.language.getMessageXpClaimed()
              .replace("%xp%", String.format("%.0f", pendingXp))
              .replace("%price%", String.format("%.2f", cost)),
            UltraDaycare.language.getPrefix(), TypeMessage.CHAT);
          open(player, plot, user, true);
        }
      }).build();
  }

  private void addCloseButton(ChestTemplate template, ServerPlayerEntity player) {
    template.set(close.getSlot(), close.getButton(action -> UltraDaycare.language.getPrincipalMenu().open(player), 1, TimeUnit.SECONDS, 1));
  }

}
