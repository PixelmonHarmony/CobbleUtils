package com.kingpixel.cobbleutils.util.events;

import com.cobblemon.mod.common.pokemon.Species;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Carlos Varas Alonso - 28/06/2024 8:43
 */
public class ArraysPokemonEvent {
  private List<ArraysPokemonListener> partyCreatedListeners = new ArrayList<>();

  public static final ArraysPokemonEvent FINISH_GENERATE_POKEMONS = new ArraysPokemonEvent();

  public void register(ArraysPokemonListener listener) {
    partyCreatedListeners.add(listener);
  }

  public void unregister(ArraysPokemonListener listener) {
    partyCreatedListeners.remove(listener);
  }

  public void clear() {
    partyCreatedListeners.clear();
  }

  public void emit(List<Species> species) {
    notifyPartyCreated(species);
  }

  private void notifyPartyCreated(List<Species> species) {
    for (ArraysPokemonListener listener : partyCreatedListeners) {
      listener.finishGenerate(species);
    }
  }
}
