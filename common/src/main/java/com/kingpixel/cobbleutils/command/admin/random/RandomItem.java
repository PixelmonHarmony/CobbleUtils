package com.kingpixel.cobbleutils.command.admin.random;

import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.util.CobbleUtilities;
import com.kingpixel.cobbleutils.util.LuckPermsUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

/**
 * @author Carlos Varas Alonso - 12/06/2024 3:47
 */
public class RandomItem implements Command<ServerCommandSource> {

  public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                              LiteralArgumentBuilder<ServerCommandSource> base) {
    dispatcher.register(
      base.requires(source -> source.hasPermissionLevel(2)).then(
        CommandManager.literal("giveitem")
          .requires(
            source -> LuckPermsUtil.checkPermission(source, 2, List.of("cobbleutils.giveitem", "cobbleutils.admin")))
          .then(
            CommandManager.argument("item", StringArgumentType.string())
              .suggests(
                (context, builder) -> {
                  CobbleUtils.poolItems.getRandomitems().forEach((key, value) -> builder.suggest(key));
                  return builder.buildFuture();
                })
              .then(
                CommandManager.argument("amount", IntegerArgumentType.integer())
                  .then(
                    CommandManager.argument("player", EntityArgumentType.player())
                      .executes(new RandomItem()))))));
  }

  @Override
  public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
    ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
    String type = StringArgumentType.getString(context, "item");
    int amount = IntegerArgumentType.getInteger(context, "amount");
    if (CobbleUtils.config.isDebug())
      CobbleUtils.LOGGER.info("RandomItem command");

    CobbleUtilities.giveRandomItem(player, type, amount);

    return 1;
  }

}
