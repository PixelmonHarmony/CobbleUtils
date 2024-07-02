package com.kingpixel.cobbleutils.party.ui;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.button.PlaceholderButton;
import ca.landonjw.gooeylibs2.api.button.linked.LinkType;
import ca.landonjw.gooeylibs2.api.button.linked.LinkedPageButton;
import ca.landonjw.gooeylibs2.api.helpers.PaginationHelper;
import ca.landonjw.gooeylibs2.api.page.LinkedPage;
import ca.landonjw.gooeylibs2.api.page.Page;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.RewardsData;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.kingpixel.cobbleutils.util.CobbleUtilities;
import com.kingpixel.cobbleutils.util.UIUtils;
import com.kingpixel.cobbleutils.util.Utils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Carlos Varas Alonso - 28/06/2024 6:11
 */
public class RewardsUI {
  public static Page getRewards(Player player) {
    ChestTemplate template = ChestTemplate.builder(6).build();
    List<Button> buttons = new ArrayList<>();

    RewardsData rewardsData = CobbleUtils.rewardsManager.getRewardsData().get(player.getUUID());

    rewardsData.getPokemons().forEach(pokemon -> buttons.add(UIUtils.createButtonPokemon(Pokemon.Companion.loadFromJSON(pokemon), action -> {
      try {
        if (Cobblemon.INSTANCE.getStorage().getParty(player.getUUID()).add(Pokemon.Companion.loadFromJSON(pokemon))) {
          rewardsData.getPokemons().remove(pokemon);
          rewardsData.writeInfo();
        }
        UIManager.openUIForcefully(action.getAction().getPlayer(), getRewards(player));
      } catch (NoPokemonStoreException e) {
        throw new RuntimeException(e);
      }
    })));

    rewardsData.getItems().forEach(item -> {
      ItemStack itemStack = CobbleUtilities.getItem(item.getItem());
      buttons.add(UIUtils.createButtonItem(itemStack, action -> {
        if (player.getInventory().getFreeSlot() == -1) return;
        if (action.getPlayer().getInventory().add(itemStack)) {
          rewardsData.getItems().remove(item);
          rewardsData.writeInfo();
          UIManager.openUIForcefully(action.getPlayer(), getRewards(player));
        }
      }));
    });

    rewardsData.getCommands().forEach(command -> {
      buttons.add(UIUtils.createButtonCommand(command, action -> {
        if (player.getInventory().getFreeSlot() == -1) return;
        CommandDispatcher<CommandSourceStack> disparador = CobbleUtils.server.getCommands().getDispatcher();
        try {
          CommandSourceStack serverSource = CobbleUtils.server.createCommandSourceStack();
          ParseResults<CommandSourceStack> parse = disparador.parse(command, serverSource);
          disparador.execute(parse);
        } catch (CommandSyntaxException e) {
          System.err.println("Error al ejecutar el comando: " + command);
          e.printStackTrace();
        }
        rewardsData.getCommands().remove(command);
        rewardsData.writeInfo();
        UIManager.openUIForcefully(action.getPlayer(), getRewards(player));

      }));
    });

    buttons.removeIf(Objects::isNull);

    LinkedPageButton previus = LinkedPageButton.builder()
      .display(Utils.parseItemId("minecraft:arrow"))
      .title(AdventureTranslator.toNative(CobbleUtils.language.getPrevious()))
      .linkType(LinkType.Previous)
      .build();

    LinkedPageButton next = LinkedPageButton.builder()
      .display(Utils.parseItemId("minecraft:arrow"))
      .title(AdventureTranslator.toNative(CobbleUtils.language.getNext()))
      .linkType(LinkType.Next)
      .build();

    GooeyButton close = GooeyButton.builder()
      .display(Items.RED_STAINED_GLASS_PANE.getDefaultInstance())
      .title(AdventureTranslator.toNative(CobbleUtils.language.getClose()))
      .onClick(action -> {
        action.getPlayer().closeContainer();
      })
      .build();


    PlaceholderButton placeholder = new PlaceholderButton();

    GooeyButton fill = GooeyButton.builder().display(Items.GRAY_STAINED_GLASS_PANE.getDefaultInstance().setHoverName(Component.literal(""))).build();
    template.fill(fill)
      .rectangle(0, 0, 5, 9, placeholder)
      .fillFromList(buttons)
      .set(5, 4, close)
      .set(5, 0, previus)
      .set(5, 8, next);

    LinkedPage.Builder linkedPageBuilder = LinkedPage.builder()
      .title(AdventureTranslator.toNative(CobbleUtils.language.getTitlemenurewards()));

    LinkedPage firstPage = PaginationHelper.createPagesFromPlaceholders(template, buttons, linkedPageBuilder);
    return firstPage;
  }


}
