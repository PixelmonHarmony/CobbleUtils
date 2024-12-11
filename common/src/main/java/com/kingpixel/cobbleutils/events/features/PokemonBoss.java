package com.kingpixel.cobbleutils.events.features;

import com.cobblemon.mod.common.Cobblemon;
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
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.kingpixel.cobbleutils.util.ArraysPokemons;
import com.kingpixel.cobbleutils.util.PokemonUtils;
import com.kingpixel.cobbleutils.util.Utils;
import kotlin.Unit;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.Formatting;

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
          apply(pokemonEntity, bossChanceByRarity, pokemonDataBoss);
        } else if (!CobbleUtils.config.getBosses().getBlacklist().contains(pokemon.showdownId())) {
          apply(pokemonEntity, bossChance, pokemonDataBoss);
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

    /*InteractionEvent.INTERACT_ENTITY.register((playerEntity, entity, hand) -> {
      if (!CobbleUtils.config.getBosses().isActive()) return EventResult.pass();
      if (entity instanceof PokemonEntity pokemonEntity) {
        String bossRarity = pokemonEntity.getPokemon().getPersistentData().getString(BOSS_RARITY_TAG);
        if (!bossRarity.isEmpty()) {
          // Todo: Advanced System Rewards show Boss Rewards
        }
      }
      return EventResult.pass();
    });*/

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

  private static Integer oldLevel = Cobblemon.INSTANCE.getConfig().getMaxPokemonLevel();

  public static void apply(PokemonEntity pokemonEntity, BossChance bossChance, PokemonDataBoss pokemonDataBoss) {
    String form = "";
    int level = 0;
    if (pokemonDataBoss != null) form = pokemonDataBoss.getFormsoraspects();

    Pokemon pokemon = pokemonEntity.getPokemon();
    PokemonProperties.Companion.parse("uncatchable=yes " + form).apply(pokemon);
    Cobblemon.INSTANCE.getConfig().setMaxPokemonLevel(bossChance.getMaxlevel());
    if (bossChance.getMinlevel() != bossChance.getMaxlevel()) {
      level = Utils.RANDOM.nextInt(bossChance.getMinlevel(), bossChance.getMaxlevel());
      pokemon.setLevel(level);
    } else {
      level = bossChance.getMinlevel();
      pokemon.setLevel(level);
    }
    Cobblemon.INSTANCE.getConfig().setMaxPokemonLevel(oldLevel);
    pokemon.getPersistentData().putString(BOSS_RARITY_TAG, bossChance.getRarity());
    pokemon.getPersistentData().putBoolean(BOSS_TAG, true);
    pokemon.setShiny(CobbleUtils.config.getBosses().isShiny());
    pokemon.getPersistentData().putString(SIZE_TAG, SIZE_CUSTOM_TAG);
    if (bossChance.getMinsize() != bossChance.getMaxsize()) {
      pokemon.setScaleModifier(Utils.RANDOM.nextFloat(bossChance.getMinsize(), bossChance.getMaxsize()));
    } else {
      pokemon.setScaleModifier(bossChance.getMinsize());
    }
    spawnboss = false;
    if (pokemonEntity != null) {
      Scoreboard scoreboard = pokemonEntity.getWorld().getScoreboard();
      Team team = scoreboard.getTeam("glowing_team");
      if (team == null) {
        team = scoreboard.addTeam("glowing_team");
        team.setColor(Formatting.GOLD); // Set the color to blue
      }
      // Add the PokemonEntity to the team
      scoreboard.addPlayerToTeam(pokemonEntity.getEntityName(), team);
      // Set the glowing effect
      pokemonEntity.setGlowing(true);
      pokemonEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, Integer.MAX_VALUE, 0x333333, false,
        false));

      pokemonEntity.setCustomName(AdventureTranslator.toNative(
        "§9§lBoss " + PokemonUtils.getTranslatedName(pokemon)));
      bossChance.getSound().start(pokemonEntity);
      bossChance.getParticle().sendParticlesNearPlayers(pokemonEntity);
    }

  }
}
