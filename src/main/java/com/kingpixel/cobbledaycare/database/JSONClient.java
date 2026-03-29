package com.kingpixel.cobbledaycare.database;

import com.kingpixel.cobbledaycare.CobbleDaycare;
import com.kingpixel.cobbledaycare.models.User;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.DataBaseConfig;
import com.kingpixel.cobbleutils.util.UtilsFile;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Improved by GitHub Copilot - 07/08/2024 9:41
 */
public class JSONClient extends DatabaseClient {
  public static final Path PATH = CobbleDaycare.getPath().resolve("data");

  public JSONClient() {
  }


  @Override
  public void connect(DataBaseConfig config) {
    CobbleUtils.LOGGER.info(CobbleDaycare.MOD_ID, "Connected to JSON database at path: " + PATH);
  }

  @Override
  public void disconnect() {
    saveAll().join();
    CobbleUtils.LOGGER.info(CobbleDaycare.MOD_ID, "Disconnected from JSON database.");
  }


  private Path getPath(UUID uuid) {
    return PATH.resolve(uuid + ".json");
  }

  @Override
  public CompletableFuture<@Nullable User> find(UUID uuid) {
    User user = DatabaseClient.USERS.getIfPresent(uuid);
    if (user != null) return CompletableFuture.completedFuture(user);
    return UtilsFile.readAsync(getPath(uuid), User.class)
      .thenCompose(CompletableFuture::completedFuture);
  }

  @Override
  public void saveOrUpdateUser(User user) {
    UtilsFile.writeAsync(getPath(user.getPlayerUUID()), user);
  }
}