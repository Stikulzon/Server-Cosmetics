package com.zefir.servercosmetics.database;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@DatabaseTable(tableName = "cosmetics")
public class CosmeticTable {
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
}
