package com.kingpixel.cobbleutils.events.features;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
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

import static com.kingpixel.cobbleutils.Model.CobbleUtilsTags.*;

/**
 * @author Carlos Varas Alonso - 20/07/2024 9:04
 */
public class PokemonBoss {

  public static void register() {
    // ? Pokemon Boss
    EntityEvent.ADD.register((entity, level) -> {
      try {
        if (!CobbleUtils.config.getBosses().isActive()) return EventResult.pass();
        if (entity instanceof PokemonEntity pokemonEntity) {
          if (((Mob) entity).isNoAi()) return EventResult.pass();
          Pokemon pokemon = pokemonEntity.getPokemon();
          if (pokemon.getShiny() || pokemon.isLegendary() || pokemon.isUltraBeast()) return EventResult.pass();
          if (pokemon.isPlayerOwned()) return EventResult.pass();
          PokemonProperties.Companion.parse("uncatchable=yes").apply(pokemon);
          BossChance bossChance = CobbleUtils.config.getBosses().getBossChance();
          if (bossChance == null) return EventResult.pass();
          pokemon.setLevel(Utils.RANDOM.nextInt(bossChance.getMinlevel(), bossChance.getMaxlevel()));
          pokemon.getPersistentData().putString(BOSS_RARITY_TAG, bossChance.getRarity());
          pokemon.getPersistentData().putBoolean(BOSS_TAG, true);
          pokemon.setNickname(Component.literal(bossChance.getRarity()));
          pokemon.setShiny(CobbleUtils.config.getBosses().isShiny());
          pokemon.getPersistentData().putString(SIZE_TAG, SIZE_CUSTOM_TAG);
          pokemon.setScaleModifier(Utils.RANDOM.nextFloat(bossChance.getMinsize(), bossChance.getMaxsize()));
        }
      } catch (Exception e) {
        e.printStackTrace();
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
          if (pokemon.getPersistentData().getBoolean(BOSS_TAG)) {
            evt.getWinners().forEach(winner -> {
              if (winner instanceof PlayerBattleActor playerBattleActor) {
                CobbleUtils.config.getBosses().giveRewards(pokemon.getPersistentData().getString(BOSS_RARITY_TAG), playerBattleActor.getEntity());
              }
            });
          }
        }
      });
      return Unit.INSTANCE;
    });
  }
}
