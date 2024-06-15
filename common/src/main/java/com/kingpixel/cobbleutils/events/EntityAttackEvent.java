package com.kingpixel.cobbleutils.events;

import ca.landonjw.gooeylibs2.api.UIManager;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.UI.LootPokeStopUI;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

import java.util.Objects;

/**
 * @author Carlos Varas Alonso - 15/06/2024 16:46
 */
public class EntityAttackEvent {
  public static void register(Player player, Level world, Entity entity, InteractionHand hand, EntityHitResult entityHitResult) {
    try {
      if (hand != InteractionHand.MAIN_HAND) {
        return; // Salir si no es la mano principal
      }

      if (entity == null) {
        return; // Salir si no hay entidad
      }

      if (!(entity instanceof PokemonEntity)) {
        return; // Salir si no es un PokemonEntity
      }

      PokemonEntity pokemon = (PokemonEntity) entity;
      String speciesName = pokemon.getPokemon().getSpecies().getName();

      if (speciesName.equalsIgnoreCase("pokestop")) {
        pokeparada(pokemon, player); // Llamar al método pokeparada si es un PokeStop
      }
    } catch (Exception e) {
      CobbleUtils.LOGGER.error("Error al interactuar con la entidad: " + entity); // Imprimir un mensaje de error en el log
      e.printStackTrace(); // Imprimir la traza de la excepción para depuración
    }
  }


  private static void pokeparada(PokemonEntity pokemon, Player player) {
    String type = CobbleUtils.pokestopManager.getTypepokestop().get(pokemon.getPokemon().getUuid()).getType();
    if (type == null) {
      player.sendSystemMessage(Component.literal("¡Tipo de PokeStop no encontrado!"));
      return;
    }
    UIManager.openUIForcefully((ServerPlayer) player, Objects.requireNonNull(LootPokeStopUI.open(type)));
  }
}
