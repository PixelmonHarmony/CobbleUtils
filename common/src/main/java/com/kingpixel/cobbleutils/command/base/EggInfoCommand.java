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
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.ItemStack;

/**
 * @author Carlos Varas Alonso - 02/08/2024 12:23
 */
public class EggInfoCommand implements Command<ServerCommandSource> {

  public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
      LiteralArgumentBuilder<ServerCommandSource> base) {
    dispatcher.register(
        base.then(
            CommandManager.argument("slot", PartySlotArgumentType.Companion.partySlot())
                .executes(context -> {
                  if (!context.getSource().isExecutedByPlayer()) {
                    return 0;
                  }
                  ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                  Pokemon pokemon = PartySlotArgumentType.Companion.getPokemon(context, "slot");
                  if (pokemon.getSpecies().showdownId().equalsIgnoreCase("egg")) {
                    player.sendMessage(
                        AdventureTranslator.toNative(
                            "Egg Info: " + EggData.from(pokemon).getInfo()));
                  } else {
                    player.sendMessage(
                        AdventureTranslator.toNative(
                            "This is not an egg"));
                  }
                  return 0;
                })));
  }

  @Override
  public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
    return 0;
  }

}
