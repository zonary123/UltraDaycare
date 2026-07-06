package com.kingpixel.ultradaycare.config;

import com.cobblemon.mod.common.api.pokemon.egg.EggGroup;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.Model.*;
import com.kingpixel.cobbleutils.util.PlayerUtils;
import com.kingpixel.cobbleutils.util.TypeMessage;
import com.kingpixel.cobbleutils.util.UtilsFile;
import com.kingpixel.ultradaycare.UltraDaycare;
import com.kingpixel.ultradaycare.models.UserInfoOptions;
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
  private static final String VIP_COOLDOWN = "cooldown.vip";
  private static final String LEGENDARY_COOLDOWN = "cooldown.legendary";
  private static final String MASTER_COOLDOWN = "cooldown.master";
  private static Map<UUID, Long> cooldownsOpenMenus = new HashMap<>();
  // ==========================================
  // 1. System & Database Configuration
  // ==========================================
  private boolean debug;
  private String lang;
  private List<String> commands;
  private String commandEggInfo;
  private String daycareMode;
  private DataBaseConfig dataBase;
  private UserInfoOptions userInfoOptions;

  // ==========================================
  // 2. General Daycare & Breeding Settings
  // ==========================================
  private boolean fixIlegalAbilities;
  private boolean canUseNativeGUI;
  private boolean showIvs;
  private boolean spawnEggWorld;
  private int raritySpawnEgg;
  private boolean allowElytra;
  private boolean dobbleDitto;
  private FilterPokemons dobbleDittoFilter;
  private PokemonBlackList blackList;
  private List<String> whitelist;
  private Map<String, Integer> limitEggs;

  // ==========================================
  // 3. Step & Time Hatching Progress
  // ==========================================
  private long ticksToWalking;
  private boolean globalMultiplierSteps;
  private float multiplierSteps;
  private Map<String, Float> multiplierStepsPermission;
  private double defaultSteps;
  private Map<EggGroup, Double> steps;
  private List<String> abilityAcceleration;
  private double multiplierAbilityAcceleration;
  private double reduceEggStepsVehicle;
  private List<String> permittedVehicles;
  private EggHatchMethod pokemmoEggHatchMethod;
  private DurationValue pokemmoEggHatchTime;
  private Map<String, DurationValue> pokemmoEggHatchTimePermissions;

  // ==========================================
  // 4. Cooldowns & Slots
  // ==========================================
  private DurationValue cooldownToOpenMenus;
  private DurationValue cooldown;
  private Map<String, DurationValue> cooldowns;
  private DurationValue defaultCooldownBreed;
  private Map<String, DurationValue> cooldownsBreed;
  private DurationValue defaultCooldownHatch;
  private Map<String, DurationValue> cooldownsHatch;
  private List<Integer> slotPlots;

  // ==========================================
  // 5. Economy & Paid Features
  // ==========================================
  private EconomyUse economyUse = new EconomyUse("IMPACTOR", "impactor:dollars");
  private boolean enableBreedingFee;
  private double breedingFeePrice;
  private boolean enablePaidExperience;
  private double payXpPrice;
  private int payXpAmount;
  private int maxLevelTraining;
  private double xpPerStep;
  private double pricePerXp;

  // ==========================================
  // 6. Offline Breeding
  // ==========================================
  private boolean enableOfflineBreeding;
  private DurationValue offlineBreedingInterval;
  private int maxOfflineEggsPerPlot;

  public Config() {
    // 1. System & Database Configuration
    this.debug = false;
    this.lang = "en";
    this.commands = List.of("daycare");
    this.commandEggInfo = "egginfo";
    this.daycareMode = "pokemon";
    this.dataBase = new DataBaseConfig();
    this.dataBase.setDatabase("ultradaycare");
    this.userInfoOptions = new UserInfoOptions();

    // 2. General Daycare & Breeding Settings
    this.fixIlegalAbilities = true;
    this.canUseNativeGUI = false;
    this.showIvs = false;
    this.spawnEggWorld = true;
    this.raritySpawnEgg = 2048;
    this.allowElytra = false; // default if not specified
    this.dobbleDitto = false;
    this.dobbleDittoFilter = new FilterPokemons();
    this.blackList = PokemonBlackList.createBlackList();
    this.blackList.getPokemons().add("egg");
    this.blackList.getLabels().add("basculegion");
    this.blackList.getLabels().add("legendary");
    this.whitelist = new ArrayList<>();
    this.limitEggs = new HashMap<>();
    this.limitEggs.put("", 1);
    this.limitEggs.put("group.vip", 2);

    // 3. Step & Time Hatching Progress
    this.ticksToWalking = 20;
    this.globalMultiplierSteps = false;
    this.multiplierSteps = 1.0f;
    this.multiplierStepsPermission = new HashMap<>();
    this.multiplierStepsPermission.put("multipliersteps.vip", 2.0f);
    this.defaultSteps = 128D;
    this.steps = new EnumMap<>(EggGroup.class);
    for (EggGroup value : EggGroup.values()) {
      this.steps.put(value, defaultSteps);
    }
    this.abilityAcceleration = List.of("magmaarmor", "flamebody", "steamengine");
    this.multiplierAbilityAcceleration = 2.0;
    this.reduceEggStepsVehicle = 2.0;
    this.permittedVehicles = List.of("minecraft:boat", "minecraft:horse", "cobblemon:pokemon");
    this.pokemmoEggHatchMethod = EggHatchMethod.TIME;
    this.pokemmoEggHatchTime = DurationValue.parse("10m");
    this.pokemmoEggHatchTimePermissions = new HashMap<>();
    this.pokemmoEggHatchTimePermissions.put("ultradaycare.hatch.vip", DurationValue.parse("5m"));

    // 4. Cooldowns & Slots
    this.cooldownToOpenMenus = DurationValue.parse("0.5s");
    this.cooldown = DurationValue.parse("3m");
    this.cooldowns = Map.of(
        VIP_COOLDOWN, DurationValue.parse("15m"),
        LEGENDARY_COOLDOWN, DurationValue.parse("10m"),
        MASTER_COOLDOWN, DurationValue.parse("5m"));
    this.defaultCooldownBreed = DurationValue.parse("60s");
    this.cooldownsBreed = new HashMap<>();
    this.cooldownsBreed.put(VIP_COOLDOWN, DurationValue.parse("30s"));
    this.defaultCooldownHatch = DurationValue.parse("60s");
    this.cooldownsHatch = new HashMap<>();
    this.cooldownsHatch.put(VIP_COOLDOWN, DurationValue.parse("30s"));
    this.slotPlots = new ArrayList<>();
    this.slotPlots.add(10);
    this.slotPlots.add(12);
    this.slotPlots.add(14);
    this.slotPlots.add(16);

    // 5. Economy & Paid Features
    this.enableBreedingFee = false;
    this.breedingFeePrice = 1000.0;
    this.enablePaidExperience = false;
    this.payXpPrice = 100.0;
    this.payXpAmount = 1;
    this.maxLevelTraining = 100;
    this.xpPerStep = 1.0;
    this.pricePerXp = 5.0;

    // 6. Offline Breeding
    this.enableOfflineBreeding = true;
    this.offlineBreedingInterval = DurationValue.parse("30m");
    this.maxOfflineEggsPerPlot = 1;
  }

  public void check() {
    if (daycareMode == null || daycareMode.isEmpty()) {
      daycareMode = "pokemon";
    }
    if (ticksToWalking < 20) {
      ticksToWalking = 20;
    }
    if (multiplierSteps < 1.0f) {
      multiplierSteps = 1.0f;
    }
    if (pokemmoEggHatchMethod == null) {
      pokemmoEggHatchMethod = EggHatchMethod.TIME;
    }
    if (pokemmoEggHatchTime == null) {
      pokemmoEggHatchTime = DurationValue.parse("10m");
    }
    if (pokemmoEggHatchTimePermissions == null) {
      pokemmoEggHatchTimePermissions = new HashMap<>();
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
        UltraDaycare.language.getMessageCooldownOpenMenu()
            .replace("%cooldown%", PlayerUtils.getCooldown(currentCooldown)),
        UltraDaycare.language.getPrefix(),
        TypeMessage.CHAT);
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
    Path path = UltraDaycare.getPath().resolve("config.json");
    try {
      Config config = UtilsFile.readOrCreate(path, Config.class, Config::new);
      config.check();
      UltraDaycare.config = config;
      UtilsFile.write(path, config);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
