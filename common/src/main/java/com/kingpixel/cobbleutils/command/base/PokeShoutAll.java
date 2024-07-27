package com.kingpixel.cobbleutils.command.base;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.util.Utils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

/**
 * @author Carlos Varas Alonso - 26/07/2024 14:14
 */
public class PokeShoutAll implements Command<CommandSourceStack> {

  public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                              LiteralArgumentBuilder<CommandSourceStack> base) {
    dispatcher.register(
      base
        .executes(context -> {
          if (!context.getSource().isPlayer()) {
            CobbleUtils.LOGGER.error("This command can only be executed by a player");
            return 0;
          }
          ServerPlayer player = context.getSource().getPlayerOrException();
          PlayerPartyStore playerPartyStore = Cobblemon.INSTANCE.getStorage().getParty(player);
          playerPartyStore.forEach(pokemon -> Utils.broadcastMessage(
            PokeShout.getMessage(player, pokemon)
          ));
          return 0;
        })
    );
  }

  @Override
  public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    return 0;
  }

}
