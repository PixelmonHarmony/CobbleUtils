package com.kingpixel.cobbleutils.Model.shops.types;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Carlos Varas Alonso - 27/08/2024 21:49
 */
@Getter
@Setter
@ToString
public abstract class ShopType {
  protected TypeShop typeShop;

  public ShopType() {
    this.typeShop = TypeShop.PERMANENT;
  }

  public ShopType(TypeShop typeShop) {
    this.typeShop = typeShop;
  }

  public enum TypeShop {
    PERMANENT,  // Stores that always display all their items and are always available
    DYNAMIC,    // Stores where items change and a specific number of items are shown randomly
    WEEKLY      // Stores that are only available on certain days of the week
  }

}


