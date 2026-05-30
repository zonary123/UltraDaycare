package com.kingpixel.ultradaycare.mechanics;

import com.cobblemon.mod.common.CobblemonItems;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.PokemonPropertyExtractor;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.ultradaycare.UltraDaycare;
import com.kingpixel.ultradaycare.models.EggBuilder;
import com.kingpixel.ultradaycare.models.EggForm;
import com.kingpixel.ultradaycare.models.HatchBuilder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.*;

@EqualsAndHashCode(callSuper = true)
@Data
public class DayCareForm extends Mechanics {

  public static final String TAG = "form";

  private final Map<String, String> forms = new HashMap<>();
  private final List<EggForm> eggForms = new ArrayList<>();
  private final List<String> blacklistForm = new ArrayList<>();
  private final List<String> blacklistFeatures = new ArrayList<>();

  public DayCareForm() {
    validateData();

    eggForms.addAll(List.of(
      new EggForm("galarian", List.of("perrserker", "sirfetchd", "mrrime", "cursola", "runerigus", "obstagoon")),
      new EggForm("paldean", List.of("clodsire")),
      new EggForm("hisuian", List.of("overqwil", "sneasler"))
    ));

    blacklistForm.addAll(List.of("halloween", "disguised"));
    blacklistFeatures.addAll(List.of("netherite_coating", "disguised"));
  }

  /* ------------------------------------------------------------ */
  /* DEBUG                                                        */
  /* ------------------------------------------------------------ */

  private void debug(String msg, Object... args) {
    if (UltraDaycare.config.isDebug()) {
      UltraDaycare.LOGGER.info(msg, args);
    }
  }

  /* ------------------------------------------------------------ */
  /* APPLY EGG                                                    */
  /* ------------------------------------------------------------ */

  @Override
  public void applyEgg(EggBuilder builder) {
    Pokemon female = builder.getFemale();
    Pokemon male = builder.getMale();
    Pokemon egg = builder.getEgg();
    Pokemon evo = builder.getFirstEvolution();

    Pokemon source = male.heldItem().getItem().equals(CobblemonItems.EVERSTONE) ? male : female;
    debug("[DayCareForm] applyEgg source={}", source.showdownId());

    String configForm = getConfigForm(source);
    if (configForm != null) {
      if (!isBlacklisted(configForm)) {
        applyForm(egg, configForm, evo);
      } else {
        applyForm(egg, "", evo);
      }
      return;
    }

    var props = source.createPokemonProperties(
      PokemonPropertyExtractor.FORM,
      PokemonPropertyExtractor.ASPECTS
    );

    if (props.getForm() != null && blacklistForm.contains(props.getForm())) {
      props.setForm("");
    }
    Set<String> aspects = new HashSet<>(props.getAspects());
    aspects.removeIf(aspect -> {
      if (aspect.startsWith("gender=") || aspect.startsWith("shiny=")) return true;
      for (String blacklisted : blacklistFeatures) {
        if (aspect.contains(blacklisted)) return true;
      }
      return false;
    });
    props.setAspects(aspects);

    String formStr = props.asString(" ");
    applyForm(egg, formStr, evo);
  }

  /* ------------------------------------------------------------ */
  /* CREATE EGG                                                   */
  /* ------------------------------------------------------------ */

  @Override
  public void createEgg(ServerPlayerEntity player, Pokemon female, Pokemon egg) {
    Pokemon evo = female;
    debug("[DayCareForm] createEgg pokemon={}", female.showdownId());

    String configForm = getConfigForm(female);
    if (configForm != null) {
      if (!isBlacklisted(configForm)) {
        applyForm(egg, configForm, evo);
      } else {
        applyForm(egg, "", evo);
      }
      return;
    }

    var props = female.createPokemonProperties(
      PokemonPropertyExtractor.FORM,
      PokemonPropertyExtractor.ASPECTS
    );

    if (props.getForm() != null && blacklistForm.contains(props.getForm())) {
      props.setForm("");
    }
    Set<String> aspects = new HashSet<>(props.getAspects());
    aspects.removeIf(aspect -> {
      if (aspect.startsWith("gender=") || aspect.startsWith("shiny=")) return true;
      for (String blacklisted : blacklistFeatures) {
        if (aspect.contains(blacklisted)) return true;
      }
      return false;
    });
    props.setAspects(aspects);

    String formStr = props.asString(" ");
    applyForm(egg, formStr, evo);
  }

  /* ------------------------------------------------------------ */
  /* SOURCES                                                      */
  /* ------------------------------------------------------------ */

  private String getConfigForm(Pokemon pokemon) {
    for (EggForm eggForm : eggForms) {
      if (eggForm.getPokemons().contains(pokemon.showdownId())) {
        return eggForm.getForm();
      }
    }
    return forms.get(pokemon.getForm().formOnlyShowdownId());
  }

  /* ------------------------------------------------------------ */
  /* BLACKLIST                                                    */
  /* ------------------------------------------------------------ */

  private boolean isBlacklisted(String... values) {
    for (String v : values) {
      if (blacklistForm.contains(v) || blacklistFeatures.contains(v)) {
        return true;
      }
    }
    return false;
  }

  /* ------------------------------------------------------------ */
  /* APPLY / HATCH                                                */
  /* ------------------------------------------------------------ */

  private void applyForm(Pokemon egg, String form, Pokemon evo) {
    egg.getPersistentData().putString(TAG, form);
    PokemonProperties.Companion.parse(form).apply(evo);
  }

  @Override
  public void applyHatch(HatchBuilder builder) {
    String form = builder.getEgg().getPersistentData().getString(TAG);
    debug("[DayCareForm] applyHatch '{}'", form);

    PokemonProperties.Companion.parse(form).apply(builder.getPokemon());
    UltraDaycare.fixBreedable(builder.getPokemon());
    builder.getEgg().getPersistentData().remove(TAG);
  }

  /* ------------------------------------------------------------ */

  @Override
  public String getEggInfo(String s, NbtCompound nbt) {
    return s.replace("%form%", nbt.getString(TAG));
  }

  @Override
  public void validateData() {
    forms.putAll(Map.of(
      "galar", "galarian",
      "paldea", "paldean",
      "hisui", "hisuian",
      "alola", "alolan"
    ));
  }

  @Override
  public String fileName() {
    return "form";
  }

  @Override
  public String replace(String text, ServerPlayerEntity player) {
    return text;
  }
}
