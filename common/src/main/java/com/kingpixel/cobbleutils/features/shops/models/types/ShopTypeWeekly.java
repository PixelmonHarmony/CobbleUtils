package com.kingpixel.cobbleutils.features.shops.models.types;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.DayOfWeek;
import java.util.List;

/**
 * @author Carlos Varas Alonso - 27/08/2024 21:50
 */
@Getter
@Setter
@ToString
public class ShopTypeWeekly extends ShopType {
  private List<DayOfWeek> dayOfWeek;

  public ShopTypeWeekly() {
    super(TypeShop.WEEKLY);
    dayOfWeek = List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
  }

}
