package com.kingpixel.cobbleutils.command.admin;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.SizeChance;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.entity.player.Player;

/**
 * @author Carlos Varas Alonso - 12/06/2024 3:47
 */
public class PokemonSize implements Command<CommandSourceStack> {

  public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                              LiteralArgumentBuilder<CommandSourceStack> base) {
    float minsize = CobbleUtils.config.getMinpokemonsize();
    float maxsize = CobbleUtils.config.getMaxpokemonsize();
    dispatcher.register(
      base.requires(source -> source.hasPermission(2)).then(
        Commands.literal("scale").requires(
            source -> source.hasPermission(2)
          )
          .then(Commands.argument("scale", FloatArgumentType.floatArg(minsize, maxsize))
            .suggests((context, builder) -> {
              for (SizeChance size : CobbleUtils.config.getPokemonsizes()) {
                builder.suggest(String.valueOf(size.getSize()));
              }
              return builder.buildFuture();
            })
            .then(Commands.argument("slot", IntegerArgumentType.integer(1, 6))
              .suggests((context, builder) -> {
                for (int i = 1; i <= 6; i++) {
                  builder.suggest(String.valueOf(i));
                }
                return builder.buildFuture();
              })
              .executes(new PokemonSize())
              .then(Commands.argument("player", EntityArgument.players())
                .executes(new PokemonSize())
              )
            )
          )
      )
    );
  }

  @Override
  public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    try {
      // Obtener argumentos del comando
      float scale = FloatArgumentType.getFloat(context, "scale");
      int slot = IntegerArgumentType.getInteger(context, "slot") - 1;

      // Obtener el jugador que ejecuta el comando
      Player player = context.getSource().getPlayerOrException();

      // Intentar obtener el jugador objetivo, si se especifica
      Player targetPlayer = null;
      try {
        targetPlayer = EntityArgument.getPlayer(context, "player");
      } catch (Exception e) {
        // El argumento "player" es opcional, si no se encuentra no pasa nada
      }

      // Si se especificó un jugador, usarlo
      if (targetPlayer != null) {
        player = targetPlayer;
      }

      // Aplicar el modificador de escala al Pokémon en la posición especificada
      Cobblemon.INSTANCE.getStorage().getParty(player.getUUID()).get(slot).setScaleModifier(scale);

      // Log de depuración
      if (CobbleUtils.config.isDebug()) {
        CobbleUtils.LOGGER.info("Scale: " + scale);
      }

    } catch (NoPokemonStoreException e) {
      // Manejo específico de la excepción NoPokemonStoreException
      throw new CommandSyntaxException(CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument(), new LiteralMessage("No Pokémon found in the specified slot."));
    } catch (Exception e) {
      // Manejo de otras excepciones genéricas
      throw new CommandSyntaxException(CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument(), new LiteralMessage("An error occurred while executing the command."));
    }

    return 1;
  }

}
