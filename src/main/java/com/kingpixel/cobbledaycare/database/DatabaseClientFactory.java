package com.kingpixel.cobbledaycare.database;


import com.kingpixel.cobbledaycare.CobbleDaycare;
import com.kingpixel.cobbleutils.Model.DataBaseConfig;

/**
 * @author Carlos Varas Alonso - 24/07/2024 21:03
 */
public class DatabaseClientFactory {

  public synchronized static DatabaseClient createDatabaseClient(DataBaseConfig database) {
    if (CobbleDaycare.database != null) CobbleDaycare.database.disconnect();
    CobbleDaycare.database = null;
    switch (database.getType()) {
      case MONGODB -> CobbleDaycare.database = new MongoDBClient();
      case JSON -> CobbleDaycare.database = new JSONClient();
      default -> throw new IllegalArgumentException("Unsupported database type: " + database.getType());
    }

    CobbleDaycare.database.connect(database);
    return CobbleDaycare.database;
  }

}

