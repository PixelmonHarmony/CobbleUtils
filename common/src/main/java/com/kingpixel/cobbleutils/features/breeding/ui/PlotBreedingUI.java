package com.kingpixel.cobbleutils.features.breeding.ui;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.page.GooeyPage;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.ItemModel;
import com.kingpixel.cobbleutils.database.DatabaseClientFactory;
import com.kingpixel.cobbleutils.features.breeding.config.BreedConfig;
import com.kingpixel.cobbleutils.features.breeding.models.PlotBreeding;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.kingpixel.cobbleutils.util.LuckPermsUtil;
import com.kingpixel.cobbleutils.util.PlayerUtils;
import com.kingpixel.cobbleutils.util.PokemonUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Carlos Varas Alonso - 02/08/2024 14:29
 */
public class PlotBreedingUI {
  public static void open(ServerPlayerEntity player) {
    DatabaseClientFactory.CheckDaycarePlots(player);
    ChestTemplate template = ChestTemplate.builder(CobbleUtils.breedconfig.getRowmenuselectplot()).build();

    int size = CobbleUtils.breedconfig.getMaxplots();
    int max = 1;

    for (int i = 0; i < size; i++) {
      if (LuckPermsUtil.checkPermission(player, CobbleUtils.breedconfig.getPermissionplot(i + 1))) max = i;
    }

    if (max > size) max = size;
    if (max < CobbleUtils.breedconfig.getDefaultNumberPlots()) max = CobbleUtils.breedconfig.getDefaultNumberPlots();

    ItemModel info = CobbleUtils.breedconfig.getInfoItem();

    BreedConfig.SuccessItems successItems = CobbleUtils.breedconfig.getSuccessItems();

    List<String> infoLore = new ArrayList<>(info.getLore());
    infoLore.replaceAll(s ->
      s.replace("%ah%", String.format("%.2f%%", successItems.getPercentageTransmitAH()))
        .replace("%destinyknot%", String.format("%.2f%%", successItems.getPercentageDestinyKnot()))
        .replace("%everstone%", String.format("%.2f%%", successItems.getPercentageEverStone()))
        .replace("%poweritem%", String.format("%.2f%%", successItems.getPercentagePowerItem()))
        .replace("%masuda%", CobbleUtils.breedconfig.isMethodmasuda() ? CobbleUtils.language.getYes() : CobbleUtils.language.getNo())
        .replace("%multipliermasuda%", String.valueOf(CobbleUtils.breedconfig.getMultipliermasuda()))
        .replace("%maxivs%", String.valueOf(CobbleUtils.breedconfig.getMaxIvsRandom()))
    );

    if (!CobbleUtils.breedconfig.isHaveMaxNumberIvsForRandom()) {
      infoLore.removeIf(s -> s.contains("%maxivs%"));
    }

    if (info.getSlot() > 0) {
      GooeyButton button = GooeyButton.builder()
        .display(info.getItemStack())
        .title(AdventureTranslator.toNative(info.getDisplayname()))
        .lore(Text.class, AdventureTranslator.toNativeL(infoLore))
        .build();
      template.set(info.getSlot(), button);
    }

    List<PlotBreeding> plots = DatabaseClientFactory.databaseClient.getPlots(player);
    for (int i = 0; i < max; i++) {
      PlotBreeding plotBreeding = plots.get(i);
      List<String> lore = new ArrayList<>(CobbleUtils.breedconfig.getPlotItem().getLore());
      int amount = plotBreeding.getEggs().size();
      List<Pokemon> pokemons = new ArrayList<>();
      pokemons.add(plotBreeding.getMale() != null ? Pokemon.Companion.loadFromJSON(plotBreeding.getMale()) : null);
      pokemons.add(plotBreeding.getFemale() != null ? Pokemon.Companion.loadFromJSON(plotBreeding.getFemale()) : null);
      lore.replaceAll(s -> PokemonUtils.replace(s, pokemons)
        .replace("%cooldown%", PlayerUtils.getCooldown(new Date(plotBreeding.getCooldown())))
        .replace("%eggs%", String.valueOf(amount)));
      ItemStack itemStack;
      if (plotBreeding.getEggs().isEmpty()) {
        itemStack = CobbleUtils.breedconfig.getPlotItem().getItemStack();
      } else {
        itemStack = CobbleUtils.breedconfig.getPlotThereAreEggs().getItemStack(amount);
      }

      int finalI = i;
      GooeyButton button = GooeyButton.builder()
        .display(itemStack)
        .title(AdventureTranslator.toNative(CobbleUtils.breedconfig.getPlotItem().getDisplayname()))
        .lore(Text.class, AdventureTranslator.toNativeL(lore))
        .onClick(action -> {
          plotBreeding.checking(player);
          PlotBreedingManagerUI.open(player, plotBreeding, finalI);
        })
        .build();
      template.set(CobbleUtils.breedconfig.getPlotSlots().get(i), button);
    }

    GooeyPage page = GooeyPage.builder()
      .template(template)
      .title(AdventureTranslator.toNative(CobbleUtils.breedconfig.getTitleselectplot()))
      .build();

    UIManager.openUIForcefully(player, page);
  }
}
