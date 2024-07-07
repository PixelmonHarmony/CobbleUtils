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
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

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
                          Player player = EntityArgument.getPlayer(context, "player");
                          ItemStack itemStack = ItemArgument.getItem(context, "item").createItemStack(1, false);
                          if (saveItem(context, player, itemStack, 1)) {
                            if (context.getSource().isPlayer()) {
                              context.getSource().getPlayer().sendSystemMessage(Component.literal("Command saved!"));
                            } else {
                              CobbleUtils.LOGGER.info("Command saved!");
                            }
                          }
                          return 1;
                        })
                        .then(
                          Commands.argument("amount", IntegerArgumentType.integer())
                            .executes(context -> {
                              Player player = EntityArgument.getPlayer(context, "player");
                              Integer amount = IntegerArgumentType.getInteger(context, "amount");
                              ItemStack itemStack = ItemArgument.getItem(context, "item").createItemStack(amount, false);
                              if (saveItem(context, player, itemStack, amount)) {
                                if (context.getSource().isPlayer()) {
                                  context.getSource().getPlayer().sendSystemMessage(Component.literal("Command saved!"));
                                } else {
                                  CobbleUtils.LOGGER.info("Command saved!");
                                }
                              }
                              return 1;
                            })
                        )
                    )
                )
            )
        )
    );
  }


  private static boolean saveItem(CommandContext<CommandSourceStack> context, Player player, ItemStack itemStack, int i) throws CommandSyntaxException {
    itemStack.setCount(i);

    if (itemStack.getCount() > itemStack.getMaxStackSize()) {
      List<ItemStack> itemStacks = new ArrayList<>();
      while (itemStack.getCount() > itemStack.getMaxStackSize()) {
        ItemStack itemStack1 = itemStack.copy();
        itemStack1.setCount(itemStack.getMaxStackSize());
        itemStacks.add(itemStack1);
        itemStack.setCount(itemStack.getCount() - itemStack.getMaxStackSize());
      }
      RewardsUtils.saveRewardItemStack(player, itemStacks);
      return true;
    }
    if (itemStack != null) {
      RewardsUtils.saveRewardItemStack(player, itemStack);
      return true;
    }
    return false;
  }

  @Override public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    return 0;
  }
}
