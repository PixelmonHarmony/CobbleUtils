package com.kingpixel.cobbleutils.party.ui;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.page.GooeyPage;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.party.models.UserParty;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.kingpixel.cobbleutils.util.Utils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * @author Carlos Varas Alonso - 28/06/2024 2:16
 */
public class PartyUI {
  public static GooeyPage getPartyMenu() {
    ChestTemplate template = ChestTemplate.builder(3).build();

    GooeyButton invites = GooeyButton.builder()
      .display(Utils.parseItemId("minecraft:emerald"))
      .title("Invites")
      .onClick(action -> {
        ServerPlayer player = action.getPlayer();
        UserParty userParty = CobbleUtils.partyManager.getUserParty().get(player.getUUID());
        if (!userParty.isHasParty()) {
          UIManager.openUIForcefully(player, PartyInvitesUI.getPartyInvites(player));
        } else {
          player.sendSystemMessage(AdventureTranslator.toNative(CobbleUtils.partyLang.getPartyAlreadyInParty()));
        }
      })
      .build();

    GooeyButton members = GooeyButton.builder()
      .display(Utils.parseItemId("minecraft:emerald"))
      .title("Members")
      .onClick(action -> {
        Player player = action.getPlayer();
        UserParty userParty = CobbleUtils.partyManager.getUserParty().get(player.getUUID());
        if (userParty.isHasParty()) {
          UIManager.openUIForcefully((ServerPlayer) player, PartyMembersUI.getPartyMembers(userParty));
        } else {
          player.sendSystemMessage(AdventureTranslator.toNative(CobbleUtils.partyLang.getPartynotInParty()));
        }
      })
      .build();

    template.set(1, 1, invites);
    template.set(1, 3, members);

    GooeyPage menu = GooeyPage.builder()
      .template(template)
      .title(AdventureTranslator.toNative(CobbleUtils.partyLang.getTitlemenu()))
      .build();
    return menu;
  }
}
