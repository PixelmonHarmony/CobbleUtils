package com.kingpixel.cobbleutils.action;

import ca.landonjw.gooeylibs2.api.button.ButtonAction;
import com.cobblemon.mod.common.pokemon.Pokemon;

/**
 * @author Carlos Varas Alonso - 29/06/2024 20:35
 */
public class PokemonButtonAction {

  private final ButtonAction action;
  private final Pokemon pokemon;

  public PokemonButtonAction(ButtonAction action, Pokemon pokemon) {
    this.action = action;
    this.pokemon = pokemon;
  }

  public ButtonAction getAction() {
    return action;
  }

  public Pokemon getPokemon() {
    return pokemon;
  }
}
