package com.kingpixel.cobbleutils;

import com.kingpixel.cobbleutils.Model.PlayerInfo;
import com.kingpixel.cobbleutils.Model.RewardsData;
import com.kingpixel.cobbleutils.command.CommandTree;
import com.kingpixel.cobbleutils.config.*;
import com.kingpixel.cobbleutils.events.*;
import com.kingpixel.cobbleutils.events.features.PokemonBoss;
import com.kingpixel.cobbleutils.managers.PartyManager;
import com.kingpixel.cobbleutils.managers.RewardsManager;
import com.kingpixel.cobbleutils.party.command.CommandsParty;
import com.kingpixel.cobbleutils.party.config.PartyConfig;
import com.kingpixel.cobbleutils.party.config.PartyLang;
import com.kingpixel.cobbleutils.party.models.UserParty;
import com.kingpixel.cobbleutils.party.util.PartyPlaceholder;
import com.kingpixel.cobbleutils.util.*;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.InteractionEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.server.MinecraftServer;

import java.util.List;
import java.util.concurrent.*;

public class CobbleUtils {
  public static final String MOD_ID = "cobbleutils";
  public static final String PATH = "/config/cobbleutils";
  public static final String PATH_LANG = PATH + "/lang/";
  public static final String PATH_RANDOM = PATH + "/random/";
  public static final String PATH_PARTY = PATH + "/party/";
  public static final String PATH_PARTY_LANG = PATH_PARTY + "lang/";
  public static final String PATH_PARTY_DATA = PATH_PARTY + "data/";
  public static final String PATH_REWARDS_DATA = PATH + "/rewards/";
  public static final UtilsLogger LOGGER = new UtilsLogger();
  public static final String MOD_NAME = "CobbleUtils";
  public static MinecraftServer server;
  public static Config config = new Config();
  public static Lang language = new Lang();
  public static PoolMoney poolMoney = new PoolMoney();
  public static PoolItems poolItems = new PoolItems();
  public static PoolPokemons poolPokemons = new PoolPokemons();
  public static SpawnRates spawnRates = new SpawnRates();
  // Party
  public static PartyConfig partyConfig = new PartyConfig();
  public static PartyLang partyLang = new PartyLang();
  public static PartyManager partyManager = new PartyManager();
  // Rewards
  public static RewardsManager rewardsManager = new RewardsManager();
  // Tasks
  private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
  private static final List<ScheduledFuture<?>> scheduledTasks = new CopyOnWriteArrayList<>();

  public static void init() {
    events();
  }

  public static void load() {
    checks();
    files();
    spawnRates.init();
    ArraysPokemons.init();
    PartyPlaceholder.register();
    sign();
    tasks();
  }

  private static void checks() {
    Utils.createDirectoryIfNeeded(PATH);
    Utils.createDirectoryIfNeeded(PATH_LANG);
    Utils.createDirectoryIfNeeded(PATH_RANDOM);
    Utils.createDirectoryIfNeeded(PATH_PARTY);
    Utils.createDirectoryIfNeeded(PATH_PARTY_LANG);
    Utils.createDirectoryIfNeeded(PATH_PARTY_DATA);
    Utils.createDirectoryIfNeeded(PATH_REWARDS_DATA);
  }


  private static void files() {
    config.init();
    language.init();
    poolItems.init();
    poolPokemons.init();
    poolMoney.init();
    partyConfig.init();
    partyLang.init();
  }

  private static void sign() {
    LOGGER.info("§e+-------------------------------+");
    LOGGER.info("§e| §6CobbleUtils");
    LOGGER.info("§e+-------------------------------+");
    LOGGER.info("§e| §6Version: §e" + "1.0.8");
    LOGGER.info("§e| §6Author: §eZonary123");
    LOGGER.info("§e| §6Website: §9https://github.com/Zonary123/CobbleUtils");
    LOGGER.info("§e| §6Discord: §9https://discord.com/invite/fKNc7FnXpa");
    LOGGER.info("§e| §6Support: §9https://github.com/Zonary123/CobbleUtils/issues");
    LOGGER.info("§e| &dDonate: §9https://ko-fi.com/zonary123");
    LOGGER.info("§e+-------------------------------+");
    LOGGER.info("§e| §6Pokemons size: §a" + CobbleUtils.config.isRandomsize());
    LOGGER.info("§e| §6Shulkers: §cUnimplemented");
    LOGGER.info("§e| §6Fossil: §a" + CobbleUtils.config.isFossil());
    LOGGER.info("§e| §6Shiny particles: §cUnimplemented");
    LOGGER.info("§e| §6Random item: §aImplemented");
    LOGGER.info("§e| §6Random money: §aImplemented");
    LOGGER.info("§e| §6Random pokemon: §aImplemented");
    LOGGER.info("§e| §6Pick Up: §cUnimplemented");
    LOGGER.info("§e| §6Party: §a" + CobbleUtils.config.isParty());
    LOGGER.info("§e| §6Rewards: §a" + CobbleUtils.config.isRewards());
    LOGGER.info("§e+-------------------------------+");
  }

  private static void events() {
    files();
    Utils.removeFiles(PATH_PARTY_DATA);

    LifecycleEvent.SERVER_STARTED.register(server -> load());

    CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> {
      CommandTree.register(dispatcher, registry);
      CommandsParty.register(dispatcher, registry);
    });

    LifecycleEvent.SERVER_STOPPING.register(server -> {
      scheduledTasks.forEach(task -> task.cancel(true));
      scheduledTasks.clear();
      LOGGER.info("CobbleUtils has been stopped.");
    });

    LifecycleEvent.SERVER_LEVEL_LOAD.register(level -> server = level.getLevel().getServer());

    PlayerEvent.PLAYER_JOIN.register(player -> {
      partyManager.getUserParty().put(player.getUUID(), new UserParty("", false));
      RewardsData rewardsData = rewardsManager.getRewardsData().computeIfAbsent(
        player.getUUID(),
        uuid -> new RewardsData(player.getGameProfile().getName(), player.getUUID())
      );

      rewardsData.init();
    });


    PlayerEvent.PLAYER_QUIT.register(player -> {
      UserParty userParty = partyManager.getUserParty().get(player.getUUID());
      if (userParty.isHasParty()) {
        partyManager.leaveParty(partyManager.getUserParty().get(player.getUUID())
            .getPartyName(),
          PlayerInfo.fromPlayer(player));
      }
    });


    InteractionEvent.RIGHT_CLICK_BLOCK.register((player, hand, blockpos, direction) -> {
      BlockRightClickEvents.register(player, hand, blockpos, direction);
      return EventResult.pass();
    });


    InteractionEvent.RIGHT_CLICK_ITEM.register(ItemRightClickEvents::register);

    // ? Add the event for fishing a pokemon

    ScaleEvent.register();

    PokerusEvents.register();

    PokemonBoss.register();

    PlayerEvent.DROP_ITEM.register(DropItemEvent::register);
  }


  private static void tasks() {
    for (ScheduledFuture<?> task : scheduledTasks) {
      task.cancel(false);
    }
    scheduledTasks.clear();

    ScheduledFuture<?> alertreward = scheduler.scheduleAtFixedRate(() -> {
      server.getPlayerList().getPlayers().forEach(player -> {
        RewardsData rewardsData = rewardsManager.getRewardsData().get(player.getUUID());
        if (RewardsUtils.hasRewards(player)) {
          int amount = rewardsData.getCommands().size() + rewardsData.getItems().size() + rewardsData.getPokemons().size();
          player.sendSystemMessage(AdventureTranslator.toNative(
            language.getMessageHaveRewards().replace("%amount%", String.valueOf(amount))
          ));
        }

      });
    }, 0, CobbleUtils.config.getAlertreward(), TimeUnit.MINUTES);

    scheduledTasks.add(alertreward);
  }
}
