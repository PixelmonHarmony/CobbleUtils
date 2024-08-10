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
import com.kingpixel.cobbleutils.Model.options.PokemonDataBoss;
import com.kingpixel.cobbleutils.util.PokemonUtils;
import com.kingpixel.cobbleutils.util.Utils;
import kotlin.Unit;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.text.Text;

import static com.kingpixel.cobbleutils.Model.CobbleUtilsTags.*;

/**
 * @author Carlos Varas Alonso - 20/07/2024 9:04
 */
public class PokemonBoss {
  private static boolean boss = false;

  public static void register() {
    // ? Pokemon Boss
    CobblemonEvents.POKEMON_ENTITY_SPAWN.subscribe(Priority.HIGHEST, (evt) -> {
      try {
        if (!CobbleUtils.config.getBosses().isActive())
          return Unit.INSTANCE;
        Entity entity = evt.getEntity();
        if (entity instanceof PokemonEntity pokemonEntity) {
          if (((MobEntity) entity).isPersistent())
            return Unit.INSTANCE;
          if (((MobEntity) entity).isAiDisabled())
            return Unit.INSTANCE;
          Pokemon pokemon = pokemonEntity.getPokemon();
          if (pokemon.isPlayerOwned())
            return Unit.INSTANCE;
          if (pokemon.getShiny() || pokemon.isLegendary() || pokemon.isUltraBeast()
            || PokemonUtils.getIvsAverage(pokemon.getIvs()) == 31)
            return Unit.INSTANCE;
          BossChance bossChance = CobbleUtils.config.getBosses().getBossChance();
          if (bossChance != null) {
            boss = true;
          }
          PokemonDataBoss pokemonDataBoss = CobbleUtils.config.getBosses().getPokemonDataBoss(pokemon);
          BossChance bossChanceByRarity = CobbleUtils.config.getBosses().getBossChanceByRarity(pokemon);
          if (CobbleUtils.config.getBosses().isForceAspectBoss()) {
            if (pokemonDataBoss == null)
              return Unit.INSTANCE;
            if (boss) {
              apply(pokemon, bossChanceByRarity);
              boss = false;
              return Unit.INSTANCE;
            }
          } else {
            if (bossChance == null)
              return Unit.INSTANCE;
            if (CobbleUtils.config.getBosses().getBlacklist().contains(pokemon.showdownId()))
              return Unit.INSTANCE;
            apply(pokemon, bossChance);
            boss = false;
            return Unit.INSTANCE;
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
      return Unit.INSTANCE;
    });

    CobblemonEvents.THROWN_POKEBALL_HIT.subscribe(Priority.HIGHEST, (evt) -> {
      if (!CobbleUtils.config.getBosses().isActive())
        return Unit.INSTANCE;
      Pokemon pokemon = evt.getPokemon().getPokemon();
      if (pokemon.getPersistentData().getBoolean("boss"))
        evt.cancel();
      return Unit.INSTANCE;
    });

    CobblemonEvents.BATTLE_VICTORY.subscribe(Priority.HIGHEST, (evt) -> {
      if (!CobbleUtils.config.getBosses().isActive())
        return Unit.INSTANCE;
      evt.getLosers().forEach(battleActor -> {
        if (battleActor instanceof PokemonBattleActor pokemonBattleActor) {
          Pokemon pokemon = pokemonBattleActor.getPokemon().getOriginalPokemon();
          if (pokemon.isPlayerOwned())
            return;
          if (pokemon.getPersistentData().getBoolean(BOSS_TAG)) {
            evt.getWinners().forEach(winner -> {
              if (winner instanceof PlayerBattleActor playerBattleActor) {
                CobbleUtils.config.getBosses().giveRewards(pokemon.getPersistentData().getString(BOSS_RARITY_TAG),
                  playerBattleActor.getEntity());
              }
            });
          }
        }
      });
      return Unit.INSTANCE;
    });
  }

  private static void apply(Pokemon pokemon, BossChance bossChance) {
    PokemonProperties.Companion.parse("uncatchable=yes ").apply(pokemon);
    pokemon.setLevel(Utils.RANDOM.nextInt(bossChance.getMinlevel(), bossChance.getMaxlevel()));
    pokemon.getPersistentData().putString(BOSS_RARITY_TAG, bossChance.getRarity());
    pokemon.getPersistentData().putBoolean(BOSS_TAG, true);
    pokemon.setNickname(Text.literal(bossChance.getRarity()));
    pokemon.setShiny(CobbleUtils.config.getBosses().isShiny());
    pokemon.getPersistentData().putString(SIZE_TAG, SIZE_CUSTOM_TAG);
    pokemon.setScaleModifier(Utils.RANDOM.nextFloat(bossChance.getMinsize(), bossChance.getMaxsize()));
  }
}
