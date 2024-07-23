package com.kingpixel.cobbleutils.party.util;

import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.party.models.PartyData;
import com.kingpixel.cobbleutils.party.models.UserParty;
import net.minecraft.server.level.ServerPlayer;


/**
 * @author Carlos Varas Alonso - 28/06/2024 2:16
 */
public class PartyUtil {
  public static String replace(ServerPlayer player, String data) {
    return data
      .replace("%party%", getParty(player))
      .replace("%party_members%", getPartyMembers(player));
  }

  private static PartyData getPartyData(ServerPlayer player) {
    return CobbleUtils.partyManager.getParties().get(getUserParty(player).getPartyName());
  }

  private static UserParty getUserParty(ServerPlayer player) {
    return CobbleUtils.partyManager.getUserParty(player.getUUID());
  }

  private static CharSequence getParty(ServerPlayer player) {
    return getUserParty(player).getPartyName();
  }

  private static CharSequence getPartyMembers(ServerPlayer player) {
    return String.valueOf(getPartyData(player).getMembers().size());
  }
}
