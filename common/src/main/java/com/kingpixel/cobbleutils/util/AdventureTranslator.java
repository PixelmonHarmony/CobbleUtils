package com.kingpixel.cobbleutils.util;

/*
 * This file is part of Impactor, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2018-2022 NickImpact
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

import com.kingpixel.cobbleutils.CobbleUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class AdventureTranslator {
  private static final MiniMessage miniMessage = MiniMessage.miniMessage();

  public static Text toNativeWithOutPrefix(String displayname) {
    return toNative(miniMessage.deserialize(replaceNative(displayname)));
  }

  public static Text toNative(String displayname) {
    return toNative(miniMessage.deserialize(replaceNative(displayname
      .replace("%prefix%", CobbleUtils.config.getPrefix())
      .replace("%partyprefix%", CobbleUtils.partyLang.getPrefix()))));
  }

  public static Text toNative(String displayname, String prefix) {
    return toNative(miniMessage.deserialize(replaceNative(displayname
      .replace("%prefix%", prefix)
      .replace("%partyprefix%", CobbleUtils.partyLang.getPrefix()))));
  }

  public static Text toNative(net.kyori.adventure.text.Component component) {
    return Text.Serializer.fromJson(GsonComponentSerializer.gson().serialize(component));
  }

  public static List<Text> toNativeL(List<String> lore) {
    List<net.kyori.adventure.text.Component> loreString = new ArrayList<>();
    for (String loreLine : lore) {
      loreString.add(miniMessage.deserialize(replaceNative(loreLine)));
    }
    return toNative(loreString);
  }

  public static List<Text> toNativeLWithOutPrefix(List<String> lore) {
    List<net.kyori.adventure.text.Component> loreString = new ArrayList<>();
    for (String loreLine : lore) {
      loreString.add(miniMessage.deserialize(replaceNative(loreLine)));
    }
    return toNativeLWithOut(loreString);
  }

  private static List<Text> toNativeLWithOut(List<net.kyori.adventure.text.Component> components) {
    List<Text> nativeComponents = new ArrayList<>();
    for (net.kyori.adventure.text.Component component : components) {
      nativeComponents.add(toNative(component));
    }
    return nativeComponents;
  }

  private static List<Text> toNative(List<net.kyori.adventure.text.Component> components) {
    List<Text> nativeComponents = new ArrayList<>();
    for (net.kyori.adventure.text.Component component : components) {
      nativeComponents.add(toNative(component));
    }
    return nativeComponents;
  }

  public static net.kyori.adventure.text.Component fromNative(Text component) {
    return GsonComponentSerializer.gson().deserialize(Text.Serializer.toJson(component));
  }

  public static net.kyori.adventure.text.Component toNativeFromString(String displayname) {
    return miniMessage.deserialize(replaceNative(displayname));
  }

  private static String replaceNative(String displayname) {
    if (displayname == null || displayname.isEmpty()) {
      return "";
    }
    displayname = displayname
      .replace("&", "§")
      .replace("§0", "<black>")
      .replace("§1", "<dark_blue>")
      .replace("§2", "<dark_green>")
      .replace("§3", "<dark_aqua>")
      .replace("§4", "<dark_red>")
      .replace("§5", "<dark_purple>")
      .replace("§6", "<gold>")
      .replace("§7", "<gray>")
      .replace("§8", "<dark_gray>")
      .replace("§9", "<blue>")
      .replace("§a", "<green>")
      .replace("§b", "<aqua>")
      .replace("§c", "<red>")
      .replace("§d", "<light_purple>")
      .replace("§e", "<yellow>")
      .replace("§f", "<white>")
      .replace("§k", "<obfuscated>")
      .replace("§l", "<bold>")
      .replace("§m", "<strikethrough>")
      .replace("§n", "<underline>")
      .replace("§o", "<italic>")
      .replace("§r", "<reset>");
    return displayname;
  }

  public static MutableText toNativeComponent(String messageContent) {
    return Text.empty().append(AdventureTranslator.toNative(messageContent));
  }
}