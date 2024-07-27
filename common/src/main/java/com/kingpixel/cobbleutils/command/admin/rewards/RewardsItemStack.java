package com.kingpixel.cobbleutils.command.admin.rewards;

import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.util.RewardsUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * @author Carlos Varas Alonso - 28/06/2024 10:51
 */
public class RewardsItemStack implements Command<CommandSourceStack> {

  public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                              LiteralArgumentBuilder<CommandSourceStack> base, CommandBuildContext registry) {
    dispatcher.register(
      base.requires(source -> source.hasPermission(2))
        .then(
          Commands.literal("save")
            .requires(source -> source.hasPermission(2))
            .then(
              Commands.argument("player", EntityArgument.player())
                .then(
                  Commands.literal("item")
                    .then(
                      Commands.argument("item", ItemArgument.item(registry))
                        .executes(context -> {
                          if (!context.getSource().isPlayer()) {
                            CobbleUtils.LOGGER.info("Only players can save items!");
                            return 0;
                          }
                          Player player = EntityArgument.getPlayer(context, "player");
                          ItemStack itemStack = ItemArgument.getItem(context, "item").createItemStack(1, false);
                          RewardsUtils.saveRewardItemStack(player, itemStack);
                          return 1;
                        })
                        .then(
                          Commands.argument("amount", IntegerArgumentType.integer())
                            .executes(context -> {
                              Player player = EntityArgument.getPlayer(context, "player");
                              Integer amount = IntegerArgumentType.getInteger(context, "amount");
                              ItemStack itemStack = ItemArgument.getItem(context, "item").createItemStack(amount, false);
                              RewardsUtils.saveRewardItemStack(player, itemStack);
                              return 1;
                            })
                        )
                    )
                )
            )
        )
    );
  }

  @Override public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    return 0;
  }
}
