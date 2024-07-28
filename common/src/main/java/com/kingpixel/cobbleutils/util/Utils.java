package com.kingpixel.cobbleutils.util;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.ItemModel;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public abstract class Utils {
  public static final Random RANDOM = new Random();

  public static CompletableFuture<Boolean> writeFileAsync(String filePath, String filename, String data) {
    CompletableFuture<Boolean> future = new CompletableFuture<>();

    Path path = Paths.get(new File("").getAbsolutePath() + filePath, filename);
    File file = path.toFile();

    if (!Files.exists(path.getParent())) {
      file.getParentFile().mkdirs();
    }

    try (AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(
      path,
      StandardOpenOption.WRITE,
      StandardOpenOption.CREATE,
      StandardOpenOption.TRUNCATE_EXISTING)) {
      ByteBuffer buffer = ByteBuffer.wrap(data.getBytes(StandardCharsets.UTF_8));

      fileChannel.write(buffer, 0, buffer, new CompletionHandler<Integer, ByteBuffer>() {
        @Override
        public void completed(Integer result, ByteBuffer attachment) {
          attachment.clear();
          try {
            fileChannel.close();
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
          future.complete(true);
        }

        @Override
        public void failed(Throwable exc, ByteBuffer attachment) {
          future.complete(writeFileSync(file, data));
        }
      });
    } catch (IOException | SecurityException e) {
      CobbleUtils.LOGGER.fatal("Unable to write file asynchronously, attempting sync write.");
      future.complete(future.complete(false));
    }

    return future;
  }

  public static boolean writeFileSync(File file, String data) {
    try (FileWriter writer = new FileWriter(file)) {
      writer.write(data);
      return true;
    } catch (IOException e) {
      CobbleUtils.LOGGER.fatal("Unable to write to file for " + CobbleUtils.MOD_ID + ". " + e);
      return false;
    }
  }


  public static CompletableFuture<Boolean> readFileAsync(String filePath, String filename,
                                                         Consumer<String> callback) {
    CompletableFuture<Boolean> future = new CompletableFuture<>();
    ExecutorService executor = Executors.newSingleThreadExecutor();

    Path path = Paths.get(new File("").getAbsolutePath() + filePath, filename);
    File file = path.toFile();

    if (!file.exists()) {
      future.complete(false);
      executor.shutdown();
      return future;
    }

    try (AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.READ)) {
      ByteBuffer buffer = ByteBuffer.allocate((int) fileChannel.size());

      Future<Integer> readResult = fileChannel.read(buffer, 0);
      readResult.get();
      buffer.flip();

      byte[] bytes = new byte[buffer.remaining()];
      buffer.get(bytes);
      String fileContent = new String(bytes, StandardCharsets.UTF_8);

      callback.accept(fileContent);

      fileChannel.close();
      executor.shutdown();
      future.complete(true);
    } catch (Exception e) {
      future.complete(readFileSync(file, callback));
      executor.shutdown();
    }

    return future;
  }

  public static boolean readFileSync(File file, Consumer<String> callback) {
    try (Scanner reader = new Scanner(file)) {
      StringBuilder data = new StringBuilder();
      while (reader.hasNextLine()) {
        data.append(reader.nextLine());
      }
      callback.accept(data.toString());
      return true;
    } catch (IOException e) {
      CobbleUtils.LOGGER.fatal("Unable to read file " + file.getName() + " for " + CobbleUtils.MOD_ID + "." + e.getMessage());
      return false;
    }
  }


  public static Gson newGson() {
    return new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
  }

  public static Gson newWithoutSpacingGson() {
    return new GsonBuilder().disableHtmlEscaping().create();
  }

  public static void broadcastMessage(String message) {
    MinecraftServer server = CobbleUtils.server;
    ArrayList<ServerPlayer> players = new ArrayList<>(server.getPlayerList().getPlayers());
    for (ServerPlayer pl : players) {
      pl.sendSystemMessage(AdventureTranslator.toNative(message));
    }
  }

  public static void broadcastMessage(Component message) {
    MinecraftServer server = CobbleUtils.server;
    ArrayList<ServerPlayer> players = new ArrayList<>(server.getPlayerList().getPlayers());
    for (ServerPlayer pl : players) {
      pl.sendSystemMessage(message);
    }
  }

  public static ItemStack parseItemId(String id) {
    CompoundTag tag = new CompoundTag();
    tag.putString("id", id);
    tag.putInt("Count", 1);
    ItemStack itemStack = ItemStack.of(tag);
    itemStack.setTag(null);
    return itemStack;
  }

  public static ItemStack parseItemId(String id, int amount) {
    CompoundTag tag = new CompoundTag();
    tag.putString("id", id);
    tag.putInt("Count", amount);
    ItemStack itemStack = ItemStack.of(tag);
    itemStack.setTag(null);
    return itemStack;
  }

  public static File getAbsolutePath(String directoryPath) {
    return new File(Paths.get(new File("").getAbsolutePath()) + directoryPath);
  }

  public static void removeFiles(String directoryPath) {
    File directory = getAbsolutePath(directoryPath);
    if (directory.exists() && directory.isDirectory()) {
      File[] files = directory.listFiles();
      if (files != null) {
        for (File file : files) {
          if (file.isFile()) {
            file.delete();
          } else if (file.isDirectory()) {
            removeFiles(file.getAbsolutePath());
          }
        }
      }
    } else {
      CobbleUtils.LOGGER.info("Directory " + directoryPath + " does not exist or is not a directory.");
    }
  }

  public static UUID getUUID(String playername) {
    return CobbleUtils.server.getPlayerList().getPlayerByName(playername).getUUID();
  }

  public static Pokemon createPokemonParse(String pokemonName) {
    return PokemonProperties.Companion.parse(pokemonName).create();
  }

  public static ItemStack parseItemModel(ItemModel itemModel, int amount) {
    ItemStack itemStack = parseItemId(itemModel.getItem(), amount);
    return addThingsItemStack(itemStack, itemModel);
  }

  public static ItemStack addThingsItemStack(ItemStack itemStack, ItemModel itemModel) {
    if (itemModel.getNbt() != null && !itemModel.getNbt().isEmpty()) {
      try {
        itemStack.setTag(TagParser.parseTag(itemModel.getNbt()));
      } catch (CommandSyntaxException ignored) {
      }
    }
    itemStack.setHoverName(AdventureTranslator.toNativeWithOutPrefix(itemModel.getDisplayname() != null ? itemModel.getDisplayname() :
      "Please set a displayname for this item"));
    if (itemModel.getCustomModelData() != 0)
      itemStack.getOrCreateTag().putInt("CustomModelData", itemModel.getCustomModelData());
    if (itemModel.getLore() != null && !itemModel.getLore().isEmpty()) {
      ListTag nbtLore = new ListTag();
      List<Component> lorecomp = AdventureTranslator.toNativeL(itemModel.getLore());
      for (Component line : lorecomp) {
        if (lorecomp.size() == 1 && line.getString().isEmpty()) continue;
        MutableComponent result = Component.empty()
          .setStyle(Style.EMPTY.withItalic(false))
          .append(line);
        nbtLore.add(StringTag.valueOf(Component.Serializer.toJson(result)));
      }
      itemStack.getOrCreateTagElement("display").put("Lore", nbtLore);
    }
    return itemStack;
  }

  public static void createDirectoryIfNeeded(String directoryPath) {
    File directory = getAbsolutePath(directoryPath);
    if (!directory.exists()) {
      if (directory.mkdirs()) {
        CobbleUtils.LOGGER.info("Created directory: " + directoryPath);
      } else {
        CobbleUtils.LOGGER.error("Failed to create directory: " + directoryPath);
      }
    }
  }

  public static ItemStack getHead(String replace, int amount) {
    ItemStack itemStack = Items.PLAYER_HEAD.getDefaultInstance();
    itemStack.getOrCreateTag().putString("SkullOwner", replace);
    itemStack.setCount(amount);
    return itemStack;
  }
}