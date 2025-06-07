package com.zefir.servercosmetics.config.entries;

import lombok.Getter;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.List;

@Getter
public record CustomItemEntry(String id, String permission, Text displayName, List<Text> lore, ItemStack itemStack,
                              ItemType type, String baseItemForModel) {
}
