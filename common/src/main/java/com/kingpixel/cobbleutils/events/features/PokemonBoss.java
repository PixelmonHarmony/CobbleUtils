package com.kingpixel.cobbleutils.events.features;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor;
import com.cobblemon.mod.common.battles.actor.PokemonBattleActor;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.options.BossChance;
import com.kingpixel.cobbleutils.util.Utils;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import kotlin.Unit;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Mob;

/**
 * @author Carlos Varas Alonso - 20/07/2024 9:04
 */
public class PokemonBoss {
  public static void register() {
    // ? Pokemon Boss
    EntityEvent.ADD.register((entity, level) -> {
      if (!CobbleUtils.config.getBosses().isActive()) return EventResult.pass();
      if (entity instanceof PokemonEntity pokemonEntity) {
        if (((Mob) entity).isNoAi()) return EventResult.pass();
        Pokemon pokemon = pokemonEntity.getPokemon();
        if (pokemon.getShiny() || pokemon.isLegendary() || pokemon.isUltraBeast()) return EventResult.pass();
        if (pokemon.isPlayerOwned()) return EventResult.pass();
        BossChance bossChance = CobbleUtils.config.getBosses().getBossChance();
        if (bossChance == null) return EventResult.pass();
        pokemon.setLevel(Utils.RANDOM.nextInt(bossChance.getMinlevel(), bossChance.getMaxlevel()));
        pokemon.setShiny(CobbleUtils.config.getBosses().isShiny());
        pokemon.setScaleModifier(Utils.RANDOM.nextFloat(bossChance.getMinsize(), bossChance.getMaxsize()));
        pokemon.getPersistentData().putString("size", "custom");
        pokemon.getPersistentData().putString("bossrarity", bossChance.getRarity());
        pokemon.getPersistentData().putBoolean("boss", true);
        pokemon.setNickname(Component.literal(bossChance.getRarity()));
      }
      return EventResult.pass();
    });

    CobblemonEvents.THROWN_POKEBALL_HIT.subscribe(Priority.NORMAL, (evt) -> {
      if (!CobbleUtils.config.getBosses().isActive()) return Unit.INSTANCE;
      Pokemon pokemon = evt.getPokemon().getPokemon();
      if (pokemon.getPersistentData().getBoolean("boss")) evt.cancel();
      return Unit.INSTANCE;
    });

    CobblemonEvents.BATTLE_VICTORY.subscribe(Priority.NORMAL, (evt) -> {
      if (!CobbleUtils.config.getBosses().isActive()) return Unit.INSTANCE;
      evt.getLosers().forEach(battleActor -> {
        if (battleActor instanceof PokemonBattleActor pokemonBattleActor) {
          Pokemon pokemon = pokemonBattleActor.getPokemon().getOriginalPokemon();
          if (pokemon.isPlayerOwned()) return;
          if (pokemon.getPersistentData().getBoolean("boss")) {
            evt.getWinners().forEach(winner -> {
              if (winner instanceof PlayerBattleActor playerBattleActor) {
                CobbleUtils.config.getBosses().giveRewards(pokemon.getPersistentData().getString("bossrarity"),
                  playerBattleActor.getEntity());
              }
            });
          }
        }
      });
      return Unit.INSTANCE;
    });
  }
}
