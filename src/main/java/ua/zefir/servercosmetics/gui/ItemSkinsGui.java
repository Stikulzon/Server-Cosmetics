package ua.zefir.servercosmetics.gui;

import static ua.zefir.servercosmetics.config.ConfigManager.ITEM_SKINS_GUI_CONFIG;
import static ua.zefir.servercosmetics.datafixer.NbtDataFixer.NEW_NBT_KEY_CUSTOM_ITEM_ID;

import com.mojang.brigadier.context.CommandContext;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.SguiUtils;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import java.util.Objects;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import ua.zefir.servercosmetics.ModInit;
import ua.zefir.servercosmetics.config.FilterButtonPair;
import ua.zefir.servercosmetics.config.ItemSkinsGuiConfig;
import ua.zefir.servercosmetics.gui.actions.ApplySkinAction;
import ua.zefir.servercosmetics.gui.filters.PermissionFilter;
import ua.zefir.servercosmetics.gui.filters.SelectedItemFilter;
import ua.zefir.servercosmetics.gui.providers.ItemSkinProvider;
import ua.zefir.servercosmetics.util.GuiUtils;

public class ItemSkinsGui {
  public static int openItemSkinsGui(CommandContext<CommandSourceStack> ctx) {
    ServerPlayer player = ctx.getSource().getPlayer();
    if (player == null) {
      ctx.getSource()
          .sendSuccess(() -> Component.literal("This command can only be run by a player."), false);
      return 1;
    }

    try {
      ItemStack handStack = player.getMainHandItem();
      var config = ITEM_SKINS_GUI_CONFIG;
      int itemSlot = ((ItemSkinsGuiConfig) config).getItemSlot();
      var provider = new ItemSkinProvider();
      var action = new ApplySkinAction(handStack, itemSlot);
      PagedItemDisplayGui gui =
          new PagedItemDisplayGui(player, config, provider, action) {
            @Override
            public boolean onAnyClick(
                int idx, ClickType ct, net.minecraft.world.inventory.ContainerInput input) {
              if (idx >= this.getVirtualSize()) {
                ItemStack newClicked = this.player.containerMenu.getSlot(idx).getItem();
                if (!newClicked.isEmpty()) {
                  SguiUtils.sendPlayerInventory(this.player);
                  var newProvider = new ItemSkinProvider();
                  var newAction = new ApplySkinAction(newClicked, itemSlot);

                  setupDynamicSlots(this, newClicked);
                  this.reinitialize(newProvider, newAction);
                }
              }
              return super.onAnyClick(idx, ct, input);
            }
          };
      gui.getFilterManager()
          .addFilter(
              "permission",
              new PermissionFilter(player),
              new FilterButtonPair(
                  config.getButtonConfig("filter.show-owned-skins-enabled"),
                  config.getButtonConfig("filter.show-owned-skins-disabled")),
              false);
      gui.getFilterManager().addFilter("selected-item", new SelectedItemFilter(), null, true, true);

      setupDynamicSlots(gui, handStack);

      gui.reinitialize(provider, action);

      gui.setLockPlayerInventory(true);
      gui.open();
    } catch (Exception e) {
      ctx.getSource()
          .sendFailure(
              Component.literal(
                  "An error occurred opening the Item Skins GUI. See console for details."));
      ModInit.LOGGER.error(
          "Failed to open item skins GUI for player {}", player.getName().getString(), e);
    }
    return 0;
  }

  private static void setupDynamicSlots(PagedItemDisplayGui gui, ItemStack targetStack) {
    int itemSlot = ((ItemSkinsGuiConfig) gui.getGuiConfig()).getItemSlot();
    if (targetStack.getItem() == Items.AIR
        || targetStack.isEmpty()
        || targetStack.getItem() == null) {
      GuiUtils.setUpButton(gui, ITEM_SKINS_GUI_CONFIG.getButtonConfig("selectItem"), () -> {});

      ((SelectedItemFilter) (gui.getFilterManager().getFilter("selected-item").filter()))
          .setSelectedItem(null);
      return;
    }

    gui.setSlot(
        itemSlot,
        new GuiElementBuilder(targetStack.copy())
            .setCallback(() -> setupDynamicSlots(gui, ItemStack.EMPTY)));

    ((SelectedItemFilter) (gui.getFilterManager().getFilter("selected-item").filter()))
        .setSelectedItem(targetStack.getItem());

    GuiUtils.setUpButton(
        gui,
        ITEM_SKINS_GUI_CONFIG.getButtonConfig("removeSkin"),
        () -> {
          if (targetStack.get(DataComponents.CUSTOM_DATA) != null
              && Objects.requireNonNull(targetStack.get(DataComponents.CUSTOM_DATA))
                  .copyTag()
                  .contains(NEW_NBT_KEY_CUSTOM_ITEM_ID)) {
            targetStack.update(
                DataComponents.CUSTOM_DATA,
                CustomData.EMPTY,
                comp -> comp.update(nbt -> nbt.remove(NEW_NBT_KEY_CUSTOM_ITEM_ID)));
          }

          gui.setSlot(itemSlot, targetStack.copy());
        });
  }
}
