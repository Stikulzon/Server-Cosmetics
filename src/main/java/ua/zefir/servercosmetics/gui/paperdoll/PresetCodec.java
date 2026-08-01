package ua.zefir.servercosmetics.gui.paperdoll;

import java.util.Arrays;
import java.util.List;

public final class PresetCodec {

  private PresetCodec() {}

  public static String encode(List<String> cosmeticIds) {
    return String.join(",", cosmeticIds);
  }

  public static List<String> decode(String presetData) {
    return Arrays.asList(presetData.split(","));
  }
}
