package ua.zefir.servercosmetics.config;

import java.util.List;
import net.minecraft.item.Item;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public record ButtonConfig(
    Text name, Item baseItem, Identifier modelPath, int slotIndex, List<String> lore) {}
