package com.kingpixel.ultradaycare.mechanics.pokemon;

import com.cobblemon.mod.common.CobblemonItems;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.PokemonPropertyExtractor;
import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.ultradaycare.UltraDaycare;
import com.kingpixel.ultradaycare.mechanics.Mechanics;
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
  private static final String REGION_BIAS_PREFIX = "region_bias=";

  private final Map<String, String> forms = new HashMap<>();
  private final List<EggForm> eggForms = new ArrayList<>();
  private final Set<String> blacklistForm = new HashSet<>();
  private final Set<String> blacklistFeatures = new HashSet<>();

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
    if (builder == null || builder.getEgg() == null) return;
    Pokemon female = builder.getFemale();
    Pokemon male = builder.getMale();
    Pokemon egg = builder.getEgg();
    Pokemon evo = builder.getFirstEvolution();

    if (female == null && male == null) return;
    Pokemon source = female;
    if (male != null && male.heldItem().getItem().equals(CobblemonItems.EVERSTONE)) {
      source = male;
    } else if (female != null && female.heldItem().getItem().equals(CobblemonItems.EVERSTONE)) {
      source = female;
    } else if (female == null) {
      source = male;
    }

    if (source == null) return;
    debug("[DayCareForm] applyEgg source={}", source.showdownId());
    processSourceForm(source, egg, evo);
  }

  @Override
  public void createEgg(ServerPlayerEntity player, Pokemon female, Pokemon egg) {
    if (female == null || egg == null) return;
    Pokemon evo = female;
    debug("[DayCareForm] createEgg pokemon={}", female.showdownId());
    processSourceForm(female, egg, evo);
  }

  private void processSourceForm(Pokemon source, Pokemon egg, Pokemon evo) {
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
  /* SOURCES                                                      */
  /* ------------------------------------------------------------ */

  private String getConfigForm(Pokemon pokemon) {
    if (pokemon == null) return null;
    for (EggForm eggForm : eggForms) {
      if (eggForm.getPokemons().contains(pokemon.showdownId())) {
        return eggForm.getForm();
      }
    }
    String formId = pokemon.getForm().formOnlyShowdownId();
    if (formId.isEmpty()) return null;

    String mappedForm = forms.get(formId.toLowerCase());
    return mappedForm != null ? mappedForm : formId;
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

  private String resolveFormName(Pokemon pokemon, String form) {
    if (pokemon == null || form == null || form.trim().isEmpty()) {
      return null;
    }
    String target = form.trim().toLowerCase();
    if (target.contains("=") || target.contains(" ")) {
      return target;
    }

    for (FormData f : pokemon.getSpecies().getForms()) {
      String fName = f.getName().toLowerCase();
      if (fName.equalsIgnoreCase(target) || fName.equalsIgnoreCase(forms.getOrDefault(target, target))) {
        return f.getName();
      }
    }

    String mapped = forms.get(target);
    if (mapped != null) {
      for (FormData f : pokemon.getSpecies().getForms()) {
        if (f.getName().equalsIgnoreCase(mapped)) {
          return f.getName();
        }
      }
    }

    return null;
  }

  private void applyForm(Pokemon egg, String form, Pokemon evo) {
    if (egg == null || form == null || form.isEmpty() || evo == null) return;

    String cleanForm = resolveFormName(evo, form);
    if (cleanForm == null) {
      cleanForm = form;
    }

    egg.getPersistentData().putString(TAG, cleanForm);
    applyFormToPokemon(evo, form);
  }

  private String formToRegionBias(String form) {
    if (form == null) return null;
    String clean = form.toLowerCase().trim();

    if (clean.contains("alola") || clean.contains("alolan")) return "alola";
    if (clean.contains("hisui") || clean.contains("hisuian")) return "hisui";
    if (clean.contains("galar") || clean.contains("galarian")) return "galar";
    if (clean.contains("paldea") || clean.contains("paldean")) return "paldea";
    return null;
  }

  private void applyFormToPokemon(Pokemon pokemon, String form) {
    if (pokemon == null || form == null || form.trim().isEmpty()) return;

    String targetForm = form.trim().toLowerCase();
    String regionBias = formToRegionBias(targetForm);

    StringBuilder query = new StringBuilder();
    query.append(targetForm);

    if (regionBias != null) {
      query.append(" ").append(REGION_BIAS_PREFIX).append(regionBias);
    }

    try {
      String queryStr = query.toString();
      PokemonProperties.Companion.parse(queryStr).apply(pokemon);
      debug("[DayCareForm] Applied properties '{}' to {}", queryStr, pokemon.showdownId());
    } catch (Exception e) {
      debug("[DayCareForm] Error applying properties '{}' to {}: {}", query.toString(), pokemon.showdownId(), e.getMessage());
    }
  }

  @Override
  public void applyHatch(HatchBuilder builder) {
    if (builder == null || builder.getEgg() == null || builder.getPokemon() == null) return;
    String form = builder.getEgg().getPersistentData().getString(TAG);
    debug("[DayCareForm] applyHatch '{}'", form);

    if (form != null && !form.isEmpty()) {
      applyFormToPokemon(builder.getPokemon(), form);
    }

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
