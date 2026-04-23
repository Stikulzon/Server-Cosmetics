package ua.zefir.servercosmetics.database;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import java.util.Objects;

@DatabaseTable(tableName = "cosmetics")
public class CosmeticEntry {
  @DatabaseField(generatedId = true)
  private int id;

  @DatabaseField(uniqueCombo = true, columnName = "uuid")
  private String uuid;

  @DatabaseField(uniqueCombo = true, columnName = "cosmetic_type")
  private String cosmeticType;

  @DatabaseField(columnName = "cosmetic_id")
  private String cosmeticId;

  @DatabaseField(columnName = "dyed_color")
  private Integer dyedColor;

  public CosmeticEntry() {}

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

  public String getCosmeticType() {
    return cosmeticType;
  }

  public void setCosmeticType(String cosmeticType) {
    this.cosmeticType = cosmeticType;
  }

  public String getCosmeticId() {
    return cosmeticId;
  }

  public void setCosmeticId(String cosmeticId) {
    this.cosmeticId = cosmeticId;
  }

  public Integer getDyedColor() {
    return dyedColor;
  }

  public void setDyedColor(Integer dyedColor) {
    this.dyedColor = dyedColor;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CosmeticEntry that = (CosmeticEntry) o;
    return id == that.id
        && Objects.equals(uuid, that.uuid)
        && Objects.equals(cosmeticType, that.cosmeticType)
        && Objects.equals(cosmeticId, that.cosmeticId)
        && Objects.equals(dyedColor, that.dyedColor);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, uuid, cosmeticType, cosmeticId, dyedColor);
  }

  @Override
  public String toString() {
    return "CosmeticEntry{"
        + "id="
        + id
        + ", uuid='"
        + uuid
        + '\''
        + ", cosmeticType='"
        + cosmeticType
        + '\''
        + ", cosmeticId='"
        + cosmeticId
        + '\''
        + ", dyedColor="
        + dyedColor
        + '}';
  }
}
