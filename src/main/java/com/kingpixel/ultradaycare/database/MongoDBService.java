package com.kingpixel.ultradaycare.database;

import com.kingpixel.cobbleutils.Model.DataBaseConfig;
import com.kingpixel.cobbleutils.util.mongodb.MongoDBManager;
import com.kingpixel.ultradaycare.models.User;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jspecify.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MongoDBService extends DatabaseClient {
  private MongoDBManager manager;
  private MongoCollection<Document> collection;

  public MongoDBService() {
  }

  @Override
  public void connect(DataBaseConfig config) {
    manager = com.kingpixel.cobbleutils.util.mongodb.MongoDBService.getOrCreateManager(config);
    collection = manager.getCollection(config.getDatabase(), "user_information");
  }

  @Override
  public void disconnect() {
    saveAll().join();
  }

  @Override
  public CompletableFuture<@Nullable User> find(UUID uuid) {
    return manager.supplyAsync(() -> {
      User user = DatabaseClient.USERS.getIfPresent(uuid);
      if (user != null) return user;
      Document document = collection.find(Filters.eq("playerUUID", uuid.toString())).first();
      if (document == null) return null;
      return User.fromDocument(document);
    });
  }

  @Override
  public CompletableFuture<Void> saveOrUpdateUser(User user) {
    return manager.runAsync(() -> {
      Bson filter = Filters.eq("playerUUID", user.getPlayerUUID().toString());
      Document document = user.toDocument();
      collection.replaceOne(filter, document, new ReplaceOptions().upsert(true));
    });
  }
}