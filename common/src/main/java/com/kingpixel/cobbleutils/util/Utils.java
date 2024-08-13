package com.kingpixel.cobbleutils.util;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.ItemModel;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

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
      CobbleUtils.LOGGER
        .fatal("Unable to read file " + file.getName() + " for " + CobbleUtils.MOD_ID + "." + e.getMessage());
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
    ArrayList<ServerPlayerEntity> players = new ArrayList<>(server.getPlayerManager().getPlayerList());
    for (ServerPlayerEntity pl : players) {
      pl.sendMessage(AdventureTranslator.toNative(message));
    }
  }

  public static void broadcastMessage(Text message) {
    MinecraftServer server = CobbleUtils.server;
    ArrayList<ServerPlayerEntity> players = new ArrayList<>(server.getPlayerManager().getPlayerList());
    for (ServerPlayerEntity pl : players) {
      pl.sendMessage(message);
    }
  }

  public static ItemStack parseItemId(String id) {
    NbtCompound tag = new NbtCompound();
    tag.putString("id", id);
    tag.putInt("Count", 1);
    ItemStack itemStack = ItemStack.fromNbt(tag);
    itemStack.setNbt(null);
    return itemStack;
  }

  public static ItemStack parseItemId(String id, int amount) {
    NbtCompound tag = new NbtCompound();
    tag.putString("id", id);
    tag.putInt("Count", amount);
    ItemStack itemStack = ItemStack.fromNbt(tag);
    itemStack.setNbt(null);
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
    return CobbleUtils.server.getPlayerManager().getPlayer(playername).getUuid();
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
        itemStack.setNbt(NbtHelper.fromNbtProviderString(itemModel.getNbt()));
      } catch (CommandSyntaxException ignored) {
      }
    }
    itemStack.setCustomName(AdventureTranslator.toNativeWithOutPrefix(
      itemModel.getDisplayname() != null ? itemModel.getDisplayname() : "Please set a displayname for this item"));
    if (itemModel.getCustomModelData() != 0)
      itemStack.getOrCreateNbt().putLong("CustomModelData", itemModel.getCustomModelData());
    if (itemModel.getLore() != null && !itemModel.getLore().isEmpty()) {
      NbtList nbtLore = new NbtList();
      List<Text> lorecomp = AdventureTranslator.toNativeL(itemModel.getLore());
      for (Text line : lorecomp) {
        if (lorecomp.size() == 1 && line.getString().isEmpty())
          continue;
        Text result = Text.empty()
          .setStyle(Style.EMPTY.withItalic(false))
          .append(line);
        nbtLore.add(NbtString.of(Text.Serializer.toJson(result)));
      }
      itemStack.getOrCreateSubNbt("display").put("Lore", nbtLore);
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
    ItemStack itemStack = Items.PLAYER_HEAD.getDefaultStack();
    itemStack.getOrCreateNbt().putString("SkullOwner", replace);
    itemStack.setCount(amount);
    return itemStack;
  }

  public static ItemStack parseItemId(String item, int amount, long customModelData) {
    ItemStack itemStack = parseItemId(item, amount);
    if (customModelData != 0) itemStack.getOrCreateNbt().putLong("CustomModelData", customModelData);
    return itemStack;
  }
}