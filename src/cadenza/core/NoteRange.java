package cadenza.core;

import java.io.Serializable;
import java.util.Optional;

public class NoteRange implements Serializable {
  private static final long serialVersionUID = 2L;
  
  private final Keyboard _keyboard;
  private final Note _lower;
  private final Note _upper;
  
  /**
   * Create a NoteRange pointing to a single note
   * @param keyboard the keyboard
   * @param single the single note
   */
  public NoteRange(Keyboard keyboard, Note single) {
    this(keyboard, single, single);
  }
  
  /**
   * Create a NoteRange pointing to a note range
   * @param keyboard the keyboard
   * @param lower the lower end of the range
   * @param upper the upper end of the range
   */
  public NoteRange(Keyboard keyboard, Note lower, Note upper) {
    _keyboard = keyboard;
    _lower = lower;
    _upper = upper;
  }
  
  /**
   * Create a NoteRange pointing to the entire range of the keyboard
   * @param keyboard the keyboard
   * @param soundingOnly <tt>true</tt> to use only the sounding range,
   *                     <tt>false</tt> to use the full range
   */
  public NoteRange(Keyboard keyboard, boolean soundingOnly) {
    this(keyboard,
         soundingOnly ? keyboard.soundingLow : keyboard.low,
         soundingOnly ? keyboard.soundingHigh : keyboard.high);
  }
  
  public static NoteRange union(NoteRange l1, NoteRange l2) {
    if (l1._keyboard != l2._keyboard)
      throw new IllegalArgumentException("NoteRanges are from different keyboards");
    
    return new NoteRange(l1._keyboard, Note.min(l1._lower, l2._lower),
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
  public Optional<NoteRange> copyTo(Keyboard newKeyboard, boolean soundingOnly) {
    final int newLow = Math.max(_lower.getMidiNumber(),
        soundingOnly ? newKeyboard.soundingLow.getMidiNumber()
                     : newKeyboard.low.getMidiNumber());
    final int newHigh = Math.min(_upper.getMidiNumber(),
        soundingOnly ? newKeyboard.soundingHigh.getMidiNumber()
                     : newKeyboard.high.getMidiNumber());
    
    if (newLow <= newHigh)
      return Optional.of(new NoteRange(newKeyboard, Note.valueOf(newLow), Note.valueOf(newHigh)));
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
    return toString(true, false);
  }
  
  public String toString(boolean includeKeyboardInfo, boolean soundingOnly) {
    final String rangePart;
    final boolean l, u;
    
    if (soundingOnly) {
      l = _lower.equals(_keyboard.soundingLow);
      u = _upper.equals(_keyboard.soundingHigh);
    } else {
      l = _lower.equals(_keyboard.low);
      u = _upper.equals(_keyboard.high);
    }
    
    if (l && u)
      rangePart = "all keys";
    else if (l)
      rangePart = "-" + _upper.toString();
    else if (u)
      rangePart = _lower.toString() + "-";
    else if (_lower.equals(_upper))
      rangePart = _lower.toString();
    else
      rangePart = _lower.toString() + "-" + _upper.toString();
    
    return rangePart + (includeKeyboardInfo ? " on " + _keyboard.name : "");
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (obj == this) return true;
    final NoteRange nro = (NoteRange) obj;
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
