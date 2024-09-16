package com.kingpixel.cobbleutils;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.properties.CustomPokemonProperty;
import com.cobblemon.mod.common.platform.events.PlatformEvents;
import com.kingpixel.cobbleutils.Model.PlayerInfo;
import com.kingpixel.cobbleutils.Model.RewardsData;
import com.kingpixel.cobbleutils.command.CommandTree;
import com.kingpixel.cobbleutils.config.*;
import com.kingpixel.cobbleutils.database.DatabaseClientFactory;
import com.kingpixel.cobbleutils.events.BlockRightClickEvents;
import com.kingpixel.cobbleutils.events.DropItemEvent;
import com.kingpixel.cobbleutils.events.ItemRightClickEvents;
import com.kingpixel.cobbleutils.events.features.FeaturesRegister;
import com.kingpixel.cobbleutils.features.Features;
import com.kingpixel.cobbleutils.features.breeding.config.BreedConfig;
import com.kingpixel.cobbleutils.managers.PartyManager;
import com.kingpixel.cobbleutils.managers.RewardsManager;
import com.kingpixel.cobbleutils.party.command.CommandsParty;
import com.kingpixel.cobbleutils.party.config.PartyConfig;
import com.kingpixel.cobbleutils.party.config.PartyLang;
import com.kingpixel.cobbleutils.party.models.UserParty;
import com.kingpixel.cobbleutils.party.util.PartyPlaceholder;
import com.kingpixel.cobbleutils.properties.BreedablePropertyType;
import com.kingpixel.cobbleutils.util.*;
import com.kingpixel.cobbleutils.util.events.ArraysPokemonEvent;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.InteractionEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import kotlin.Unit;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;
import java.util.concurrent.*;

public class CobbleUtils extends ShopExtend {
  public static final String MOD_ID = "cobbleutils";
  public static final String PATH = "/config/cobbleutils";
  public static final String PATH_LANG = PATH + "/lang/";
  public static final String PATH_RANDOM = PATH + "/random/";
  public static final String PATH_PARTY = PATH + "/party/";
  public static final String PATH_PARTY_LANG = PATH_PARTY + "lang/";
  public static final String PATH_PARTY_DATA = PATH_PARTY + "data/";
  public static final String PATH_REWARDS_DATA = PATH + "/rewards/";
  public static final String PATH_BREED = PATH + "/breed/";
  public static final String PATH_BREED_DATA = PATH_BREED + "data/";
  public static final String PATH_SHOP = CobbleUtils.PATH + "/shop/";
  public static final String PATH_SHOPS = PATH_SHOP + "shops/";
  public static final UtilsLogger LOGGER = new UtilsLogger();
  public static final String MOD_NAME = "CobbleUtils";
  public static MinecraftServer server;
  public static Config config = new Config();
  public static BreedConfig breedconfig = new BreedConfig();
  public static PoolMoney poolMoney = new PoolMoney();
  public static PoolItems poolItems = new PoolItems();
  public static PoolPokemons poolPokemons = new PoolPokemons();
  public static SpawnRates spawnRates = new SpawnRates();
  public static ShopConfig shopConfig = new ShopConfig();
  // Lang
  public static Lang language = new Lang();
  public static ShopLang shopLang = new ShopLang();
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
    sign();
    tasks();
    Features.register();
    //addAllPermissions();
  }

  private static void checks() {
    Utils.createDirectoryIfNeeded(PATH);
    Utils.createDirectoryIfNeeded(PATH_LANG);
    Utils.createDirectoryIfNeeded(PATH_RANDOM);
    Utils.createDirectoryIfNeeded(PATH_PARTY);
    Utils.createDirectoryIfNeeded(PATH_PARTY_LANG);
    Utils.createDirectoryIfNeeded(PATH_PARTY_DATA);
    Utils.createDirectoryIfNeeded(PATH_REWARDS_DATA);
    Utils.createDirectoryIfNeeded(PATH_BREED);
    Utils.createDirectoryIfNeeded(PATH_BREED_DATA);
  }


  private static void files() {
    language.init();
    shopLang.init();
    config.init();
    breedconfig.init();
    poolItems.init();
    poolPokemons.init();
    poolMoney.init();
    partyConfig.init();
    partyLang.init();
    shopConfig.init(PATH_SHOP, MOD_ID, PATH_SHOPS);
    DatabaseClientFactory.createDatabaseClient(config.getDatabase().getType(),
      config.getDatabase().getDatabase(),
      config.getDatabase().getUrl(),
      config.getDatabase().getUser(),
      config.getDatabase().getPassword());

  }

  private static void sign() {
    info(MOD_NAME, "1.1.1", "CobbleUtils");
    LOGGER.info("§e| §6Pokemons size: " + isActive(CobbleUtils.config.isRandomsize()));
    LOGGER.info("§e| §6Shulkers: §cUnimplemented");
    LOGGER.info("§e| §6Fossil: " + isActive(CobbleUtils.config.isFossil()));
    LOGGER.info("§e| §6Shiny particles: §cUnimplemented");
    LOGGER.info("§e| §6Random item: §aImplemented");
    LOGGER.info("§e| §6Random money: §aImplemented");
    LOGGER.info("§e| §6Random pokemon: §aImplemented");
    LOGGER.info("§e| §6Pick Up: §cUnimplemented");
    LOGGER.info("§e| §6Fishing: &cUnimplemented");
    LOGGER.info("§e| §6Shop: " + isActive(CobbleUtils.config.isShops()));
    LOGGER.info("§e| §6Party: " + isActive(CobbleUtils.config.isParty()));
    LOGGER.info("§e| §6Rewards: " + isActive(CobbleUtils.config.isRewards()));
    LOGGER.info("§e| §6Pokerus: " + isActive(CobbleUtils.config.getPokerus().isActive()));
    LOGGER.info("§e| §6Breeding: " + isActive(CobbleUtils.breedconfig.isActive()));
    LOGGER.info("§e| §6Bosses: " + isActive(CobbleUtils.config.getBosses().isActive()));
    LOGGER.info("§e+-------------------------------+");
  }

  public static void info(String mod, String version, String github) {
    LOGGER.info("§e+-------------------------------+");
    LOGGER.info("§e| §6" + mod);
    LOGGER.info("§e+-------------------------------+");
    LOGGER.info("§e| §6Version: §e" + version);
    LOGGER.info("§e| §6Author: §eZonary123");
    LOGGER.info("§e| §6Website: §9https://github.com/Zonary123/" + github);
    LOGGER.info("§e| §6Discord: §9https://discord.com/invite/fKNc7FnXpa");
    LOGGER.info("§e| §6Support: §9https://github.com/Zonary123/" + github + "/issues");
    LOGGER.info("§e| &dDonate: §9https://ko-fi.com/zonary123");
    LOGGER.info("§e+-------------------------------+");
  }

  private static void events() {
    files();

    Utils.removeFiles(PATH_PARTY_DATA);

    CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> {
      CommandTree.register(dispatcher, registry);
      CommandsParty.register(dispatcher, registry);
    });

    LifecycleEvent.SERVER_STARTED.register(server -> {
      load();
    });


    LifecycleEvent.SERVER_STOPPING.register(server -> {
      scheduledTasks.forEach(task -> task.cancel(true));
      scheduledTasks.clear();
      ArraysPokemonEvent.FINISH_GENERATE_POKEMONS.clear();
    });


    LifecycleEvent.SERVER_LEVEL_LOAD.register(level -> server = level.getServer());

    PlayerEvent.PLAYER_JOIN.register(player -> {
      fixInventory(player);
      partyManager.getUserParty().put(player.getUuid(), new UserParty("", false));
      RewardsData rewardsData = rewardsManager.getRewardsData().computeIfAbsent(
        player.getUuid(),
        uuid -> new RewardsData(player.getGameProfile().getName(), player.getUuid())
      );
      rewardsData.init();
    });


    PlayerEvent.PLAYER_QUIT.register(player -> {
      UserParty userParty = partyManager.getUserParty().get(player.getUuid());
      if (userParty == null) return;
      if (userParty.isHasParty()) {
        partyManager.leaveParty(partyManager.getUserParty().get(player.getUuid())
            .getPartyName(),
          PlayerInfo.fromPlayer(player));
      }
    });


    InteractionEvent.RIGHT_CLICK_BLOCK.register((player, hand, blockpos, direction) -> {
      try {
        BlockRightClickEvents.register((ServerPlayerEntity) player, hand, blockpos, direction);
      } catch (ClassCastException e) {
        BlockRightClickEvents.register(PlayerUtils.castPlayer(player), hand, blockpos, direction);
      }
      return EventResult.pass();
    });

    PlatformEvents.SERVER_STARTED.subscribe(Priority.NORMAL, (evt) -> {
      CustomPokemonProperty.Companion.register(BreedablePropertyType.getInstance());
      return Unit.INSTANCE;
    });

    InteractionEvent.RIGHT_CLICK_ITEM.register(ItemRightClickEvents::register);

    // ? Add the event for fishing a pokemon

    FeaturesRegister.register();

    PlayerEvent.DROP_ITEM.register(DropItemEvent::register);


    // Fix empty nbt
    PlayerEvent.PICKUP_ITEM_PRE.register((player, itemEntity, itemStack) -> {
      NbtCompound nbt = itemStack.getNbt();
      if (nbt != null) {
        if (nbt.isEmpty()) {
          if (config.isDebug()) {
            LOGGER.info("Item: " + ItemUtils.getTranslatedName(itemStack) + " has been fixed");
          }
          itemStack.setNbt(null);
        }
      }
      return EventResult.pass();
    });

    PartyPlaceholder.register();
  }


  public static void addAllPermissions() {
    String[] permissions = {
      "cobbleutils.user",
      "cobbleutils.admin",
      "cobbleutils.party",
      "cobbleutils.rewards",
      "cobbleutils.breed",
      "cobbleutils.bosses",
      "cobbleutils.pokerus",
      "cobbleutils.pokeshoutplus",
      "cobbleutils.pokeshoutplusall",
      "cobbleutils.hatch",
      "cobbleutils.scale",
      "cobbleutils.endbattle",
      "cobbleutils.giveitem",
      "cobbleutils.givepoke",
      "cobbleutils.givemoney",
      "cobbleutils.reload",
      "cobbleutils.shinytoken",
      "cobbleutils.pokerename",
      "cobbleutils.breedable",
      "cobbleutils.egg",
      "cobbleutils.egginfo",
      "cobbleutils.breeother",
      "cobbleutils.breedpokemons",
      "cobbleutils.breedable",
      "cobbleutils.boss",
    };

    for (String permission : permissions) {
      LuckPermsUtil.addPermission(permission);
    }
  }


  private static void tasks() {
    for (ScheduledFuture<?> task : scheduledTasks) {
      task.cancel(false);
    }
    scheduledTasks.clear();

    ScheduledFuture<?> alertreward =
      scheduler.scheduleAtFixedRate(() -> server.getPlayerManager().getPlayerList().forEach(player -> {
        RewardsData rewardsData = rewardsManager.getRewardsData().get(player.getUuid());
        if (RewardsUtils.hasRewards(player)) {
          int amount = rewardsData.getCommands().size() + rewardsData.getItems().size() + rewardsData.getPokemons().size();
          player.sendMessage(AdventureTranslator.toNative(
            language.getMessageHaveRewards().replace("%amount%", String.valueOf(amount))
          ));
        }
      }), 0, CobbleUtils.config.getAlertreward(), TimeUnit.MINUTES);

    ScheduledFuture<?> fixnbt =
      scheduler.scheduleAtFixedRate(
        () -> server.getPlayerManager().getPlayerList().forEach(CobbleUtils::fixInventory),
        0, 15, TimeUnit.SECONDS);

    scheduledTasks.add(alertreward);
    scheduledTasks.add(fixnbt);
  }


  private static void fixInventory(ServerPlayerEntity player) {
    player.getInventory().combinedInventory.forEach(inv -> {
      inv.forEach(itemStack -> {
        NbtCompound nbt = itemStack.getNbt();
        if (nbt != null) {
          if (nbt.isEmpty()) {
            if (config.isDebug()) {
              LOGGER.info("Item: " + ItemUtils.getTranslatedName(itemStack) + " has been fixed");
            }
            itemStack.setNbt(null);
          }

        }
      });
    });
  }

  private static String isActive(boolean active) {
    if (active) {
      return "§aActive";
    } else {
      return "§cInactive";
    }
  }
}
