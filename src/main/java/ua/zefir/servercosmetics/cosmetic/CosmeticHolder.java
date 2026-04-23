package ua.zefir.servercosmetics.cosmetic;

import java.util.List;
import ua.zefir.servercosmetics.data.ItemType;

public interface CosmeticHolder {
  Cosmetic getCosmeticFor(ItemType type);

  void initCosmetics();

  void removeCosmetics();

  void tickArmor();

  List<Cosmetic> getCosmeticsList();
}
