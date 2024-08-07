package com.kingpixel.cobbleutils.features.breeding;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.features.breeding.events.EggThrow;
import com.kingpixel.cobbleutils.features.breeding.events.PastureUI;
import com.kingpixel.cobbleutils.features.breeding.events.WalkBreeding;
import com.kingpixel.cobbleutils.features.breeding.manager.ManagerPlotEggs;
import com.kingpixel.cobbleutils.features.breeding.models.EggData;
import com.kingpixel.cobbleutils.util.Utils;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.ChunkEvent;
import dev.architectury.event.events.common.InteractionEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

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

    if (CobbleUtils.server != null) {
      CobbleUtils.server.getPlayerList().getPlayers().forEach(managerPlotEggs::checking);
    }

    scheduler.scheduleAtFixedRate(() -> {
        try {
          CobbleUtils.server.getPlayerList().getPlayers().forEach(managerPlotEggs::checking);
        } catch (Exception e) {
          e.printStackTrace();
        }
      },
      0, 1, TimeUnit.MINUTES);

    LifecycleEvent.SERVER_STOPPING.register(instance -> scheduler.shutdown());


    ChunkEvent.SAVE_DATA.register((chunkAccess, level, compoundTag) -> {
      if (!CobbleUtils.breedconfig.isSpawnEggWorld()) return;
      if (Utils.RANDOM.nextInt(1000) < CobbleUtils.breedconfig.getPercentagespawnegg()) {
        EggData.spawnEgg(chunkAccess, level);
      }
    });

    PlayerEvent.ATTACK_ENTITY.register((player, level, target, hand, result) -> egg(target, (ServerPlayer) player));

    InteractionEvent.INTERACT_ENTITY.register((player, entity, hand) -> egg(entity, (ServerPlayer) player));
  }

  private static EventResult egg(Entity entity, ServerPlayer player) {
    try {
      if (entity == null) return EventResult.pass();
      if (entity instanceof PokemonEntity pokemonEntity) {
        Pokemon pokemon = pokemonEntity.getPokemon();
        if (pokemon.getSpecies().showdownId().equalsIgnoreCase("egg")) {
          if (pokemon.getPersistentData().getBoolean("EggSpawned")) {
            pokemon.getPersistentData().remove("EggSpawned");
            CobbleUtils.LOGGER.info("persistentdata: " + pokemon.getPersistentData().getAsString());
            try {
              Cobblemon.INSTANCE.getStorage().getParty(player.getUUID()).add(pokemon);
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

  private static void events() {
    WalkBreeding.register();
    EggThrow.register();
    PastureUI.register();
  }
}
