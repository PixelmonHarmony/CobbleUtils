package com.kingpixel.cobbleutils.properties;

import com.cobblemon.mod.common.api.properties.CustomPokemonPropertyType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * @author Carlos Varas Alonso - 04/08/2024 19:40
 */
public class SizePropertyType implements CustomPokemonPropertyType<SizeProperty> {
  @NotNull @Override public Iterable<String> getKeys() {
    return List.of("size");
  }

  @Override public boolean getNeedsKey() {
    return false;
  }

  @Nullable @Override public SizeProperty fromString(@Nullable String s) {
    return null;
  }

  @NotNull @Override public Collection<String> examples() {
    return List.of("size");
  }
}
