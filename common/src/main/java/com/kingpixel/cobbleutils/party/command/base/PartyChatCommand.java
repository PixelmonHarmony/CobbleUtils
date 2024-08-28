package com.kingpixel.cobbleutils.party.command.base;

import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.party.models.PartyChat;
import com.kingpixel.cobbleutils.party.models.UserParty;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * @author Carlos Varas Alonso - 28/06/2024 6:01
 */
public class PartyChatCommand implements Command<ServerCommandSource> {

  public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                              LiteralArgumentBuilder<ServerCommandSource> base) {
    dispatcher.register(
      base.then(CommandManager.literal("chat")
        .then(
          CommandManager.argument("message", StringArgumentType.greedyString())
            .executes(new PartyChatCommand()))));
  }

  @Override
  public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
    if (!context.getSource().isExecutedByPlayer()) {
      CobbleUtils.server.sendMessage(AdventureTranslator.toNative("You must be a player to use this command"));
      return 0;
    }
    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
    String message = StringArgumentType.getString(context, "message");

    UserParty userParty = CobbleUtils.partyManager.getUserParty().get(player.getUuid());
    if (userParty.isHasParty()) {
      PartyChat.fromPlayer(player, message).sendToParty();
    } else {
      player.sendMessage(
        AdventureTranslator.toNative(CobbleUtils.partyLang.getPartynotInParty()));
      return 0;
    }
    return 1;
  }

}