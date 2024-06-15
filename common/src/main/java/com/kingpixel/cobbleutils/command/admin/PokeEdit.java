package com.kingpixel.cobbleutils.command.admin;

import com.kingpixel.cobbleutils.util.CobbleUtilities;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;

/**
 * @author Carlos Varas Alonso - 12/06/2024 3:47
 */
public class PokeEdit implements Command<CommandSourceStack> {

  public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                              LiteralArgumentBuilder<CommandSourceStack> base) {
    // /cobbleutils pokeedit <player> Abrira un menu con pc y party del jugador
  }

  @Override
  public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    CobbleUtilities.unimplemented(context.getSource().getPlayerOrException());
    return 1;
  }

}
