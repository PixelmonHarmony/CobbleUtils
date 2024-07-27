package com.kingpixel.cobbleutils.util;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.button.ButtonAction;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.button.PlaceholderButton;
import ca.landonjw.gooeylibs2.api.button.linked.LinkType;
import ca.landonjw.gooeylibs2.api.button.linked.LinkedPageButton;
import ca.landonjw.gooeylibs2.api.helpers.PaginationHelper;
import ca.landonjw.gooeylibs2.api.page.GooeyPage;
import ca.landonjw.gooeylibs2.api.page.LinkedPage;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.moves.Move;
import com.cobblemon.mod.common.api.storage.NoPokemonStoreException;
import com.cobblemon.mod.common.api.storage.party.PlayerPartyStore;
import com.cobblemon.mod.common.api.storage.pc.PCStore;
import com.cobblemon.mod.common.item.PokemonItem;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.kingpixel.cobbleutils.CobbleUtils;
import com.kingpixel.cobbleutils.Model.ItemModel;
import com.kingpixel.cobbleutils.action.PokemonButtonAction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Carlos Varas Alonso - 28/06/2024 10:31
 */
public class UIUtils {
  /**
   * Create a button with the pokemon data
   *
   * @param pokemon       The pokemon to get the data
   * @param actionpokemon The action to do when the button is clicked
   *
   * @return The button with the pokemon data
   */
  public static GooeyButton createButtonPokemon(Pokemon pokemon, Consumer<PokemonButtonAction> actionpokemon) {
    if (pokemon == null) {
      return GooeyButton.builder()
        .display(CobbleUtils.language.getItemNoPokemon().getItemStack())
        .title(AdventureTranslator.toNative(CobbleUtils.language.getItemNoPokemon().getDisplayname()))
        .lore(Component.class, AdventureTranslator.toNativeL(CobbleUtils.language.getItemNoPokemon().getLore()))
        .build();
    }

    return GooeyButton.builder()
      .display(PokemonItem.from(pokemon, 1, new Vector4f(5, 5, 5, 5)))
      .title(AdventureTranslator.toNative(PokemonUtils.replace(CobbleUtils.language.getPokemonnameformat(), pokemon)))
      .lore(Component.class, AdventureTranslator.toNativeL(lorepokemon(pokemon)))
      .onClick(action -> actionpokemon.accept(new PokemonButtonAction(action, pokemon)))
      .build();
  }

  /**
   * Create a button with the pokemon data
   *
   * @param actionConfirm The action to do when the button is clicked
   * @param closeaction   The action to do when the button is clicked
   *
   * @return The button with the pokemon data
   */
  public static GooeyPage confirmMenu(Consumer<ButtonAction> actionConfirm, Consumer<ButtonAction> closeaction) {
    ChestTemplate template = ChestTemplate.builder(3).build();
    template.fill(GooeyButton.builder()
        .display(Utils.parseItemId(CobbleUtils.config.getFill())).build())
      .rectangle(0, 0, 2, 2, new PlaceholderButton())
      .set(1, 1, getConfirmButton(actionConfirm))
      .set(1, 2, getCancelButton(closeaction));
    return GooeyPage.builder()
      .template(template)
      .title(AdventureTranslator.toNative(CobbleUtils.language.getTitleconfirm()))
      .build();
  }

  /**
   * Create a button with the pokemon data
   *
   * @param pokemon       The pokemon to get the data
   * @param lore          The lore to replace
   * @param add           If add the default lore
   * @param actionpokemon The action to do when the button is clicked
   *
   * @return The button with the pokemon data
   */
  public static GooeyButton createButtonPokemon(Pokemon pokemon, List<String> lore, boolean add,
                                                Consumer<PokemonButtonAction> actionpokemon) {
    if (pokemon == null) {
      return GooeyButton.builder()
        .display(CobbleUtils.language.getItemNoPokemon().getItemStack())
        .title(AdventureTranslator.toNative(CobbleUtils.language.getItemNoPokemon().getDisplayname()))
        .lore(Component.class, AdventureTranslator.toNativeL(CobbleUtils.language.getItemNoPokemon().getLore()))
        .build();
    }

    return GooeyButton.builder()
      .display(PokemonItem.from(pokemon))
      .title(AdventureTranslator.toNative(PokemonUtils.replace(CobbleUtils.language.getPokemonnameformat(), pokemon)))
      .lore(Component.class, AdventureTranslator.toNativeL(lorepokemon(pokemon, lore, add)))
      .onClick(action -> actionpokemon.accept(new PokemonButtonAction(action, pokemon)))
      .build();
  }

  /**
   * Create a button with the pokemon data
   *
   * @param pokemon The pokemon to get the data
   * @param lore    The lore to replace
   * @param add     If add the default lore
   *
   * @return The button with the pokemon data
   */
  public static List<String> lorepokemon(Pokemon pokemon, List<String> lore, boolean add) {
    List<String> finalLore = new ArrayList<>();
    if (add) lore.addAll(new ArrayList<>(CobbleUtils.language.getLorepokemon()));
    for (String s : lore) {
      String replaced = PokemonUtils.replace(s, pokemon);
      for (int i = 0; i < 4; i++) {
        replaced = replaced.replace("%move" + (i + 1) + "%", move(pokemon.getMoveSet().get(i)));
      }

      finalLore.add(replaced);
    }
    return finalLore;
  }

  /**
   * Create a button with the pokemon data
   *
   * @param pokemon The pokemon to get the data
   * @param lore    The lore to replace
   *
   * @return The button with the pokemon data
   */
  public static List<String> lorepokemon(Pokemon pokemon, List<String> lore) {
    List<String> finalLore = new ArrayList<>();
    lore.addAll(new ArrayList<>(CobbleUtils.language.getLorepokemon()));
    for (String s : lore) {
      String replaced = PokemonUtils.replace(s, pokemon);
      for (int i = 0; i < 4; i++) {
        replaced = replaced.replace("%move" + (i + 1) + "%", move(pokemon.getMoveSet().get(i)));
      }

      finalLore.add(replaced);
    }
    return finalLore;
  }

  /**
   * Create a button with the pokemon data
   *
   * @param pokemon The pokemon to get the data
   *
   * @return The button with the pokemon data
   */
  public static List<String> lorepokemon(Pokemon pokemon) {
    List<String> lore = new ArrayList<>(CobbleUtils.language.getLorepokemon());
    List<String> finalLore = new ArrayList<>();
    for (String s : lore) {
      String replaced = PokemonUtils.replace(s, pokemon);
      for (int i = 0; i < 4; i++) {
        replaced = replaced.replace("%move" + (i + 1) + "%", move(pokemon.getMoveSet().get(i)));
      }

      finalLore.add(replaced);
    }
    return finalLore;
  }

  /**
   * Get the move name
   *
   * @param move The move to get the name
   *
   * @return The move name
   */
  private static String move(Move move) {
    if (move != null) {
      return PokemonUtils.getMoveTranslate(move);
    }
    return CobbleUtils.language.getNone();
  }

  /**
   * Create a button with the pokemon data
   *
   * @param command The command to execute
   * @param action  The action to do when the button is clicked
   *
   * @return The button with the pokemon data
   */
  public static GooeyButton createButtonCommand(String command, Consumer<ButtonAction> action) {
    AtomicReference<GooeyButton> button = new AtomicReference<>();
    CobbleUtils.config.getItemsCommands().forEach((c, i) -> {
      if (command.startsWith(c) && button.get() == null) {
        button.set(GooeyButton.builder()
          .display(i.getItemStack())
          .title(AdventureTranslator.toNative(i.getDisplayname()))
          .lore(Component.class, AdventureTranslator.toNativeL(i.getLore()))
          .onClick(action)
          .build());
      }
    });
    if (button.get() != null) return button.get();
    return GooeyButton.builder()
      .display(CobbleUtils.language.getItemCommand().getItemStack())
      .title(command)
      .lore(Component.class, AdventureTranslator.toNativeL(CobbleUtils.language.getItemCommand().getLore()))
      .onClick(action)
      .build();
  }

  /**
   * Create a button with the item data
   *
   * @param itemStack The item to get the data
   * @param action    The action to do when the button is clicked
   *
   * @return The button with the item data
   */
  public static GooeyButton createButtonItem(ItemStack itemStack, Consumer<ButtonAction> action) {
    Component title;
    ItemStack item = itemStack.copy();
    if (item.getTag() != null) {
      title = AdventureTranslator.toNative(item.getTag().getCompound("display").getString("Name"));
    } else {
      title = AdventureTranslator.toNative(CobbleUtils.language.getColoritem() + item.getHoverName().getString());
    }
    return GooeyButton.builder()
      .display(item)
      .title(title)
      .onClick(action)
      .build();
  }

  /**
   * Create a button with the item data
   *
   * @param pcStore       The pcStore to get the data
   * @param actionpokemon The action to do when the button is clicked
   * @param closeaction   The action to do when the button is clicked
   * @param titlemenu     The title of the menu
   *
   * @return The button with the item data
   *
   * @throws ExecutionException   If the computation threw an exception
   * @throws InterruptedException If the current thread was interrupted
   */
  public static GooeyPage createPagePc(PCStore pcStore,
                                       Consumer<PokemonButtonAction> actionpokemon,
                                       Consumer<ButtonAction> closeaction,
                                       String titlemenu) throws ExecutionException, InterruptedException {
    ChestTemplate template = ChestTemplate.builder(6).build();

    // Lista de futuros para almacenar los resultados de CompletableFuture
    List<CompletableFuture<Button>> buttonFutures = new ArrayList<>();

    // Crear CompletableFuture para cada Pokemon en pcStore
    for (Pokemon pokemon : pcStore) {
      CompletableFuture<Button> buttonFuture = CompletableFuture.supplyAsync(() ->
        UIUtils.createButtonPokemon(pokemon, action ->
          actionpokemon.accept(new PokemonButtonAction(action.getAction(), pokemon))
        )
      );
      buttonFutures.add(buttonFuture);
    }

    // Convertir lista de CompletableFuture a un CompletableFuture de lista de botones
    CompletableFuture<List<Button>> allButtonsFuture = CompletableFuture.allOf(
        buttonFutures.toArray(new CompletableFuture[0]))
      .thenApply(v ->
        buttonFutures.stream()
          .map(CompletableFuture::join)
          .collect(Collectors.toList())
      );

    // Obtener la lista de botones una vez completados todos los futuros
    return allButtonsFuture.thenApplyAsync(buttonsList -> {
      // Añadir los botones a la plantilla
      template.fill(GooeyButton.builder()
          .display(Utils.parseItemId("minecraft:gray_stained_glass_pane")
            .setHoverName(Component.literal(""))).build())
        .rectangle(0, 0, 5, 9, new PlaceholderButton())
        .fillFromList(buttonsList)
        .set(5, 4, getCloseButton(closeaction))
        .set(5, 0, getLinkedPageButton(CobbleUtils.language.getItemPrevious(), LinkType.Previous))
        .set(5, 8, getLinkedPageButton(CobbleUtils.language.getItemNext(), LinkType.Next));

      // Construir la página vinculada
      LinkedPage.Builder linkedPageBuilder = LinkedPage.builder().title(AdventureTranslator.toNative(titlemenu));
      return PaginationHelper.createPagesFromPlaceholders(template, buttonsList, linkedPageBuilder);
    }).get();
  }

  /**
   * Create a button with the item data
   *
   * @param partyStore    The partyStore to get the data
   * @param actionpokemon The action to do when the button is clicked
   * @param titlemenu     The title of the menu
   *
   * @return The button with the item data
   *
   * @throws ExecutionException   If the computation threw an exception
   * @throws InterruptedException If the current thread was interrupted
   */
  public static GooeyPage createPageParty(PlayerPartyStore partyStore,
                                          Consumer<PokemonButtonAction> actionpokemon,
                                          String titlemenu) throws ExecutionException, InterruptedException {
    ChestTemplate template = ChestTemplate.builder(4).build();

    List<CompletableFuture<Void>> slotFutures = new ArrayList<>();

    for (int i = 0; i < partyStore.size(); i++) {
      int slotIndex = i;
      CompletableFuture<Void> slotFuture = CompletableFuture.runAsync(() -> {
        GooeyButton slot;
        Pokemon pokemon = partyStore.get(slotIndex);
        slot = UIUtils.createButtonPokemon(pokemon, actionpokemon);
        int row = slotIndex / 3 + 1;
        int col = slotIndex % 3 + 3;
        template.set(row, col, slot);
      });
      slotFutures.add(slotFuture);
    }

    CompletableFuture<Void> allSlotsFuture = CompletableFuture.allOf(
      slotFutures.toArray(new CompletableFuture[0]));

    return allSlotsFuture.thenApplyAsync(v -> {
      template.fill(GooeyButton.builder()
        .display(new ItemStack(Items.GRAY_STAINED_GLASS_PANE)
          .setHoverName(AdventureTranslator.toNative(""))).build());
      return GooeyPage.builder()
        .template(template)
        .title(AdventureTranslator.toNative(CobbleUtils.language.getTitleparty()))
        .build();
    }).get();
  }

  /**
   * Create a button with the item data
   *
   * @param player        The player to get the data
   * @param actionpokemon The action to do when the button is clicked
   *
   * @return The button with the item data
   *
   * @throws ExecutionException   If the computation threw an exception
   * @throws InterruptedException If the current thread was interrupted
   */
  public static GooeyPage createPageParty(ServerPlayer player, Consumer<PokemonButtonAction> actionpokemon,
                                          Consumer<ButtonAction> actionclose) throws ExecutionException,
    InterruptedException {
    ChestTemplate template = ChestTemplate.builder(4).build();
    PlayerPartyStore partyStore = null;
    partyStore = Cobblemon.INSTANCE.getStorage().getParty(player);
    List<CompletableFuture<Void>> slotFutures = new ArrayList<>();

    for (int i = 0; i < partyStore.size(); i++) {
      int slotIndex = i;
      PlayerPartyStore finalPartyStore = partyStore;
      CompletableFuture<Void> slotFuture = CompletableFuture.runAsync(() -> {
        GooeyButton slot;
        Pokemon pokemon = finalPartyStore.get(slotIndex);
        slot = UIUtils.createButtonPokemon(pokemon, actionpokemon);
        int row = slotIndex / 3 + 1;
        int col = slotIndex % 3 + 3;
        template.set(row, col, slot);
      });
      slotFutures.add(slotFuture);
    }

    CompletableFuture<Void> allSlotsFuture = CompletableFuture.allOf(
      slotFutures.toArray(new CompletableFuture[0]));

    return allSlotsFuture.thenApplyAsync(v -> {
      template.fill(GooeyButton.builder()
        .display(new ItemStack(Items.GRAY_STAINED_GLASS_PANE)
          .setHoverName(AdventureTranslator.toNative(""))).build());
      template.set(0, 4, getPcButton(player, actionpokemon, actionclose));
      return GooeyPage.builder()
        .template(template)
        .title(AdventureTranslator.toNative(CobbleUtils.language.getTitleparty()))
        .build();
    }).get();
  }

  /**
   * Get the linked page button
   *
   * @param itemModel The item model to get the button
   * @param linkType  The type of link
   *
   * @return The linked page button
   */
  public static LinkedPageButton getLinkedPageButton(ItemModel itemModel, LinkType linkType) {
    return LinkedPageButton.builder()
      .display(itemModel.getItemStack())
      .title(AdventureTranslator.toNative(itemModel.getDisplayname()))
      .linkType(linkType)
      .build();
  }

  /**
   * Get the linked page button
   *
   * @param itemModel The item model to get the button
   * @param linkType  The type of link
   * @param action    The action to do when the button is clicked
   *
   * @return The linked page button
   */
  public static LinkedPageButton getLinkedPageButton(ItemModel itemModel, LinkType linkType, Consumer<ButtonAction> action) {
    return LinkedPageButton.builder()
      .display(itemModel.getItemStack())
      .title(AdventureTranslator.toNative(itemModel.getDisplayname()))
      .linkType(linkType)
      .onClick(action)
      .build();
  }

  /**
   * Get the close button
   *
   * @param action The action to do when the button is clicked
   *
   * @return The close button
   */
  public static GooeyButton getCloseButton(Consumer<ButtonAction> action) {
    ItemModel itemModel = CobbleUtils.language.getItemClose();
    return GooeyButton.builder()
      .display(itemModel.getItemStack())
      .title(AdventureTranslator.toNativeWithOutPrefix(itemModel.getDisplayname()))
      .onClick(action)
      .build();
  }

  /**
   * Get the pc button
   *
   * @param player        The player to get the data
   * @param actionpokemon The action to do when the button is clicked
   * @param closeaction   The action to do when the button is clicked
   *
   * @return The pc button
   */
  public static GooeyButton getPcButton(Player player, Consumer<PokemonButtonAction> actionpokemon,
                                        Consumer<ButtonAction> closeaction) {
    ItemModel itemModel = CobbleUtils.language.getItemPc();
    PCStore pcStore = null;
    try {
      pcStore = Cobblemon.INSTANCE.getStorage().getPC(player.getUUID());
    } catch (NoPokemonStoreException e) {
      e.printStackTrace();

    }
    PCStore finalPcStore = pcStore;
    return GooeyButton.builder()
      .display(itemModel.getItemStack())
      .title(AdventureTranslator.toNative(itemModel.getDisplayname()))
      .onClick(action -> {
        try {
          UIManager.openUIForcefully(action.getPlayer(), createPagePc(finalPcStore, actionpokemon,
            closeaction, CobbleUtils.language.getTitlepc()));
        } catch (ExecutionException | InterruptedException e) {
          e.printStackTrace();
        }
      })
      .build();
  }

  /**
   * Get the confirm button
   *
   * @param action The action to do when the button is clicked
   *
   * @return The confirm button
   */
  public static GooeyButton getConfirmButton(Consumer<ButtonAction> action) {
    ItemModel itemModel = CobbleUtils.language.getItemConfirm();
    return GooeyButton.builder()
      .display(itemModel.getItemStack())
      .title(AdventureTranslator.toNative(itemModel.getDisplayname()))
      .lore(Component.class, AdventureTranslator.toNativeLWithOutPrefix(itemModel.getLore()))
      .onClick(action)
      .build();
  }

  /**
   * Get the back button
   *
   * @param pokemon       The pokemon to get the data
   * @param actionPokemon The action to do when the button is clicked
   *
   * @return The confirm button
   */
  public static GooeyButton getConfirmButton(Pokemon pokemon, Consumer<PokemonButtonAction> actionPokemon) {
    ItemModel itemModel = CobbleUtils.language.getItemConfirm();
    return GooeyButton.builder()
      .display(itemModel.getItemStack())
      .title(AdventureTranslator.toNative(itemModel.getDisplayname()))
      .lore(Component.class, AdventureTranslator.toNativeLWithOutPrefix(itemModel.getLore()))
      .onClick(action -> actionPokemon.accept(new PokemonButtonAction(action, pokemon)))
      .build();
  }

  /**
   * Get the cancel button
   *
   * @param action The action to do when the button is clicked
   *
   * @return The cancel button
   */
  public static GooeyButton getCancelButton(Consumer<ButtonAction> action) {
    ItemModel itemModel = CobbleUtils.language.getItemCancel();
    return GooeyButton.builder()
      .display(itemModel.getItemStack())
      .title(AdventureTranslator.toNativeWithOutPrefix(itemModel.getDisplayname()))
      .onClick(action)
      .build();
  }

  /**
   * Get the back button
   *
   * @param pokemon       The pokemon to get the data
   * @param actionPokemon The action to do when the button is clicked
   *
   * @return The confirm button
   */
  public static GooeyButton getCancelButton(Pokemon pokemon, Consumer<PokemonButtonAction> actionPokemon) {
    ItemModel itemModel = CobbleUtils.language.getItemCancel();
    return GooeyButton.builder()
      .display(itemModel.getItemStack())
      .title(AdventureTranslator.toNativeWithOutPrefix(itemModel.getDisplayname()))
      .lore(Component.class, AdventureTranslator.toNativeLWithOutPrefix(itemModel.getLore()))
      .onClick(action -> actionPokemon.accept(new PokemonButtonAction(action, pokemon)))
      .build();
  }

  /**
   * Get the previous button
   *
   * @param action The action to do when the button is clicked
   *
   * @return The previous button
   */
  public static LinkedPageButton getPreviousButton(Consumer<ButtonAction> action) {
    ItemModel itemModel = CobbleUtils.language.getItemPrevious();
    return LinkedPageButton.builder()
      .display(itemModel.getItemStack())
      .title(AdventureTranslator.toNativeWithOutPrefix(itemModel.getDisplayname()))
      .linkType(LinkType.Previous)
      .onClick(action)
      .build();
  }

  /**
   * Get the next button
   *
   * @param action The action to do when the button is clicked
   *
   * @return The next button
   */
  public static LinkedPageButton getNextButton(Consumer<ButtonAction> action) {
    ItemModel itemModel = CobbleUtils.language.getItemNext();
    return LinkedPageButton.builder()
      .display(itemModel.getItemStack())
      .title(AdventureTranslator.toNativeWithOutPrefix(itemModel.getDisplayname()))
      .linkType(LinkType.Next)
      .onClick(action)
      .build();
  }


}


