package com.kingpixel.cobbleutils.util.events;

import com.cobblemon.mod.common.pokemon.Species;

import java.util.List;

/**
 * @author Carlos Varas Alonso - 28/06/2024 8:45
 */
public interface ArraysPokemonListener {
  void finishGenerate(List<Species> species);
}
