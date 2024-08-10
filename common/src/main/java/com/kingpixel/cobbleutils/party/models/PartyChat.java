package com.kingpixel.cobbleutils.party.models;

import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import lombok.Getter;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Date;

/**
 * @author Carlos Varas Alonso - 28/06/2024 2:51
 */
@Getter
public class PartyChat {
  private String partyname;
  private String playername;
  private String message;
  private Date date;

  public PartyChat(String partyname, String playername, String message) {
    this.partyname = partyname;
    this.playername = playername;
    this.message = message;
    this.date = new Date();
  }

  public static PartyChat fromPlayer(ServerPlayerEntity player, String message) {
    return new PartyChat(CobbleUtils.partyManager.getUserParty().get(player.getUuid()).getPartyName(),
      player.getGameProfile().getName(),
      message);
  }

  public void sendToParty() {
    CobbleUtils.partyManager.getParties().get(partyname).getMembers()
      .forEach((playerInfo) -> CobbleUtils.server.getPlayerManager().getPlayer(playerInfo.getUuid()).sendMessage(
        AdventureTranslator.toNative(CobbleUtils.partyLang.getPartyChat()
          .replace("%partyname%", partyname)
          .replace("%player%", playername)
          .replace("%message%", message)
          .replace("%date%", date.toString()))));
  }
}
