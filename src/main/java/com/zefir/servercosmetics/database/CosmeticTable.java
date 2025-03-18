package com.zefir.servercosmetics.database;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@DatabaseTable(tableName = "cosmetics")
public class CosmeticTable {
    @DatabaseField(id = true)
    private String uuid;
    @DatabaseField
    private String itemId;
    @DatabaseField
    private Integer customModelData;
    @DatabaseField
    private Integer dyedColorComponent;
    @DatabaseField
    private String displayName;
}
