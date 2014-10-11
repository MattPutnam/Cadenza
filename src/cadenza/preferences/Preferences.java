package cadenza.preferences;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import cadenza.core.Keyboard;
import cadenza.core.Note;

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
  }
  
  /**
   * Reads the default Keyboard from the preferences file.  This is an IO operation
   * and cannot be called from the Swing Event thread.
   * @return the default keyboard from the preferences file
   * @throws Exception If any IO exception occurs
   */
  public static Keyboard readDefaultKeyboard() throws Exception {
    SwingUtils.throwIfEventThread();
    
    return buildDefaultKeyboard(PropertiesFileReader.readAll(PREFERENCES_FILE));
  }
  
  /**
   * Builds the default Keyboard from the given preferences map.  This is not
   * an IO operation and can be called from anywhere.
   * @param loadedMap the pre-loaded preferences map
   * @return the default Keyboard from the given preferences map.
   */
  public static Keyboard buildDefaultKeyboard(Map<String, String> loadedMap) {
    final Keyboard kbd = new Keyboard(1);
    
    final String keyboardName = loadedMap.get(Keys.Keyboard.NAME);
    if (keyboardName != null)
      kbd.name = keyboardName;
    
    final String keyboardChannel = loadedMap.get(Keys.Keyboard.CHANNEL);
    if (keyboardChannel != null)
      kbd.channel = Integer.parseInt(keyboardChannel);
    
    final String fullRange = loadedMap.get(Keys.Keyboard.RANGE);
    if (fullRange != null) {
      final Note[] notes = parse(fullRange);
      kbd.low = notes[0];
      kbd.high = notes[1];
    }
    
    final String soundingRange = loadedMap.get(Keys.Keyboard.SOUNDING_RANGE);
    if (soundingRange != null) {
      final Note[] notes = parse(soundingRange);
      kbd.soundingLow = notes[0];
      kbd.soundingHigh = notes[1];
    }
    
    return kbd;
  }
  
  /**
   * Commits the given keyboard to the given preferences map.  This is not an
   * IO operation can can be called from anywhere.
   * @param preferences the loaded preferences map
   * @param keyboard the keyboard to commit to <tt>preferences</tt>
   */
  public static void commitDefaultKeyboard(Map<String, String> preferences, Keyboard keyboard) {
    preferences.put(Keys.Keyboard.NAME,           keyboard.name);
    preferences.put(Keys.Keyboard.CHANNEL,        String.valueOf(keyboard.channel));
    preferences.put(Keys.Keyboard.RANGE,          keyboard.low + "-" + keyboard.high);
    preferences.put(Keys.Keyboard.SOUNDING_RANGE, keyboard.soundingLow + "-" + keyboard.soundingHigh);
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
