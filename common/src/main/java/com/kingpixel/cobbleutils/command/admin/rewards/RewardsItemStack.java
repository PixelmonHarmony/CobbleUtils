package com.kingpixel.cobbleutils.command.admin.rewards;

import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.util.LuckPermsUtil;
import com.kingpixel.cobbleutils.util.RewardsUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

/**
 * @author Carlos Varas Alonso - 28/06/2024 10:51
 */
public class RewardsItemStack implements Command<ServerCommandSource> {

  public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                              LiteralArgumentBuilder<ServerCommandSource> base,
                              CommandRegistryAccess registry) {
    dispatcher.register(
      base
        .then(
          CommandManager.literal("save")
            .requires(source -> LuckPermsUtil.checkPermission(source, 2, List.of("cobbleutils.storage_rewards.save", "cobbleutils" +
              ".admin")))
            .then(
              CommandManager.argument("player", EntityArgumentType.player())
                .then(
                  CommandManager.literal("item")
                    .then(
                      CommandManager.argument("item", ItemStackArgumentType.itemStack(registry))
                        .executes(context -> {
                          if (!context.getSource().isExecutedByPlayer()) {
                            CobbleUtils.LOGGER.info("Only players can save items!");
                            return 0;
                          }
                          ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                          ItemStack itemStack = ItemStackArgumentType.getItemStackArgument(context, "item").createStack(1, true);
                          RewardsUtils.saveRewardItemStack(player, itemStack);
                          return 1;
                        })
                        .then(
                          CommandManager.argument("amount", IntegerArgumentType.integer())
                            .executes(context -> {
                              ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                              int amount = IntegerArgumentType.getInteger(context, "amount");
                              ItemStack itemStack =
                                ItemStackArgumentType.getItemStackArgument(context, "item").createStack(amount, true);
                              RewardsUtils.saveRewardItemStack(player, itemStack);
                              return 1;
                            })))))));
  }

  @Override
  public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
    return 0;
  }
}
