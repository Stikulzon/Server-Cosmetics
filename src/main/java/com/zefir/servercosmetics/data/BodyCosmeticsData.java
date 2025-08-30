package com.zefir.servercosmetics.data;

import eu.pb4.polymer.resourcepack.api.PolymerModelData;

public record BodyCosmeticsData(PolymerModelData polymerModelWhenSneaking, boolean mirrored) implements ICosmeticData {
}
