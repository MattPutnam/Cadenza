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
public class Preferences {
  private Preferences() {}
  
  private static final File PREFERENCES_FILE = new File("resources/preferences.txt");
  
  private static class Keys {
    private static class Keyboard {
      private static String NAME           = "keyboard.name";
      private static String CHANNEL        = "keyboard.channel";
      private static String RANGE          = "keyboard.range";
      private static String SOUNDING_RANGE = "keyboard.soundingrange";
    }
    
    private static class Synthesizer {
      private static String SYNTH      = "synthesizer.synth";
      private static String CHANNELS   = "synthesizer.channels";
      private static String EXPANSIONS = "synthesizer.expansions";
    }
    
    private static class Midiport {
      private static String INPUT  = "midiport.input";
      private static String OUTPUT = "midiport.output";
    }
  }
  
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
    final List<Integer> channels = channelString == null ? new ArrayList<Integer>(0) : Utils.parseRangeString(channelString);
    
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
   * Returns the default MIDI I/O ports from the preferences file.  This is not
   * an IO operation and can be called from anywhere.
   * @param loadedPrefs the pre-loaded preferences map
   * @return the default MIDI I/O ports from the given preferences map.  The
   * first item is the input port, the second is the output port
   */
  public static String[] buildDefaultMIDIPorts(Map<String, String> loadedPrefs) {
    return new String[] {loadedPrefs.get(Keys.Midiport.INPUT), loadedPrefs.get(Keys.Midiport.OUTPUT)};
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
      final Note note = new Note(MidiUtilities.noteNameToNumber(range.trim()));
      return new Note[] {note, note};
    }
    
    return new Note[] { new Note(MidiUtilities.noteNameToNumber(range.substring(0, hyphenIndex).trim())),
                        new Note(MidiUtilities.noteNameToNumber(range.substring(hyphenIndex +1).trim()))};
  }
}
