package com.kingpixel.cobbleutils.command.admin;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.command.argument.PokemonArgumentType;
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
          Commands.argument("pokemon", PokemonArgumentType.Companion.pokemon())
            .executes(context -> {
              if (!context.getSource().isPlayer()) {
                return 0;
              }
              ServerPlayer player = context.getSource().getPlayerOrException();
              Species species = PokemonArgumentType.Companion.getPokemon(context, "pokemon");
              Pokemon pokemon = PokemonProperties.Companion.parse(species.showdownId()).create();
              Pokemon egg = PokemonProperties.Companion.parse("egg").create();

              String type1 = pokemon.getPrimaryType().getName();
              String type2 = pokemon.getSecondaryType() == null ? "" : pokemon.getSecondaryType().getName();

              egg.getPersistentData().putString("species", species.showdownId());
              egg.getPersistentData().putString("nature", pokemon.getNature().getName().getPath());
              egg.getPersistentData().putString("ability", pokemon.getAbility().getName());
              egg.getPersistentData().putString("type", type1 + (type2.isEmpty() ? "" : "," + type2));
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