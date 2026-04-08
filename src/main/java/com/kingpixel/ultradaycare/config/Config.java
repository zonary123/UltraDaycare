package com.kingpixel.cobbledaycare.config;

import com.cobblemon.mod.common.api.pokemon.egg.EggGroup;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbledaycare.CobbleDaycare;
import com.kingpixel.cobbledaycare.models.UserInfoOptions;
import com.kingpixel.cobbleutils.Model.*;
import com.kingpixel.cobbleutils.util.PlayerUtils;
import com.kingpixel.cobbleutils.util.TypeMessage;
import com.kingpixel.cobbleutils.util.UtilsFile;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.network.ServerPlayerEntity;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * @author Carlos Varas Alonso - 30/01/2025 23:47
 */
@Getter
@Setter
public class Config {
  private static Map<UUID, Long> cooldownsOpenMenus = new HashMap<>();
  private static final String VIP_COOLDOWN = "cooldown.vip";
  private static final String LEGENDARY_COOLDOWN = "cooldown.legendary";
  private static final String MASTER_COOLDOWN = "cooldown.master";

  // System & Meta
  private boolean debug;
  private String lang;
  private DataBaseConfig dataBase;
  private UserInfoOptions userInfoOptions;
  private String commandEggInfo;
  private List<String> commands;

  // Mechanics General
  private boolean fixIlegalAbilities;
  private boolean canUseNativeGUI;
  private boolean showIvs;
  private boolean spawnEggWorld;
  private int raritySpawnEgg;
  private boolean allowElytra;

  // Breeding Core
  private boolean dobbleDitto;
  private FilterPokemons dobbleDittoFilter;
  private PokemonBlackList blackList;
  private List<String> whitelist;
  private Map<String, Integer> limitEggs;

  // Movement & Progress
  private long ticksToWalking;
  private boolean globalMultiplierSteps;
  private float multiplierSteps;
  private Map<String, Float> multiplierStepsPermission;
  private double defaultSteps;
  private Map<EggGroup, Double> steps;
  private double multiplierAbilityAcceleration;
  private List<String> abilityAcceleration;
  private double reduceEggStepsVehicle;
  private List<String> permittedVehicles;

  // Cooldowns
  private DurationValue cooldown;
  private Map<String, DurationValue> cooldowns;
  private DurationValue defaultCooldownBreed;
  private Map<String, DurationValue> cooldownsBreed;
  private DurationValue defaultCooldownHatch;
  private Map<String, DurationValue> cooldownsHatch;
  private DurationValue cooldownToOpenMenus;
  private List<Integer> slotPlots;

  // Economy & Paid Features
  private EconomyUse economyUse = new EconomyUse("IMPACTOR", "impactor:dollars");
  
  // -- Paid Experience (Instant) --
  private boolean enablePaidExperience;
  private double payXpPrice;
  private int payXpAmount;
  private int maxLevelTraining;

  // -- NEW: Progressive Experience (Pending) --
  private double xpPerStep;
  private double pricePerXp;

  // -- NEW: Breeding Access Fee --
  private boolean enableBreedingFee;
  private double breedingFeePrice;

  // -- NEW: Offline Breeding --
  private boolean enableOfflineBreeding;
  private DurationValue offlineBreedingInterval;
  private int maxOfflineEggsPerPlot;

  public Config() {
    this.debug = false;
    this.fixIlegalAbilities = true;
    this.canUseNativeGUI = false;
    this.lang = "en";
    this.userInfoOptions = new UserInfoOptions();
    this.dataBase = new DataBaseConfig();
    this.showIvs = false;
    this.dobbleDitto = false;
    this.spawnEggWorld = true;
    this.allowElytra = true;
    this.commands = List.of("daycare");
    this.commandEggInfo = "egginfo";
    this.globalMultiplierSteps = false;
    this.multiplierAbilityAcceleration = 1.0;
    this.dataBase.setDatabase("ultradaycare");
    this.defaultSteps = 128D;
    this.steps = new EnumMap<>(EggGroup.class);
    for (EggGroup value : EggGroup.values()) {
      this.steps.put(value, defaultSteps);
    }
    this.blackList = new PokemonBlackList();
    this.blackList.getPokemons().add("egg");
    this.blackList.getLabels().add("basculegion");
    this.blackList.getLabels().add("legendary");
    this.limitEggs = new HashMap<>();
    this.limitEggs.put("", 1);
    this.limitEggs.put("group.vip", 2);
    this.abilityAcceleration = List.of("magmaarmor",
      "flamebody",
      "steamengine");
    this.reduceEggStepsVehicle = 2f;
    this.multiplierSteps = 1.0f;
    this.multiplierStepsPermission = new HashMap<>();
    this.multiplierStepsPermission.put("multipliersteps.vip", 2.0f);
    this.permittedVehicles = List.of("minecraft:boat", "minecraft:horse", "cobblemon:pokemon");
    this.cooldownToOpenMenus = DurationValue.parse("0.5s");
    this.cooldown = DurationValue.parse("3m");
    this.cooldowns = Map.of(
      VIP_COOLDOWN, DurationValue.parse("15m"),
      LEGENDARY_COOLDOWN, DurationValue.parse("10m"),
      MASTER_COOLDOWN, DurationValue.parse("5m")
    );
    this.ticksToWalking = 20;
    this.slotPlots = new ArrayList<>();
    this.slotPlots.add(10);
    this.slotPlots.add(12);
    this.slotPlots.add(14);
    this.slotPlots.add(16);
    this.raritySpawnEgg = 2048;
    this.defaultCooldownBreed = DurationValue.parse("60s");
    this.cooldownsBreed = new HashMap<>();
    this.cooldownsBreed.put(VIP_COOLDOWN, DurationValue.parse("30s"));
    this.defaultCooldownHatch = DurationValue.parse("60s");
    this.cooldownsHatch = new HashMap<>();
    this.cooldownsHatch.put(VIP_COOLDOWN, DurationValue.parse("30s"));
    this.whitelist = new ArrayList<>();
    this.dobbleDittoFilter = new FilterPokemons();
    this.maxLevelTraining = 100;
    this.enableBreedingFee = false;
    this.breedingFeePrice = 1000.0;
    this.enableOfflineBreeding = true;
    this.offlineBreedingInterval = DurationValue.parse("30m");
    this.maxOfflineEggsPerPlot = 1;
    this.xpPerStep = 1.0;
    this.pricePerXp = 5.0;
  }

  public void check() {
    if (ticksToWalking < 20) {
      ticksToWalking = 20;
    }
    if (multiplierSteps < 1.0f) {
      multiplierSteps = 1.0f;
    }
    if (cooldownToOpenMenus.toMillis() <= 100 || cooldownToOpenMenus.toSeconds() >= 5) {
      cooldownToOpenMenus = DurationValue.parse("0.5s");
    }
  }

  public boolean hasOpenCooldown(ServerPlayerEntity player) {
    long currentCooldown = cooldownsOpenMenus.getOrDefault(player.getUuid(), 0L);
    if (currentCooldown <= System.currentTimeMillis()) {
      cooldownsOpenMenus.put(player.getUuid(), System.currentTimeMillis() + cooldownToOpenMenus.toMillis());
      return false;
    }
    PlayerUtils.sendMessage(
      player,
      CobbleDaycare.language.getMessageCooldownOpenMenu()
        .replace("%cooldown%", PlayerUtils.getCooldown(currentCooldown)),
      CobbleDaycare.language.getPrefix(),
      TypeMessage.CHAT
    );
    return true;
  }

  public double getSteps(Pokemon pokemon) {
    double d = this.defaultSteps;
    for (EggGroup eggGroup : pokemon.getForm().getEggGroups()) {
      if (this.steps.containsKey(eggGroup) && this.steps.get(eggGroup) < d) {
        d = this.steps.get(eggGroup);
      }
    }
    return d;
  }

  public void init() {
    Path path = CobbleDaycare.getPath().resolve("config.json");
    try {
      Config config = UtilsFile.read(path, Config.class);
      if (config != null) {
        CobbleDaycare.config.check();
      } else {
        config = new Config();
      }
      config.check();
      CobbleDaycare.config = config;
      UtilsFile.write(path, config);
    } catch (IOException e) {
      e.printStackTrace();
    }

  }
}
