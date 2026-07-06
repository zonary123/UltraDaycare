package com.kingpixel.ultradaycare.config;

/**
 * Defines the method used for hatching eggs in PokeMMO daycare mode.
 */
public enum EggHatchMethod {
  /**
   * Eggs hatch purely by walking/steps.
   */
  STEPS,

  /**
   * Eggs hatch purely by time (passively).
   */
  TIME,

  /**
   * Eggs hatch by both walking/steps and time (whichever comes first).
   */
  ALL
}
