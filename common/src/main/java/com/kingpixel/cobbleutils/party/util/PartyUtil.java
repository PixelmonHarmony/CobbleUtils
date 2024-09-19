package com.kingpixel.cobbleutils.party.util;

import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.party.models.PartyData;
import com.kingpixel.cobbleutils.party.models.UserParty;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * @author Carlos Varas Alonso - 28/06/2024 2:16
 */
public class PartyUtil {
  public static String replace(ServerPlayerEntity player, String data) {
    return data
      .replace("%party%", getParty(player))
      .replace("%party_members%", getPartyMembers(player));
  }

  private static PartyData getPartyData(ServerPlayerEntity player) {
    return CobbleUtils.partyManager.getParty(player);
  }

  private static UserParty getUserParty(ServerPlayerEntity player) {
    return CobbleUtils.partyManager.getUserParty().get(player);
  }

  private static CharSequence getParty(ServerPlayerEntity player) {
    return CobbleUtils.partyManager.getParty(player).getName();
  }

  private static CharSequence getPartyMembers(ServerPlayerEntity player) {
    return String.valueOf(getPartyData(player).getMembers().size());
  }
}
