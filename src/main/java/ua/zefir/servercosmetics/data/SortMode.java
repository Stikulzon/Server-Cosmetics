package ua.zefir.servercosmetics.data;

public enum SortMode {
  DEFAULT,
  NAME,
  RECENT;

  public SortMode next() {
    SortMode[] vals = values();
    return vals[(this.ordinal() + 1) % vals.length];
  }
}
