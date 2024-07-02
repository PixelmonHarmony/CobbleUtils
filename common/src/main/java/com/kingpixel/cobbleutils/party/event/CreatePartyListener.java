package com.kingpixel.cobbleutils.party.event;

import com.kingpixel.cobbleutils.party.models.PartyData;

/**
 * @author Carlos Varas Alonso - 28/06/2024 8:45
 */
public interface CreatePartyListener {
  void onCreateParty(PartyData partyData);
}
