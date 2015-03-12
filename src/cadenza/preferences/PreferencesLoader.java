package cadenza.preferences;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cadenza.core.Keyboard;
import cadenza.core.Note;
import cadenza.core.Synthesizer;
import cadenza.preferences.PatchSearchOptions.PatchSearchMode;
import cadenza.synths.Synthesizers;

import common.Utils;
import common.io.PropertiesFileReader;
import common.midi.MidiUtilities;
import common.swing.SwingUtils;

/**
 * Utility for loading and saving the global Cadenza preferences
 * 
 * @author Matt Putnam
 */
public final class PreferencesLoader {
  private PreferencesLoader() {}
  
  private static final File PREFERENCES_FILE = new File("resources/preferences.txt");
  
  /////////////////////////////////////////////////////////////////////////////
  // Read block
  
  /**
   * Reads all of the preferences from the preferences file.  This is an IO
   * operation and cannot be called from the Swing Event thread.
   * @return all of the preferences from the preferences file
   * @throws Exception If any IO exception occurs
   */
  public static Map<String, String> readAllPreferences() throws Exception {
    SwingUtils.throwIfEventThread();
    
    return PropertiesFileReader.readAll(PREFERENCES_FILE);
  }
  
  /**
   * Reads the default Keyboard from the preferences file.  This is an IO operation
   * and cannot be called from the Swing Event thread.
   * @return the default Keyboard from the preferences file
   * @throws Exception If any IO exception occurs
   */
  public static Keyboard readDefaultKeyboard() throws Exception {
    return buildDefaultKeyboard(readAllPreferences());
  }
  
  /**
   * Builds the default Keyboard from the given preferences map.  This is not
   * an IO operation and can be called from anywhere.
   * @param loadedPrefs the pre-loaded preferences map
   * @return the default Keyboard from the given preferences map
   */
  public static Keyboard buildDefaultKeyboard(Map<String, String> loadedPrefs) {
    final Keyboard kbd = new Keyboard(1);
    
    final String keyboardName = loadedPrefs.get(Keys.Keyboard.NAME);
    if (keyboardName != null)
      kbd.name = keyboardName;
    
    final String keyboardChannel = loadedPrefs.get(Keys.Keyboard.CHANNEL);
    if (keyboardChannel != null)
      kbd.channel = Integer.parseInt(keyboardChannel);
    
    final String fullRange = loadedPrefs.get(Keys.Keyboard.RANGE);
    if (fullRange != null) {
      final Note[] notes = parse(fullRange);
      kbd.low = notes[0];
      kbd.high = notes[1];
    }
    
    final String soundingRange = loadedPrefs.get(Keys.Keyboard.SOUNDING_RANGE);
    if (soundingRange != null) {
      final Note[] notes = parse(soundingRange);
      kbd.soundingLow = notes[0];
      kbd.soundingHigh = notes[1];
    }
    
    return kbd;
  }
  
  /**
   * Reads the default Synthesizer from the preferences file.  This is an IO
   * operation and cannot be called from the Swing Event thread.
   * @return the default Synthesizer from the preferences file.
   * @throws Exception If any IO exception occurs
   */
  public static Synthesizer readDefaultSynthesizer() throws Exception {
    return buildDefaultSynthesizer(readAllPreferences());
  }
  
  /**
   * Builds the default Synthesizer from the given preferences map.  This is
   * not an IO operation and can be called from anywhere.
   * @param loadedPrefs the pre-loaded preferences map
   * @return the default Synthesizer from the given preferences map
   */
  public static Synthesizer buildDefaultSynthesizer(Map<String, String> loadedPrefs) {
    final String synthname = loadedPrefs.get(Keys.Synthesizer.SYNTH);
    if (synthname == null) return null;
    
    final String channelString = loadedPrefs.get(Keys.Synthesizer.CHANNELS);
    final List<Integer> channels = channelString == null ? new ArrayList<>(0) : Utils.parseRangeString(channelString);
    
    final String expansionsString = loadedPrefs.get(Keys.Synthesizer.EXPANSIONS);
    final Map<String, String> expansions = Synthesizer.parseExpansions(expansionsString);
    
    return new Synthesizer(synthname, Synthesizers.getBanksForSynth(synthname), expansions, channels);
  }
  
  /**
   * Reads the default MIDI I/O ports from the preferences file.  This is an IO
   * operation and cannot be called from the Swing Event thread.
   * @return the default MIDI I/O ports from the preferences file.  The first
   * item is the input port, the second is the output port
   * @throws Exception If any IO exception occurs
   */
  public static String[] readDefaultMIDIPorts() throws Exception {
    return buildDefaultMIDIPorts(readAllPreferences());
  }
  
  /**
   * Builds the default MIDI I/O ports from the given preferences map.  This is not
   * an IO operation and can be called from anywhere.
   * @param loadedPrefs the pre-loaded preferences map
   * @return the default MIDI I/O ports from the given preferences map.  The
   * first item is the input port, the second is the output port
   */
  public static String[] buildDefaultMIDIPorts(Map<String, String> loadedPrefs) {
    return new String[] {loadedPrefs.get(Keys.Midiport.INPUT), loadedPrefs.get(Keys.Midiport.OUTPUT)};
  }
  
  /**
   * Reads the MIDI input options from the preferences file.  This is an IO
   * operation and cannot be called from the Swing Event thread.
   * @return the MIDI input options from the preferences file
   * @throws Exception If any IO exception occurs
   */
  public static MIDIInputOptions readMIDIInputOptions() throws Exception {
    return buildMIDIInputOptions(readAllPreferences());
  }
  
  /**
   * Builds the MIDI input options from the given preferences map.  This is not an IO
   * operation and can be called from anywhere.
   * @param loadedPrefs the pre-loaded preferences map
   * @return the MIDI input options from the given preferences map
   */
  public static MIDIInputOptions buildMIDIInputOptions(Map<String, String> loadedPrefs) {
    return new MIDIInputOptions(
        Boolean.parseBoolean(loadedPrefs.get(Keys.Input.ALLOW_MIDI_INPUT)),
        
        Boolean.parseBoolean(loadedPrefs.get(Keys.Input.ALLOW_VOLUME)),
        Boolean.parseBoolean(loadedPrefs.get(Keys.Input.VOLUME_STRICT)),
        
        Boolean.parseBoolean(loadedPrefs.get(Keys.Input.ALLOW_PATCHUSAGE)),
        Boolean.parseBoolean(loadedPrefs.get(Keys.Input.PATCHUSAGE_SINGLE)),
        Boolean.parseBoolean(loadedPrefs.get(Keys.Input.PATCHUSAGE_RANGE)),
        Boolean.parseBoolean(loadedPrefs.get(Keys.Input.PATCHUSAGE_WHOLE))
    );
  }
  
  /**
   * Reads the patch search option from the preferences file.  This is an
   * IO operation and cannot be called from the Swing Event thread.
   * @return the patch search options
   * @throws Exception If any IO exception occurs
   */
  public static PatchSearchOptions readPatchSearchOptions() throws Exception {
    return buildPatchSearchOptions(readAllPreferences());
  }
  
  /**
   * Builds the patch search options from the given preferences map.  This
   * is not an IO operation and can be called from anywhere.
   * @param loadedPrefs the pre-loaded preferences map
   * @return the patch search options
   */
  public static PatchSearchOptions buildPatchSearchOptions(Map<String, String> loadedPrefs) {
    return new PatchSearchOptions(
        PatchSearchMode.valueOf(loadedPrefs.get(Keys.PatchSearch.MODE)),
        Boolean.valueOf(loadedPrefs.get(Keys.PatchSearch.CASE_SENSITIVE)).booleanValue());
  }
  
  /////////////////////////////////////////////////////////////////////////////
  // Write block
  
  /**
   * Commits the given keyboard to the given preferences map.  This is not an
   * IO operation and can be called from anywhere.
   * @param preferences the loaded preferences map
   * @param keyboard the Keyboard to commit to <tt>preferences</tt>
   */
  public static void commitDefaultKeyboard(Map<String, String> preferences, Keyboard keyboard) {
    preferences.put(Keys.Keyboard.NAME,           keyboard.name);
    preferences.put(Keys.Keyboard.CHANNEL,        String.valueOf(keyboard.channel));
    preferences.put(Keys.Keyboard.RANGE,          keyboard.low + "-" + keyboard.high);
    preferences.put(Keys.Keyboard.SOUNDING_RANGE, keyboard.soundingLow + "-" + keyboard.soundingHigh);
    
    Preferences._defaultKeyboard = keyboard;
  }
  
  /**
   * Commits the given synth to the given preferences map.  This is not an IO
   * operation and can be called from anywhere.
   * @param preferences the loaded preferences map
   * @param synth the Synthesizer to commit to <tt>preferences</tt>
   */
  public static void commitDefaultSynthesizer(Map<String, String> preferences, Synthesizer synth) {
    preferences.put(Keys.Synthesizer.SYNTH,      synth.getName());
    preferences.put(Keys.Synthesizer.CHANNELS,   Utils.makeRangeString(synth.getChannels()));
    preferences.put(Keys.Synthesizer.EXPANSIONS, synth.getExpansionString());
    
    Preferences._defaultSynthesizer = synth;
  }
  
  /**
   * Commits the given MIDI I/O ports to the given preferences map.  This is
   * not an IO operation and can be called from anywhere.
   * @param preferences the loaded preferences map
   * @param ports the MIDI I/O ports to commit to <tt>preferences</tt>.  The
   * first should be the input port, the second should be the output pot.
   */
  public static void commitDefaultMIDIPorts(Map<String, String> preferences, String[] ports) {
    preferences.put(Keys.Midiport.INPUT,  ports[0]);
    preferences.put(Keys.Midiport.OUTPUT, ports[1]);
    
    Preferences._defaultMIDIPorts = ports;
  }
  
  /**
   * Commits the given MIDI input preferences to the given preferences map.
   * This is not an IO operation and can be called from anywhere.
   * @param preferences the loaded preferences map
   * @param options the MIDI input options to commit
   */
  public static void commitInputOptions(Map<String, String> preferences, MIDIInputOptions options) {
    preferences.put(Keys.Input.ALLOW_MIDI_INPUT,  Boolean.toString(options.allowMIDIInput()));
    
    preferences.put(Keys.Input.ALLOW_VOLUME,      Boolean.toString(options.allowVolumeInput()));
    preferences.put(Keys.Input.VOLUME_STRICT,     Boolean.toString(options.isVolumeStrict()));
    
    preferences.put(Keys.Input.ALLOW_PATCHUSAGE,  Boolean.toString(options.allowPatchUsageInput()));
    preferences.put(Keys.Input.PATCHUSAGE_SINGLE, Boolean.toString(options.allowSinglePatchUsage()));
    preferences.put(Keys.Input.PATCHUSAGE_RANGE,  Boolean.toString(options.allowRangePatchUsage()));
    preferences.put(Keys.Input.PATCHUSAGE_WHOLE,  Boolean.toString(options.allowWholePatchUsage()));
    
    Preferences._midiInputOptions = options;
  }
  
  /**
   * Commits the given patch search mode to the given preferences map.
   * This is not an IO operation and can be called from anywhere.
   * @param preferences the loaded preferences map
   * @param mode the patch search mode
   */
  public static void commitPatchSearchOptions(Map<String, String> preferences, PatchSearchOptions options) {
    preferences.put(Keys.PatchSearch.MODE, options.getSearchMode().name());
    preferences.put(Keys.PatchSearch.CASE_SENSITIVE, Boolean.toString(options.isCaseSensitive()));
    
    Preferences._patchSearchOptions = options;
  }
  
  /**
   * Writes the given preferences to the file.  This is an IO operation and
   * cannot be called from the Swing Event thread.
   * @param preferences the preferences map to write
   * @throws IOException If any IO exception occurs
   */
  public static void writePreferences(Map<String, String> preferences) throws IOException {
    SwingUtils.throwIfEventThread();
    
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(PREFERENCES_FILE))) {
      for (final Map.Entry<String, String> entry : preferences.entrySet()) {
        writer.write(entry.getKey() + " = " + entry.getValue());
        writer.newLine();
      }
    }
  }
  
  private static Note[] parse(String range) {
    final int hyphenIndex = range.indexOf("-");
    if (hyphenIndex == -1) {
      final Note note = Note.valueOf(MidiUtilities.noteNameToNumber(range.trim()));
      return new Note[] {note, note};
    }
    
    return new Note[] { Note.valueOf(MidiUtilities.noteNameToNumber(range.substring(0, hyphenIndex).trim())),
                        Note.valueOf(MidiUtilities.noteNameToNumber(range.substring(hyphenIndex +1).trim()))};
  }
}
