package ua.zefir.servercosmetics.data;

public record EquipmentSlotConfig(String key, int row, int col, ItemType type, boolean visible) {
  public int slotIndex() {
    return row * 9 + col;
  }
}
