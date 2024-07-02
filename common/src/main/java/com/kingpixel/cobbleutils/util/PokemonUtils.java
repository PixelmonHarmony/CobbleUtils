package com.kingpixel.cobbleutils.util;

import com.cobblemon.mod.common.api.abilities.Ability;
import com.cobblemon.mod.common.api.moves.Move;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.pokeball.PokeBall;
import com.cobblemon.mod.common.pokemon.*;
import com.kingpixel.cobbleutils.CobbleUtils;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Carlos Varas Alonso - 28/06/2024 18:53
 */
public class PokemonUtils {
  /**
   * Replace the placeholders with the pokemon data
   *
   * @param lore    The lore to replace
   * @param pokemon The pokemon to get the data
   *
   * @return The lore with the replaced placeholders
   */
  public static List<String> replace(List<String> lore, Pokemon pokemon) {
    List<String> finalLore = new ArrayList<>();
    for (String s : lore) {
      if (s.contains("%lorepokemon%")) {
        for (String additionalLine : CobbleUtils.language.getLorepokemon()) {
          replace(pokemon, finalLore, additionalLine);
        }
      } else {
        replace(pokemon, finalLore, s);
      }
    }
    return finalLore;
  }

  private static void replace(Pokemon pokemon, List<String> finalLore, String s) {
    String replaced = replace(s, pokemon);
    for (int i = 0; i < 4; i++) {
      replaced = replaced.replace("%move" + (i + 1) + "%", getMoveTranslate(pokemon.getMoveSet().get(i)));
    }
    replaced = replaced.replace("%lorepokemon%", "");
    finalLore.add(replaced);
  }

  /**
   * Get the showdown id of the pokemon
   *
   * @param pokemon The pokemon to get the id
   *
   * @return The showdown id of the pokemon
   */
  public static String getIdentifierPokemon(Pokemon pokemon) {
    return pokemon.getSpecies().showdownId();
  }

  /**
   * Replace the placeholders with the pokemon data
   *
   * @param s       The string to replace
   * @param pokemon The pokemon to get the data
   *
   * @return The string with the replaced placeholders
   */
  public static String replace(String s, Pokemon pokemon) {
    Nature nature = pokemon.getNature();
    if (pokemon.getPersistentData().contains("breedable") &&
      !pokemon.getPersistentData().getBoolean("breedable")) {
      s = s.replace("%breedable%", pokemon.getPersistentData().getBoolean("breedable") ? CobbleUtils.language.getYes() :
        CobbleUtils.language.getNo());
    } else {
      s = s.replace("%breedable%", CobbleUtils.language.getYes());
    }

    return s.replace("%level%",
        String.valueOf(pokemon.getLevel()))
      .replace("%nature%", PokemonUtils.getNatureTranslate(nature))
      .replace("%pokemon%", pokemon.getSpecies().getName())
      .replace("%shiny%", pokemon.getShiny() ? CobbleUtils.language.getSymbolshiny() : "")
      .replace("%ability%", PokemonUtils.getAbilityTranslate(pokemon.getAbility()))
      .replace("%ivshp%", String.valueOf(pokemon.getIvs().get(Stats.HP)))
      .replace("%ivsatk%", String.valueOf(pokemon.getIvs().get(Stats.ATTACK)))
      .replace("%ivsdef%", String.valueOf(pokemon.getIvs().get(Stats.DEFENCE)))
      .replace("%ivsspa%", String.valueOf(pokemon.getIvs().get(Stats.SPECIAL_ATTACK)))
      .replace("%ivsspdef%", String.valueOf(pokemon.getIvs().get(Stats.SPECIAL_DEFENCE)))
      .replace("%ivsspeed%", String.valueOf(pokemon.getIvs().get(Stats.SPEED)))
      .replace("%evshp%", String.valueOf(pokemon.getEvs().get(Stats.HP)))
      .replace("%evsatk%", String.valueOf(pokemon.getEvs().get(Stats.ATTACK)))
      .replace("%evsdef%", String.valueOf(pokemon.getEvs().get(Stats.DEFENCE)))
      .replace("%evsspa%", String.valueOf(pokemon.getEvs().get(Stats.SPECIAL_ATTACK)))
      .replace("%evsspdef%", String.valueOf(pokemon.getEvs().get(Stats.SPECIAL_DEFENCE)))
      .replace("%evsspeed%", String.valueOf(pokemon.getEvs().get(Stats.SPEED)))
      .replace("%legendary%", pokemon.isLegendary() ? CobbleUtils.language.getYes() :
        CobbleUtils.language.getNo())
      .replace("%item%", pokemon.heldItem() == null ? CobbleUtils.language.getNone() : pokemon.heldItem().getHoverName().getString())
      .replace("%size%", PokemonUtils.getSize(pokemon))
      .replace("%form%", CobbleUtils.language.getForms().getOrDefault(pokemon.getForm().getName(), pokemon.getForm().getName()))
      .replace("%up%", PokemonUtils.getStatTranslate(nature.getIncreasedStat()))
      .replace("%down%", PokemonUtils.getStatTranslate(nature.getDecreasedStat()))
      .replace("%ball%", PokemonUtils.getPokeBallTranslate(pokemon.getCaughtBall()))
      .replace("%gender%", PokemonUtils.getGenderTranslate(pokemon.getGender()))
      .replace("%ivs%", getIvsAverage(pokemon.getIvs()).toString())
      .replace("%evs%", getEvsAverage(pokemon.getEvs()).toString())
      .replace("%move1%", getMoveTranslate(pokemon.getMoveSet().get(0)))
      .replace("%move2%", getMoveTranslate(pokemon.getMoveSet().get(1)))
      .replace("%move3%", getMoveTranslate(pokemon.getMoveSet().get(2)))
      .replace("%move4%", getMoveTranslate(pokemon.getMoveSet().get(3)))
      .replace("%tradeable%", pokemon.getTradeable() ? CobbleUtils.language.getYes() : CobbleUtils.language.getNo())
      .replace("%owner%", pokemon.getOwnerPlayer() != null ?
        pokemon.getOwnerPlayer().getGameProfile().getName() : CobbleUtils.language.getNone())
      .replace("%ultrabeast%", pokemon.isUltraBeast() ? CobbleUtils.language.getYes() : CobbleUtils.language.getNo());
  }

  /**
   * Get the size of the pokemon
   *
   * @param pokemon The pokemon to get the size
   *
   * @return The size of the pokemon
   */
  public static String getSize(Pokemon pokemon) {
    return CobbleUtils.config.getSizeName(pokemon.getScaleModifier());
  }

  /**
   * Get the average of the IVs
   *
   * @param iVs The IVs to get the average
   *
   * @return The average of the IVs
   */
  public static Integer getIvsAverage(IVs iVs) {
    AtomicInteger sum = new AtomicInteger();
    iVs.forEach((ivs) -> sum.addAndGet(ivs.getValue()));
    return sum.get() / 6;
  }

  /**
   * Get the average of the EVs
   *
   * @param eVs The EVs to get the average
   *
   * @return The average of the EVs
   */
  public static Integer getEvsAverage(EVs eVs) {
    AtomicInteger sum = new AtomicInteger();
    eVs.forEach((evs) -> sum.addAndGet(evs.getValue()));
    return sum.get() / 6;
  }

  /**
   * Get the ability translation
   *
   * @param ability The ability to translate
   *
   * @return The ability translation
   */
  public static String getAbilityTranslate(Ability ability) {
    return Component.translatable("cobblemon.ability." + ability.getName()).getString();
  }

  /**
   * Get the nature translation
   *
   * @param nature The nature to translate
   *
   * @return The nature translation
   */
  public static String getNatureTranslate(Nature nature) {
    return Component.translatable("cobblemon.nature." + nature.getName().getPath()).getString();
  }

  /**
   * Get the move translation
   *
   * @param move The move to translate
   *
   * @return The move translation
   */
  public static String getMoveTranslate(Move move) {
    if (move == null) return CobbleUtils.language.getNone();
    return Component.translatable("cobblemon.move." + move.getName()).getString();
  }

  /**
   * Get the stat translation
   *
   * @param stat The stat to translate
   *
   * @return The stat translation
   */
  public static String getStatTranslate(Stat stat) {
    if (stat == null) {
      return "";
    }
    switch (stat.getIdentifier().toLanguageKey()) {
      case "cobblemon.hp":
        return Component.translatable("cobblemon.ui.stats.hp").getString();
      case "cobblemon.attack":
        return Component.translatable("cobblemon.ui.stats.atk").getString();
      case "cobblemon.defence":
        return Component.translatable("cobblemon.ui.stats.def").getString();
      case "cobblemon.special_attack":
        return Component.translatable("cobblemon.ui.stats.sp_atk").getString();
      case "cobblemon.special_defence":
        return Component.translatable("cobblemon.ui.stats.sp_def").getString();
      case "cobblemon.speed":
        return Component.translatable("cobblemon.ui.stats.speed").getString();
      default:
        return "";
    }
  }

  /**
   * Get the pokeball translation
   *
   * @param caughtBall The pokeball to translate
   *
   * @return The pokeball translation
   */
  public static String getPokeBallTranslate(PokeBall caughtBall) {
    return caughtBall == null ? CobbleUtils.language.getNone() :
      Component.translatable("item.cobblemon." + caughtBall.getName().getPath()).getString();
  }

  /**
   * Get the gender translation
   *
   * @param gender The gender to translate
   *
   * @return The gender translation
   */
  public static String getGenderTranslate(Gender gender) {
    return CobbleUtils.language.getGender().getOrDefault(gender.getShowdownName(), gender.getShowdownName());
  }

}
