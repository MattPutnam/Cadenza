package cadenza.core;

import java.util.HashMap;
import java.util.Map;

public class ControlNames {
  private static final Map<Integer, String> _names = new HashMap<>();
  static {
    _names.put(Integer.valueOf(0), "Bank Select");
    _names.put(Integer.valueOf(1), "Modulation Wheel");
    _names.put(Integer.valueOf(2), "Breath Controller");
    _names.put(Integer.valueOf(4), "Foot Controller");
    _names.put(Integer.valueOf(5), "Portamento Time");
    _names.put(Integer.valueOf(6), "Data Entry MSB");
    _names.put(Integer.valueOf(7), "Main Volume");
    _names.put(Integer.valueOf(8), "Balance");
    _names.put(Integer.valueOf(10), "Pan");
    _names.put(Integer.valueOf(11), "Expression");
    _names.put(Integer.valueOf(12), "Effect Control 1");
    _names.put(Integer.valueOf(13), "Effect Control 2");
    _names.put(Integer.valueOf(16), "General Purpose Controller 1");
    _names.put(Integer.valueOf(17), "General Purpose Controller 2");
    _names.put(Integer.valueOf(18), "General Purpose Controller 3");
    _names.put(Integer.valueOf(19), "General Purpose Controller 4");
    _names.put(Integer.valueOf(64), "Damper Pedal (Sustain)");
    _names.put(Integer.valueOf(65), "Portamento");
    _names.put(Integer.valueOf(66), "Sostenuto");
    _names.put(Integer.valueOf(67), "Soft Pedal");
    _names.put(Integer.valueOf(68), "Legato Footswitch");
    _names.put(Integer.valueOf(69), "Hold 2");
    _names.put(Integer.valueOf(70), "Sound Controller 1 (default: Sound Variation)");
    _names.put(Integer.valueOf(71), "Sound Controller 2 (default: Timbre/Harmonic Content)");
    _names.put(Integer.valueOf(72), "Sound Controller 3 (default: Release Time)");
    _names.put(Integer.valueOf(73), "Sound Controller 4 (default: Attack Time)");
    _names.put(Integer.valueOf(74), "Sound Controller 5 (default: Brightness)");
    _names.put(Integer.valueOf(75), "Sound Controller 6");
    _names.put(Integer.valueOf(76), "Sound Controller 7");
    _names.put(Integer.valueOf(77), "Sound Controller 8");
    _names.put(Integer.valueOf(78), "Sound Controller 9");
    _names.put(Integer.valueOf(79), "Sound Controller 10");
    _names.put(Integer.valueOf(80), "General Purpose Controller 5");
    _names.put(Integer.valueOf(81), "General Purpose Controller 6");
    _names.put(Integer.valueOf(82), "General Purpose Controller 7");
    _names.put(Integer.valueOf(83), "General Purpose Controller 8");
    _names.put(Integer.valueOf(84), "Portamento Control");
    _names.put(Integer.valueOf(91), "Effects 1 Depth (previously External Effects Depth)");
    _names.put(Integer.valueOf(92), "Effects 2 Depth (previously Tremolo Depth)");
    _names.put(Integer.valueOf(93), "Effects 3 Depth (previously Chorus Depth)");
    _names.put(Integer.valueOf(94), "Effects 4 Depth (previously Detune Depth)");
    _names.put(Integer.valueOf(95), "Effects 5 Depth (previously Phaser Depth)");
    _names.put(Integer.valueOf(96), "Data Increment");
    _names.put(Integer.valueOf(97), "Data Decrement");
    _names.put(Integer.valueOf(98), "Non-Registered Parameter Number LSB");
    _names.put(Integer.valueOf(99), "Non-Registered Parameter Number MSB");
    _names.put(Integer.valueOf(100), "Registered Parameter Number LSB");
    _names.put(Integer.valueOf(101), "Registered Parameter Number MSB");
    _names.put(Integer.valueOf(121), "Reset All Controllers");
    _names.put(Integer.valueOf(122), "Local Control");
    _names.put(Integer.valueOf(123), "All Notes Off");
    _names.put(Integer.valueOf(124), "Omni Off");
    _names.put(Integer.valueOf(125), "Omni On");
    _names.put(Integer.valueOf(126), "Mono On (Poly Off)");
    _names.put(Integer.valueOf(127), "Poly On (Mono Off)");
  }
  
  private ControlNames() {}
  
  public static String getName(Integer controlNumber) {
    final String val = _names.get(controlNumber);
    return val == null ? "[Undefined]" : val;
  }
  
  public static String getName(int controlNumber) {
    return getName(Integer.valueOf(controlNumber));
  }
}
