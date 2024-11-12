package com.kingpixel.cobbleutils.database;

import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.DataBaseConfig;
import com.kingpixel.cobbleutils.features.breeding.models.PlotBreeding;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Carlos Varas Alonso - 24/07/2024 21:03
 */
public class DatabaseClientFactory {
  public static DatabaseClient databaseClient;

  public static DatabaseClient createDatabaseClient(DataBaseConfig database) {
    if (databaseClient != null) {
      databaseClient.disconnect();
    }
    switch (database.getType()) {
      case MONGODB -> databaseClient = new MongoDBClient(database);
      case JSON -> databaseClient = new JSONClient(database);
      default -> databaseClient = new JSONClient(database);
    }
    databaseClient.connect();
    return databaseClient;
  }

  // Daycare
  public static void CheckDaycarePlots(ServerPlayerEntity player) {
    List<PlotBreeding> plots = databaseClient.getPlots(player);
    if (plots == null || plots.isEmpty()) {
      plots = new ArrayList<>();
      for (int i = 0; i < CobbleUtils.breedconfig.getPlotSlots().size(); i++) {
        plots.add(new PlotBreeding());
      }
    }
    AtomicBoolean update = new AtomicBoolean(false);
    plots.forEach(plotBreeding -> {
      if (plotBreeding.checking(player)) {
        update.set(true);
      }
    });
    if (update.get()) {
      databaseClient.savePlots(player, plots);
    }
  }

}
