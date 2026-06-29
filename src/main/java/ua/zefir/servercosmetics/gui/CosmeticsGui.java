package ua.zefir.servercosmetics.gui;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import ua.zefir.servercosmetics.ModInit;
import ua.zefir.servercosmetics.config.ConfigManager;
import ua.zefir.servercosmetics.config.CosmeticsGuiConfig;
import ua.zefir.servercosmetics.gui.paperdoll.PaperDollGui;

public class CosmeticsGui {

  public static int openGui(CommandContext<ServerCommandSource> ctx) {
    ServerPlayerEntity player = ctx.getSource().getPlayer();
    if (player == null) {
      ctx.getSource()
          .sendFeedback(() -> Text.literal("This command can only be run by a player."), false);
      return 1;
    }

    try {
      PaperDollGui gui =
          new PaperDollGui(player, (CosmeticsGuiConfig) ConfigManager.COSMETICS_GUI_CONFIG);
      gui.openAndPopulate();
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
