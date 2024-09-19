package com.kingpixel.cobbleutils.managers;

import com.kingpixel.cobbleutils.Model.PlayerInfo;
import com.kingpixel.cobbleutils.party.models.PartyCreateResult;
import com.kingpixel.cobbleutils.party.models.PartyData;
import com.kingpixel.cobbleutils.party.models.UserParty;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import lombok.Data;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author Carlos Varas Alonso - 28/06/2024 2:28
 */
@Data
public class PartyManager {
  // Map<PartyUUID, PartyData> / Info from the party
  private Map<UUID, PartyData> parties;
  // Map<UserUUID, PartyName> / Info from the user
  private Map<UUID, UserParty> userParty;

  public PartyManager() {
    this.parties = new HashMap<>();
    this.userParty = new HashMap<>();
  }


  public PartyData getParty(ServerPlayerEntity player) {
    UserParty infoUser = userParty.getOrDefault(player.getUuid(), null);
    return infoUser != null ? parties.get(infoUser.getPartyId()) : null;
  }

  public boolean isPlayerInParty(ServerPlayerEntity player) {
    return getParty(player) != null;
  }


  public PartyCreateResult createParty(ServerPlayerEntity player, String partyName) {
    PartyData partyData = getParty(player);
    if (partyData != null) return PartyCreateResult.ALREADY_IN_PARTY;
    partyData = new PartyData(partyName, PlayerInfo.fromPlayer(player));
    parties.put(partyData.getId(), partyData);
    userParty.put(player.getUuid(), new UserParty(partyData.getId()));
    return PartyCreateResult.SUCCESS;
  }

  public void sendInvite(ServerPlayerEntity player, ServerPlayerEntity invite) {
    PartyData partyData = getParty(player);
    if (partyData == null || !playerCanInvite(player)) return;
    partyData.getInvites().add(invite.getUuid());
  }

  public boolean leaveParty(ServerPlayerEntity player) {
    PartyData partyData = getParty(player);
    if (partyData == null) return false;
    partyData.getMembers().removeIf(playerInfo -> playerInfo.getPlayeruuid().equals(player.getUuid()));
    userParty.remove(player.getUuid());
    return true;
  }

  public boolean kickPlayer(ServerPlayerEntity player, ServerPlayerEntity target) {
    PartyData partyData = getParty(player);
    if (partyData == null) return false;
    if (!partyData.getOwner().getPlayeruuid().equals(player.getUuid())) return false;
    partyData.getMembers().removeIf(playerInfo -> playerInfo.getPlayeruuid().equals(target.getUuid()));
    userParty.remove(target.getUuid());
    return true;
  }

  public ArrayList<PlayerInfo> getMembers(ServerPlayerEntity player) {
    PartyData partyData = getParty(player);
    if (partyData == null) return new ArrayList<>();
    return new ArrayList<>(partyData.getMembers());
  }

  public boolean acceptInvite(ServerPlayerEntity player, UUID partyId) {
    PartyData partyData = parties.get(partyId);
    if (partyData == null) return false;
    if (!partyData.getInvites().remove(player.getUuid())) return false;
    partyData.getMembers().add(PlayerInfo.fromPlayer(player));
    userParty.put(player.getUuid(), new UserParty(partyData.getId()));
    parties.entrySet().stream()
      .filter(entry -> entry.getValue().getInvites().contains(player.getUuid()))
      .forEach(entry -> entry.getValue().getInvites().remove(player.getUuid()));
    return true;
  }

  public boolean playerCanInvite(ServerPlayerEntity player) {
    PartyData partyData = getParty(player);
    if (partyData == null) return false;
    return partyData.getOwner().getPlayeruuid().equals(player.getUuid());
  }

  public CompletableFuture<Suggestions> suggestParties(CommandContext<ServerCommandSource> serverCommandSourceCommandContext, SuggestionsBuilder suggestionsBuilder) {
    parties.values().forEach(partyData -> {
      if (partyData.getInvites().contains(serverCommandSourceCommandContext.getSource().getPlayer().getUuid())) {
        suggestionsBuilder.suggest(partyData.getName());
      }
    });
    return suggestionsBuilder.buildFuture();
  }

  public boolean isOwner(ServerPlayerEntity player) {
    PartyData partyData = getParty(player);
    if (partyData == null) return false;
    return partyData.getOwner().getPlayeruuid().equals(player.getUuid());
  }

  public ArrayList<PartyData> getInvites(ServerPlayerEntity player) {
    ArrayList<PartyData> invites = new ArrayList<>();
    parties.values().forEach(partyData -> {
      if (partyData.getInvites().contains(player.getUuid())) {
        invites.add(partyData);
      }
    });
    return invites;
  }
}
