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
  
  public static class Volume {
    private static boolean _allowVolumeInput = true;
    public static boolean isAllowVolumeInput() { return _allowVolumeInput; }
    private static boolean _volumeIsStrict = true;
    public static boolean isVolumeStrict() { return _volumeIsStrict; }
  }
  
  public static class PatchUsage {
    private static boolean _allowPatchUsageInput = true;
    public static boolean isAllowPatchUsageInput() { return _allowPatchUsageInput; }
    private static boolean _allowSinglePatchUsage = true;
    public static boolean isAllowSinglePatchUsage() { return _allowSinglePatchUsage; }
    private static boolean _allowRangePatchUsage = true;
    public static boolean isAllowRangePatchUsage() { return _allowRangePatchUsage; }
    private static boolean _allowWholePatchUsage = true;
    public static boolean isAllowWholePatchUsage() { return _allowWholePatchUsage; }
  }
  
  public static void match(boolean[] options) {
    _allowMIDIInput   = options[0];
    
    Volume._allowVolumeInput = options[1];
    Volume._volumeIsStrict   = options[2];
    
    PatchUsage._allowPatchUsageInput = options[3];
    PatchUsage._allowSinglePatchUsage = options[4];
    PatchUsage._allowRangePatchUsage = options[5];
    PatchUsage._allowWholePatchUsage = options[6];
  }
}
