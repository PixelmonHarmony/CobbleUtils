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
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Carlos Varas Alonso - 02/08/2024 18:54
 */
public class PlotSelectPokemonUI {

  public static void selectPokemon(ServerPlayerEntity player, PlotBreeding plotBreeding, Gender gender) {
    int row = CobbleUtils.breedconfig.getRowmenuselectpokemon();
    ChestTemplate template = ChestTemplate.builder(row).build();

    List<Pokemon> pokemons = new ArrayList<>();
    try {
      Cobblemon.INSTANCE.getStorage().getParty(player)
        .forEach(pokemon -> processPokemon(pokemons, pokemon, player, gender, plotBreeding));
      Cobblemon.INSTANCE.getStorage().getPC(player.getUuid())
        .forEach(pokemon -> processPokemon(pokemons, pokemon, player, gender, plotBreeding));
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
        .lore(Text.class, AdventureTranslator.toNativeL(PokemonUtils.replaceLore(pokemon)))
        .onClick(action -> {
          try {
            Cobblemon.INSTANCE.getStorage().getPC(player.getUuid()).remove(pokemon);
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

    LinkedPage.Builder linkedPageBuilder = LinkedPage.builder()
      .title(AdventureTranslator.toNative(CobbleUtils.breedconfig.getTitleselectpokemon()));
    LinkedPage page = PaginationHelper.createPagesFromPlaceholders(template, buttons, linkedPageBuilder);
    UIManager.openUIForcefully(player, page);
  }

  public static boolean isAcceptablePokemon(Pokemon pokemon, Gender gender, PlotBreeding plotBreeding,
                                            ServerPlayerEntity player,
                                            boolean notify) {
    if (pokemon.isUncatchable()) {
      CobbleUtils.LOGGER.info("Pokemon is uncatchable");
      return false;
    }

    if (pokemon.getSpecies().getEggGroups().contains(EggGroup.UNDISCOVERED) ||
      pokemon.getSpecies().showdownId().equalsIgnoreCase("egg")) {
      if (notify) {
        player.sendMessage(AdventureTranslator.toNative(CobbleUtils.breedconfig.getNotCompatible()));
      }
      return false;
    }

    if (pokemon.getSpecies().showdownId().equalsIgnoreCase("ditto") && !CobbleUtils.breedconfig.isDitto()) {
      if (notify) {
        player.sendMessage(AdventureTranslator.toNative(CobbleUtils.breedconfig.getNotCompatible()));
      }
      return false;
    }

    if (!PokemonUtils.isBreedable(pokemon) ||
      CobbleUtils.breedconfig.getBlacklist().contains(pokemon.getSpecies().showdownId())) {
      if (notify) {
        player.sendMessage(
          AdventureTranslator.toNative(
            PokemonUtils.replace(CobbleUtils.breedconfig.getNotbreedable(), pokemon)
          )
        );
      }
      return false;
    }

    boolean isInWhitelist = CobbleUtils.breedconfig.getWhitelist().contains(pokemon.getSpecies().showdownId());
    boolean isLegendaryOrUltraBeast = pokemon.isLegendary() || pokemon.isUltraBeast();

    if (isLegendaryOrUltraBeast && !isInWhitelist) {
      if (notify) {
        player.sendMessage(
          AdventureTranslator.toNative(
            PokemonUtils.replace(CobbleUtils.breedconfig.getBlacklisted(), pokemon)
          )
        );
      }
      return false;
    }

    Pokemon otherGender = plotBreeding.obtainOtherGender(gender);

    if (otherGender == null) {
      boolean result =
        isInWhitelist || pokemon.getGender() == gender || pokemon.getGender() == Gender.GENDERLESS || (CobbleUtils.breedconfig.isDitto() && pokemon.getSpecies().showdownId().equalsIgnoreCase("ditto"));
      if (!result && notify) {
        player.sendMessage(
          AdventureTranslator.toNative(
            PokemonUtils.replace(CobbleUtils.breedconfig.getNotCompatible(), pokemon)
          )
        );
      }
      return result;
    }

    boolean isOtherDitto = otherGender.getSpecies().showdownId().equalsIgnoreCase("ditto");
    boolean isGenderless = pokemon.getGender() == Gender.GENDERLESS;
    boolean areCompatible = EggData.isCompatible(otherGender, pokemon);

    if (pokemon.getSpecies().showdownId().equalsIgnoreCase("ditto")) {
      if (isOtherDitto) {
        if (notify) {
          player.sendMessage(
            AdventureTranslator.toNative(
              PokemonUtils.replace(CobbleUtils.breedconfig.getNotdoubleditto(), pokemon)
            )
          );
        }
        return CobbleUtils.breedconfig.isDoubleditto();
      } else {
        boolean result = areCompatible || (pokemon.getSpecies().showdownId().equalsIgnoreCase("ditto") && CobbleUtils.breedconfig.isDitto());
        if (!result && notify) {
          player.sendMessage(
            AdventureTranslator.toNative(
              PokemonUtils.replace(CobbleUtils.breedconfig.getNotCompatible(), pokemon)
            )
          );
        }
        return result;
      }
    }

    if (isOtherDitto) {
      boolean result = areCompatible || isInWhitelist || !pokemon.getSpecies().showdownId().equalsIgnoreCase("ditto");
      if (!result && notify) {
        player.sendMessage(
          AdventureTranslator.toNative(
            PokemonUtils.replace(CobbleUtils.breedconfig.getNotCompatible(), pokemon)
          )
        );
      }
      return result;
    } else {
      boolean result = areCompatible && (pokemon.getGender() == gender || isGenderless) ||
        (isInWhitelist && (pokemon.getGender() == gender || isGenderless));
      if (!result && notify) {
        player.sendMessage(
          AdventureTranslator.toNative(
            PokemonUtils.replace(CobbleUtils.breedconfig.getNotCompatible(), pokemon)
          )
        );
      }
      return result;
    }
  }


  public static boolean arePokemonsCompatible(Pokemon malePokemon, Pokemon femalePokemon, ServerPlayerEntity player, boolean notify) {
    // Verifica si cualquiera de los Pokémon es inatrapable
    if (malePokemon.isUncatchable() || femalePokemon.isUncatchable()) {
      CobbleUtils.LOGGER.info("One or both Pokemons are uncatchable");
      return false;
    }

    // Verifica si alguno de los Pokémon pertenece al grupo de huevo "UNDISCOVERED" o es un huevo
    if (malePokemon.getSpecies().getEggGroups().contains(EggGroup.UNDISCOVERED) ||
      femalePokemon.getSpecies().getEggGroups().contains(EggGroup.UNDISCOVERED) ||
      malePokemon.getSpecies().showdownId().equalsIgnoreCase("egg") ||
      femalePokemon.getSpecies().showdownId().equalsIgnoreCase("egg")) {
      if (notify) {
        player.sendMessage(AdventureTranslator.toNative(CobbleUtils.breedconfig.getNotCompatible()));
      }
      return false;
    }

    // Verifica si la cría con Ditto está permitida
    if ((malePokemon.getSpecies().showdownId().equalsIgnoreCase("ditto") && !CobbleUtils.breedconfig.isDitto()) ||
      (femalePokemon.getSpecies().showdownId().equalsIgnoreCase("ditto") && !CobbleUtils.breedconfig.isDitto())) {
      if (notify) {
        player.sendMessage(AdventureTranslator.toNative(CobbleUtils.breedconfig.getNotCompatible()));
      }
      return false;
    }

    // Verifica si alguno de los Pokémon no es breedable o está en la lista negra
    if (!PokemonUtils.isBreedable(malePokemon) || !PokemonUtils.isBreedable(femalePokemon) ||
      CobbleUtils.breedconfig.getBlacklist().contains(malePokemon.getSpecies().showdownId()) ||
      CobbleUtils.breedconfig.getBlacklist().contains(femalePokemon.getSpecies().showdownId())) {
      if (notify) {
        player.sendMessage(
          AdventureTranslator.toNative(
            PokemonUtils.replace(CobbleUtils.breedconfig.getNotbreedable(), malePokemon)
          )
        );
      }
      return false;
    }

    boolean isMaleInWhitelist = CobbleUtils.breedconfig.getWhitelist().contains(malePokemon.getSpecies().showdownId());
    boolean isFemaleInWhitelist = CobbleUtils.breedconfig.getWhitelist().contains(femalePokemon.getSpecies().showdownId());
    boolean isMaleLegendaryOrUltraBeast = malePokemon.isLegendary() || malePokemon.isUltraBeast();
    boolean isFemaleLegendaryOrUltraBeast = femalePokemon.isLegendary() || femalePokemon.isUltraBeast();

    // Si cualquiera de los Pokémon es legendario o una Ultra Bestia y no está en la whitelist, no son compatibles
    if ((isMaleLegendaryOrUltraBeast && !isMaleInWhitelist) || (isFemaleLegendaryOrUltraBeast && !isFemaleInWhitelist)) {
      if (notify) {
        player.sendMessage(
          AdventureTranslator.toNative(
            PokemonUtils.replace(CobbleUtils.breedconfig.getBlacklisted(), malePokemon)
          )
        );
      }
      return false;
    }

    boolean isMaleDitto = malePokemon.getSpecies().showdownId().equalsIgnoreCase("ditto");
    boolean isFemaleDitto = femalePokemon.getSpecies().showdownId().equalsIgnoreCase("ditto");
    boolean areCompatible = EggData.isCompatible(malePokemon, femalePokemon);

    // Maneja los casos de cría con Ditto
    if (isMaleDitto || isFemaleDitto) {
      if (isMaleDitto && isFemaleDitto) {
        boolean result = CobbleUtils.breedconfig.isDoubleditto();
        if (notify && !result) {
          player.sendMessage(
            AdventureTranslator.toNative(
              PokemonUtils.replace(CobbleUtils.breedconfig.getNotdoubleditto(), malePokemon)
            )
          );
        }
        return result;
      } else {
        boolean result = areCompatible || CobbleUtils.breedconfig.isDitto();
        if (!result && notify) {
          player.sendMessage(
            AdventureTranslator.toNative(
              PokemonUtils.replace(CobbleUtils.breedconfig.getNotditto(), malePokemon)
            )
          );
        }
        return result;
      }
    }

    // Caso general: Verificar si son compatibles según sus géneros y egg groups
    boolean result = areCompatible ||
      (isMaleInWhitelist && malePokemon.getGender() == Gender.MALE) ||
      (isFemaleInWhitelist && femalePokemon.getGender() == Gender.FEMALE);

    if (!result && notify) {
      player.sendMessage(
        AdventureTranslator.toNative(
          PokemonUtils.replace(CobbleUtils.breedconfig.getNotCompatible(), malePokemon)
        )
      );
    }

    return result;
  }


  private static void processPokemon(Collection<Pokemon> pokemons, Pokemon pokemon, ServerPlayerEntity player,
                                     Gender gender, PlotBreeding plotBreeding) {
    if (isAcceptablePokemon(pokemon, gender, plotBreeding, player, false)) {
      pokemons.add(pokemon);
    }
  }

}
