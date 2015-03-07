package cadenza.preferences;

import java.util.Map;

import cadenza.core.Keyboard;
import cadenza.core.Synthesizer;

import common.swing.SwingUtils;

public final class Preferences {
  private Preferences() {}
  
  public static void init() {
    final Map<String, String> prefs;
    try {
      SwingUtils.throwIfEventThread();
      prefs = PreferencesLoader.readAllPreferences();
    } catch (Exception e) {
      System.err.println("Exception trying to load preferences: ");
      e.printStackTrace();
      return;
    }
    
    _defaultKeyboard = PreferencesLoader.buildDefaultKeyboard(prefs);
    _defaultSynthesizer = PreferencesLoader.buildDefaultSynthesizer(prefs);
    _defaultMIDIPorts = PreferencesLoader.buildDefaultMIDIPorts(prefs);
    
    matchMIDIInputOptions(PreferencesLoader.buildMIDIInputOptions(prefs));
  }
  
  static Keyboard _defaultKeyboard;
  public static Keyboard getDefaultKeyboard() { return _defaultKeyboard; }
  
  static Synthesizer _defaultSynthesizer;
  public static Synthesizer getDefaultSynthesizer() { return _defaultSynthesizer; }
  
  static String[] _defaultMIDIPorts;
  public static String[] getDefaultMIDIPorts() { return _defaultMIDIPorts; }
  
  //////////////////////
  // MIDI Input block
  private static boolean _allowMIDIInput = true;
  public static boolean allowMIDIInput() { return _allowMIDIInput; }
  
  private static boolean _allowVolumeInput = true;
  public static boolean allowVolumeInput() { return _allowVolumeInput; }
  private static boolean _volumeIsStrict = true;
  public static boolean isVolumeStrict() { return _volumeIsStrict; }

  private static boolean _allowPatchUsageInput = true;
  public static boolean allowPatchUsageInput() { return _allowPatchUsageInput; }
  private static boolean _allowSinglePatchUsage = true;
  public static boolean allowSinglePatchUsage() { return _allowSinglePatchUsage; }
  private static boolean _allowRangePatchUsage = true;
  public static boolean allowRangePatchUsage() { return _allowRangePatchUsage; }
  private static boolean _allowWholePatchUsage = true;
  public static boolean allowWholePatchUsage() { return _allowWholePatchUsage; }
  
  static void matchMIDIInputOptions(boolean[] options) {
    _allowMIDIInput = options[0];
    
    _allowVolumeInput = options[1];
    _volumeIsStrict   = options[2];
    
    _allowPatchUsageInput  = options[3];
    _allowSinglePatchUsage = options[4];
    _allowRangePatchUsage  = options[5];
    _allowWholePatchUsage  = options[6];
  }
  // End MIDI Input block
  ///////////////////////
  
  ///////////////////////
  // Patch search block
  public static final int SIMPLE = 0;
  public static final int PIPES = 1;
  public static final int REGEX = 2;
  
  static int _patchSearchMode = PIPES;
  public static int getPatchSearchMode() { return _patchSearchMode; }
}
