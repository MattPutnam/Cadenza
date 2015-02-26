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
  
  public static void match(boolean[] options) {
    _allowMIDIInput = options[0];
  }
}
