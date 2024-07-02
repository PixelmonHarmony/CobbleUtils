package com.kingpixel.cobbleutils.party.command.base;

import com.kingpixel.cobbleutils.CobbleUtils;
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
 * @author Carlos Varas Alonso - 28/06/2024 4:19
 */
public class PartyInvite implements Command<CommandSourceStack> {

  public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                              LiteralArgumentBuilder<CommandSourceStack> base) {
    dispatcher.register(
      base.then(Commands.literal("invite")
        .then(
          Commands.argument("player", EntityArgument.player())
            .executes(new PartyInvite())
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
    Player invite = EntityArgument.getPlayer(context, "player");
    UserParty ownerdata = CobbleUtils.partyManager.getUserParty().get(player.getUUID());
    UserParty inviteData = CobbleUtils.partyManager.getUserParty().get(invite.getUUID());
    CobbleUtils.partyManager.invitePlayer(player.getUUID(), invite.getUUID(), ownerdata, inviteData);
    return 1;
  }

}
