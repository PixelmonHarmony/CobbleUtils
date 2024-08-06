package com.kingpixel.cobbleutils.properties;

import com.cobblemon.mod.common.api.properties.CustomPokemonPropertyType;
import com.kingpixel.cobbleutils.CobbleUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Carlos Varas Alonso - 04/08/2024 19:40
 */
public class SizePropertyType implements CustomPokemonPropertyType<SizeProperty> {
  private static final SizePropertyType INSTANCE = new SizePropertyType();

  public SizePropertyType() {
  }

  public static SizePropertyType getInstance() {
    return INSTANCE;
  }

  @NotNull @Override public Iterable<String> getKeys() {
    return Collections.singleton("size");
  }


  @Nullable @Override public SizeProperty fromString(@Nullable String s) {
    return new SizeProperty(s);
  }

  @NotNull @Override public Collection<String> examples() {
    Set<String> sizes = new HashSet<>();
    CobbleUtils.config.getPokemonsizes().forEach(sizeChance -> sizes.add(sizeChance.getId()));
    sizes.add("aleatory");
    return sizes;
  }

  @Override public boolean getNeedsKey() {
    return true;
  }
}
