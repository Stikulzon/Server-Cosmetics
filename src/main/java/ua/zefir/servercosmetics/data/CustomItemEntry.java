package ua.zefir.servercosmetics.data;

import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public record CustomItemEntry(
    String id,
    String permission,
    Text displayName,
    List<Text> lore,
    ItemStack itemStack,
    ItemType type,
    String baseItemForModel,
    int sortingPriority,
    List<Tags> tags,
    ICosmeticData cosmeticData) {}
