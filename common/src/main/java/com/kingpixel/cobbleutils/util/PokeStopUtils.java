package com.kingpixel.cobbleutils.util;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.Pokestop.LootPokestop;
import com.kingpixel.cobbleutils.Model.Pokestop.PokeStopData;
import com.kingpixel.cobbleutils.Model.Pokestop.PokeStopModel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Carlos Varas Alonso - 14/06/2024 22:40
 */
public class PokeStopUtils {
  public static void createPokestop(String type, Player player) {
    try {
      Vec3 pos = player.position();
      Level level = player.level();
      PokemonEntity pokemonEntity = PokemonProperties.Companion.parse("pokestop uncatchable=true").createEntity(level);
      if (pokemonEntity == null) {
        player.sendSystemMessage(Component.literal("Failed to create Pokemon entity."));
        return;
      }
      AtomicReference<PokeStopModel> pokestopm = new AtomicReference<>();
      CobbleUtils.pokestops.getPokestops().forEach(pokestop -> {
        if (pokestop.getType().equals(type)) {
          pokestopm.set(pokestop);
        }
      });

      player.sendSystemMessage(Component.literal("Pokestop type: " + type));
      if (pokestopm.get() == null) {
        player.sendSystemMessage(Component.literal("Pokestop type not found."));
        return;
      }


      try {
        Mob mob = pokemonEntity;
        mob.setNoAi(true);
        mob.setInvulnerable(true);
        mob.setSpeed(0);
        mob.setPos(pos.x(), pos.y(), pos.z());
        mob.setPersistenceRequired();
        level.addFreshEntity(mob);
        Pokemon toSpawn = pokemonEntity.getPokemon();
        UUID pokestopuuid = toSpawn.getUuid();
        PokeStopData pokeStopData = CobbleUtils.pokestopManager.getTypepokestop().get(pokestopuuid);
        if (pokeStopData == null) {
          pokeStopData = new PokeStopData(type, pos);
        } else {
          pokeStopData.setPos(pos);
        }
        CobbleUtils.pokestopManager.getTypepokestop().put(pokestopuuid, pokeStopData);
        CobbleUtils.pokestopManager.writeInfo();

        player.sendSystemMessage(Component.literal("Pokestop created."));
      } catch (Exception e) {
        player.sendSystemMessage(Component.literal("Error spawning Pokemon entity"));
        CobbleUtils.LOGGER.error("Error spawning Pokemon entity: " + e);
        System.out.println("Error spawning Pokemon entity: " + e);
        e.printStackTrace();
      }

    } catch (Exception e) {
      player.sendSystemMessage(Component.literal("Error creating pokestop"));
      CobbleUtils.LOGGER.error("Error creating pokestop: " + e);
      System.out.println("Error creating pokestop: " + e);
      e.printStackTrace();
    }
  }

  public static void giveloot(PokemonEntity pokemon, Player player) {
    try {
      UUID pokemonuuid = pokemon.getPokemon().getUuid();
      UUID playeruuid = player.getUUID();
      String type = CobbleUtils.pokestopManager.getTypepokestop().get(pokemonuuid).getType();
      PokeStopModel pokeStopModel = CobbleUtils.pokestops.getPokestops().stream().filter(pokestop -> pokestop.getType().equals(type)).findFirst().orElse(null);
      if (pokeStopModel == null) {
        player.sendSystemMessage(Component.literal("Error al tocar la pokeparada no existe el tipo de pokeparada"));
        return;
      }
      List<LootPokestop> lootPokestops = pokeStopModel.generateRewards();
      lootPokestops.forEach(lootPokestop -> {
        ItemStack itemStack = Utils.parseItemId(lootPokestop.getItem());
        CobbleUtils.server.execute(() -> {
          if (!player.getInventory().add(itemStack)) {
            player.drop(itemStack, true);
          }
        });
      });
      CobbleUtils.pokestopManager.addIfNotExistsPokestop(playeruuid, pokemonuuid, pokeStopModel.getCooldown());
      CobbleUtils.pokestopManager.writeInfo();
    } catch (Exception e) {
      player.sendSystemMessage(Component.literal("Error al tocar la pokeparada"));
      e.printStackTrace();
    }
  }

  public static Date getCooldown(int cooldown) {
    Date date = new Date();
    date.setTime(date.getTime() + TimeUnit.MINUTES.toMillis(cooldown));
    return date;
  }
}
