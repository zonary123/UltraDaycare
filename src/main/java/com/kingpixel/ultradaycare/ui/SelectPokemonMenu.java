package com.kingpixel.cobbledaycare.ui;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.page.GooeyPage;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.item.PokemonItem;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbledaycare.CobbleDaycare;
import com.kingpixel.cobbledaycare.models.Plot;
import com.kingpixel.cobbledaycare.models.SelectGender;
import com.kingpixel.cobbledaycare.models.User;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.ItemModel;
import com.kingpixel.cobbleutils.Model.PanelsConfig;
import com.kingpixel.cobbleutils.Model.Rectangle;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.kingpixel.cobbleutils.util.PokemonUtils;
import lombok.Data;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Author: Carlos Varas Alonso - 11/03/2025 5:56
 * Refactor optimizado para mejor rendimiento.
 */
@Data
public class SelectPokemonMenu {
  private static final int POKEMONS_PER_PAGE = 45;

  private final int rows;
  private final String title;
  private final Rectangle rectangle;
  private final ItemModel previous;
  private final ItemModel close;
  private final ItemModel next;
  private final List<PanelsConfig> panels;

  public SelectPokemonMenu() {
    this.rows = 6;
    this.title = "&6Select Pokemon";
    this.rectangle = new Rectangle(rows);
    this.previous = new ItemModel(45, "minecraft:arrow", "&6Previous", Collections.emptyList(), 1);
    this.close = new ItemModel(49, "minecraft:barrier", "&cClose", Collections.emptyList(), 1);
    this.next = new ItemModel(53, "minecraft:arrow", "&6Next", Collections.emptyList(), 1);
    this.panels = new ArrayList<>();
    panels.add(new PanelsConfig(new ItemModel("minecraft:gray_stained_glass_pane"), rows));
  }

  public void open(ServerPlayerEntity player, Plot plot, User user, SelectGender gender, int position) {
    if (CobbleDaycare.config.hasOpenCooldown(player)) return;
    CobbleDaycare.getAsyncContext().runAsync(() -> {
      ChestTemplate template = ChestTemplate.builder(rows).build();
      PanelsConfig.applyConfig(template, panels);
      rectangle.apply(template);

      List<Button> buttons = getButtons(plot, player, gender, user);

      close.applyTemplate(template, close.getButton(action -> CobbleDaycare.language.getPlotMenu().open(player, plot, user, true)));

      if (position > 0) {
        previous.applyTemplate(template, previous.getButton(action -> open(player, plot, user, gender, Math.max(0, position - POKEMONS_PER_PAGE))));
      }

      if (buttons.size() > POKEMONS_PER_PAGE + position) {
        next.applyTemplate(template, next.getButton(action -> open(player, plot, user, gender, position + POKEMONS_PER_PAGE)));
      }

      GooeyPage page = GooeyPage.builder()
        .template(template)
        .title(AdventureTranslator.toNative(title))
        .build();

      List<Button> sublist = buttons.subList(position, Math.min(buttons.size(), position + POKEMONS_PER_PAGE));
      for (int i = 0; i < sublist.size(); i++) {
        template.set(i, sublist.get(i));
      }

      CobbleUtils.server.execute(() -> UIManager.openUIForcefully(player, page));
    });
  }

  private List<Button> getButtons(Plot plot, ServerPlayerEntity player, SelectGender gender, User user) {
    List<Button> buttons = new ArrayList<>();

    for (Pokemon pokemon : Cobblemon.INSTANCE.getStorage().getParty(player)) {
      if (pokemon != null && plot.canBreed(pokemon, gender)) {
        addPokemon(pokemon, plot, player, gender, user, buttons);
      }
    }

    var pc = Cobblemon.INSTANCE.getStorage().getPC(player);
    for (Pokemon pokemon : pc) {
      if (pokemon != null && plot.canBreed(pokemon, gender)) {
        addPokemon(pokemon, plot, player, gender, user, buttons);
      }
    }

    return buttons;
  }


  private void addPokemon(@NotNull Pokemon pokemon, Plot plot, ServerPlayerEntity player,
                          SelectGender gender, User user, List<Button> buttons) {
    ItemStack display = PokemonItem.from(pokemon);
    List<String> lore = PokemonUtils.replaceLore(pokemon);

    GooeyButton button = GooeyButton.builder()
      .display(display)
      .with(DataComponentTypes.CUSTOM_NAME, AdventureTranslator.toNative(PokemonUtils.getTranslatedName(pokemon)))
      .with(DataComponentTypes.LORE, new LoreComponent(AdventureTranslator.toNativeL(lore)))
      .onClick(action -> CobbleDaycare.getAsyncContext().runAsync(() -> {
        plot.addPokemon(player, pokemon, gender, user);
        CobbleDaycare.language.getPlotMenu().open(player, plot, user, true);
      }))
      .build();

    buttons.add(button);
  }
}
