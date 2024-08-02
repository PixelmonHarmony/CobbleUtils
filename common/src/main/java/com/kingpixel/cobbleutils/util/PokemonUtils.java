package com.kingpixel.cobbleutils.util;

import com.cobblemon.mod.common.api.abilities.Ability;
import com.cobblemon.mod.common.api.abilities.AbilityTemplate;
import com.cobblemon.mod.common.api.abilities.PotentialAbility;
import com.cobblemon.mod.common.api.moves.Move;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.api.types.ElementalType;
import com.cobblemon.mod.common.pokeball.PokeBall;
import com.cobblemon.mod.common.pokemon.*;
import com.cobblemon.mod.common.pokemon.abilities.HiddenAbilityType;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.ScalePokemonData;
import com.kingpixel.cobbleutils.Model.SizeChanceWithoutItem;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
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
   * @param pokemon The pokemon to get the data
   *
   * @return The lore with the replaced placeholders
   */
  public static List<String> replaceLore(Pokemon pokemon) {
    return replace(CobbleUtils.language.getLorepokemon(), pokemon);
  }

  /**
   * Replace the placeholders with the pokemon data
   *
   * @param pokemon The pokemon to get the data
   *
   * @return The string with the replaced placeholders
   */
  public static String replace(Pokemon pokemon) {
    return replace(CobbleUtils.language.getPokemonnameformat(), pokemon);
  }

  /**
   * Replace the placeholders with the pokemon data
   *
   * @param message The string to replace
   * @param pokemon The pokemon to get the data
   *
   * @return The string with the replaced placeholders
   */
  public static String replace(String message, Pokemon pokemon) {
    if (pokemon == null) {
      return message;
    }

    if (message.contains("%lorepokemon%")) {
      StringBuilder loreStringBuilder = new StringBuilder();
      CobbleUtils.language.getLorepokemon().forEach(lore -> loreStringBuilder.append(lore).append("\n"));

      String lorepokemon = loreStringBuilder.toString();
      message = message.replace("%lorepokemon%", lorepokemon);
    }

    Nature nature = pokemon.getNature();

    if (message.contains("%")) {
      message = message
        .replace("%level%", String.valueOf(pokemon.getLevel()))
        .replace("%nature%", getNatureTranslate(nature))
        .replace("%pokemon%", pokemon.getSpecies().getName())
        .replace("%shiny%", pokemon.getShiny() ? CobbleUtils.language.getSymbolshiny() : "")
        .replace("%ability%", getAbilityTranslate(pokemon.getAbility()))
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
        .replace("%legendary%", pokemon.isLegendary() ? CobbleUtils.language.getYes() : CobbleUtils.language.getNo())
        .replace("%item%", ItemUtils.getTranslatedName(pokemon.heldItem()))
        .replace("%size%", getSize(pokemon))
        .replace("%form%", CobbleUtils.language.getForms().getOrDefault(pokemon.getForm().getName(), pokemon.getForm().getName()))
        .replace("%up%", getStatTranslate(nature.getIncreasedStat()))
        .replace("%down%", getStatTranslate(nature.getDecreasedStat()))
        .replace("%ball%", getPokeBallTranslate(pokemon.getCaughtBall()))
        .replace("%gender%", getGenderTranslate(pokemon.getGender()))
        .replace("%ivs%", getIvsAverage(pokemon.getIvs()).toString())
        .replace("%evs%", getEvsTotal(pokemon.getEvs()).toString())
        .replace("%move1%", getMoveTranslate(pokemon.getMoveSet().get(0)))
        .replace("%move2%", getMoveTranslate(pokemon.getMoveSet().get(1)))
        .replace("%move3%", getMoveTranslate(pokemon.getMoveSet().get(2)))
        .replace("%move4%", getMoveTranslate(pokemon.getMoveSet().get(3)))
        .replace("%tradeable%", pokemon.getTradeable() ? CobbleUtils.language.getYes() : CobbleUtils.language.getNo())
        .replace("%owner%", getOwnerName(pokemon))
        .replace("%ultrabeast%", pokemon.isUltraBeast() ? CobbleUtils.language.getYes() : CobbleUtils.language.getNo())
        .replace("%types%", getType(pokemon))
        .replace("%rarity%", String.valueOf(getRarity(pokemon)))
        .replace("%breedable%", isBreedable(pokemon) ? CobbleUtils.language.getYes() : CobbleUtils.language.getNo())
        .replace("%pokerus%", isPokerus(pokemon) ? CobbleUtils.language.getYes() : CobbleUtils.language.getNo())
        .replace("%friendship%", String.valueOf(pokemon.getFriendship()))
        .replace("%ah%", haveAH(pokemon) ? CobbleUtils.language.getAH() : "");
    }

    return message;
  }

  /**
   * Check if the pokemon has pokerus
   *
   * @param pokemon The pokemon to check
   *
   * @return If the pokemon has pokerus
   */
  public static boolean isPokerus(Pokemon pokemon) {
    return pokemon.getPersistentData().getBoolean("pokerus");
  }

  /**
   * Replace the placeholders with the pokemon data
   *
   * @param message  The string to replace
   * @param pokemons The pokemon to get the data
   *
   * @return The string with the replaced placeholders
   */
  public static String replace(String message, List<Pokemon> pokemons) {
    if (pokemons.isEmpty()) {
      return message;
    }

    for (int i = 0; i < pokemons.size(); i++) {
      Pokemon pokemon = pokemons.get(i);
      String indexedMessage = message;
      int index = i + 1;
      Nature nature = pokemon.getNature();
      if (indexedMessage.contains("%")) {
        if (pokemon == null) {
          continue;
        }
        indexedMessage = indexedMessage.replace("%level" + index + "%", String.valueOf(pokemon.getLevel()))
          .replace("%nature" + index + "%", getNatureTranslate(nature))
          .replace("%pokemon" + index + "%", getTranslatedName(pokemon))
          .replace("%shiny" + index + "%", pokemon.getShiny() ? CobbleUtils.language.getSymbolshiny() : "")
          .replace("%ability" + index + "%", getAbilityTranslate(pokemon.getAbility()))
          .replace("%ivshp" + index + "%", String.valueOf(pokemon.getIvs().get(Stats.HP)))
          .replace("%ivsatk" + index + "%", String.valueOf(pokemon.getIvs().get(Stats.ATTACK)))
          .replace("%ivsdef" + index + "%", String.valueOf(pokemon.getIvs().get(Stats.DEFENCE)))
          .replace("%ivsspa" + index + "%", String.valueOf(pokemon.getIvs().get(Stats.SPECIAL_ATTACK)))
          .replace("%ivsspdef" + index + "%", String.valueOf(pokemon.getIvs().get(Stats.SPECIAL_DEFENCE)))
          .replace("%ivsspeed" + index + "%", String.valueOf(pokemon.getIvs().get(Stats.SPEED)))
          .replace("%evshp" + index + "%", String.valueOf(pokemon.getEvs().get(Stats.HP)))
          .replace("%evsatk" + index + "%", String.valueOf(pokemon.getEvs().get(Stats.ATTACK)))
          .replace("%evsdef" + index + "%", String.valueOf(pokemon.getEvs().get(Stats.DEFENCE)))
          .replace("%evsspa" + index + "%", String.valueOf(pokemon.getEvs().get(Stats.SPECIAL_ATTACK)))
          .replace("%evsspdef" + index + "%", String.valueOf(pokemon.getEvs().get(Stats.SPECIAL_DEFENCE)))
          .replace("%evsspeed" + index + "%", String.valueOf(pokemon.getEvs().get(Stats.SPEED)))
          .replace("%legendary" + index + "%", pokemon.isLegendary() ? CobbleUtils.language.getYes() : CobbleUtils.language.getNo())
          .replace("%item" + index + "%", pokemon.heldItem().getHoverName().getString())
          .replace("%size" + index + "%", getSize(pokemon))
          .replace("%form" + index + "%", CobbleUtils.language.getForms().getOrDefault(pokemon.getForm().getName(), pokemon.getForm().getName()))
          .replace("%up" + index + "%", getStatTranslate(nature.getIncreasedStat()))
          .replace("%down" + index + "%", getStatTranslate(nature.getDecreasedStat()))
          .replace("%ball" + index + "%", getPokeBallTranslate(pokemon.getCaughtBall()))
          .replace("%gender" + index + "%", getGenderTranslate(pokemon.getGender()))
          .replace("%ivs" + index + "%", getIvsAverage(pokemon.getIvs()).toString())
          .replace("%evs" + index + "%", getEvsTotal(pokemon.getEvs()).toString())
          .replace("%move" + index + "1%", getMoveTranslate(pokemon.getMoveSet().get(0)))
          .replace("%move" + index + "2%", getMoveTranslate(pokemon.getMoveSet().get(1)))
          .replace("%move" + index + "3%", getMoveTranslate(pokemon.getMoveSet().get(2)))
          .replace("%move" + index + "4%", getMoveTranslate(pokemon.getMoveSet().get(3)))
          .replace("%tradeable" + index + "%", pokemon.getTradeable() ? CobbleUtils.language.getYes() : CobbleUtils.language.getNo())
          .replace("%owner" + index + "%", getOwnerName(pokemon))
          .replace("%ultrabeast" + index + "%", pokemon.isUltraBeast() ? CobbleUtils.language.getYes() : CobbleUtils.language.getNo())
          .replace("%types" + index + "%", getType(pokemon))
          .replace("%rarity" + index + "%", String.valueOf(getRarity(pokemon)))
          .replace("%breedable" + index + "%", isBreedable(pokemon) ? CobbleUtils.language.getYes() : CobbleUtils.language.getNo());

      }

      message = indexedMessage;
    }

    return message;
  }

  /**
   *
   */
  public static String getTranslatedName(Pokemon pokemon) {
    return "<lang:cobblemon.species." + pokemon.getSpecies().showdownId() + ".name>";
  }


  /**
   * Check if the pokemon is breedable
   *
   * @param pokemon The pokemon to check
   *
   * @return If the pokemon is breedable
   */
  public static boolean isBreedable(Pokemon pokemon) {
    if (pokemon.getPersistentData() != null && pokemon.getPersistentData().contains("breedable") &&
      !pokemon.getPersistentData().getBoolean("breedable")) {
      return pokemon.getPersistentData().getBoolean("breedable");
    } else {
      return true;
    }
  }

  /**
   * Get the owner name of the pokemon
   *
   * @param pokemon The pokemon to get the owner name
   *
   * @return The owner name of the pokemon
   */
  public static String getOwnerName(Pokemon pokemon) {
    String owner = pokemon.getOriginalTrainerName();
    if (owner == null) owner = CobbleUtils.language.getNone();
    return owner;
  }

  /**
   * Get the size of the pokemon
   *
   * @param pokemon The pokemon to get the size
   *
   * @return The size of the pokemon
   */
  public static String getSize(Pokemon pokemon) {
    return getSizeName(pokemon);
  }

  /**
   * Get the total of the IVs
   *
   * @param iVs The IVs to get the total
   *
   * @return The total of the IVs
   */
  public static Integer getIvsTotal(IVs iVs) {
    AtomicInteger sum = new AtomicInteger();
    iVs.forEach((ivs) -> sum.addAndGet(ivs.getValue()));
    return sum.get();
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
   * Get the total of the EVs
   *
   * @param eVs The EVs to get the total
   *
   * @return The total of the EVs
   */
  public static Integer getEvsTotal(EVs eVs) {
    AtomicInteger sum = new AtomicInteger();
    eVs.forEach((evs) -> sum.addAndGet(evs.getValue()));
    return sum.get();
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
    return "<lang:cobblemon.ability." + ability.getName() + ">";
  }

  /**
   * Get the nature translation
   *
   * @param nature The nature to translate
   *
   * @return The nature translation
   */
  public static String getNatureTranslate(Nature nature) {
    return "<lang:cobblemon.nature." + nature.getName().getPath() + ">";
  }

  private static String getMoveColor(ElementalType type, String lang) {
    if (type == null) return CobbleUtils.language.getNone();
    String color = CobbleUtils.language.getMovecolor().getOrDefault(type.getName(), "");
    if (color.contains("gradient")) return color + "<lang:" + lang + ">" + "</gradient>";
    return color + "<lang:" + lang + ">";
  }

  /**
   * Get the type of the pokemon
   *
   * @param pokemon The pokemon to get the type
   *
   * @return The type of the pokemon
   */
  public static String getType(Pokemon pokemon) {

    StringBuilder s =
      new StringBuilder(CobbleUtils.language.getTypes().getOrDefault(pokemon.getPrimaryType().getName(), pokemon.getPrimaryType().getName()));
    if (pokemon.getSecondaryType() != null) {
      s.append(" &7/ ");
      s.append(CobbleUtils.language.getTypes().getOrDefault(pokemon.getSecondaryType().getName(), pokemon.getSecondaryType().getName()));
    }
    return s.toString();
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
    return getMoveColor(move.getType(), "cobblemon.move." + move.getName());
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
        return "<lang:cobblemon.ui.stats.hp>";
      case "cobblemon.attack":
        return "<lang:cobblemon.ui.stats.atk>";
      case "cobblemon.defence":
        return "<lang:cobblemon.ui.stats.def>";
      case "cobblemon.special_attack":
        return "<lang:cobblemon.ui.stats.sp_atk>";
      case "cobblemon.special_defence":
        return "<lang:cobblemon.ui.stats.sp_def>";
      case "cobblemon.speed":
        return "<lang:cobblemon.ui.stats.speed>";
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

  /**
   * Get the rarity of the pokemon
   *
   * @param pokemon The pokemon to get the rarity
   *
   * @return The rarity of the pokemon
   */
  public static double getRarity(Pokemon pokemon) {
    return CobbleUtils.spawnRates.getRarity(pokemon);
  }

  /**
   * Get the rarity of the pokemon
   *
   * @param pokemon The pokemon to get the rarity
   *
   * @return The rarity of the pokemon
   */
  public static String getRarityS(Pokemon pokemon) {
    double rarity = getRarity(pokemon);
    for (String key : CobbleUtils.config.getRarity().keySet()) {
      if (rarity < CobbleUtils.config.getRarity().get(key)) {
        return key;
      }
    }
    return "common";
  }

  /**
   * Get the size of the pokemon
   *
   * @param pokemon The pokemon to get the size
   *
   * @return The size of the pokemon
   */
  public static String getSizeName(Pokemon pokemon) {
    float scaleModifier = pokemon.getScaleModifier();
    ScalePokemonData scalePokemonData = ScalePokemonData.getScalePokemonData(pokemon);
    return scalePokemonData.getSizes().stream()
      .min(Comparator.comparingDouble(size -> Math.abs(size.getSize() - scaleModifier)))
      .map(SizeChanceWithoutItem::getId)
      .orElse("Normal");
  }

  /**
   * Get the hidden ability of the pokemon
   *
   * @param pokemon The pokemon to get the hidden ability
   *
   * @return The hidden ability of the pokemon
   */
  public static Ability getAH(Pokemon pokemon) {
    return getAH(pokemon.getSpecies());
  }

  /**
   * Get the hidden ability of the species
   *
   * @param species The species to get the hidden ability
   *
   * @return The hidden ability of the species
   */
  public static Ability getAH(Species species) {
    for (PotentialAbility ability : species.getAbilities()) {
      if (ability.getType() instanceof HiddenAbilityType) {
        return ability.getTemplate().create(true);
      }
    }
    return null;
  }

  /**
   * Check if the pokemon has the hidden ability
   *
   * @param pokemon The pokemon to check
   *
   * @return If the pokemon has the hidden ability
   */
  public static boolean haveAH(Pokemon pokemon) {
    for (PotentialAbility ability : pokemon.getSpecies().getAbilities()) {
      if (ability.getType() instanceof HiddenAbilityType) {
        return ability.getTemplate().getName().equalsIgnoreCase(pokemon.getAbility().getName());
      }
    }
    return false;
  }

  /**
   * Check if the pokemon has the hidden ability
   *
   * @param pokemon         The pokemon to check
   * @param abilityTemplate The ability to check
   *
   * @return If the pokemon has the hidden ability
   */
  public static boolean isAH(Pokemon pokemon, AbilityTemplate abilityTemplate) {
    return isAH(pokemon.getSpecies(), abilityTemplate.create(true));
  }

  /**
   * Check if the species has the hidden ability
   *
   * @param species The species to check
   * @param ability The ability to check
   *
   * @return If the species has the hidden ability
   */
  private static boolean isAH(Species species, Ability ability) {
    for (PotentialAbility potentialAbility : species.getAbilities()) {
      if (potentialAbility.getType() instanceof HiddenAbilityType) {
        if (potentialAbility.getTemplate().create(true).getName().equalsIgnoreCase(ability.getName())) {
          return true;
        }
      }
    }
    return false;
  }


}
