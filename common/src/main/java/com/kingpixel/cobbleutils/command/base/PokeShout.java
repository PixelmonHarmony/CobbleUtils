package com.kingpixel.cobbleutils.command.base;

import com.cobblemon.mod.common.command.argument.PartySlotArgumentType;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.managers.CobbleUtilsPermission;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.kingpixel.cobbleutils.util.PlayerUtils;
import com.kingpixel.cobbleutils.util.PokemonUtils;
import com.kingpixel.cobbleutils.util.Utils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;

/**
 * @author Carlos Varas Alonso - 26/07/2024 14:14
 */
public class PokeShout implements Command<CommandSourceStack> {

  public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                              LiteralArgumentBuilder<CommandSourceStack> base) {
    dispatcher.register(
      base
        .requires(source -> CobbleUtilsPermission.checkPermission(source, CobbleUtils.permission.USER_PERMISSION)
          || CobbleUtilsPermission.checkPermission(source, CobbleUtils.permission.POKESHOUT_PERMISSION))
        .then(
          Commands.argument("slot", PartySlotArgumentType.Companion.partySlot())
            .executes(context -> {
              if (!context.getSource().isPlayer()) {
                CobbleUtils.LOGGER.error("This command can only be executed by a player");
                return 0;
              }
              context.getSource().sendSuccess(() -> Component.literal("commands.cobbleutils.pokeshout.success"), true);
              Pokemon pokemon = PartySlotArgumentType.Companion.getPokemon(context, "slot");
              ServerPlayer player = context.getSource().getPlayerOrException();
              if (pokemon != null) {
                Utils.broadcastMessage(getMessage(player, pokemon));
              } else {
                PlayerUtils.sendMessage(player, CobbleUtils.language.getMessageNoPokemon());
              }


              return 0;
            })
        )
    );
  }

  @Override
  public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    return 0;
  }


  public static Component getMessage(ServerPlayer player, Pokemon pokemon) {
    String baseMessage = CobbleUtils.language.getMessagePokeShout();
    String playerName = player.getGameProfile().getName();
    String messageContent = PokemonUtils.replace(baseMessage, pokemon).replace("%player%", playerName);

    return Component.empty().append(AdventureTranslator.toNative(messageContent))
      .withStyle(Style.EMPTY
        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
          AdventureTranslator.toNative(PokemonUtils.replace(
            "%lorepokemon%", pokemon
          ))))
      );
  }
}
