package com.kingpixel.cobbleutils.command.base;

import com.cobblemon.mod.common.Cobblemon;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.kingpixel.cobbleutils.util.CobbleUtilities;
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
 * @author Carlos Varas Alonso - 12/06/2024 3:47
 */
public class EndBattle implements Command<ServerCommandSource> {

  public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                              LiteralArgumentBuilder<ServerCommandSource> base) {
    dispatcher.register(
      CommandManager.literal("endbattle")
        .executes(new EndBattle())
        .requires(source -> source.hasPermissionLevel(2))
        .then(CommandManager.argument("player", EntityArgumentType.players())
          .requires(source -> source.hasPermissionLevel(2))
          .executes(new EndBattle())));
  }

  @Override
  public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
    ServerPlayerEntity targetPlayer = null;
    try {

      try {
        targetPlayer = EntityArgumentType.getPlayer(context, "player");
      } catch (Exception ignored) {
      }
      if (targetPlayer != null) {
        player = targetPlayer;
      }
      if (CobbleUtilities.isBattle(player))
        Cobblemon.INSTANCE.getBattleRegistry().getBattleByParticipatingPlayer(player).end();
      return 1;
    } catch (Exception e) {
      CobbleUtils.LOGGER.error(e.getMessage());
      player.sendMessage(AdventureTranslator.toNative(CobbleUtils.language.getMessagearebattle()));
      return 0;
    }
  }

}
