package ua.zefir.servercosmetics.ext;

import java.util.List;
import ua.zefir.servercosmetics.data.ItemType;

public interface ICosmetics {
  ICosmetic getCosmeticFor(ItemType type);

  void initCosmetics();

  void removeCosmetics();

  void tickArmor();

  List<ICosmetic> getCosmeticsList();
}
