package com.kingpixel.cobbleutils;

import com.kingpixel.cobbleutils.command.CommandTree;
import com.kingpixel.cobbleutils.config.*;
import com.kingpixel.cobbleutils.events.*;
import com.kingpixel.cobbleutils.manager.PokestopManager;
import com.kingpixel.cobbleutils.util.UtilsLogger;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.InteractionEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.server.MinecraftServer;

public class CobbleUtils {
  public static final String MOD_ID = "cobbleutils";
  public static final String PATH = "/config/cobbleutils";
  public static final String PATH_LANG = PATH + "/lang/";
  public static final String PATH_RANDOM = PATH + "/random/";
  public static final String PATH_DATA = PATH + "/data/";
  public static final UtilsLogger LOGGER = new UtilsLogger();
  public static final String MOD_NAME = "CobbleUtils";
  public static MinecraftServer server;
  public static Config config = new Config();
  public static Lang language = new Lang();
  public static PoolItems poolItems = new PoolItems();
  public static PoolPokemons poolPokemons = new PoolPokemons();
  public static PokestopConfig pokestops = new PokestopConfig();
  public static PokestopManager pokestopManager = new PokestopManager();

  public static void init() {
    LOGGER.info("Initializing " + MOD_ID);
    events();
  }

  public static void load() {
    files();
    managers();
  }

  private static void managers() {
    pokestopManager.init();
  }

  private static void files() {
    config.init();
    language.init();
    poolItems.init();
    poolPokemons.init();
    pokestops.init();
  }

  private static void events() {
    files();
    CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> CommandTree.register(dispatcher));
    LifecycleEvent.SERVER_STARTED.register(server -> load());
    LifecycleEvent.SERVER_LEVEL_LOAD.register(level -> server = level.getLevel().getServer());
    PlayerEvent.PLAYER_JOIN.register((player) -> pokestopManager.addIfNotExistsPlayer(player.getUUID()));
    InteractionEvent.RIGHT_CLICK_BLOCK.register((player, hand, blockpos, direction) -> {
      BlockRightClickEvents.register(player, hand, blockpos, direction);
      return EventResult.pass();
    });
    InteractionEvent.INTERACT_ENTITY.register((player, entity, hand) -> {
      EntityRightClickEvent.register(player, entity, hand);
      return EventResult.pass();
    });
    PlayerEvent.ATTACK_ENTITY.register((player, world, entity, hand, entityHitResult) -> {
      EntityAttackEvent.register(player, world, entity, hand, entityHitResult);
      return EventResult.pass();
    });
    PokemonSpawn.register();
    FossilEvent.register();
  }


}
