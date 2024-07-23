package com.kingpixel.cobbleutils.party.util;

import com.kingpixel.cobbleutils.CobbleUtils;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import net.minecraft.resources.ResourceLocation;

/**
 * @author Carlos Varas Alonso - 02/07/2024 18:34
 */
public class PartyPlaceholder {
  public static void register() {
    // Fabric and forge eu.pb4.placeholders
    try {
      Placeholders.register(
        new ResourceLocation("cobbleutils", "party"),
        (ctx, arg) -> {
          if (!ctx.hasPlayer())
            return PlaceholderResult.value("");
          if (CobbleUtils.partyManager.isPlayerInParty(ctx.player())) {
            return PlaceholderResult.value(CobbleUtils.partyManager.getUserParty(ctx.player().getUUID()).getPartyName());
          } else {
            return PlaceholderResult.value("");
          }
        });

      Placeholders.register(
        new ResourceLocation("cobbleutils", "party_members"),
        (ctx, arg) -> {
          if (!ctx.hasPlayer())
            return PlaceholderResult.value("");
          assert ctx.player() != null;
          if (CobbleUtils.partyManager.isPlayerInParty(ctx.player())) {
            return PlaceholderResult.value(String.valueOf(CobbleUtils.partyManager.getParties().get(CobbleUtils.partyManager.getUserParty(ctx.player().getUUID()).getPartyName()).getMembers().size()));
          } else {
            return PlaceholderResult.value("");
          }
        });
    } catch (NoSuchMethodError | NoClassDefFoundError | Exception ignored) {
    }
    // Spigot PlaceholderAPI
    /*try {

    } catch (NoSuchMethodError | NoClassDefFoundError | Exception ignored) {
    }*/
  }
}
