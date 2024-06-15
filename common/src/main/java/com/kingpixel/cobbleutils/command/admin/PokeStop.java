package com.kingpixel.cobbleutils.command.admin;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.util.PokeStopUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/**
 * @author Carlos Varas Alonso - 14/06/2024 22:53
 */
public class PokeStop implements Command<CommandSourceStack> {
  public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
                              LiteralArgumentBuilder<CommandSourceStack> base) {
    dispatcher.register(
      Commands.literal("cobbleutils").then(
        Commands.literal("pokestop").requires(source -> source.hasPermission(2)).then(
          Commands.literal("add")
            .requires(source -> source.hasPermission(2))
            .then(
              Commands.argument("type", StringArgumentType.string())
                .suggests((context, builder) -> {
                  CobbleUtils.pokestops.getPokestops().forEach(pokestop -> builder.suggest(pokestop.getType()));
                  return builder.buildFuture();
                })
                .executes(new PokeStop())
            )
        ).then(
          Commands.literal("edit")
            .requires(source -> source.hasPermission(2))
            .then(
              Commands.argument("type", StringArgumentType.string())
                .suggests((context, builder) -> {
                  CobbleUtils.pokestops.getPokestops().forEach(pokestop -> builder.suggest(pokestop.getType()));
                  return builder.buildFuture();
                })
                .then(
                  Commands.argument("entity", EntityArgument.entity())
                    .executes(context -> {
                      edit(context);
                      return 1;
                    })
                )
            )
        ).then(
          Commands.literal("remove")
            .requires(source -> source.hasPermission(2))
            .then(
              Commands.argument("entity", EntityArgument.entity())
                .executes(context -> {
                  remove(context);
                  return 1;
                })
            )
        ).then(
          Commands.literal("list")
            .requires(source -> source.hasPermission(2))
            .executes(context -> {
              list(context);
              return 1;
            })
        ).then(
          Commands.literal("resetcooldowns")
            .requires(source -> source.hasPermission(2))
            .executes(context -> {
              resetcooldowns(context);
              return 1;
            })
        ).then(
          Commands.literal("tp")
            .requires(source -> source.hasPermission(2))
            .then(
              Commands.argument("pos", Vec3Argument.vec3())
                .suggests((context, builder) -> {
                  CobbleUtils.pokestopManager.getTypepokestop().forEach((key, value) -> builder.suggest(String.valueOf(value.getPos())));
                  return builder.buildFuture();
                })
                .executes(context -> {
                  teleport(context);
                  return 1;
                })
            )
        )
      )
    );


  }

  @Override
  public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    Player player = context.getSource().getPlayerOrException();
    String type = StringArgumentType.getString(context, "type");
    PokeStopUtils.createPokestop(type, player);
    player.sendSystemMessage(Component.literal("PokeStop added! " + type));
    return 1;
  }

  private static void edit(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    try {
      Player player = context.getSource().getPlayerOrException();
      Entity entity = EntityArgument.getEntity(context, "entity");
      String type = StringArgumentType.getString(context, "type");
      UUID pokestopuuid;
      if (entity instanceof PokemonEntity) {
        PokemonEntity pokemon = (PokemonEntity) entity;
        pokestopuuid = pokemon.getPokemon().getUuid();
        CobbleUtils.pokestopManager.getCooldownplayer().forEach((key, value) -> value.remove(pokestopuuid));
      } else {
        pokestopuuid = null;
      }
      if (pokestopuuid == null) {
        player.sendSystemMessage(Component.literal("PokeStop not found!"));
        return;
      }
      CobbleUtils.pokestopManager.getTypepokestop().get(pokestopuuid).setType(type);
      CobbleUtils.pokestopManager.writeInfo();
      player.sendSystemMessage(Component.literal("PokeStop edited! " + type));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void remove(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    Player player = context.getSource().getPlayerOrException();
    Entity entity = EntityArgument.getEntity(context, "entity");
    entity.remove(Entity.RemovalReason.KILLED);
    if (entity instanceof PokemonEntity) {
      PokemonEntity pokemon = (PokemonEntity) entity;
      UUID pokestopuuid = pokemon.getPokemon().getUuid();
      CobbleUtils.pokestopManager.getTypepokestop().remove(pokestopuuid);
      CobbleUtils.pokestopManager.getCooldownplayer().forEach((key, value) -> value.remove(pokestopuuid));
      CobbleUtils.pokestopManager.writeInfo();
      player.sendSystemMessage(Component.literal("PokeStop removed!"));
    }
  }

  private static void list(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    if (context.getSource().isPlayer()) {
      Player player = context.getSource().getPlayerOrException();
      player.sendSystemMessage(Component.literal("PokeStops:"));
      CobbleUtils.pokestopManager.getTypepokestop().forEach((key, value) -> {
        player.sendSystemMessage(Component.literal("Type: " + value.getType() + " UUID: " + key));
      });
    } else {

    }
  }

  private static void resetcooldowns(CommandContext<CommandSourceStack> context) {
    CobbleUtils.pokestopManager.getCooldownplayer().clear();
    CobbleUtils.pokestopManager.writeInfo();
  }

  private static void teleport(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
    Player player = context.getSource().getPlayerOrException();
    Vec3 pos = Vec3Argument.getVec3(context, "pos");
    player.teleportTo(pos.x(), pos.y(), pos.z());
  }
}
