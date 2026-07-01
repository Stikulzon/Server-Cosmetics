package ua.zefir.servercosmetics.mixin;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ua.zefir.servercosmetics.cosmetic.ArmorCosmetic;
import ua.zefir.servercosmetics.cosmetic.BodyCosmetic;
import ua.zefir.servercosmetics.cosmetic.Cosmetic;
import ua.zefir.servercosmetics.cosmetic.CosmeticHolder;
import ua.zefir.servercosmetics.cosmetic.GuiStateHolder;
import ua.zefir.servercosmetics.data.ItemType;
import ua.zefir.servercosmetics.data.SortMode;
import ua.zefir.servercosmetics.util.Utils;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin_cosmetics implements CosmeticHolder, GuiStateHolder {
  @Unique private final List<Cosmetic> cosmeticsList = new ArrayList<>();

  @Unique private String guiSelectedSlotKey = null;
  @Unique private int guiCurrentPage = 0;
  @Unique private SortMode guiSortMode = SortMode.DEFAULT;
  @Unique private ItemType guiTypeFilter = null;
  @Unique private boolean guiAvailableOnly = true;

  @Inject(method = "<init>", at = @At("TAIL"))
  private void init(CallbackInfo ci) {
    ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

    cosmeticsList.addAll(
        List.of(
            new BodyCosmetic(player, ItemType.BODY_COSMETIC),
            new ArmorCosmetic(player, ItemType.HAT),
            new ArmorCosmetic(player, ItemType.CHESTPLATE),
            new ArmorCosmetic(player, ItemType.LEGGINGS),
            new ArmorCosmetic(player, ItemType.BOOTS)));
  }

  @Inject(method = "playerTick", at = @At("TAIL"))
  private void sendBackpackCosmeticPacket(CallbackInfo ci) {
    tickArmor();
  }

  @Inject(method = "copyFrom", at = @At("TAIL"))
  private void onRespawn(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
    ((CosmeticHolder) oldPlayer).removeCosmetics();

    this.initCosmetics();
  }

  @Inject(method = "worldChanged", at = @At("TAIL"))
  private void onDimensionChange(ServerWorld origin, CallbackInfo ci) {
    this.initCosmetics();
  }

  @Override
  public void tickArmor() {
    cosmeticsList.forEach(Cosmetic::tick);
  }

  @Override
  public void initCosmetics() {
    ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
    if (player.getEntityWorld() != null) {
      player.getEntityWorld().getServer().execute(() -> cosmeticsList.forEach(Cosmetic::init));
    } else {
      cosmeticsList.forEach(Cosmetic::init);
    }
  }

  @Override
  public void removeCosmetics() {
    cosmeticsList.forEach(Cosmetic::onUnload);
  }

  @Override
  public List<Cosmetic> getCosmeticsList() {
    return cosmeticsList;
  }

  @Override
  public Cosmetic getCosmeticFor(ItemType itemType) {

    for (Cosmetic cosmetic : cosmeticsList) {
      if (cosmetic.getItemType() == Utils.getRealEquipedItemType(itemType)) {
        return cosmetic;
      }
    }
    throw new IllegalArgumentException("No cosmetic found for the type: " + itemType);
  }

  @Override
  public String getGuiSelectedSlotKey() {
    return guiSelectedSlotKey;
  }

  @Override
  public void setGuiSelectedSlotKey(String key) {
    this.guiSelectedSlotKey = key;
  }

  @Override
  public int getGuiCurrentPage() {
    return guiCurrentPage;
  }

  @Override
  public void setGuiCurrentPage(int page) {
    this.guiCurrentPage = page;
  }

  @Override
  public SortMode getGuiSortMode() {
    return guiSortMode;
  }

  @Override
  public void setGuiSortMode(SortMode mode) {
    this.guiSortMode = mode;
  }

  @Override
  public ItemType getGuiTypeFilter() {
    return guiTypeFilter;
  }

  @Override
  public void setGuiTypeFilter(ItemType type) {
    this.guiTypeFilter = type;
  }

  @Override
  public boolean isGuiAvailableOnly() {
    return guiAvailableOnly;
  }

  @Override
  public void setGuiAvailableOnly(boolean availableOnly) {
    this.guiAvailableOnly = availableOnly;
  }

  @Override
  public void resetGuiState() {
    guiSelectedSlotKey = null;
    guiCurrentPage = 0;
    guiSortMode = SortMode.DEFAULT;
    guiTypeFilter = null;
    guiAvailableOnly = true;
  }
}
