package com.kingpixel.cobbleutils.command.admin;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.command.argument.PartySlotArgumentType;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.features.breeding.util.AdventureBreeding;
import com.kingpixel.cobbleutils.util.PlayerUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author Carlos Varas Alonso - 09/08/2024 3:27
 */
public class Hatch implements Command<CommandSourceStack> {
  private static Map<UUID, Long> cooldowns = new HashMap<>();

  public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                              LiteralArgumentBuilder<CommandSourceStack> base) {

    dispatcher.register(
      base
        .requires(source -> source.hasPermission(2))
        .then(
          Commands.argument("egg", PartySlotArgumentType.Companion.partySlot())
            .executes(context -> {
              if (!context.getSource().isPlayer()) return 0;
              ServerPlayer player = context.getSource().getPlayerOrException();
              Long cooldown = cooldowns.get(player.getUUID());

              if (cooldown != null && PlayerUtils.isCooldown(cooldown)) {
                player.sendSystemMessage(AdventureBreeding.adventure(
                  CobbleUtils.language.getMessageCooldown()
                    .replace("%cooldown%", PlayerUtils.getCooldown(new Date(cooldown)))
                ));
                return 0;
              }

              if (Cobblemon.INSTANCE.getBattleRegistry().getBattleByParticipatingPlayer(player) != null) return 0;
              Pokemon egg = PartySlotArgumentType.Companion.getPokemon(context, "egg");

              if (egg.getSpecies().showdownId().equalsIgnoreCase("egg")) {
                cooldowns.put(player.getUUID(),
                  new Date().getTime() + TimeUnit.SECONDS.toMillis(CobbleUtils.breedconfig.getCooldowninstaHatchInSeconds()));
                egg.getPersistentData().putInt("steps", 0);
                egg.getPersistentData().putInt("cycles", 0);
              }
              return 1;
            })
        )
    );

  }

  @Override public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    return 0;
  }
}