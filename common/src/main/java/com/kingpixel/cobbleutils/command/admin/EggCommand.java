package com.kingpixel.cobbleutils.command.admin;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.command.argument.PokemonPropertiesArgumentType;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * @author Carlos Varas Alonso - 23/07/2024 22:18
 */
public class EggCommand implements Command<CommandSourceStack> {
  public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                              LiteralArgumentBuilder<CommandSourceStack> base) {
    dispatcher.register(base.then(


      Commands.literal("egg")
        .requires(source -> source.hasPermission(2))
        .then(
          Commands.argument("pokemon", PokemonPropertiesArgumentType.Companion.properties())
            .executes(context -> {
              if (!context.getSource().isPlayer()) {
                return 0;
              }
              ServerPlayer player = context.getSource().getPlayerOrException();
              Pokemon pokemon = PokemonPropertiesArgumentType.Companion.getPokemonProperties(context, "pokemon").create();
              Species species = pokemon.getSpecies();
              Pokemon egg = PokemonProperties.Companion.parse("egg type_egg=" + pokemon.showdownId()).create();
              egg.getPersistentData().putString("species", species.showdownId());
              egg.getPersistentData().putString("nature", pokemon.getNature().getName().getPath());
              egg.getPersistentData().putString("ability", pokemon.getAbility().getName());
              egg.getPersistentData().putString("form", pokemon.getForm().getAspects().isEmpty() ? "" : pokemon.getForm().getAspects().get(0));
              egg.getPersistentData().putInt("level", 1);
              if (CobbleUtils.config.isDebug()) {
                egg.getPersistentData().putInt("steps", 0);
              } else {
                egg.getPersistentData().putInt("steps", 200);
              }
              if (CobbleUtils.config.isDebug()) {
                egg.getPersistentData().putInt("cycles", 0);
              } else {
                egg.getPersistentData().putInt("cycles", pokemon.getSpecies().getEggCycles());
              }
              egg.setNickname(Component.literal("Egg " + pokemon.getSpecies().getTranslatedName().getString()));
              if (CobbleUtils.config.isDebug()) {
                CobbleUtils.LOGGER.info("Egg create: " + egg.getPersistentData().getAsString());
              }
              Cobblemon.INSTANCE.getStorage().getParty(player).add(egg);
              return 1;
            })
        ))
    );


  }

  @Override public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {

    return 1;
  }
}