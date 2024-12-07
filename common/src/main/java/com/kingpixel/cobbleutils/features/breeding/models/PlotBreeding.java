package com.kingpixel.cobbleutils.features.breeding.models;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.api.PermissionApi;
import com.kingpixel.cobbleutils.util.LuckPermsUtil;
import com.kingpixel.cobbleutils.util.RewardsUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.network.ServerPlayerEntity;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
public class PlotBreeding {
  private JsonObject male;
  private JsonObject female;
  private List<JsonObject> eggs;
  private long cooldown;

  public PlotBreeding() {
    male = null;
    female = null;
    eggs = new ArrayList<>();
    cooldown = applyCooldown(null); // Aplicar cooldown inicial sin jugador
  }

  // Convierte la instancia de PlotBreeding a un documento MongoDB
  public Document toDocument(String playerId) {
    Document doc = new Document();
    doc.append("playerId", playerId); // Identificador del jugador
    doc.append("cooldown", cooldown);

    // Convertimos male y female a JSON String si no están vacíos
    if (male != null) {
      doc.append("male", male.toString());  // No hace falta parsear ya que `male.toString()` ya es un JSON válido
    }
    if (female != null) {
      doc.append("female", female.toString());  // Lo mismo para `female`
    }

    // Convierte la lista de huevos a documentos JSON
    List<Document> eggDocs = new ArrayList<>();
    for (JsonObject egg : eggs) {
      try {
        // Asegurarse de que cada huevo es un objeto JSON válido
        eggDocs.add(Document.parse(egg.toString()));
      } catch (Exception e) {
        System.err.println("Error al convertir el huevo a documento JSON: " + e.getMessage());
      }
    }
    doc.append("eggs", eggDocs);

    return doc;
  }

  // Crea una instancia de PlotBreeding a partir de un documento MongoDB
  public static PlotBreeding fromDocument(Document doc) {
    PlotBreeding plot = new PlotBreeding();

    if (doc.containsKey("cooldown")) {
      plot.setCooldown(doc.getLong("cooldown"));
    }

    // Parse male y female como JsonObject usando JsonParser
    if (doc.containsKey("male")) {
      try {
        plot.setMale(JsonParser.parseString(doc.getString("male")).getAsJsonObject());
      } catch (Exception e) {
        System.err.println("Error al parsear 'male': " + e.getMessage());
      }
    }
    if (doc.containsKey("female")) {
      try {
        plot.setFemale(JsonParser.parseString(doc.getString("female")).getAsJsonObject());
      } catch (Exception e) {
        System.err.println("Error al parsear 'female': " + e.getMessage());
      }
    }

    // Procesa cada huevo en la lista de documentos y conviértelo a JsonObject
    if (doc.containsKey("eggs")) {
      List<Document> eggDocs = (List<Document>) doc.get("eggs");
      List<JsonObject> eggList = new ArrayList<>();
      for (Document eggDoc : eggDocs) {
        try {
          eggList.add(JsonParser.parseString(eggDoc.toJson()).getAsJsonObject());
        } catch (Exception e) {
          System.err.println("Error al parsear huevo JSON: " + e.getMessage());
        }
      }
      plot.setEggs(eggList);
    }

    return plot;
  }

  private long applyCooldown(ServerPlayerEntity player) {
    AtomicInteger cooldown = new AtomicInteger(CobbleUtils.breedconfig.getCooldown());
    CobbleUtils.breedconfig.getCooldowns().forEach((permission, time) -> {
      if (player != null && LuckPermsUtil.checkPermission(player, permission)) {
        if (time < cooldown.get()) {
          cooldown.set(time);
        }
      }
    });
    return TimeUnit.MINUTES.toMillis(cooldown.get()); // Devuelve solo el tiempo de cooldown en milisegundos
  }

  private Pokemon getPokemonMale() {
    if (male == null) return null;
    return Pokemon.Companion.loadFromJSON(male);
  }

  private Pokemon getPokemonFemale() {
    if (female == null) return null;
    return Pokemon.Companion.loadFromJSON(female);
  }

  public boolean checking(ServerPlayerEntity player) {
    if (!CobbleUtils.breedconfig.isActive()) return false;
    boolean banPokemon = false;
    if (CobbleUtils.breedconfig.getBlacklist().contains((male == null ? "null" : getPokemonMale().showdownId()))
      || CobbleUtils.breedconfig.getBlacklistForm().contains((male == null ? "null" : getPokemonMale().getForm().getName()))) {
      Cobblemon.INSTANCE.getStorage().getParty(player).add(getPokemonMale());
      male = null;
      banPokemon = true;
    }

    if (CobbleUtils.breedconfig.getBlacklist().contains((female == null ? "null" : getPokemonFemale().showdownId()))
      || CobbleUtils.breedconfig.getBlacklistForm().contains((female == null ? "null" :
      getPokemonFemale().getForm().getName()))) {
      Cobblemon.INSTANCE.getStorage().getParty(player).add(getPokemonFemale());
      female = null;
      banPokemon = true;
    }

    if (banPokemon) {
      return true;
    }

    long playerCooldownMillis = applyCooldown(player);

    long currentTime = new Date().getTime();
    long remainingCooldown = cooldown - currentTime;

    if (remainingCooldown > playerCooldownMillis) {
      cooldown = currentTime + playerCooldownMillis;
      return true;
    }

    if (male == null || female == null) {
      cooldown = currentTime + playerCooldownMillis;
      return true;
    }

    if (eggs == null) eggs = new ArrayList<>();

    if (cooldown < currentTime) {
      try {
        Pokemon pokemon = EggData.createEgg(getPokemonMale(), getPokemonFemale(), player, this);
        if (pokemon != null) {
          if (eggs.size() >= CobbleUtils.breedconfig.getMaxeggperplot())
            return true;

          cooldown = currentTime + playerCooldownMillis;

          if (PermissionApi.hasPermission(player, "cobbleutils.breeding.autoclaim", 4)) {
            RewardsUtils.saveRewardPokemon(player, pokemon);
          } else {
            eggs.add(pokemon.saveToJSON(new JsonObject()));
          }
          return true;
        }
      } catch (NoPokemonStoreException e) {
        e.printStackTrace();
      }
    }
    return true;
  }

  public boolean addMale(Pokemon pokemon) {
    if (pokemon.isLegendary() || pokemon.isUltraBeast())
      return false;
    setMale(pokemon.saveToJSON(new JsonObject()));
    return true;
  }

  public boolean addFemale(Pokemon pokemon) {
    if (pokemon.isLegendary() || pokemon.isUltraBeast())
      return false;
    setFemale(pokemon.saveToJSON(new JsonObject()));
    return true;
  }

  public Pokemon obtainMale() {
    return (male == null ? null : Pokemon.Companion.loadFromJSON(male));
  }

  public Pokemon obtainFemale() {
    return (female == null ? null : Pokemon.Companion.loadFromJSON(female));
  }

  public void add(Pokemon pokemon, Gender gender) {
    if (gender == Gender.MALE) {
      addMale(pokemon);
    } else if (gender == Gender.FEMALE) {
      addFemale(pokemon);
    }
  }

  public Pokemon obtainOtherGender(Gender gender) {
    if (gender == Gender.FEMALE) {
      return obtainMale();
    } else {
      return obtainFemale();
    }
  }

  public Pokemon getFirstEgg() {
    if (eggs.isEmpty()) return null;
    return Pokemon.Companion.loadFromJSON(eggs.get(0));
  }
}
