package cadenza.core;

import java.io.Serializable;
import java.util.Optional;

public class Location implements Serializable {
  private static final long serialVersionUID = 2L;
  
  private final Keyboard _keyboard;
  private final Note _lower;
  private final Note _upper;
  
  /**
   * Create a Location pointing to a single note
   * @param keyboard the keyboard
   * @param single the single note
   */
  public Location(Keyboard keyboard, Note single) {
    this(keyboard, single, single);
  }
  
  /**
   * Create a Location pointing to a note range
   * @param keyboard the keyboard
   * @param lower the lower end of the range
   * @param upper the upper end of the range
   */
  public Location(Keyboard keyboard, Note lower, Note upper) {
    _keyboard = keyboard;
    _lower = lower;
    _upper = upper;
  }
  
  /**
   * Create a Location pointing to the entire range of the keyboard
   * @param keyboard the keyboard
   * @param soundingOnly <tt>true</tt> to use only the sounding range,
   *                     <tt>false</tt> to use the full range
   */
  public Location(Keyboard keyboard, boolean soundingOnly) {
    this(keyboard,
         soundingOnly ? keyboard.soundingLow : keyboard.low,
         soundingOnly ? keyboard.soundingHigh : keyboard.high);
  }
  
  public static Location union(Location l1, Location l2) {
    if (l1._keyboard != l2._keyboard)
      throw new IllegalArgumentException("Locations are from different keyboards");
    
    return new Location(l1._keyboard, Note.min(l1._lower, l2._lower),
                                      Note.max(l1._upper, l2._upper));
  }
  
  /**
   * Copies this range to a new keyboard.  If the new keyboard's range is less
   * than this, the resulting range is shrunk.  If the resulting range is empty,
   * an empty result is returned.
   * @param newKeyboard the new keyboard
   * @param soundingOnly <tt>true</tt> to use only the sounding range,
   *                     <tt>false</tt> to use the full range
   * @return <tt>Optional.of(<some range>) if the range fits on the new keyboard,
   *         <tt>Optional.empty() otherwise
   */
  public Optional<Location> copyTo(Keyboard newKeyboard, boolean soundingOnly) {
    final int newLow = Math.max(_lower.getMidiNumber(),
        soundingOnly ? newKeyboard.soundingLow.getMidiNumber()
                     : newKeyboard.low.getMidiNumber());
    final int newHigh = Math.min(_upper.getMidiNumber(),
        soundingOnly ? newKeyboard.soundingHigh.getMidiNumber()
                     : newKeyboard.high.getMidiNumber());
    
    if (newLow <= newHigh)
      return Optional.of(new Location(newKeyboard, Note.valueOf(newLow), Note.valueOf(newHigh)));
    else
      return Optional.empty();
  }
  
  public Keyboard getKeyboard() {
    return _keyboard;
  }
  
  public Note getLower() {
    return _lower;
  }
  
  public Note getUpper() {
    return _upper;
  }
  
  @Override
  public String toString() {
    return toString(true);
  }
  
  public String toString(boolean includeKeyboardInfo) {
    return _lower.toString() + "-" + _upper.toString() + (includeKeyboardInfo ? " on " + _keyboard.name : "");
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (obj == this) return true;
    final Location nro = (Location) obj;
    return _lower == nro._lower && _upper == nro._upper;
  }
  
  @Override
  public int hashCode() {
    int hashCode = _keyboard.hashCode();
    hashCode = 31*hashCode + _lower.hashCode();
    hashCode = 31*hashCode + _upper.hashCode();
    return hashCode;
  }
  
  /**
   * Determines whether or not the given MIDI note number falls within this range
   * @param midiNumber - the MIDI number of the pitch to check
   * @param keyboard - the Keyboard this note was pressed from
   * @return <tt>true</tt> iff the pitch is within this range
   */
  public boolean contains(int midiNumber) {
    return _lower.getMidiNumber() <= midiNumber && midiNumber <= _upper.getMidiNumber();
  }
  
}
