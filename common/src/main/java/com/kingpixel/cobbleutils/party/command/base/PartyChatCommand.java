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
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.entity.player.Player;

import java.util.regex.Pattern;

/**
 * @author Carlos Varas Alonso - 28/06/2024 6:01
 */
public class PartyChatCommand implements Command<CommandSourceStack> {

  public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                              LiteralArgumentBuilder<CommandSourceStack> base) {
    dispatcher.register(
      base.then(Commands.literal("chat")
        .then(
          Commands.argument("message", StringArgumentType.greedyString())
            .executes(new PartyChatCommand())
        )
      )
    );
  }

  @Override
  public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    if (!context.getSource().isPlayer()) {
      CobbleUtils.server.sendSystemMessage(AdventureTranslator.toNative("You must be a player to use this command"));
      return 0;
    }
    Player player = context.getSource().getPlayerOrException();
    String message = StringArgumentType.getString(context, "message");
    if (Pattern.compile("[^a-zA-Z0-9ñÑ&§<:#> ]").matcher(message).find()) {
      player.sendSystemMessage(
        AdventureTranslator.toNative(CobbleUtils.partyLang.getPartyChatNotValidMessage())
      );
      return 0;
    }
    UserParty userParty = CobbleUtils.partyManager.getUserParty().get(player.getUUID());
    if (userParty.isHasParty()) {
      PartyChat.fromPlayer(player, message).sendToParty();
    } else {
      player.sendSystemMessage(
        AdventureTranslator.toNative(CobbleUtils.partyLang.getPartynotInParty())
      );
      return 0;
    }
    return 1;
  }

}