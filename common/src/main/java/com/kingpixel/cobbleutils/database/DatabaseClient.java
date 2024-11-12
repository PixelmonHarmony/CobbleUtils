package com.kingpixel.cobbleutils.database;

import com.kingpixel.cobbleutils.features.breeding.models.PlotBreeding;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

/**
 * @author Carlos Varas Alonso - 24/07/2024 21:02
 */
public interface DatabaseClient {
  void connect();

  void disconnect();

  void save();

  // Daycare
  List<PlotBreeding> getPlots(ServerPlayerEntity player);

  void savePlots(ServerPlayerEntity player, List<PlotBreeding> plots);

  void checkDaycarePlots(ServerPlayerEntity player);

  void removeDataIfNecessary(ServerPlayerEntity player);
}
