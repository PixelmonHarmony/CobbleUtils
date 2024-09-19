package com.kingpixel.cobbleutils.party.models;

import lombok.Data;

import java.util.UUID;

/**
 * @author Carlos Varas Alonso - 28/06/2024 3:31
 */
@Data
public class UserParty {
  private UUID partyId;

  public UserParty(UUID partyId) {
    this.partyId = partyId;
  }


}
