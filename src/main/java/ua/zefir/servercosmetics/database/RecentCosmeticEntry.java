package ua.zefir.servercosmetics.database;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import java.util.Objects;

@DatabaseTable(tableName = "recent_cosmetics")
public class RecentCosmeticEntry {
  @DatabaseField(generatedId = true)
  private int id;

  @DatabaseField(uniqueCombo = true, columnName = "uuid")
  private String uuid;

  @DatabaseField(uniqueCombo = true, columnName = "cosmetic_id")
  private String cosmeticId;

  @DatabaseField(columnName = "last_used")
  private long lastUsed;

  public RecentCosmeticEntry() {}

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

  public String getCosmeticId() {
    return cosmeticId;
  }

  public void setCosmeticId(String cosmeticId) {
    this.cosmeticId = cosmeticId;
  }

  public long getLastUsed() {
    return lastUsed;
  }

  public void setLastUsed(long lastUsed) {
    this.lastUsed = lastUsed;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RecentCosmeticEntry that = (RecentCosmeticEntry) o;
    return id == that.id
        && lastUsed == that.lastUsed
        && Objects.equals(uuid, that.uuid)
        && Objects.equals(cosmeticId, that.cosmeticId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, uuid, cosmeticId, lastUsed);
  }

  @Override
  public String toString() {
    return "RecentCosmeticEntry{"
        + "id="
        + id
        + ", uuid='"
        + uuid
        + '\''
        + ", cosmeticId='"
        + cosmeticId
        + '\''
        + ", lastUsed="
        + lastUsed
        + '}';
  }
}
