package com.kingpixel.cobbleutils.party.util;

/**
 * @author Carlos Varas Alonso - 02/07/2024 18:34
 */
public class PartyPlaceholder {
  public static void register() {
    // Fabric and forge eu.pb4.placeholders
    /*try {
      Placeholders.register(
        new Identifier("cobbleutils", "party"),
        (ctx, arg) -> {
          if (!ctx.hasPlayer())
            return PlaceholderResult.value("");
          if (CobbleUtils.partyManager.isPlayerInParty(ctx.player())) {
            return PlaceholderResult.value(CobbleUtils.partyManager.getUserParty(ctx.player().getUuid()).getPartyName());
          } else {
            return PlaceholderResult.value("");
          }
        });

      Placeholders.register(
        new Identifier("cobbleutils", "party_members"),
        (ctx, arg) -> {
          if (!ctx.hasPlayer())
            return PlaceholderResult.value("");
          assert ctx.player() != null;
          if (CobbleUtils.partyManager.isPlayerInParty(ctx.player())) {
            return PlaceholderResult.value(String.valueOf(CobbleUtils.partyManager.getParties().get(CobbleUtils.partyManager.getUserParty(ctx.player().getUuid()).getPartyName()).getMembers().size()));
          } else {
            return PlaceholderResult.value("");
          }
        });
    } catch (NoSuchMethodError | NoClassDefFoundError | Exception ignored) {
    }*/
    // Spigot PlaceholderAPI
    /*try {

    } catch (NoSuchMethodError | NoClassDefFoundError | Exception ignored) {
    }*/
  }
}
