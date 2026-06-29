package ua.zefir.servercosmetics.database;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import java.util.Objects;

@DatabaseTable(tableName = "presets")
public class PresetEntry {
  @DatabaseField(generatedId = true)
  private int id;

  @DatabaseField(uniqueCombo = true, columnName = "uuid")
  private String uuid;

  @DatabaseField(uniqueCombo = true, columnName = "slot")
  private int slot;

  @DatabaseField(columnName = "cosmetic_ids")
  private String cosmeticIds;

  public PresetEntry() {}

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public int getSlot() {
    return slot;
  }

  public void setSlot(int slot) {
    this.slot = slot;
  }

  public String getCosmeticIds() {
    return cosmeticIds;
  }

  public void setCosmeticIds(String cosmeticIds) {
    this.cosmeticIds = cosmeticIds;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PresetEntry that = (PresetEntry) o;
    return id == that.id && Objects.equals(uuid, that.uuid) && slot == that.slot;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, uuid, slot);
  }
}
