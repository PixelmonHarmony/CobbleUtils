package com.kingpixel.cobbleutils.command.base.shops;

import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.config.ShopConfig;
import com.kingpixel.cobbleutils.features.shops.Shop;
import com.kingpixel.cobbleutils.features.shops.ShopConfigMenu;
import com.kingpixel.cobbleutils.features.shops.models.types.ShopType;
import com.kingpixel.cobbleutils.features.shops.models.types.ShopTypeDynamic;
import com.kingpixel.cobbleutils.features.shops.models.types.ShopTypeDynamicWeekly;
import com.kingpixel.cobbleutils.util.AdventureTranslator;
import com.kingpixel.cobbleutils.util.LuckPermsUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
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
 * @author Carlos Varas Alonso - 13/08/2024 18:53
 */
public class ShopCommand implements Command<ServerCommandSource> {
  public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                              LiteralArgumentBuilder<ServerCommandSource> base,
                              ShopConfig shopConfig, String mod_id, boolean api) {
    if (!api) {
      dispatcher.register(
        base
          .executes(context -> {
            if (!context.getSource().isExecutedByPlayer()) {
              CobbleUtils.LOGGER.error("This command can only be executed by a player");
              return 0;
            }
            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
            if (CobbleUtils.config.isDebug()) {
              CobbleUtils.LOGGER.info("Opening shop config menu");
              CobbleUtils.LOGGER.info("ShopConfig: " + shopConfig);
            }
            ShopConfigMenu.open(player, shopConfig, mod_id, false);
            return 0;
          })
          .then(
            CommandManager.literal("shops")
              .requires(
                source -> LuckPermsUtil.checkPermission(
                  source, 2, List.of(mod_id + ".admin", mod_id + ".shop.shops")
                )
              )
              .then(
                CommandManager.argument("shop", StringArgumentType.string())
                  .suggests((context, builder) -> {
                    ShopConfigMenu.getShopsMod(mod_id).forEach(shop -> {
                      if (context.getSource().isExecutedByPlayer()) {
                        if (LuckPermsUtil.checkPermission(
                          context.getSource(), 2, List.of(mod_id + ".admin", mod_id + ".shop." + shop.getId())
                        )) {
                          builder.suggest(shop.getId());
                        }
                      } else {
                        builder.suggest(shop.getId());
                      }
                    });
                    return builder.buildFuture();
                  })
                  .executes(context -> {
                    if (!context.getSource().isExecutedByPlayer()) {
                      CobbleUtils.LOGGER.error("This command can only be executed by a player");
                      return 0;
                    }
                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                    String shop = StringArgumentType.getString(context, "shop");
                    if (LuckPermsUtil.checkPermission(player, mod_id + ".shop." + shop)) {
                      shopConfig.getShop().open(player, shop, shopConfig, mod_id, false);
                      return 1;
                    } else {
                      player.sendMessage(
                        AdventureTranslator.toNative(
                          CobbleUtils.shopLang.getMessageNotHavePermission()
                            .replace("%prefix%", CobbleUtils.language.getPrefixShop())
                        )
                      );
                    }
                    return 0;
                  })
                  .then(
                    CommandManager.argument("player", EntityArgumentType.player())
                      .requires(source -> LuckPermsUtil.checkPermission(
                        source, 2, List.of(mod_id + ".admin", mod_id + ".shopother")
                      ))
                      .executes(context -> {
                        ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                        String shop = StringArgumentType.getString(context, "shop");
                        shopConfig.getShop().open(player, shop, shopConfig, mod_id, true);
                        /*if (LuckPermsUtil.checkPermission(player, mod_id + ".shop." + shop)) {
                          return 1;
                        } else {
                          player.sendMessage(
                            AdventureTranslator.toNative(
                              CobbleUtils.shopLang.getMessageNotHavePermission()
                                .replace("%prefix%", CobbleUtils.language.getPrefixShop())
                            )
                          );
                        }*/
                        return 1;
                      }).then(
                        CommandManager.argument("close", StringArgumentType.string())
                          .suggests((context, builder) -> {
                            builder.suggest("true");
                            builder.suggest("false");
                            return builder.buildFuture();
                          }).requires(source -> LuckPermsUtil.checkPermission(
                            source, 2, List.of(mod_id + ".admin", mod_id + ".shopother")
                          ))
                          .executes(context -> {
                            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                            String shop = StringArgumentType.getString(context, "shop");
                            boolean close = Boolean.parseBoolean(StringArgumentType.getString(context, "close"));
                            shopConfig.getShop().open(player, shop, shopConfig, mod_id, close);
                            return 1;
                          })
                      )
                  )
              )
          ).then(
            CommandManager.literal("reload")
              .requires(source -> LuckPermsUtil.checkPermission(
                source, 2, List.of(mod_id + ".admin", mod_id + ".shopreload")
              ))
              .executes(context -> {
                CobbleUtils.load();
                if (!context.getSource().isExecutedByPlayer()) {
                  CobbleUtils.LOGGER.info("Reloaded");
                } else {
                  context.getSource().getPlayer().sendMessage(
                    AdventureTranslator.toNative(
                      CobbleUtils.language.getMessageReload()
                        .replace("%prefix%", CobbleUtils.language.getPrefixShop())
                    )
                  );
                }
                return 1;
              })
          ).then(
            CommandManager.literal("resetDynamics")
              .requires(source -> LuckPermsUtil.checkPermission(
                source, 2, List.of(mod_id + ".admin", mod_id + ".shopresetdynamics"))
              )
              .then(
                CommandManager.argument("shop", StringArgumentType.string())
                  .suggests((context, builder) -> {
                    ShopConfigMenu.getShopsMod(mod_id).forEach(shop -> {
                      if (shop.getShopType().getTypeShop() == ShopType.TypeShop.DYNAMIC || shop.getShopType().getTypeShop() == ShopType.TypeShop.DYNAMIC_WEEKLY) {
                        if (context.getSource().isExecutedByPlayer()) {
                          if (LuckPermsUtil.checkPermission(
                            context.getSource(), 2, List.of(mod_id + ".admin", mod_id + ".shop." + shop.getId())
                          )) {
                            builder.suggest(shop.getId());
                          }
                        } else {
                          builder.suggest(shop.getId());
                        }
                      }
                    });
                    return builder.buildFuture();
                  })
                  .executes(context -> {
                    String s = StringArgumentType.getString(context, "shop");
                    Shop shop = ShopConfigMenu.getShop(s);
                    if (shop.getShopType() instanceof ShopTypeDynamic shopTypeDynamic) {
                      shopTypeDynamic.replenish(shop);
                    } else if (shop.getShopType() instanceof ShopTypeDynamicWeekly shopTypeDynamicWeekly) {
                      shopTypeDynamicWeekly.replenish(shop);
                    }
                    return 0;
                  })
              )

          )
      );
    } else {
      dispatcher.register(
        base
          .then(
            CommandManager.literal("shop")
              .executes(context -> {
                if (!context.getSource().isExecutedByPlayer()) {
                  CobbleUtils.LOGGER.error("This command can only be executed by a player");
                  return 0;
                }
                ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                if (CobbleUtils.config.isDebug()) {
                  CobbleUtils.LOGGER.info("Opening shop config menu");
                  CobbleUtils.LOGGER.info("ShopConfig: " + shopConfig);
                }
                ShopConfigMenu.open(player, shopConfig, mod_id, false);
                return 0;
              }).then(
                CommandManager.literal("shops")
                  .requires(
                    source -> LuckPermsUtil.checkPermission(
                      source, 2, List.of(mod_id + ".admin", mod_id + ".shop.shops")
                    )
                  )
                  .then(
                    CommandManager.argument("shop", StringArgumentType.string())
                      .suggests((context, builder) -> {
                        ShopConfigMenu.getShopsMod(mod_id).forEach(shop -> {
                          if (context.getSource().isExecutedByPlayer()) {
                            if (LuckPermsUtil.checkPermission(
                              context.getSource(), 2, List.of(mod_id + ".admin", mod_id + ".shop." + shop.getId())
                            )) {
                              builder.suggest(shop.getId());
                            }
                          } else {
                            builder.suggest(shop.getId());
                          }
                        });
                        return builder.buildFuture();
                      })
                      .executes(context -> {
                        if (!context.getSource().isExecutedByPlayer()) {
                          CobbleUtils.LOGGER.error("This command can only be executed by a player");
                          return 0;
                        }
                        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                        String shop = StringArgumentType.getString(context, "shop");
                        if (LuckPermsUtil.checkPermission(player, mod_id + ".shop." + shop)) {
                          shopConfig.getShop().open(player, shop, shopConfig, mod_id, false);
                          return 1;
                        } else {
                          player.sendMessage(
                            AdventureTranslator.toNative(
                              CobbleUtils.shopLang.getMessageNotHavePermission()
                                .replace("%prefix%", CobbleUtils.language.getPrefixShop())
                            )
                          );
                        }
                        return 0;
                      })
                      .then(
                        CommandManager.argument("player", EntityArgumentType.player())
                          .requires(source -> LuckPermsUtil.checkPermission(
                            source, 2, List.of(mod_id + ".admin", mod_id + ".shopother")
                          ))
                          .executes(context -> {
                            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                            String shop = StringArgumentType.getString(context, "shop");
                            if (LuckPermsUtil.checkPermission(player, mod_id + ".shop." + shop)) {
                              shopConfig.getShop().open(player, shop, shopConfig, mod_id, true);
                              return 1;
                            } else {
                              player.sendMessage(
                                AdventureTranslator.toNative(
                                  CobbleUtils.shopLang.getMessageNotHavePermission()
                                    .replace("%prefix%", CobbleUtils.language.getPrefixShop())
                                )
                              );
                            }
                            return 1;
                          })
                      )
                  )
              ).then(
                CommandManager.literal("reload")
                  .requires(source -> LuckPermsUtil.checkPermission(
                    source, 2, List.of(mod_id + ".admin", mod_id + ".shopreload")
                  ))
                  .executes(context -> {
                    CobbleUtils.load();
                    if (!context.getSource().isExecutedByPlayer()) {
                      CobbleUtils.LOGGER.info("Reloaded");
                    } else {
                      context.getSource().getPlayer().sendMessage(
                        AdventureTranslator.toNative(
                          CobbleUtils.language.getMessageReload()
                            .replace("%prefix%", CobbleUtils.language.getPrefixShop())
                        )
                      );
                    }
                    return 1;
                  })
              ).then(
                CommandManager.literal("resetDynamics")
                  .requires(source -> LuckPermsUtil.checkPermission(
                    source, 2, List.of(mod_id + ".admin", mod_id + ".shopresetdynamics"))
                  )
                  .then(
                    CommandManager.argument("shop", StringArgumentType.string())
                      .suggests((context, builder) -> {
                        ShopConfigMenu.getShopsMod(mod_id).forEach(shop -> {
                          if (shop.getShopType().getTypeShop() == ShopType.TypeShop.DYNAMIC || shop.getShopType().getTypeShop() == ShopType.TypeShop.DYNAMIC_WEEKLY) {
                            if (context.getSource().isExecutedByPlayer()) {
                              if (LuckPermsUtil.checkPermission(
                                context.getSource(), 2, List.of(mod_id + ".admin", mod_id + ".shop." + shop.getId())
                              )) {
                                builder.suggest(shop.getId());
                              }
                            } else {
                              builder.suggest(shop.getId());
                            }
                          }
                        });
                        return builder.buildFuture();
                      })
                      .executes(context -> {
                        String s = StringArgumentType.getString(context, "shop");
                        Shop shop = ShopConfigMenu.getShop(s);
                        if (shop.getShopType() instanceof ShopTypeDynamic shopTypeDynamic) {
                          shopTypeDynamic.replenish(shop);
                        } else if (shop.getShopType() instanceof ShopTypeDynamicWeekly shopTypeDynamicWeekly) {
                          shopTypeDynamicWeekly.replenish(shop);
                        }
                        return 0;
                      })
                  )

              )
          )

      );
    }
  }

  @Override public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
    return 0;
  }
}
