package com.kingpixel.cobbledaycare.database;

import com.kingpixel.cobbledaycare.CobbleDaycare;
import com.kingpixel.cobbledaycare.models.User;
import com.kingpixel.cobbleutils.Model.DataBaseConfig;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jspecify.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MongoDBClient extends DatabaseClient {

  private MongoClient mongoClient;
  private MongoDatabase database;
  private MongoCollection<Document> collection;

  public MongoDBClient() {
  }

  @Override
  public void connect(DataBaseConfig config) {
    var settings = MongoClientSettings.builder()
      .applicationName("CobbleDaycare")
      .applyConnectionString(new ConnectionString(config.getUrl()))
      .build();
    mongoClient = MongoClients.create(settings);
    database = mongoClient.getDatabase(config.getDatabase());
    collection = database.getCollection("user_information");
  }

  @Override
  public void disconnect() {
    saveAll().join();
    if (mongoClient != null) mongoClient.close();
    mongoClient = null;
  }

  @Override
  public CompletableFuture<@Nullable User> find(UUID uuid) {
    return CobbleDaycare.ASYNC_CONTEXT.supply(() -> {
      User user = DatabaseClient.USERS.getIfPresent(uuid);
      if (user != null) return user;
      Document document = collection.find(Filters.eq("playerUUID", uuid.toString())).first();
      if (document == null) return null;
      return User.fromDocument(document);
    });
  }

  @Override
  public void saveOrUpdateUser(User user) {
    Bson filter = Filters.eq("playerUUID", user.getPlayerUUID().toString());
    Document document = user.toDocument();
    collection.replaceOne(filter, document, new ReplaceOptions().upsert(true));
  }


}