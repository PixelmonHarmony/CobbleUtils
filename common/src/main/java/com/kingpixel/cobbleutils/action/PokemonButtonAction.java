package com.kingpixel.cobbleutils.action;

import ca.landonjw.gooeylibs2.api.button.ButtonAction;
import com.cobblemon.mod.common.pokemon.Pokemon;
import lombok.Getter;

/**
 * @author Carlos Varas Alonso - 29/06/2024 20:35
 */
@Getter
public class PokemonButtonAction {

  private final ButtonAction action;
  private final Pokemon pokemon;

  public PokemonButtonAction(ButtonAction action, Pokemon pokemon) {
    this.action = action;
    this.pokemon = pokemon;
  }
}
