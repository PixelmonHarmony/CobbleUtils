package com.kingpixel.cobbleutils.features.breeding;

import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.features.breeding.events.EggThrow;
import com.kingpixel.cobbleutils.features.breeding.events.PastureUI;
import com.kingpixel.cobbleutils.features.breeding.events.WalkBreeding;
import com.kingpixel.cobbleutils.features.breeding.manager.ManagerPlotEggs;
import dev.architectury.event.events.common.PlayerEvent;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Carlos Varas Alonso - 23/07/2024 9:24
 */
public class Breeding {
  public static ManagerPlotEggs managerPlotEggs = new ManagerPlotEggs();
  private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

  public static void register() {
    events();

    PlayerEvent.PLAYER_JOIN.register(managerPlotEggs::init);
    PlayerEvent.PLAYER_QUIT.register(managerPlotEggs::remove);

    scheduler.scheduleAtFixedRate(() -> {
        try {
          CobbleUtils.server.getPlayerList().getPlayers().forEach(managerPlotEggs::checking);
        } catch (Exception e) {
          CobbleUtils.LOGGER.error("Error in ManagerPlotEggs: " + e.getMessage());
        }
      },
      0, 5, TimeUnit.SECONDS);

  }

  private static void events() {
    WalkBreeding.register();
    EggThrow.register();
    PastureUI.register();
  }
}
