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
import com.kingpixel.cobbleutils.util.ArraysPokemons;
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
  private static boolean spawnboss = false;

  public static void register() {
    CobblemonEvents.POKEMON_ENTITY_SPAWN.subscribe(Priority.HIGH, (evt) -> {
      try {
        if (!CobbleUtils.config.getBosses().isActive()) {
          return Unit.INSTANCE;
        }

        if (!spawnboss) {
          if (Utils.RANDOM.nextInt(CobbleUtils.config.getBosses().getRarity()) != 0) {
            return Unit.INSTANCE;
          } else {
            spawnboss = true;
          }
        }


        Entity entity = evt.getEntity();
        if (!(entity instanceof PokemonEntity pokemonEntity)) {
          return Unit.INSTANCE;
        }

        MobEntity mobEntity = (MobEntity) entity;
        if (mobEntity.isPersistent() || mobEntity.isAiDisabled()) {
          return Unit.INSTANCE;
        }

        Pokemon pokemon = pokemonEntity.getPokemon();
        if (pokemon.isPlayerOwned() || pokemon.getShiny() || pokemon.isLegendary() || pokemon.isUltraBeast()
          || PokemonUtils.getIvsAverage(pokemon.getIvs()) == 31 || ArraysPokemons.getTypePokemon(pokemon) != ArraysPokemons.TypePokemon.NORMAL) {
          return Unit.INSTANCE;
        }

        BossChance bossChance = CobbleUtils.config.getBosses().getBossChance();
        if (bossChance == null) return Unit.INSTANCE;


        PokemonDataBoss pokemonDataBoss = CobbleUtils.config.getBosses().getPokemonDataBoss(pokemon);
        BossChance bossChanceByRarity = CobbleUtils.config.getBosses().getBossChanceByRarity(pokemon);

        if (CobbleUtils.config.getBosses().isForceAspectBoss()) {
          if (pokemonDataBoss == null) return Unit.INSTANCE;
          apply(pokemon, bossChanceByRarity, pokemonDataBoss);
        } else if (!CobbleUtils.config.getBosses().getBlacklist().contains(pokemon.showdownId())) {
          apply(pokemon, bossChance, pokemonDataBoss);
        }
      } catch (Exception e) {  // Reemplaza con excepciones específicas
        e.printStackTrace();
      }
      return Unit.INSTANCE;
    });

    CobblemonEvents.THROWN_POKEBALL_HIT.subscribe(Priority.HIGH, (evt) -> {
      if (!CobbleUtils.config.getBosses().isActive()) return Unit.INSTANCE;

      if (evt.getPokemon().getPokemon().getPersistentData().getBoolean(BOSS_TAG)) {
        evt.cancel();
      }
      return Unit.INSTANCE;
    });

    CobblemonEvents.BATTLE_VICTORY.subscribe(Priority.HIGH, (evt) -> {
      if (!CobbleUtils.config.getBosses().isActive()) {
        return Unit.INSTANCE;
      }

      evt.getLosers().forEach(battleActor -> {
        if (battleActor instanceof PokemonBattleActor pokemonBattleActor) {
          Pokemon pokemon = pokemonBattleActor.getPokemon().getOriginalPokemon();
          if (!pokemon.isPlayerOwned() && pokemon.getPersistentData().getBoolean(BOSS_TAG)) {
            evt.getWinners().forEach(winner -> {
              if (winner instanceof PlayerBattleActor playerBattleActor) {
                CobbleUtils.config.getBosses().giveRewards(
                  pokemon.getPersistentData().getString(BOSS_RARITY_TAG),
                  playerBattleActor.getEntity());
              }
            });
          }
        }
      });
      return Unit.INSTANCE;
    });
  }

  public static void apply(Pokemon pokemon, BossChance bossChance, PokemonDataBoss pokemonDataBoss) {
    String form = "";

    if (pokemonDataBoss != null) form = pokemonDataBoss.getFormsoraspects();

    PokemonProperties.Companion.parse("uncatchable=yes " + form).apply(pokemon);
    if (bossChance.getMinlevel() != bossChance.getMaxlevel()) {
      pokemon.setLevel(Utils.RANDOM.nextInt(bossChance.getMinlevel(), bossChance.getMaxlevel()));
    } else {
      pokemon.setLevel(bossChance.getMinlevel());
    }
    pokemon.getPersistentData().putString(BOSS_RARITY_TAG, bossChance.getRarity());
    pokemon.getPersistentData().putBoolean(BOSS_TAG, true);
    pokemon.setNickname(Text.literal(bossChance.getRarity()));
    pokemon.setShiny(CobbleUtils.config.getBosses().isShiny());
    pokemon.getPersistentData().putString(SIZE_TAG, SIZE_CUSTOM_TAG);
    if (bossChance.getMinsize() != bossChance.getMaxsize()) {
      pokemon.setScaleModifier(Utils.RANDOM.nextFloat(bossChance.getMinsize(), bossChance.getMaxsize()));
    } else {
      pokemon.setScaleModifier(bossChance.getMinsize());
    }
    spawnboss = false;
  }
}
