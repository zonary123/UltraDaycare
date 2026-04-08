package com.kingpixel.ultradaycare.database;

import com.kingpixel.cobbleutils.Model.DataBaseConfig;
import com.kingpixel.cobbleutils.util.UtilsFile;
import com.kingpixel.ultradaycare.UltraDaycare;
import com.kingpixel.ultradaycare.models.User;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Improved by GitHub Copilot - 07/08/2024 9:41
 */
public class JSONService extends DatabaseClient {
  public static final Path PATH = UltraDaycare.getPath().resolve("data");

  public JSONService() {
  }


  @Override
  public void connect(DataBaseConfig config) {
    UltraDaycare.LOGGER.info(UltraDaycare.MOD_ID, "Connected to JSON database at path: " + PATH);
  }

  @Override
  public void disconnect() {
    saveAll().join();
    UltraDaycare.LOGGER.info(UltraDaycare.MOD_ID, "Disconnected from JSON database.");
  }


  private Path getPath(UUID uuid) {
    return PATH.resolve(uuid + ".json");
  }

  @Override
  public CompletableFuture<@Nullable User> find(UUID uuid) {
    User user = DatabaseClient.USERS.getIfPresent(uuid);
    if (user != null) return CompletableFuture.completedFuture(user);
    return UtilsFile.readAsync(getPath(uuid), User.class);
  }

  @Override
  public CompletableFuture<Void> saveOrUpdateUser(User user) {
    return UtilsFile.writeAsync(getPath(user.getPlayerUUID()), user);
  }
}