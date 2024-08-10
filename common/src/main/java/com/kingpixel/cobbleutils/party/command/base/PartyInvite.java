package com.kingpixel.cobbleutils.party.command.base;

import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.party.models.UserParty;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * @author Carlos Varas Alonso - 28/06/2024 4:19
 */
public class PartyInvite implements Command<ServerCommandSource> {

  public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                              LiteralArgumentBuilder<ServerCommandSource> base) {
    dispatcher.register(
      base.then(CommandManager.literal("invite")
        .then(
          CommandManager.argument("player", EntityArgumentType.player())
            .executes(new PartyInvite()))));
  }

  @Override
  public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
    if (!context.getSource().isExecutedByPlayer()) {
      CobbleUtils.server.sendMessage(AdventureTranslator.toNative("You must be a player to use this command"));
      return 0;
    }
    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
    ServerPlayerEntity invite = EntityArgumentType.getPlayer(context, "player");
    UserParty ownerdata = CobbleUtils.partyManager.getUserParty().get(player.getUuid());
    UserParty inviteData = CobbleUtils.partyManager.getUserParty().get(invite.getUuid());
    CobbleUtils.partyManager.invitePlayer(player.getUuid(), invite.getUuid(), ownerdata, inviteData);
    return 1;
  }

}
