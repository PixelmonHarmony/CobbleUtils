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
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * @author Carlos Varas Alonso - 12/06/2024 3:47
 */
public class EndBattle implements Command<CommandSourceStack> {

  public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                              LiteralArgumentBuilder<CommandSourceStack> base) {
    dispatcher.register(
      Commands.literal("endbattle")
        .executes(new EndBattle())
        .requires(source -> source.hasPermission(2))
        .then(Commands.argument("player", EntityArgument.players())
          .requires(source -> source.hasPermission(2))
          .executes(new EndBattle())
        )
    );
  }

  @Override
  public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    Player player = context.getSource().getPlayerOrException();
    Player targetPlayer = null;
    try {

      try {
        targetPlayer = EntityArgument.getPlayer(context, "player");
      } catch (Exception ignored) {
      }
      if (targetPlayer != null) {
        player = targetPlayer;
      }
      if (CobbleUtilities.isBattle(player))
        Cobblemon.INSTANCE.getBattleRegistry().getBattleByParticipatingPlayer((ServerPlayer) player).end();
      return 1;
    } catch (Exception e) {
      CobbleUtils.LOGGER.error(e.getMessage());
      player.sendSystemMessage(AdventureTranslator.toNative(CobbleUtils.language.getMessagearebattle()));
      return 0;
    }
  }

}
