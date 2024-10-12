package com.kingpixel.cobbleutils.features.breeding.events;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.features.breeding.models.EggData;
import com.kingpixel.cobbleutils.util.PlayerUtils;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.entity.EntityPose;
import net.minecraft.util.math.Vec3d;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Carlos Varas Alonso - 23/07/2024 21:49
 */
public class WalkBreeding {
  private static Map<UUID, Vec3d> lastPosition = new ConcurrentHashMap<>();
  private static Map<UUID, Integer> ticks = new ConcurrentHashMap<>() {
  };

  public static void register() {

    TickEvent.PLAYER_POST.register(player -> {
      if (!player.isInPose(EntityPose.FALL_FLYING) &&
        !player.isInPose(EntityPose.SLEEPING)) {
        ticks.compute(player.getUuid(), (uuid, integer) -> integer == null ? 1 : integer + 1);
        if (ticks.get(player.getUuid()) % CobbleUtils.breedconfig.getTickstocheck() != 0)
          return;
        AtomicInteger distanceMoved = new AtomicInteger();
        try {
          if (lastPosition.get(player.getUuid()) == null) {
            lastPosition.put(player.getUuid(), new Vec3d(player.getX(), 0, player.getZ()));
            return;
          } else {
            Vec3d currentPosition = new Vec3d(player.getX(), 0, player.getZ());
            double total = currentPosition.distanceTo(lastPosition.get(player.getUuid()));
            if (total >= 25) {
              lastPosition.put(player.getUuid(), currentPosition);
              return;
            }
            distanceMoved.set((int) Math.min(20, total));
            lastPosition.put(player.getUuid(), currentPosition);
          }
          PlayerPartyStore playerPartyStore = Cobblemon.INSTANCE.getStorage().getParty(player.getUuid());
          if (playerPartyStore.size() == 0)
            return;
          Pokemon firstplace = playerPartyStore.get(0);
          boolean duplicate;
          if (firstplace != null) {
            if (firstplace.getAbility().getName().equalsIgnoreCase("flamebody")
              || firstplace.getAbility().getName().equalsIgnoreCase("magmaarmor")) {
              duplicate = true;
            } else {
              duplicate = false;
            }
          } else {
            duplicate = false;
          }

          playerPartyStore.forEach(pokemon -> {
            if (!pokemon.showdownId().equalsIgnoreCase("egg"))
              return;
            pokemon.setCurrentHealth(0);
            EggData eggData = EggData.from(pokemon);
            if (eggData == null)
              return;
            int distance = distanceMoved.get();
            if (duplicate) {
              distance = distance * 2;
            }
            eggData.steps(PlayerUtils.castPlayer(player), pokemon, distance);
          });
        } catch (NoPokemonStoreException e) {
          throw new RuntimeException(e);
        }
      }
    });

  }
}
