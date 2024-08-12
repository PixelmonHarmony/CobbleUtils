package com.kingpixel.cobbleutils.features.breeding.models;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.ItemModel;
import com.kingpixel.cobbleutils.Model.PokemonData;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Carlos Varas Alonso - 12/08/2024 12:37
 */
@Getter
@Setter
@ToString
public class Incense extends ItemModel {
  private String name;
  private List<PokemonIncense> pokemonIncense;

  public Incense() {
    super("minecraft:emerald", "Full Incense", List.of(""), 1);
    name = "Full Incense";
    pokemonIncense = new ArrayList<>();
    pokemonIncense.add(new PokemonIncense());
  }

  public Incense(String name, String displayname, List<String> lore, int custommodeldata,
                 List<PokemonIncense> pokemonIncense) {
    super("minecraft:emerald", displayname, lore, custommodeldata);
    this.name = name;
    this.pokemonIncense = pokemonIncense;
  }

  public boolean isIncense(ItemStack itemStack) {
    if (itemStack == null) return false;
    if (itemStack.getItem() == Items.AIR) return false;
    if (itemStack.getNbt() == null || itemStack.getNbt().isEmpty()) return false;
    return itemStack.getItem().equals(this.getItemStack().getItem()) && itemStack.getNbt().getInt("CustomModelData") == getCustomModelData();
  }

  public String getChild(Pokemon pokemon) {
    String pokemonName = pokemon.getSpecies().showdownId();
    CobbleUtils.LOGGER.info("Looking for: " + pokemonName);

    if (isIncense(pokemon.heldItem())) {
      CobbleUtils.LOGGER.info("Pokemon holds incense");
      for (PokemonIncense pokemonIncense1 : pokemonIncense) {
        CobbleUtils.LOGGER.info("Checking Parent: " + pokemonIncense1.getParent().getPokename());
        if (pokemonIncense1.getParent().getPokename().equalsIgnoreCase(pokemonName)) {
          String childName = pokemonIncense1.getChild().getPokename();
          CobbleUtils.LOGGER.info("Match found! Child: " + childName);
          return childName;
        }
      }
    } else {
      CobbleUtils.LOGGER.info("Pokemon does not hold incense");
      for (PokemonIncense pokemonIncense1 : pokemonIncense) {
        CobbleUtils.LOGGER.info("Checking Parent: " + pokemonIncense1.getParent().getPokename());
        if (pokemonIncense1.getParent().getPokename().equalsIgnoreCase(pokemonName)) {
          CobbleUtils.LOGGER.info("Match found! Parent: " + pokemonIncense1.getParent().getPokename());
          return pokemonIncense1.getParent().getPokename();
        }
      }
    }

    CobbleUtils.LOGGER.info("No match found in Incense list.");
    return null;
  }


  public static List<Incense> defaultIncenses() {
    List<Incense> incenses = new ArrayList<>();
    incenses.add(new Incense("Full Incense", "Full Incense", List.of(""), 1, List.of(
      new PokemonIncense(
        new PokemonData("snorlax", "normal"),
        new PokemonData("munchlax", "normal")
      )
    )));
    incenses.add(new Incense("Lax Incense", "Lax Incense", List.of(""), 2, List.of(
      new PokemonIncense(
        new PokemonData("wobbuffet", "normal"),
        new PokemonData("wynaut", "normal")
      )
    )));
    incenses.add(new Incense("Sea Incense", "Sea Incense", List.of(""), 3, List.of(
      new PokemonIncense(
        new PokemonData("marill", "normal"),
        new PokemonData("azurill", "normal")
      )
    )));
    incenses.add(new Incense("Rose Incense", "Rose Incense", List.of(""), 4, List.of(
      new PokemonIncense(
        new PokemonData("roselia", "normal"),
        new PokemonData("budew", "normal")
      )
    )));
    incenses.add(new Incense("Pure Incese", "Pure Incese", List.of(""), 5, List.of(
      new PokemonIncense(
        new PokemonData("chimecho", "normal"),
        new PokemonData("chingling", "normal")
      )
    )));
    incenses.add(new Incense("Rock Incense", "Rock Incense", List.of(""), 6, List.of(
      new PokemonIncense(
        new PokemonData("sudowoodo", "normal"),
        new PokemonData("bonsly", "normal")
      )
    )));
    incenses.add(new Incense("Odd Incense", "Odd Incense", List.of(""), 7, List.of(
      new PokemonIncense(
        new PokemonData("mrmime", "normal"),
        new PokemonData("mimejr", "normal")
      )
    )));
    incenses.add(new Incense("Luck Incense", "Luck Incense", List.of(""), 8, List.of(
      new PokemonIncense(
        new PokemonData("chansey", "normal"),
        new PokemonData("happiny", "normal")
      )
    )));
    incenses.add(new Incense("Wave Incense", "Wave Incense", List.of(""), 9, List.of(
      new PokemonIncense(
        new PokemonData("mantyke", "normal"),
        new PokemonData("mantyke", "normal")
      )
    )));

    return incenses;
  }

}
