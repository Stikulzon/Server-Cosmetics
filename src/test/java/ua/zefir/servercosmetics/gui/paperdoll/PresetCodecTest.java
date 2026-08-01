package ua.zefir.servercosmetics.gui.paperdoll;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class PresetCodecTest {

  @Test
  void encodesIdsByConfiguredPosition() {
    assertEquals("hat,,boots,", PresetCodec.encode(List.of("hat", "", "boots", "")));
  }

  @Test
  void preservesInternalEmptyPositionsWhenDecoding() {
    assertEquals(List.of("hat", "", "boots"), PresetCodec.decode("hat,,boots"));
  }

  @Test
  void dropsTrailingEmptyPositionsWhenDecoding() {
    assertEquals(List.of("hat"), PresetCodec.decode("hat,,"));
    assertEquals(List.of(), PresetCodec.decode(",,"));
  }

  @Test
  void preservesWhitespaceForCallersToTrim() {
    assertEquals(List.of(" hat ", " boots "), PresetCodec.decode(" hat , boots "));
  }
}
