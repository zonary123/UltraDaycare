package com.kingpixel.ultradaycare.util;

import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.ultradaycare.UltraDaycare;

import java.io.File;
import java.nio.file.Path;

/**
 * Service to handle migration from CobbleDaycare to UltraDaycare.
 *
 * @author Carlos Varas Alonso - 07/04/2026
 */
public class MigrationService {

  private MigrationService() {
  }

  public static void migrate() {
    Path legacyPath = CobbleUtils.getPath().resolve("cobbledaycare");
    Path newPath = UltraDaycare.getPath();

    File legacyFile = legacyPath.toFile();
    File newFile = newPath.toFile();

    if (legacyFile.exists() && !newFile.exists()) {
      UltraDaycare.LOGGER.info("Legacy configuration found! Migrating cobbledaycare -> ultradaycare...");
      if (legacyFile.renameTo(newFile)) {
        UltraDaycare.LOGGER.info("Migration successful.");
      } else {
        UltraDaycare.LOGGER.error("Migration failed! Please manually rename the 'config/cobbledaycare' folder to 'config/ultradaycare'.");
      }
    }
  }
}
