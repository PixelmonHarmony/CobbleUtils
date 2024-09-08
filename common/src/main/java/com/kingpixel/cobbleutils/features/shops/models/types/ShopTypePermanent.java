package com.kingpixel.cobbleutils.features.shops.models.types;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Carlos Varas Alonso - 27/08/2024 21:50
 */
@Getter
@Setter
@ToString
public class ShopTypePermanent extends ShopType {
  public ShopTypePermanent() {
    super(TypeShop.PERMANENT);
  }
}
