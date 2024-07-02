package com.kingpixel.cobbleutils.command.admin.rewards;

import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.util.RewardsUtils;
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
 * @author Carlos Varas Alonso - 28/06/2024 10:51
 */
public class RewardsCommand implements Command<CommandSourceStack> {
  public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                              LiteralArgumentBuilder<CommandSourceStack> base) {
    dispatcher.register(
      base.requires(source -> source.hasPermission(2))
        .then(
          Commands.literal("save")
            .requires(source -> source.hasPermission(2))
            .then(
              Commands.argument("player", EntityArgument.player())
                .then(
                  Commands.literal("command")
                    .then(
                      Commands.argument("command", StringArgumentType.string())
                        .executes(new RewardsCommand()))
                )
            )
        )
    );
  }

  @Override public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    if (CobbleUtils.config.isDebug())
      CobbleUtils.LOGGER.info("RewardsPokemon command");
    Player player = EntityArgument.getPlayer(context, "player");
    String command = StringArgumentType.getString(context, "command");
    RewardsUtils.saveRewardCommand(player, command);
    return 1;
  }
}
