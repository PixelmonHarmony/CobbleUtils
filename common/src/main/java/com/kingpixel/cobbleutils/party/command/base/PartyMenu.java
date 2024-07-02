package com.kingpixel.cobbleutils.party.command.base;

import ca.landonjw.gooeylibs2.api.UIManager;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.party.ui.PartyUI;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/**
 * @author Carlos Varas Alonso - 28/06/2024 2:23
 */
public class PartyMenu implements Command<CommandSourceStack> {

  public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                              LiteralArgumentBuilder<CommandSourceStack> base) {
    dispatcher.register(
      base.then(Commands.literal("menu")
        .executes(new PartyMenu())
      )
    );
  }

  @Override
  public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    if (!context.getSource().isPlayer()) {
      CobbleUtils.server.sendSystemMessage(AdventureTranslator.toNative("You must be a player to use this command"));
      return 0;
    }


    try {
      UIManager.openUIForcefully(context.getSource().getPlayerOrException(),
        PartyUI.getPartyMenu());
    } catch (Exception e) {
      e.printStackTrace();
    }

    return 1;
  }

}
