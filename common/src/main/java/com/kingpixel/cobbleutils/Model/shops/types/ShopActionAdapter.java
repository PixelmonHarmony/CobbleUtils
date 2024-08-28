package com.kingpixel.cobbleutils.Model.shops.types;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.kingpixel.cobbleutils.Model.shops.ShopTransactions;

import java.io.IOException;

public class ShopActionAdapter extends TypeAdapter<ShopTransactions.ShopAction> {
  @Override
  public void write(JsonWriter out, ShopTransactions.ShopAction value) throws IOException {
    out.value(value.name());
  }

  @Override
  public ShopTransactions.ShopAction read(JsonReader in) throws IOException {
    return ShopTransactions.ShopAction.valueOf(in.nextString());
  }
}



