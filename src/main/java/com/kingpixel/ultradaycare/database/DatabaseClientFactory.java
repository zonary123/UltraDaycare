package com.kingpixel.ultradaycare.database;


import com.kingpixel.cobbleutils.Model.DataBaseConfig;
import com.kingpixel.ultradaycare.UltraDaycare;

/**
 * @author Carlos Varas Alonso - 24/07/2024 21:03
 */
public class DatabaseClientFactory {

  public synchronized static DatabaseClient createDatabaseClient(DataBaseConfig database) {
    if (UltraDaycare.database != null) UltraDaycare.database.disconnect();
    UltraDaycare.database = null;
    switch (database.getType()) {
      case MONGODB -> UltraDaycare.database = new MongoDBService();
      case JSON -> UltraDaycare.database = new JSONService();
      default -> throw new IllegalArgumentException("Unsupported database type: " + database.getType());
    }

    UltraDaycare.database.connect(database);
    return UltraDaycare.database;
  }

}

