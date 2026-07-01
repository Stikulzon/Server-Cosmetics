package ua.zefir.servercosmetics.cosmetic;

import ua.zefir.servercosmetics.data.ItemType;
import ua.zefir.servercosmetics.data.SortMode;

public interface GuiStateHolder {
  String getGuiSelectedSlotKey();

  void setGuiSelectedSlotKey(String key);

  int getGuiCurrentPage();

  void setGuiCurrentPage(int page);

  SortMode getGuiSortMode();

  void setGuiSortMode(SortMode mode);

  ItemType getGuiTypeFilter();

  void setGuiTypeFilter(ItemType type);

  boolean isGuiAvailableOnly();

  void setGuiAvailableOnly(boolean availableOnly);

  void resetGuiState();
}
