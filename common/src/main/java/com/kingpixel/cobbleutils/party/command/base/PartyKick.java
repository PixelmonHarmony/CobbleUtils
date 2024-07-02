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
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.player.Player;

/**
 * @author Carlos Varas Alonso - 28/06/2024 4:05
 */
public class PartyKick implements Command<CommandSourceStack> {

  public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                              LiteralArgumentBuilder<CommandSourceStack> base) {
    dispatcher.register(
      base.then(Commands.literal("kick")
        .then(
          Commands.argument("player", EntityArgument.player())
            .executes(new PartyKick())
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

    Player owner = context.getSource().getPlayerOrException();
    Player member = EntityArgument.getPlayer(context, "player");
    UserParty userParty = CobbleUtils.partyManager.getUserParty().get(owner.getUUID());
    if (userParty.isHasParty()) {
      CobbleUtils.partyManager.kickPlayer(userParty.getPartyName(), PlayerInfo.fromPlayer(owner), PlayerInfo.fromPlayer(member));
    } else {
      owner.sendSystemMessage(AdventureTranslator.toNative(CobbleUtils.partyLang.getPartynotInParty()));
    }
    return 1;
  }

}
