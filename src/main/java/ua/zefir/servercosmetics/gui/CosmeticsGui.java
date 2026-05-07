package ua.zefir.servercosmetics.gui;

import static ua.zefir.servercosmetics.config.ConfigManager.COSMETICS_GUI_CONFIG;

import com.mojang.brigadier.context.CommandContext;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import ua.zefir.servercosmetics.ModInit;
import ua.zefir.servercosmetics.config.FilterButtonPair;
import ua.zefir.servercosmetics.data.ItemType;
import ua.zefir.servercosmetics.gui.actions.EquipCosmeticAction;
import ua.zefir.servercosmetics.gui.filters.ItemTypeFilter;
import ua.zefir.servercosmetics.gui.filters.PermissionFilter;
import ua.zefir.servercosmetics.gui.providers.StandaloneCosmeticProvider;
import ua.zefir.servercosmetics.util.GuiUtils;

public class CosmeticsGui {

  public static int openGui(CommandContext<ServerCommandSource> ctx) {
    ServerPlayerEntity player = ctx.getSource().getPlayer();
    if (player == null) {
      ctx.getSource()
          .sendFeedback(() -> Text.literal("This command can only be run by a player."), false);
      return 1;
    }

    try {
      var config = COSMETICS_GUI_CONFIG;
      var provider = new StandaloneCosmeticProvider();
      var action = new EquipCosmeticAction();

      PagedItemDisplayGui gui = new PagedItemDisplayGui(player, config, provider, action);

      gui.getFilterManager()
          .addFilter(
              "permission",
              new PermissionFilter(player),
              new FilterButtonPair(
                  config.getButtonConfig("filter.show-owned-skins-enabled"),
                  config.getButtonConfig("filter.show-owned-skins-disabled")),
              false);

      gui.getFilterManager()
          .addFilter(
              "hat",
              new ItemTypeFilter(List.of(ItemType.HAT)),
              new FilterButtonPair(
                  config.getButtonConfig("filter.hats-enabled"),
                  config.getButtonConfig("filter.hats-disabled")),
              true);

      gui.getFilterManager()
          .addFilter(
              "body-cosmetic",
              new ItemTypeFilter(List.of(ItemType.BODY_COSMETIC)),
              new FilterButtonPair(
                  config.getButtonConfig("filter.body-cosmetics-enabled"),
                  config.getButtonConfig("filter.body-cosmetics-disabled")),
              false);
      gui.getFilterManager()
          .addFilter(
              "armor",
              new ItemTypeFilter(
                  List.of(
                      ItemType.HELMET,
                      ItemType.CHESTPLATE,
                      ItemType.LEGGINGS,
                      ItemType.BOOTS,
                      ItemType.HAT_BODY_COSMETIC,
                      ItemType.CHESTPLATE_BODY_COSMETIC,
                      ItemType.LEGGINGS_BODY_COSMETIC,
                      ItemType.BOOTS_BODY_COSMETIC)),
              new FilterButtonPair(
                  config.getButtonConfig("filter.armor-cosmetics-enabled"),
                  config.getButtonConfig("filter.armor-cosmetics-disabled")),
              false);

      GuiUtils.setUpButton(
          gui,
          config.getButtonConfig("removeSkin"),
          () -> {
            gui.close();
            for (ItemType type : gui.getFilterManager().getTargetTypes()) {
              action.execute(player, ItemStack.EMPTY, type);
            }
          });

      gui.reinitialize(provider, action);
      gui.open();

    } catch (Exception e) {
      ctx.getSource()
          .sendError(
              Text.literal(
                  "An error occurred opening the Cosmetics GUI. See console for details."));
      ModInit.LOGGER.error(
          "Failed to open cosmetics GUI for player {}", player.getName().getString(), e);
    }
    return 0;
  }
}
