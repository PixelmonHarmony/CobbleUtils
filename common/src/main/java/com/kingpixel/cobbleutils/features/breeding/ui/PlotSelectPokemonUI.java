package com.kingpixel.cobbleutils.features.breeding.ui;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.button.PlaceholderButton;
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

    List<Pokemon> pokemons = getPlayerPokemons(player, gender, plotBreeding);
    List<Button> buttons = createPokemonButtons(pokemons, player, plotBreeding, gender);

    configureTemplate(template, buttons, row, player, plotBreeding);

    LinkedPage.Builder linkedPageBuilder = LinkedPage.builder()
      .title(AdventureTranslator.toNative(CobbleUtils.breedconfig.getTitleselectpokemon()));
    LinkedPage page = PaginationHelper.createPagesFromPlaceholders(template, buttons, linkedPageBuilder);
    UIManager.openUIForcefully(player, page);
  }

  private static List<Pokemon> getPlayerPokemons(ServerPlayerEntity player, Gender gender, PlotBreeding plotBreeding) {
    List<Pokemon> pokemons = new ArrayList<>();
    try {
      Cobblemon.INSTANCE.getStorage().getParty(player)
        .forEach(pokemon -> processPokemon(pokemons, pokemon, player, gender, plotBreeding));
      Cobblemon.INSTANCE.getStorage().getPC(player.getUuid())
        .forEach(pokemon -> processPokemon(pokemons, pokemon, player, gender, plotBreeding));
    } catch (NoPokemonStoreException e) {
      throw new RuntimeException(e);
    }
    return pokemons;
  }

  private static List<Button> createPokemonButtons(List<Pokemon> pokemons, ServerPlayerEntity player, PlotBreeding plotBreeding, Gender gender) {
    List<Button> buttons = new ArrayList<>();
    for (Pokemon pokemon : pokemons) {
      GooeyButton button = GooeyButton.builder()
        .display(PokemonItem.from(pokemon))
        .title(AdventureTranslator.toNative(PokemonUtils.replace(pokemon)))
        .lore(Text.class, AdventureTranslator.toNativeL(PokemonUtils.replaceLore(pokemon)))
        .onClick(action -> handlePokemonSelection(pokemon, player, plotBreeding, gender))
        .build();
      buttons.add(button);
    }
    return buttons;
  }

  private static void handlePokemonSelection(Pokemon pokemon, ServerPlayerEntity player, PlotBreeding plotBreeding, Gender gender) {
    try {
      Cobblemon.INSTANCE.getStorage().getPC(player.getUuid()).remove(pokemon);
      Cobblemon.INSTANCE.getStorage().getParty(player).remove(pokemon);
    } catch (NoPokemonStoreException e) {
      throw new RuntimeException(e);
    }
    plotBreeding.add(pokemon, gender);
    Breeding.managerPlotEggs.writeInfo(player);
    PlotBreedingManagerUI.open(player, plotBreeding);
  }

  private static void configureTemplate(ChestTemplate template, List<Button> buttons, int row, ServerPlayerEntity player, PlotBreeding plotBreeding) {
    template.set(row - 1, 0, UIUtils.getPreviousButton(action -> {
    }));
    template.set(row - 1, 4, UIUtils.getCloseButton(action -> PlotBreedingManagerUI.open(player, plotBreeding)));
    template.set(row - 1, 8, UIUtils.getNextButton(action -> {
    }));
    template.fill(GooeyButton.builder().display(Utils.parseItemId(CobbleUtils.config.getFill())).title("").build());
    template.rectangle(0, 0, row - 1, 9, new PlaceholderButton());
    template.fillFromList(buttons);
  }

  public static boolean isAcceptablePokemon(Pokemon pokemon, Gender gender, PlotBreeding plotBreeding, ServerPlayerEntity player, boolean notify) {
    if (!isPokemonBreedable(pokemon, player, notify)) return false;
    return checkCompatibility(pokemon, gender, plotBreeding, player, notify);
  }

  private static boolean isPokemonBreedable(Pokemon pokemon, ServerPlayerEntity player, boolean notify) {
    if (pokemon.getSpecies().getEggGroups().contains(EggGroup.UNDISCOVERED) ||
      pokemon.getSpecies().showdownId().equalsIgnoreCase("egg") ||
      (pokemon.getSpecies().showdownId().equalsIgnoreCase("ditto") && !CobbleUtils.breedconfig.isDitto()) ||
      !PokemonUtils.isBreedable(pokemon) ||
      CobbleUtils.breedconfig.getBlacklist().contains(pokemon.getSpecies().showdownId())) {
      if (notify && player != null) {
        player.sendMessage(AdventureTranslator.toNative(PokemonUtils.replace(CobbleUtils.breedconfig.getNotbreedable(), pokemon)));
      }
      return false;
    }
    return true;
  }

  private static boolean checkCompatibility(Pokemon pokemon, Gender gender, PlotBreeding plotBreeding, ServerPlayerEntity player, boolean notify) {
    Pokemon otherGender = plotBreeding.obtainOtherGender(gender);
    boolean isInWhitelist = CobbleUtils.breedconfig.getWhitelist().contains(pokemon.getSpecies().showdownId());
    boolean isLegendaryOrUltraBeast = pokemon.isLegendary() || pokemon.isUltraBeast();

    if (isLegendaryOrUltraBeast && !isInWhitelist) {
      sendMessageIfNeeded(player, notify, CobbleUtils.breedconfig.getBlacklisted(), pokemon);
      return false;
    }

    if (otherGender == null) {
      return isInWhitelist || pokemon.getGender() == gender || pokemon.getGender() == Gender.GENDERLESS || isDittoBreedingAllowed(pokemon);
    }

    boolean isOtherDitto = otherGender.getSpecies().showdownId().equalsIgnoreCase("ditto");
    boolean isGenderless = pokemon.getGender() == Gender.GENDERLESS;
    boolean areCompatible = EggData.isCompatible(otherGender, pokemon);

    return isDittoCompatibility(pokemon, isOtherDitto, areCompatible, notify, player) ||
      areCompatible && (pokemon.getGender() == gender || isGenderless) ||
      isInWhitelist && (pokemon.getGender() == gender || isGenderless);
  }

  private static boolean isDittoBreedingAllowed(Pokemon pokemon) {
    return CobbleUtils.breedconfig.isDitto() && pokemon.getSpecies().showdownId().equalsIgnoreCase("ditto");
  }

  private static boolean isDittoCompatibility(Pokemon pokemon, boolean isOtherDitto, boolean areCompatible, boolean notify, ServerPlayerEntity player) {
    if (pokemon.getSpecies().showdownId().equalsIgnoreCase("ditto")) {
      if (isOtherDitto) {
        return CobbleUtils.breedconfig.isDoubleditto();
      } else {
        return areCompatible || CobbleUtils.breedconfig.isDitto();
      }
    }
    if (isOtherDitto) {
      return areCompatible || CobbleUtils.breedconfig.getWhitelist().contains(pokemon.getSpecies().showdownId());
    }
    return false;
  }

  public static boolean arePokemonsCompatible(Pokemon malePokemon, Pokemon femalePokemon, ServerPlayerEntity player, boolean notify) {
    if (!isPokemonBreedable(malePokemon, player, notify) || !isPokemonBreedable(femalePokemon, player, notify)) {
      return false;
    }
    boolean areCompatible = EggData.isCompatible(malePokemon, femalePokemon);
    return checkDittoBreedingCompatibility(malePokemon, femalePokemon, areCompatible, notify, player);
  }

  private static boolean checkDittoBreedingCompatibility(Pokemon malePokemon, Pokemon femalePokemon, boolean areCompatible, boolean notify, ServerPlayerEntity player) {
    boolean isMaleDitto = malePokemon.getSpecies().showdownId().equalsIgnoreCase("ditto");
    boolean isFemaleDitto = femalePokemon.getSpecies().showdownId().equalsIgnoreCase("ditto");
    boolean isDoubledittoEnabled = CobbleUtils.breedconfig.isDoubleditto();

    if (isMaleDitto && isFemaleDitto) {
      return isDoubledittoEnabled;
    } else if (isMaleDitto || isFemaleDitto) {
      return areCompatible || CobbleUtils.breedconfig.isDitto();
    } else {
      return areCompatible;
    }
  }

  private static void processPokemon(Collection<Pokemon> pokemons, Pokemon pokemon, ServerPlayerEntity player, Gender gender, PlotBreeding plotBreeding) {
    if (isAcceptablePokemon(pokemon, gender, plotBreeding, player, true)) {
      pokemons.add(pokemon);
    }
  }

  private static void sendMessageIfNeeded(ServerPlayerEntity player, boolean notify, String message, Pokemon pokemon) {
    if (notify && player != null) {
      player.sendMessage(AdventureTranslator.toNative(PokemonUtils.replace(message, pokemon)));
    }
  }
}
