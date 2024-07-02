package com.kingpixel.cobbleutils.command.admin.random;

import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.util.CobbleUtilities;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.player.Player;

/**
 * @author Carlos Varas Alonso - 13/06/2024 10:11
 */
public class RandomPokemon implements Command<CommandSourceStack> {
  public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                              LiteralArgumentBuilder<CommandSourceStack> base) {
    dispatcher.register(
      base.requires(source -> source.hasPermission(2))
        .then(
          Commands.literal("givepoke").requires(source -> source.hasPermission(2))
            .then(
              Commands.argument("type", StringArgumentType.string())
                .suggests(
                  (context, builder) -> {
                    CobbleUtils.poolPokemons.getRandompokemons().forEach((key, value) -> builder.suggest(key));
                    return builder.buildFuture();
                  }
                )

                .then(
                  Commands.argument("player", EntityArgument.player())
                    .executes(new RandomPokemon())
                )

            )
        )
    );
  }

  @Override
  public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    Player player = EntityArgument.getPlayer(context, "player");
    String type = StringArgumentType.getString(context, "type");
    if (CobbleUtils.config.isDebug())
      CobbleUtils.LOGGER.info("RandomPokemon command");

    try {
      CobbleUtilities.giveRandomPokemon(player, type);
    } catch (NoPokemonStoreException e) {
      e.printStackTrace();
    }


    return 1;
  }
}
