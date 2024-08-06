package com.kingpixel.cobbleutils.properties;

/**
 * @author Carlos Varas Alonso - 04/08/2024 19:40
 */

import com.cobblemon.mod.common.api.properties.CustomPokemonPropertyType;
import com.kingpixel.cobbleutils.CobbleUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ScalePropertyType implements CustomPokemonPropertyType<ScaleProperty> {
  private static final ScalePropertyType INSTANCE = new ScalePropertyType();

  public ScalePropertyType() {
  }

  public static ScalePropertyType getInstance() {
    return INSTANCE;
  }

  @Override
  public @NotNull Set<String> getKeys() {
    return Collections.singleton("scale");
  }

  @Override
  public ScaleProperty fromString(String value) {
    return new ScaleProperty(value);
  }


  @NotNull @Override public Collection<String> examples() {
    Set<String> sizes = new HashSet<>();
    CobbleUtils.config.getPokemonsizes().forEach(sizeChance -> sizes.add(String.valueOf(sizeChance.getSize())));
    return sizes;
  }

  @Override public boolean getNeedsKey() {
    return true;
  }
}

