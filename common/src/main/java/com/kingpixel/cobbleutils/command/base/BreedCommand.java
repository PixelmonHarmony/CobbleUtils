package com.kingpixel.cobbleutils.command.base;

import com.cobblemon.mod.common.Cobblemon;
import com.kingpixel.cobbleutils.features.breeding.ui.PlotBreedingUI;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

/**
 * @author Carlos Varas Alonso - 02/08/2024 12:23
 */
public class BreedCommand implements Command<CommandSourceStack> {

  public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                              LiteralArgumentBuilder<CommandSourceStack> base) {
    dispatcher.register(
      base.executes(
        context -> {
          if (!context.getSource().isPlayer()) {
            return 0;
          }
          ServerPlayer player = context.getSource().getPlayerOrException();
          if (Cobblemon.INSTANCE.getBattleRegistry().getBattleByParticipatingPlayer(player) == null) {
            return 0;
          }
          PlotBreedingUI.open(player);
          return 1;
        })
    );

  }

  @Override
  public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    return 0;
  }


}
