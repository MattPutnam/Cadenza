package cadenza.control.midiinput;

import cadenza.preferences.Preferences;

public class MIDIInputPreferences {
  private MIDIInputPreferences() {}
  
  static {
    try {
      match(Preferences.readMIDIInputOptions());
    } catch (Exception e) {
      System.err.println("Exception trying to load MIDI input preferences:");
      e.printStackTrace();
    }
  }
  
  private static boolean _allowMIDIInput = true;
  public static boolean isAllowMIDIInput() { return _allowMIDIInput; }
  
  private static boolean _allowVolumeInput = true;
  public static boolean isAllowVolumeInput() { return _allowVolumeInput; }
  private static boolean _volumeIsStrict = true;
  public static boolean isVolumeStrict() { return _volumeIsStrict; }
  
  public static void match(boolean[] options) {
    _allowMIDIInput   = options[0];
    _allowVolumeInput = options[1];
    _volumeIsStrict   = options[2];
  }
}
