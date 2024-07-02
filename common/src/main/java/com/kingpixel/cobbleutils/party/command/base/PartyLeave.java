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
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.entity.player.Player;

/**
 * @author Carlos Varas Alonso - 28/06/2024 4:05
 */
public class PartyLeave implements Command<CommandSourceStack> {

  public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                              LiteralArgumentBuilder<CommandSourceStack> base) {
    dispatcher.register(
      base.then(Commands.literal("leave")
        .executes(new PartyLeave())
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
    UserParty userParty = CobbleUtils.partyManager.getUserParty().get(player.getUUID());
    if (userParty.isHasParty()) {
      CobbleUtils.partyManager.leaveParty(userParty.getPartyName(), PlayerInfo.fromPlayer(player));
    } else {
      player.sendSystemMessage(AdventureTranslator.toNative(CobbleUtils.partyLang.getPartynotInParty()));
    }
    return 1;
  }

}
