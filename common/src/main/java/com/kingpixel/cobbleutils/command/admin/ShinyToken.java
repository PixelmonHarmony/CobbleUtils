package com.kingpixel.cobbleutils.command.admin;

import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.kingpixel.cobbleutils.util.RewardsUtils;
import com.kingpixel.cobbleutils.util.Utils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * @author Carlos Varas Alonso - 28/06/2024 20:04
 */
public class ShinyToken implements Command<CommandSourceStack> {
  public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                              LiteralArgumentBuilder<CommandSourceStack> base) {

    dispatcher.register(
      base
        .requires(source -> source.hasPermission(2))
        .then(
          Commands.literal("shinytoken")
            .requires(
              source -> source.hasPermission(2)
            )
            .then(
              Commands.argument("player", EntityArgument.player())
                .then(
                  Commands.argument("amount", IntegerArgumentType.integer(1, 64))
                    .executes(new ShinyToken())
                )
            )
        )
    );

  }

  @Override public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    Player player = EntityArgument.getPlayer(context, "player");
    int amount = IntegerArgumentType.getInteger(context, "amount");
    String lore = "";
    List<Component> comp = AdventureTranslator.toNativeL(CobbleUtils.config.getShinytoken().getLore());
    for (Component c : comp) {
      lore += c.getString() + "\n";
    }
    ItemStack itemStack = Utils.parseItemId(CobbleUtils.config.getShinytoken().getItem());
    itemStack.setCount(amount);
    itemStack.setHoverName(AdventureTranslator.toNative(CobbleUtils.config.getShinytoken().getDisplayname()));
    itemStack.getTag().getCompound("display").putString("Lore", lore);
    itemStack.addTagElement("shinytoken", ItemStack.EMPTY.save(new CompoundTag()));

    if (!player.getInventory().add(itemStack)) {
      RewardsUtils.saveRewardItemStack(player, itemStack);
    }
    return 1;
  }
}
