package cadenza.preferences;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cadenza.core.Keyboard;
import cadenza.core.Synthesizer;

import common.swing.SwingUtils;

public final class Preferences {
  private static final Logger LOG = LogManager.getLogger(Preferences.class);
  
  private Preferences() {}
  
  public static void init() {
    final Map<String, String> prefs;
    try {
      SwingUtils.throwIfEventThread();
      prefs = PreferencesLoader.readAllPreferences();
    } catch (Exception e) {
      LOG.fatal("Exception trying to load preferences", e);
      return;
    }
    
    _defaultKeyboard = PreferencesLoader.buildDefaultKeyboard(prefs);
    _defaultSynthesizer = PreferencesLoader.buildDefaultSynthesizer(prefs);
    _defaultMIDIPorts = PreferencesLoader.buildDefaultMIDIPorts(prefs);
    _midiInputOptions = PreferencesLoader.buildMIDIInputOptions(prefs);
    _patchSearchOptions = PreferencesLoader.buildPatchSearchOptions(prefs);
  }
  
  static Keyboard _defaultKeyboard;
  public static Keyboard getDefaultKeyboard() { return _defaultKeyboard; }
  
  static Synthesizer _defaultSynthesizer;
  public static Synthesizer getDefaultSynthesizer() { return _defaultSynthesizer; }
  
  static String[] _defaultMIDIPorts;
  public static String[] getDefaultMIDIPorts() { return _defaultMIDIPorts; }
  
  static MIDIInputOptions _midiInputOptions;
  public static MIDIInputOptions getMIDIInputOptions() { return _midiInputOptions; }
  
  static PatchSearchOptions _patchSearchOptions;
  public static PatchSearchOptions getPatchSearchOptions() { return _patchSearchOptions; }
}
