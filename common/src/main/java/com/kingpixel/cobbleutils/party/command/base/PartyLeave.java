package com.kingpixel.cobbleutils.party.command.base;

import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.PlayerInfo;
import com.kingpixel.cobbleutils.party.models.UserParty;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * @author Carlos Varas Alonso - 28/06/2024 4:05
 */
public class PartyLeave implements Command<ServerCommandSource> {

  public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                              LiteralArgumentBuilder<ServerCommandSource> base) {
    dispatcher.register(
      base.then(CommandManager.literal("leave")
        .executes(new PartyLeave())));
  }

  @Override
  public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
    if (!context.getSource().isExecutedByPlayer()) {
      CobbleUtils.server.sendMessage(AdventureTranslator.toNative("You must be a player to use this command"));
      return 0;
    }

    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
    UserParty userParty = CobbleUtils.partyManager.getUserParty().get(player.getUuid());
    if (userParty.isHasParty()) {
      CobbleUtils.partyManager.leaveParty(userParty.getPartyName(), PlayerInfo.fromPlayer(player));
    } else {
      player.sendMessage(AdventureTranslator.toNative(CobbleUtils.partyLang.getPartynotInParty()));
    }
    return 1;
  }

}
