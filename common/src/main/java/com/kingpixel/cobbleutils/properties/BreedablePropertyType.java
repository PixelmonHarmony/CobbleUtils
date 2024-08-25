package com.kingpixel.cobbleutils.properties;

import com.cobblemon.mod.common.api.properties.CustomPokemonPropertyType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * @author Carlos Varas Alonso - 04/08/2024 19:40
 */
public class BreedablePropertyType implements CustomPokemonPropertyType<PokerusProperty> {
  public static final BreedablePropertyType INSTANCE = new BreedablePropertyType();

  public BreedablePropertyType() {
  }

  public static BreedablePropertyType getInstance() {
    return INSTANCE;
  }

  @NotNull @Override public Iterable<String> getKeys() {
    return Collections.singleton("breedable");
  }

  @Override public boolean getNeedsKey() {
    return true;
  }

  @Nullable @Override public PokerusProperty fromString(@Nullable String s) {
    return new PokerusProperty(Boolean.parseBoolean(s));
  }

  @NotNull @Override public Collection<String> examples() {
    return Set.of("true", "false");
  }
}
