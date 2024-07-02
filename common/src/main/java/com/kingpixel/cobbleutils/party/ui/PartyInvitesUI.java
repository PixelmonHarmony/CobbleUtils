package com.kingpixel.cobbleutils.party.ui;

import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.button.PlaceholderButton;
import ca.landonjw.gooeylibs2.api.button.linked.LinkType;
import ca.landonjw.gooeylibs2.api.button.linked.LinkedPageButton;
import ca.landonjw.gooeylibs2.api.helpers.PaginationHelper;
import ca.landonjw.gooeylibs2.api.page.LinkedPage;
import ca.landonjw.gooeylibs2.api.page.Page;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.PlayerInfo;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.kingpixel.cobbleutils.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Carlos Varas Alonso - 28/06/2024 6:11
 */
public class PartyInvitesUI {
  public static Page getPartyInvites(Player player) {
    ChestTemplate template = ChestTemplate.builder(2).build();
    List<Button> buttons = new ArrayList<>();

    CobbleUtils.partyManager.getParties().forEach((uuid, party) -> {
      if (party.getInvites().contains(player.getUUID())) {
        GooeyButton invite = GooeyButton.builder()
          .display(Utils.parseItemId("minecraft:emerald"))
          .title(party.getName())
          .onClick(action -> {
            CobbleUtils.partyManager.joinParty(party.getName(), PlayerInfo.fromPlayer(player));
          })
          .build();
        buttons.add(invite);
      }
    });

    buttons.removeIf(Objects::isNull);

    LinkedPageButton previus = LinkedPageButton.builder()
      .display(Utils.parseItemId("minecraft:arrow"))
      .title(AdventureTranslator.toNative("Previous Page"))
      .linkType(LinkType.Previous)
      .build();

    LinkedPageButton next = LinkedPageButton.builder()
      .display(Utils.parseItemId("minecraft:arrow"))
      .title(AdventureTranslator.toNative("Next Page"))
      .linkType(LinkType.Next)
      .build();

    GooeyButton close = GooeyButton.builder()
      .display(Items.RED_STAINED_GLASS_PANE.getDefaultInstance().setHoverName(Component.literal("Close")))
      .onClick(action -> {
        action.getPlayer().closeContainer();
      })
      .build();


    PlaceholderButton placeholder = new PlaceholderButton();

    GooeyButton fill = GooeyButton.builder().display(Items.GRAY_STAINED_GLASS_PANE.getDefaultInstance().setHoverName(Component.literal(""))).build();
    template.fill(fill)
      .rectangle(0, 0, 2, 9, placeholder)
      .fillFromList(buttons)
      .set(1, 4, close)
      .set(1, 0, previus)
      .set(1, 8, next);

    LinkedPage.Builder linkedPageBuilder = LinkedPage.builder()
      .title(CobbleUtils.partyLang.getTitleinvites());

    LinkedPage firstPage = PaginationHelper.createPagesFromPlaceholders(template, buttons, linkedPageBuilder);
    return firstPage;
  }
}
