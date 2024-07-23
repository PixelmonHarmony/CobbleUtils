package com.kingpixel.cobbleutils.command.base;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.kingpixel.cobbleutils.util.ArraysPokemons;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.entity.player.Player;

/**
 * @author Carlos Varas Alonso - 03/07/2024 23:40
 */
public class TestCommand implements Command<CommandSourceStack> {

  public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                              LiteralArgumentBuilder<CommandSourceStack> base) {
    dispatcher.register(
      Commands.literal("testrandompoke")
        .requires(source -> source.hasPermission(2))
        .then(
          Commands.literal("normal")
            .executes(context -> {
              Player player = context.getSource().getPlayerOrException();
              try {
                Cobblemon.INSTANCE.getStorage().getParty(player.getUUID()).add(ArraysPokemons.getRandomPokemon());
              } catch (NoPokemonStoreException e) {
                e.printStackTrace();
              }
              return 1;
            })
        )
        .then(
          Commands.literal("legendary")
            .requires(source -> source.hasPermission(2))
            .executes(context -> {
              Player player = context.getSource().getPlayerOrException();
              try {
                Cobblemon.INSTANCE.getStorage().getParty(player.getUUID()).add(ArraysPokemons.getRandomLegendary());
              } catch (NoPokemonStoreException e) {
                e.printStackTrace();
              }
              return 1;
            })
        )
        .then(
          Commands.literal("ultrabeast")
            .requires(source -> source.hasPermission(2))
            .executes(context -> {
              Player player = context.getSource().getPlayerOrException();
              try {
                Cobblemon.INSTANCE.getStorage().getParty(player.getUUID()).add(ArraysPokemons.getRandomUltraBeast());
              } catch (NoPokemonStoreException e) {
                e.printStackTrace();
              }
              return 1;
            })
        )
        .then(
          Commands.literal("arrayspokemonsizes")
            .requires(source -> source.hasPermission(2))
            .executes(context -> {

              return 1;
            })
        )
    );
  }

  @Override
  public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {


    return 1;
  }
}
