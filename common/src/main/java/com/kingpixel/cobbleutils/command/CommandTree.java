package com.kingpixel.cobbleutils.command;

import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.command.admin.*;
import com.kingpixel.cobbleutils.command.admin.boss.SpawnBoss;
import com.kingpixel.cobbleutils.command.admin.egg.EggCommand;
import com.kingpixel.cobbleutils.command.admin.egg.Hatch;
import com.kingpixel.cobbleutils.command.admin.egg.IncenseCommand;
import com.kingpixel.cobbleutils.command.admin.random.RandomItem;
import com.kingpixel.cobbleutils.command.admin.random.RandomMoney;
import com.kingpixel.cobbleutils.command.admin.random.RandomPokemon;
import com.kingpixel.cobbleutils.command.admin.rewards.*;
import com.kingpixel.cobbleutils.command.base.*;
import com.kingpixel.cobbleutils.command.base.shops.ShopCommand;
import com.kingpixel.cobbleutils.command.base.shops.ShopSellCommand;
import com.kingpixel.cobbleutils.util.LuckPermsUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;

/**
 * @author Carlos Varas Alonso - 10/06/2024 14:08
 */
public class CommandTree {

  public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registry) {


    PokeShout.register(dispatcher, CommandManager.literal(CobbleUtils.config.getPokeshout()));
    PokeShoutAll.register(dispatcher, CommandManager.literal(CobbleUtils.config.getPokeshoutall()));
    Hatch.register(dispatcher, CommandManager.literal("hatch"));


    for (String literal : CobbleUtils.config.getCommmandplugin()) {
      LiteralArgumentBuilder<ServerCommandSource> base = CommandManager.literal(literal).requires(source ->
        LuckPermsUtil.checkPermission(source, 2, List.of("cobbleutils.admin")));
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

        // /cobbleutils incense <item>
        IncenseCommand.register(dispatcher, base);
        // /egginfo <slot>
        EggInfoCommand.register(dispatcher, CommandManager.literal("egginfo"));
      }


    }

    // Rewards
    if (CobbleUtils.config.isRewards()) {
      for (String literal : CobbleUtils.config.getCommandrewards()) {
        LiteralArgumentBuilder<ServerCommandSource> base = CommandManager.literal(literal)
          .requires(source -> LuckPermsUtil.checkPermission(source, 2, List.of("cobbleutils.user")));
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
        LiteralArgumentBuilder<ServerCommandSource> base = CommandManager.literal(literal);

        // /cobbleutils egg <pokemon>
        BreedCommand.register(dispatcher, base);
      }
    }

    if (CobbleUtils.config.isShops()) {
      ShopCommand.register(dispatcher, CommandManager.literal("shop"));
      ShopSellCommand.register(dispatcher, CommandManager.literal("sell"));
    }

  }


}
