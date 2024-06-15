package com.kingpixel.cobbleutils.events;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.Pokestop.PokeStopModel;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.kingpixel.cobbleutils.util.CobbleUtilities;
import com.kingpixel.cobbleutils.util.PokeStopUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

/**
 * @author Carlos Varas Alonso - 14/06/2024 21:39
 */
public class EntityRightClickEvent {
  public static void register(Player player, Entity entity, InteractionHand hand) {
    if (hand == InteractionHand.MAIN_HAND) {
      if (entity == null) {
        return;
      }
      try {
        if (entity instanceof PokemonEntity) {
          PokemonEntity pokemon = (PokemonEntity) entity;
          // PokeStop
          if (pokemon.getPokemon().getSpecies().getName().equalsIgnoreCase("pokestop")) {
            pokeparada(pokemon, player);
          }
        }
      } catch (Exception ignored) {
      }
    }
  }

  private static void pokeparada(PokemonEntity pokemon, Player player) {
    try {
      UUID pokemonuuid = pokemon.getPokemon().getUuid();

      String type = CobbleUtils.pokestopManager.getTypepokestop().get(pokemonuuid).getType();
      PokeStopModel pokeStopModel = CobbleUtils.pokestops.getPokestops().stream()
        .filter(pokestop -> pokestop.getType().equals(type))
        .findFirst()
        .orElse(null);

      if (pokeStopModel == null) {
        player.sendSystemMessage(Component.literal("Error al tocar la pokeparada no existe el tipo de pokeparada"));
        return;
      }

      Date cooldown = CobbleUtils.pokestopManager.getCooldownplayer().getOrDefault(player.getUUID(), new HashMap<>()).get(pokemonuuid);

      if (cooldown != null) {
        if (cooldown.after(new Date())) {
          String message = CobbleUtils.language.getMessagepokestopcooldown();
          message = message.replace("%cooldown%", CobbleUtilities.convertSecondsToTime((int) ((cooldown.getTime() - new Date().getTime()) / 1000)));
          player.sendSystemMessage(AdventureTranslator.toNative(message));
          return;
        } else {
          CobbleUtils.pokestopManager.getCooldownplayer().get(player.getUUID()).remove(pokemonuuid);
        }
      }

      PokeStopUtils.giveloot(pokemon, player);

      // Registra el nuevo cooldown
      CobbleUtils.pokestopManager.addIfNotExistsPokestop(player.getUUID(), pokemonuuid, pokeStopModel.getCooldown());

    } catch (Exception e) {
      player.sendSystemMessage(Component.literal("Error al tocar la pokeparada"));
      e.printStackTrace();
    }
  }
}
