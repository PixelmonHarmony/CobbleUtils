package com.kingpixel.cobbleutils.command.admin.random;

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
 * @author Carlos Varas Alonso - 12/06/2024 3:47
 */
public class RandomMoney implements Command<CommandSourceStack> {

  public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                              LiteralArgumentBuilder<CommandSourceStack> base) {
    dispatcher.register(
      base.requires(source -> source.hasPermission(2)).then(
        Commands.literal("givemoney").requires(
            source -> source.hasPermission(2)
          )
          .then(
            Commands.argument("type", StringArgumentType.string())
              .suggests(
                (context, builder) -> {
                  CobbleUtils.poolMoney.getRandomMoney().forEach((key, value) -> builder.suggest(key));
                  return builder.buildFuture();
                }
              )
              .then(
                Commands.argument("player", EntityArgument.player())
                  .executes(new RandomMoney())
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
      CobbleUtils.LOGGER.info("RandomMoney command");

    CobbleUtilities.giveRandomMoney(player, type);
    return 1;
  }

}
