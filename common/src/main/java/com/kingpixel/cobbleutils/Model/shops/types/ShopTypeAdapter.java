package com.kingpixel.cobbleutils.Model.shops.types;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

public class ShopTypeAdapter extends TypeAdapter<ShopType> {

  private static final String TYPE_FIELD = "typeShop";
  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

  @Override
  public void write(JsonWriter out, ShopType value) throws IOException {
    out.beginObject();
    out.name(TYPE_FIELD).value(value.typeShop.name());

    if (value instanceof ShopTypeDynamic dynamic) {
      out.name("minutes").value(dynamic.getMinutes());
      out.name("amountProducts").value(dynamic.getAmountProducts());
      // Escribe productos aquí, si es necesario
    } else if (value instanceof ShopTypeWeekly weekly) {
      out.name("dayOfWeek");
      out.beginArray();
      for (DayOfWeek day : weekly.getDayOfWeek()) {
        out.value(day.name());
      }
      out.endArray();
    } else if (value instanceof ShopTypeDynamicWeekly shopTypeDynamicWeekly) {
      out.name("minutes").value(shopTypeDynamicWeekly.getMinutes());
      out.name("amountProducts").value(shopTypeDynamicWeekly.getAmountProducts());
      out.name("dayOfWeek");
      out.beginArray();
      for (DayOfWeek day : shopTypeDynamicWeekly.getDayOfWeek()) {
        out.value(day.name());
      }
      out.endArray();
    }

    out.endObject();
  }

  @Override
  public ShopType read(JsonReader in) throws IOException {
    in.beginObject();
    ShopType.TypeShop typeShop = ShopType.TypeShop.PERMANENT; // Valor predeterminado
    int minutes = 60;
    int amountProducts = 10;
    List<DayOfWeek> dayOfWeek = new ArrayList<>();

    while (in.hasNext()) {
      String name = in.nextName();
      switch (name) {
        case TYPE_FIELD:
          typeShop = ShopType.TypeShop.valueOf(in.nextString());
          break;
        case "minutes":
          minutes = in.nextInt();
          break;
        case "amountProducts":
          amountProducts = in.nextInt();
          break;
        case "dayOfWeek":
          in.beginArray();
          while (in.hasNext()) {
            dayOfWeek.add(DayOfWeek.valueOf(in.nextString()));
          }
          in.endArray();
          break;
        default:
          in.skipValue();
          break;
      }
    }
    in.endObject();

    switch (typeShop) {
      case DYNAMIC:
        ShopTypeDynamic dynamic = new ShopTypeDynamic();
        dynamic.setMinutes(minutes);
        dynamic.setAmountProducts(amountProducts);
        return dynamic;
      case WEEKLY:
        ShopTypeWeekly weekly = new ShopTypeWeekly();
        weekly.setDayOfWeek(dayOfWeek);
        return weekly;
      case DYNAMIC_WEEKLY:
        ShopTypeDynamicWeekly dynamicWeekly = new ShopTypeDynamicWeekly();
        dynamicWeekly.setMinutes(minutes);
        dynamicWeekly.setAmountProducts(amountProducts);
        dynamicWeekly.setDayOfWeek(dayOfWeek);
        return dynamicWeekly;
      case PERMANENT:
      default:
        return new ShopTypePermanent();
    }
  }
}
