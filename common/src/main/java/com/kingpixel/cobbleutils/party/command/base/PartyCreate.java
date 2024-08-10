package com.kingpixel.cobbleutils.party.command.base;

import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.PlayerInfo;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.ItemStack;

/**
 * @author Carlos Varas Alonso - 28/06/2024 2:26
 */
public class PartyCreate implements Command<ServerCommandSource> {

  public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
      LiteralArgumentBuilder<ServerCommandSource> base) {
    dispatcher.register(
        base.then(CommandManager.literal("create")
            .then(
                CommandManager.argument("name", StringArgumentType.greedyString())
                    .executes(new PartyCreate()))));
  }

  @Override
  public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
    if (!context.getSource().isExecutedByPlayer()) {
      CobbleUtils.server.sendMessage(AdventureTranslator.toNative("You must be a player to use this command"));
      return 0;
    }
    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
    String name = StringArgumentType.getString(context, "name");
    CobbleUtils.partyManager.createParty(name, new PlayerInfo(player.getGameProfile().getName(), player.getUuid()));
    return 1;
  }

}
