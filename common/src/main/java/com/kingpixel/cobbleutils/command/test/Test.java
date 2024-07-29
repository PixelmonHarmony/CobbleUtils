package com.kingpixel.cobbleutils.command.test;

import com.cobblemon.mod.common.api.moves.BenchedMove;
import com.cobblemon.mod.common.command.argument.PartySlotArgumentType;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.gson.JsonObject;
import com.kingpixel.cobbleutils.util.PlayerUtils;
import com.kingpixel.cobbleutils.util.PokemonUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * @author Carlos Varas Alonso - 21/07/2024 5:47
 */
public class Test {

  public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                              LiteralArgumentBuilder<CommandSourceStack> base) {
    dispatcher.register(
      Commands.literal("eggmoves")
        .requires(source -> source.hasPermission(2))
        .then(
          Commands.argument("slot", PartySlotArgumentType.Companion.partySlot())
            .executes(context -> {
              Pokemon pokemon = PartySlotArgumentType.Companion.getPokemon(context, "slot");
              pokemon.getSpecies().getMoves().getEggMoves().forEach(move -> {
                BenchedMove benchedMove = BenchedMove.Companion.loadFromJSON(move.create().saveToJSON(new JsonObject()));
                pokemon.getBenchedMoves().add(benchedMove);
              });
              return 1;
            })
        )
    );

    dispatcher.register(
      base.then(
        Commands.literal("head")
          .requires(commandSourceStack -> commandSourceStack.hasPermission(2))
          .executes(context -> {
            if (!context.getSource().isPlayer()) return 0;
            ServerPlayer player = context.getSource().getPlayerOrException();
            player.getInventory().add(PlayerUtils.getHeadItem(player));
            player.sendSystemMessage(Component.literal(String.valueOf(player.getGameProfile().getId())));
            return 1;
          })
      )
    );

    dispatcher.register(
      base.then(
        Commands.literal("ah")
          .requires(commandSourceStack -> commandSourceStack.hasPermission(2))
          .then(
            Commands.argument("slot", PartySlotArgumentType.Companion.partySlot())
              .executes(context -> {
                if (!context.getSource().isPlayer()) return 0;
                ServerPlayer player = context.getSource().getPlayerOrException();
                Pokemon pokemon = PartySlotArgumentType.Companion.getPokemon(context, "slot");
                PokemonUtils.getAH(pokemon);
                return 1;
              })
          )
      )
    );
  }
}
