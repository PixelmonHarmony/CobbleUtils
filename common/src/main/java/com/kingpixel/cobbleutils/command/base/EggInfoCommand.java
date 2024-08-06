package com.kingpixel.cobbleutils.command.base;

import com.cobblemon.mod.common.command.argument.PartySlotArgumentType;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.features.breeding.models.EggData;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

/**
 * @author Carlos Varas Alonso - 02/08/2024 12:23
 */
public class EggInfoCommand implements Command<CommandSourceStack> {

  public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                              LiteralArgumentBuilder<CommandSourceStack> base) {
    dispatcher.register(
      base.then(
        Commands.argument("slot", PartySlotArgumentType.Companion.partySlot())
          .executes(context -> {
            if (!context.getSource().isPlayer()) {
              return 0;
            }
            ServerPlayer player = context.getSource().getPlayerOrException();
            Pokemon pokemon = PartySlotArgumentType.Companion.getPokemon(context, "slot");
            if (pokemon.getSpecies().showdownId().equalsIgnoreCase("egg")) {
              player.sendSystemMessage(
                AdventureTranslator.toNative(
                  "Egg Info: " + EggData.from(pokemon).getInfo()
                )
              );
            } else {
              player.sendSystemMessage(
                AdventureTranslator.toNative(
                  "This is not an egg"
                )
              );
            }
            return 0;
          })
      )
    );
  }

  @Override
  public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    return 0;
  }


}
