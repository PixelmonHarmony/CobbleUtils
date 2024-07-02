package com.kingpixel.cobbleutils.command.admin.random;

import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.util.CobbleUtilities;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
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
public class RandomItem implements Command<CommandSourceStack> {

  public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                              LiteralArgumentBuilder<CommandSourceStack> base) {
    dispatcher.register(
      base.requires(source -> source.hasPermission(2)).then(
        Commands.literal("giveitem").requires(
            source -> source.hasPermission(2)
          )
          .then(
            Commands.argument("type", StringArgumentType.string())
              .suggests(
                (context, builder) -> {
                  CobbleUtils.poolItems.getRandomitems().forEach((key, value) -> builder.suggest(key));
                  return builder.buildFuture();
                }
              )
              .then(
                Commands.argument("amount", IntegerArgumentType.integer())
                  .then(
                    Commands.argument("player", EntityArgument.player())
                      .executes(new RandomItem())
                  )
              )
          )
      )
    );
  }

  @Override
  public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    Player player = EntityArgument.getPlayer(context, "player");
    String type = StringArgumentType.getString(context, "type");
    int amount = IntegerArgumentType.getInteger(context, "amount");
    if (CobbleUtils.config.isDebug())
      CobbleUtils.LOGGER.info("RandomItem command");

    CobbleUtilities.giveRandomItem(player, type, amount);

    return 1;
  }

}
