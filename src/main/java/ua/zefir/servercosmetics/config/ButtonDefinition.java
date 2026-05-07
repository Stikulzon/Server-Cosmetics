package ua.zefir.servercosmetics.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record ButtonDefinition(
    String key, String name, String item, String textureName, int slotIndex, List<String> lore) {

  public Map<String, Object> toPropertyMap() {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("name", name);
    map.put("item", item);
    if (textureName != null && !textureName.isEmpty()) {
      map.put("textureName", textureName);
    }
    if (slotIndex >= 0) {
      map.put("slotIndex", slotIndex);
    }
    if (!lore.isEmpty()) {
      map.put("lore", lore);
    }
    return map;
  }
}
