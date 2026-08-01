package ua.zefir.servercosmetics.config;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

public record ButtonConfig(
    Component name, Item baseItem, Identifier modelPath, int slotIndex, List<String> lore) {}
