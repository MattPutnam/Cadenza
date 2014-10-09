package cadenza.core;

import java.io.Serializable;
import java.util.List;

/**
 * A keyboard as used in Cadenza.  Keyboards have a name, a high and low note,
 * and a flag indicating if they're the main keyboard.
 * 
 * @author Matt Putnam
 */
public class Keyboard implements Serializable {
  private static final long serialVersionUID = 1L;
  
  /** The low note on the keyboard */
  public Note low;
  
  /** The high note on the keyboard */
  public Note high;
  
  public Note soundingLow;
  
  public Note soundingHigh;
  
  /** The display name of the keyboard */
  public String name;
  
  /** Whether or not this is a main keyboard */
  public boolean isMain;
  
  public int channel;
  
  public Keyboard(int channel) {
    this(Note.A0, Note.C8, Note.A0, Note.C8, "Keyboard", false, channel);
  }
  
  public Keyboard(Note low, Note high, Note soundingLow, Note soundingHigh, String name, boolean isMain, int channel) {
    this.low = low;
    this.high = high;
    this.soundingLow = soundingLow;
    this.soundingHigh = soundingHigh;
    this.name = name;
    this.isMain = isMain;
    this.channel = channel;
  }
  
  @Override
  public String toString() {
    return name;
  }
  
  @Override
  public int hashCode() {
    int hashCode = low.hashCode();
    hashCode = 31*hashCode + high.hashCode();
    hashCode = 31*hashCode + name.hashCode();
    hashCode = 2 *hashCode  + (isMain ? 1 : 0);
    hashCode = 15*hashCode + channel;
    return hashCode;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    final Keyboard k = (Keyboard) obj;
    return this.low == k.low &&
         this.high == k.high &&
         this.name.equals(k.name) &&
         this.isMain == k.isMain &&
         this.channel == k.channel;
  }
  
  public static int findFirstAvailableChannel(List<Keyboard> keyboards) {
    int c = 1;
    loop:
    while (true) {
      for (final Keyboard keyboard : keyboards) {
        if (keyboard.channel == c) {
          ++c;
          continue loop;
        }
      }
      return c;
    }
  }
  
  /**
   * Finds the main keyboard in a list of keyboards
   * @param keyboards - the list of keyboards to search
   * @return the main keyboard in the list of keyboards, or the first
   * keyboard in the list if none is found
   */
  public static Keyboard findMain(List<Keyboard> keyboards) {
    for (final Keyboard keyboard : keyboards) {
      if (keyboard.isMain) {
        return keyboard;
      }
    }
    return keyboards.get(0);
  }

  /**
   * Finds the first non-main keyboard in a list of keyboards
   * @param keyboards - the list of keyboards to search
   * @return the first non-main keyboard in the list, or the first
   * keyboard if only main keyboards are found
   */
  public static Keyboard findSecondary(List<Keyboard> keyboards) {
    for (final Keyboard keyboard : keyboards) {
      if (!keyboard.isMain) {
        return keyboard;
      }
    }
    return keyboards.get(0);
  }
}
