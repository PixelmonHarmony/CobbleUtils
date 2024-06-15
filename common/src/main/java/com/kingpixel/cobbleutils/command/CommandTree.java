package com.kingpixel.cobbleutils.command;

import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.command.admin.*;
import com.kingpixel.cobbleutils.command.base.EndBattle;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/**
 * @author Carlos Varas Alonso - 10/06/2024 14:08
 */
public class CommandTree {
  private static final String literal = "cobbleutils";

  public static void register(
    CommandDispatcher<CommandSourceStack> dispatcher
  ) {
    LiteralArgumentBuilder<CommandSourceStack> base = Commands.literal(literal);

    // /cobbleutils
    dispatcher.register(
      base.executes(context -> {
        CobbleUtils.LOGGER.info("CobbleRaids command");
        return 1;
      }));

    // /cobbleutils scale <scale> <slot> and /cobbleutils scale <scale> <slot> <player>
    PokemonSize.register(dispatcher, base);

    // /cobbleutils endbattle and /cobbleutils endbattle <player>
    EndBattle.register(dispatcher, base);

    // /cobbleutils giveitem <type> <amount> <player>
    RandomItem.register(dispatcher, base);
    
    // /cobbleutils givepoke <type> <player>
    RandomPokemon.register(dispatcher, base);

    // /cobbleutils reload
    Reload.register(dispatcher, base);

    // /cobbleutils pokestop add <type>
    PokeStop.register(dispatcher, base);
  }

}
