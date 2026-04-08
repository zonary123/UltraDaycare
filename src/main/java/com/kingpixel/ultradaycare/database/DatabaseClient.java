package com.kingpixel.cobbledaycare.database;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.kingpixel.cobbledaycare.models.User;
import com.kingpixel.cobbleutils.Model.DataBaseConfig;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author Carlos Varas Alonso - 24/07/2024 21:02
 */
public abstract class DatabaseClient {
  public static final Cache<UUID, User> USERS = Caffeine.newBuilder()
    .maximumSize(200)
    .build();

  public abstract void connect(DataBaseConfig config);

  public abstract void disconnect();

  public @Nullable User getUser(ServerPlayerEntity player) {
    return getUser(player.getUuid());
  }

  public @Nullable User getUser(UUID uuid) {
    return USERS.getIfPresent(uuid);
  }

  public CompletableFuture<@Nullable User> find(ServerPlayerEntity player) {
    return find(player.getUuid());
  }

  public abstract CompletableFuture<@Nullable User> find(UUID uuid);

  public abstract CompletableFuture<Void> saveOrUpdateUser(User user);

  public CompletableFuture<Void> saveAll() {
    List<CompletableFuture<Void>> futures = USERS.asMap().values().stream()
      .map(user -> CompletableFuture.runAsync(() -> saveOrUpdateUser(user)))
      .toList();

    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
  }
}
