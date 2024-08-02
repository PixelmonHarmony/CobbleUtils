package com.kingpixel.cobbleutils.features.breeding.events;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.kingpixel.cobbleutils.features.breeding.models.EggData;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Carlos Varas Alonso - 23/07/2024 21:49
 */
public class WalkBreeding {
  private static Map<UUID, Vec3> lastPosition = new HashMap<>();
  private static Map<UUID, Integer> ticks = new HashMap<>();

  public static void register() {

    TickEvent.PLAYER_POST.register(player -> {
      if (!player.hasPose(Pose.FALL_FLYING) && !player.isInvulnerable() && !player.isCreative() && !player.isSpectator()) {
        ticks.compute(player.getUUID(), (uuid, integer) -> integer == null ? 1 : integer + 1);
        if (ticks.get(player.getUUID()) % 20 != 0) return;
        int distanceMoved;
        try {
          if (lastPosition.get(player.getUUID()) == null) {
            lastPosition.put(player.getUUID(), new Vec3(player.getX(), 0, player.getZ()));
            return;
          } else {
            Vec3 currentPosition = new Vec3(player.getX(), 0, player.getZ());
            distanceMoved = (int) Math.min(999, currentPosition.distanceTo(lastPosition.get(player.getUUID())));
            lastPosition.put(player.getUUID(), currentPosition);
          }

          Cobblemon.INSTANCE.getStorage().getParty(player.getUUID()).forEach(pokemon -> {
            if (!pokemon.showdownId().equalsIgnoreCase("egg")) return;
            pokemon.setCurrentHealth(0);
            EggData.from(pokemon).steps(pokemon, distanceMoved);
          });
        } catch (NoPokemonStoreException e) {
          throw new RuntimeException(e);
        }
      }
    });

  }
}
