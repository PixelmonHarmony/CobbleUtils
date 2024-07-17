package com.kingpixel.cobbleutils.command.admin;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.SizeChance;
import com.kingpixel.cobbleutils.util.PlayerUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

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
              .executes(context -> {
                if (!context.getSource().isPlayer()) {
                  CobbleUtils.LOGGER.info("This command can only be executed by a player");
                  return 0;
                }
                ServerPlayer player = context.getSource().getPlayerOrException();
                int slot = IntegerArgumentType.getInteger(context, "slot");
                float scale = FloatArgumentType.getFloat(context, "scale");
                try {
                  scale(player, slot, scale);
                } catch (NoPokemonStoreException e) {
                  e.printStackTrace();
                }
                return 1;
              })
              .then(Commands.argument("player", EntityArgument.players())
                .executes(context -> {
                  ServerPlayer player = EntityArgument.getPlayer(context, "player");
                  int slot = IntegerArgumentType.getInteger(context, "slot");
                  float scale = FloatArgumentType.getFloat(context, "scale");
                  try {
                    scale(player, slot, scale);
                  } catch (NoPokemonStoreException e) {
                    e.printStackTrace();
                  }
                  return 1;
                })
              )
            )
          )
      )
    );
  }

  @Override
  public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    return 1;
  }

  private static void scale(ServerPlayer player, int slot, float scale) throws NoPokemonStoreException {
    Pokemon pokemon = Cobblemon.INSTANCE.getStorage().getParty(player.getUUID()).get(--slot);
    if (pokemon == null) {
      PlayerUtils.sendMessage(player, CobbleUtils.language.getMessageNoPokemon());
      return;
    }
    pokemon.setScaleModifier(scale);
    pokemon.getPersistentData().putString("size", "custom");
  }

}
