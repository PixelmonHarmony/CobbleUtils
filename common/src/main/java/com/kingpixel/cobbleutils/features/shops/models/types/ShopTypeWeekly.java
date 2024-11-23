package com.kingpixel.cobbleutils.features.shops.models.types;

import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.util.PlayerUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.server.network.ServerPlayerEntity;

import java.time.DayOfWeek;
import java.util.Date;
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

  @Override
  public boolean isAvailable(ServerPlayerEntity player) {
    boolean isAvailable = dayOfWeek.contains(DayOfWeek.from(new Date().toInstant()));
    if (!isAvailable) {
      PlayerUtils.sendMessage(
        player,
        "This shop is only available on the following days: " + dayOfWeek,
        CobbleUtils.language.getPrefixShop()
      );
    }
    return isAvailable;
  }
}
