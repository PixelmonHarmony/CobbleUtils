package com.kingpixel.cobbleutils.features.breeding;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.CobbleUtilsTags;
import com.kingpixel.cobbleutils.features.breeding.events.EggThrow;
import com.kingpixel.cobbleutils.features.breeding.events.NationalityPokemon;
import com.kingpixel.cobbleutils.features.breeding.events.PastureUI;
import com.kingpixel.cobbleutils.features.breeding.events.WalkBreeding;
import com.kingpixel.cobbleutils.features.breeding.manager.ManagerPlotEggs;
import com.kingpixel.cobbleutils.features.breeding.models.EggData;
import com.kingpixel.cobbleutils.util.RewardsUtils;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.ChunkEvent;
import dev.architectury.event.events.common.InteractionEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * @author Carlos Varas Alonso - 23/07/2024 9:24
 */
public class Breeding {
  public static ManagerPlotEggs managerPlotEggs = new ManagerPlotEggs();
  public static Map<UUID, String> playerCountry = new HashMap<>();
  private static final String API_URL_IP = "http://ip-api.com/json/";
  private static boolean active = false;
  private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
  private static final List<ScheduledFuture<?>> scheduledTasks = new CopyOnWriteArrayList<>();
  private static final String FOLDER_WORLD = "/world/region/";

  public static void register() {
    if (!active) {
      events();
      active = true;
    }

    if (CobbleUtils.server != null) {
      CobbleUtils.server.getPlayerManager().getPlayerList().forEach(managerPlotEggs::checking);
    }

    for (ScheduledFuture<?> task : scheduledTasks) {
      task.cancel(false);
    }
    scheduledTasks.clear();

    // Crear una nueva tarea
    ScheduledFuture<?> checkegg = scheduler.scheduleAtFixedRate(() -> {
      try {
        CobbleUtils.server.getPlayerManager().getPlayerList().forEach(managerPlotEggs::checking);
      } catch (Exception e) {
        e.printStackTrace();
      }
      CobbleUtils.server.getPlayerManager().getPlayerList().forEach(player -> {
        String country = playerCountry.get(player.getUuid());
        if (country == null)
          return;
        try {
          Cobblemon.INSTANCE.getStorage().getPC(player.getUuid()).forEach(pokemon -> {
            if (pokemon.getPersistentData().getString(CobbleUtilsTags.COUNTRY_TAG).isEmpty()) {
              pokemon.getPersistentData().putString(CobbleUtilsTags.COUNTRY_TAG, country);
            }
          });
          Cobblemon.INSTANCE.getStorage().getParty(player.getUuid()).forEach(pokemon -> {
            if (pokemon.getPersistentData().getString(CobbleUtilsTags.COUNTRY_TAG).isEmpty()) {
              pokemon.getPersistentData().putString(CobbleUtilsTags.COUNTRY_TAG, country);
            }
          });
        } catch (NoPokemonStoreException e) {
          e.printStackTrace();
        }
      });
    }, 0, CobbleUtils.breedconfig.getCheckEggToBreedInSeconds(), TimeUnit.SECONDS);

    scheduledTasks.add(checkegg);
  }

  private static void events() {
    PlayerEvent.PLAYER_JOIN.register(player -> {
      managerPlotEggs.init(player);
      countryPlayer(player);
    });

    LifecycleEvent.SERVER_STOPPING.register(instance -> {
      for (ScheduledFuture<?> task : scheduledTasks) {
        task.cancel(false);
      }
      scheduledTasks.clear();
    });

    ChunkEvent.LOAD_DATA.register((chunk, level, nbtCompound) -> {
      if (!CobbleUtils.breedconfig.isSpawnEggWorld()) return;
      if (nbtCompound.isEmpty()) {
        EggData.spawnEgg(chunk, level);
      }
    });


    PlayerEvent.ATTACK_ENTITY
      .register((player, level, target, hand, result) -> egg(target, (ServerPlayerEntity) player));

    InteractionEvent.INTERACT_ENTITY.register((player, entity, hand) -> egg(entity, (ServerPlayerEntity) player));

    WalkBreeding.register();
    EggThrow.register();
    PastureUI.register();
    NationalityPokemon.register();
  }

  private static EventResult egg(Entity entity, ServerPlayerEntity player) {
    try {
      if (entity == null)
        return EventResult.pass();
      if (entity instanceof PokemonEntity pokemonEntity) {
        Pokemon pokemon = pokemonEntity.getPokemon();
        if (pokemon.getSpecies().showdownId().equalsIgnoreCase("egg")) {
          if (pokemon.getPersistentData().getBoolean("EggSpawned")) {
            pokemon.getPersistentData().remove("EggSpawned");
            CobbleUtils.LOGGER.info("persistentdata: " + pokemon.getPersistentData());
            try {
              RewardsUtils.saveRewardPokemon(player, pokemon);
            } catch (NoPokemonStoreException e) {
              throw new RuntimeException(e);
            }
            pokemonEntity.remove(Entity.RemovalReason.KILLED);
          }
        }
        return EventResult.pass();
      }
      return EventResult.pass();
    } catch (Exception e) {
      e.printStackTrace();
      return EventResult.pass();
    }
  }

  private static void countryPlayer(ServerPlayerEntity player) {
    if (playerCountry.get(player.getUuid()) != null)
      return;
    CompletableFuture.runAsync(() -> {
      HttpURLConnection conn = null;
      BufferedReader in = null;
      try {
        URL url = new URL(API_URL_IP + player.getIp());
        conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        JsonObject json = JsonParser.parseReader(in).getAsJsonObject();

        if (json.has("country")) {
          String city = json.get("country").getAsString();
          playerCountry.put(player.getUuid(), city);
        }

      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        // Ensure resources are closed
        try {
          if (in != null) {
            in.close();
          }
          if (conn != null) {
            conn.disconnect();
          }
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    });
  }
}
