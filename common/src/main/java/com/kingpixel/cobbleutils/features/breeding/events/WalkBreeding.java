package com.kingpixel.cobbleutils.features.breeding.events;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.moves.BenchedMove;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.pokemon.Species;
import com.google.gson.JsonObject;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.features.breeding.models.EggData;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Carlos Varas Alonso - 23/07/2024 21:49
 */
public class WalkBreeding {
  private Map<UUID, Vec3i> lastPos = new HashMap<>();
  private static boolean active = false;

  public static void register() {

    TickEvent.PLAYER_POST.register(player -> {
      if (player.isSprinting()) {
        try {
          Cobblemon.INSTANCE.getStorage().getParty(player.getUUID()).forEach(pokemon -> {
            if (!pokemon.showdownId().equalsIgnoreCase("egg")) return;
            EggData eggData = EggData.from(pokemon);
            int walk = pokemon.getPersistentData().getInt("steps");
            if (CobbleUtils.config.isDebug())
              CobbleUtils.LOGGER.info(eggData.toString());
            pokemon.getPersistentData().putInt("steps", walk - 1);
            if (walk <= 0) {
              String species = pokemon.getPersistentData().getString("species");
              Species species1 = PokemonProperties.Companion.parse(species).create().getSpecies();
              player.sendSystemMessage(
                Component.literal(
                  "Your egg has hatched into a " + species1.getName() + "!"
                )
              );
              pokemon.getPersistentData().remove("steps");
              pokemon.getPersistentData().remove("species");
              pokemon.setSpecies(species1);
              species1.getMoves().getEggMoves().forEach(move -> pokemon.getBenchedMoves().add(BenchedMove.Companion.loadFromJSON(move.create().saveToJSON(new JsonObject()))));
              pokemon.setNickname(Component.literal(species1.getName()));
            }
          });
        } catch (NoPokemonStoreException e) {
          throw new RuntimeException(e);
        }
      }
    });

  }
}
