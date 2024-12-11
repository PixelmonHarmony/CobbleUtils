package com.kingpixel.cobbleutils.Model.options;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.Model.ItemChance;
import com.kingpixel.cobbleutils.util.Utils;
import lombok.Data;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

/**
 * @author Carlos Varas Alonso - 20/07/2024 9:06
 */
@Data
public class Boss {
  private boolean active;
  private boolean shiny;
  private boolean forceAspectBoss;
  private int rarity;
  private List<String> blacklist;
  private List<BossChance> bossChances;

  public Boss() {
    this.active = true;
    this.shiny = true;
    this.forceAspectBoss = false;
    this.rarity = 8192;
    this.blacklist = List.of("ditto");
    this.bossChances = List.of(new BossChance(), new BossChance("uncommon"));
  }

  public Boss(List<BossChance> bossChances) {
    this();
    this.bossChances = bossChances;
  }

  public BossChance getBossChance(String rarity) {
    return bossChances.stream()
      .filter(bossChance -> bossChance.getRarity().equalsIgnoreCase(rarity))
      .findFirst()
      .orElse(null);
  }

  public PokemonDataBoss getPokemonDataBoss(Pokemon pokemon) {
    String showdownId = pokemon.showdownId();
    return bossChances.stream()
      .map(BossChance::getPokemons)
      .filter(pokemonDataBoss -> pokemonDataBoss.getPokemon().contains(showdownId)
        || pokemonDataBoss.getPokemon().contains("*"))
      .findFirst()
      .orElse(null);
  }

  public BossChance getBossChanceByRarity(Pokemon pokemon) {
    String showdownId = pokemon.showdownId();
    return bossChances.stream()
      .filter(bossChance -> bossChance.getPokemons().getPokemon().contains(showdownId)
        || bossChance.getPokemons().getPokemon().contains("*"))
      .findFirst()
      .orElse(null);
  }

  public BossChance getBossChance() {
    if (Utils.RANDOM.nextInt(this.rarity) == 0) {
      return getBossChanceByWeight(bossChances);
    }
    return null;
  }

  public static BossChance getBossChanceByWeight(List<BossChance> bossChances) {
    double totalWeight = bossChances.stream().mapToDouble(BossChance::getChance).sum();
    double randomValue = Utils.RANDOM.nextDouble() * totalWeight;

    for (BossChance bossChance : bossChances) {
      randomValue -= bossChance.getChance();
      if (randomValue <= 0) {
        return bossChance;
      }
    }

    return null;
  }

  public void giveRewards(String bossRarity, ServerPlayerEntity player) {
    BossChance bossChance = getBossChance(bossRarity);
    if (bossChance == null) {
      return;
    }

    if (bossChance.isAllrewards()) {
      ItemChance.getAllRewards(bossChance.getRewards(), player);
    } else {
      ItemChance.getRandomRewards(bossChance.getRewards(), player, bossChance.getAmountrewards());
    }
  }
}
