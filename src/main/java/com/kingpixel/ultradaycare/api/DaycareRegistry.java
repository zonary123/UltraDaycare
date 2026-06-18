package com.kingpixel.ultradaycare.api;

import com.kingpixel.ultradaycare.UltraDaycare;
import com.kingpixel.ultradaycare.mechanics.Mechanics;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry class for registering daycare modes and breeding mechanics.
 * Third-party mods can use this to register their custom modes and rules.
 */
public class DaycareRegistry {
  private static final Map<String, DaycareMode> MODES = new HashMap<>();

  private DaycareRegistry() {
    // Utility class
  }

  /**
   * Registers a custom daycare mode.
   *
   * @param mode The daycare mode to register.
   */
  public static void registerMode(DaycareMode mode) {
    if (mode != null) {
      MODES.put(mode.getId().toLowerCase(), mode);
    }
  }

  /**
   * Retrieves a registered daycare mode by its ID.
   *
   * @param id The daycare mode ID.
   * @return The registered DaycareMode, or null if not found.
   */
  public static DaycareMode getMode(String id) {
    return MODES.get(id.toLowerCase());
  }

  /**
   * Retrieves all registered daycare modes.
   *
   * @return An unmodifiable map of registered daycare modes.
   */
  public static Map<String, DaycareMode> getModes() {
    return Map.copyOf(MODES);
  }

  /**
   * Registers a custom breeding mechanic.
   *
   * @param mechanic The breeding mechanic to register.
   */
  public static void registerMechanic(Mechanics mechanic) {
    UltraDaycare.registerMechanic(mechanic);
  }
}
