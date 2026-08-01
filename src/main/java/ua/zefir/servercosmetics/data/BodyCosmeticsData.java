package ua.zefir.servercosmetics.data;

import net.minecraft.resources.Identifier;

public record BodyCosmeticsData(
    Identifier modelWhenSneaking,
    boolean mirrored,
    boolean autoAlignment,
    boolean offsetWhenSneaking,
    boolean autoscale)
    implements CosmeticData {}
