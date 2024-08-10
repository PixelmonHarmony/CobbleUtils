package com.kingpixel.cobbleutils.features.breeding.ui;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.button.PlaceholderButton;
import ca.landonjw.gooeylibs2.api.button.linked.LinkedPageButton;
import ca.landonjw.gooeylibs2.api.helpers.PaginationHelper;
import ca.landonjw.gooeylibs2.api.page.LinkedPage;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.pokemon.egg.EggGroup;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.item.PokemonItem;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.features.breeding.Breeding;
import com.kingpixel.cobbleutils.features.breeding.models.EggData;
import com.kingpixel.cobbleutils.features.breeding.models.PlotBreeding;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.kingpixel.cobbleutils.util.PokemonUtils;
import com.kingpixel.cobbleutils.util.UIUtils;
import com.kingpixel.cobbleutils.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Carlos Varas Alonso - 02/08/2024 18:54
 */
public class PlotSelectPokemonUI {

  public static void selectPokemon(ServerPlayer player, PlotBreeding plotBreeding, Gender gender) {
    int row = CobbleUtils.breedconfig.getRowmenuselectpokemon();
    ChestTemplate template = ChestTemplate.builder(row).build();

    List<Pokemon> pokemons = new ArrayList<>();
    try {
      Cobblemon.INSTANCE.getStorage().getParty(player).forEach(pokemon -> processPokemon(pokemons, pokemon, player, gender, plotBreeding));
      Cobblemon.INSTANCE.getStorage().getPC(player.getUUID()).forEach(pokemon -> processPokemon(pokemons, pokemon, player, gender, plotBreeding));
    } catch (NoPokemonStoreException e) {
      throw new RuntimeException(e);
    }

    List<Button> buttons = new ArrayList<>();
    int size = pokemons.size();

    for (int i = 0; i < size; i++) {
      Pokemon pokemon = pokemons.get(i);
      GooeyButton button = GooeyButton.builder()
        .display(PokemonItem.from(pokemon))
        .title(AdventureTranslator.toNative(PokemonUtils.replace(pokemon)))
        .lore(Component.class, AdventureTranslator.toNativeL(PokemonUtils.replaceLore(pokemon)))
        .onClick(action -> {
          try {
            Cobblemon.INSTANCE.getStorage().getPC(player.getUUID()).remove(pokemon);
            Cobblemon.INSTANCE.getStorage().getParty(player).remove(pokemon);
          } catch (NoPokemonStoreException e) {
            throw new RuntimeException(e);
          }
          plotBreeding.add(pokemon, gender);
          Breeding.managerPlotEggs.writeInfo(player);
          PlotBreedingManagerUI.open(player, plotBreeding);
        })
        .build();
      buttons.add(button);
    }
    LinkedPageButton back = UIUtils.getPreviousButton(action -> {

    });

    LinkedPageButton next = UIUtils.getNextButton(action -> {

    });

    GooeyButton close = UIUtils.getCloseButton(action -> {
      PlotBreedingManagerUI.open(player, plotBreeding);
    });

    template.set(row - 1, 0, back);
    template.set(row - 1, 4, close);
    template.set(row - 1, 8, next);
    template.fill(GooeyButton.builder()
      .display(Utils.parseItemId(CobbleUtils.config.getFill()))
      .title("")
      .build());
    template.rectangle(0, 0, row - 1, 9, new PlaceholderButton());
    template.fillFromList(buttons);

    LinkedPage.Builder linkedPageBuilder = LinkedPage.builder().title(AdventureTranslator.toNative(CobbleUtils.breedconfig.getTitleselectpokemon()));
    LinkedPage page = PaginationHelper.createPagesFromPlaceholders(template, buttons, linkedPageBuilder);
    UIManager.openUIForcefully(player, page);
  }

  private static void processPokemon(Collection<Pokemon> pokemons, Pokemon pokemon, ServerPlayer player, Gender gender,
                                     PlotBreeding plotBreeding) {
    // Verifica si el Pokémon pertenece al grupo de huevo "UNDISCOVERED" o si es un huevo, y retorna si es así
    if (pokemon.getSpecies().getEggGroups().contains(EggGroup.UNDISCOVERED) ||
      pokemon.getSpecies().showdownId().equalsIgnoreCase("egg")) {
      return;
    }

    // Verifica si se permite la cría con Ditto y retorna si no está permitido
    if (pokemon.getSpecies().showdownId().equalsIgnoreCase("ditto") && !CobbleUtils.breedconfig.isDitto()) {
      return;
    }

    Pokemon otherGender = plotBreeding.obtainOtherGender(gender);

    // Verifica si el Pokémon no es breedable o si está en la lista negra
    if (!PokemonUtils.isBreedable(pokemon) ||
      CobbleUtils.breedconfig.getBlacklist().contains(pokemon.getSpecies().showdownId())) {
      return;
    }

    // Verifica si el Pokémon está en la whitelist y si es legendario o una Ultra Bestia
    boolean isInWhitelist = CobbleUtils.breedconfig.getWhitelist().contains(pokemon.getSpecies().showdownId());
    boolean isLegendaryOrUltraBeast = pokemon.isLegendary() || pokemon.isUltraBeast();

    // Si el Pokémon es legendario o una Ultra Bestia y no está en la whitelist, retorna
    if (isLegendaryOrUltraBeast && !isInWhitelist) {
      return;
    }

    // Si no hay otro género disponible
    if (otherGender == null) {
      if (isInWhitelist || pokemon.getGender() == gender || pokemon.getGender() == Gender.GENDERLESS) {
        pokemons.add(pokemon);
      } else if (CobbleUtils.breedconfig.isDitto() && pokemon.getSpecies().showdownId().equalsIgnoreCase("ditto")) {
        pokemons.add(pokemon);
      }
      return;
    }

    // Verifica si el otro Pokémon es Ditto
    boolean isOtherDitto = otherGender.getSpecies().showdownId().equalsIgnoreCase("ditto");
    boolean isGenderless = pokemon.getGender() == Gender.GENDERLESS;
    boolean areCompatible = EggData.isCompatible(otherGender, pokemon);

    // Maneja la cría de Ditto
    if (pokemon.getSpecies().showdownId().equalsIgnoreCase("ditto")) {
      // Si se permite la cría de Ditto x Ditto
      if (isOtherDitto) {
        if (CobbleUtils.breedconfig.isDoubleditto()) {
          pokemons.add(pokemon);
        }
      } else {
        // Si no es Ditto x Ditto, pero es compatible o Ditto está permitido
        if (areCompatible) {
          pokemons.add(pokemon);
        } else if (pokemon.getSpecies().showdownId().equalsIgnoreCase("ditto") && CobbleUtils.breedconfig.isDitto()) {
          pokemons.add(pokemon);
        }
      }
      return;
    }

    // Si el otro Pokémon es Ditto y no es Ditto x Ditto, pero son compatibles
    if (isOtherDitto) {
      if (areCompatible) {
        pokemons.add(pokemon);
      } else if (isInWhitelist) {
        pokemons.add(pokemon);
      } else if (pokemon.getSpecies().showdownId().equalsIgnoreCase("ditto") && CobbleUtils.breedconfig.isDitto()) {
        pokemons.add(pokemon);
      } else if (!pokemon.getSpecies().showdownId().equalsIgnoreCase("ditto")) {
        pokemons.add(pokemon);
      }
    } else {
      // Caso general: agregar el Pokémon si es compatible o cumple otras condiciones
      if (areCompatible && (pokemon.getGender() == gender || isGenderless)) {
        pokemons.add(pokemon);
      } else if (isInWhitelist && (pokemon.getGender() == gender || isGenderless)) {
        pokemons.add(pokemon);
      }
    }
  }


}
