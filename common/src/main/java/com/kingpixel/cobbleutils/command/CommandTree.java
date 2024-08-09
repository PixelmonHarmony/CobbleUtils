package com.kingpixel.cobbleutils.command;

import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.command.admin.*;
import com.kingpixel.cobbleutils.command.admin.boss.SpawnBoss;
import com.kingpixel.cobbleutils.command.admin.random.RandomItem;
import com.kingpixel.cobbleutils.command.admin.random.RandomMoney;
import com.kingpixel.cobbleutils.command.admin.random.RandomPokemon;
import com.kingpixel.cobbleutils.command.admin.rewards.*;
import com.kingpixel.cobbleutils.command.base.*;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/**
 * @author Carlos Varas Alonso - 10/06/2024 14:08
 */
public class CommandTree {

  public static void register(
    CommandDispatcher<CommandSourceStack> dispatcher,
    CommandBuildContext registry) {


    PokeShout.register(dispatcher, Commands.literal(CobbleUtils.config.getPokeshout()));
    PokeShoutAll.register(dispatcher, Commands.literal(CobbleUtils.config.getPokeshoutall()));
    Hatch.register(dispatcher, Commands.literal("hatch"));


    for (String literal : CobbleUtils.config.getCommmandplugin()) {
      LiteralArgumentBuilder<CommandSourceStack> base = Commands.literal(literal);
      // /cobbleutils scale <scale> <slot> and /cobbleutils scale <scale> <slot> <player>
      PokemonSize.register(dispatcher, base);

      // /cobbleutils endbattle and /cobbleutils endbattle <player>
      EndBattle.register(dispatcher, base);

      // /cobbleutils giveitem <type> <amount> <player>
      RandomItem.register(dispatcher, base);

      // /cobbleutils givepoke <type> <player>
      RandomPokemon.register(dispatcher, base);

      // /cobbleutils givemoney <amount> <player>
      RandomMoney.register(dispatcher, base);

      // /cobbleutils reload
      Reload.register(dispatcher, base);

      // /cobbleutils shinytoken <player> <amount>
      ShinyToken.register(dispatcher, base);

      // /cobbleutils pokerename <slot> <name>
      PokeRename.register(dispatcher, base);

      // /cobbleutils pokerus <slot> <player>
      PokerusCommand.register(dispatcher, base);

      // /cobbleutils breedable <slot> <breedable>
      BreedableCommand.register(dispatcher, base);


      // /cobbleutils boss <rarity> <coords>
      SpawnBoss.register(dispatcher, base);

      if (CobbleUtils.breedconfig.isActive()) {
        // /cobbleutils egg <pokemon>
        EggCommand.register(dispatcher, base);

        // /egginfo <slot>
        EggInfoCommand.register(dispatcher, Commands.literal("egginfo"));
      }


    }

    // Rewards
    if (CobbleUtils.config.isRewards()) {
      for (String literal : CobbleUtils.config.getCommandrewards()) {
        LiteralArgumentBuilder<CommandSourceStack> base = Commands.literal(literal);
        Rewards.register(dispatcher, base);
        RewardsPokemon.register(dispatcher, base);
        RewardsItemStack.register(dispatcher, base, registry);
        RewardsCommand.register(dispatcher, base);
        RewardsClaim.register(dispatcher, base);
        RewardsRemove.register(dispatcher, base);
        RewardsReload.register(dispatcher, base);
      }
    }

    if (CobbleUtils.breedconfig.isActive()) {
      for (String literal : CobbleUtils.breedconfig.getEggcommand()) {
        LiteralArgumentBuilder<CommandSourceStack> base = Commands.literal(literal);

        // /cobbleutils egg <pokemon>
        BreedCommand.register(dispatcher, base);
      }
    }

  }

}
