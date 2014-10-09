package cadenza.core;

import java.io.Serializable;

import common.midi.MidiUtilities;

public class Location implements Serializable {
  private static final long serialVersionUID = 1L;
  
  /**
   * Enumerates the types of note ranges available
   */
  public static enum RangeType {
    WHOLE_KEYBOARD, SINGLE_NOTE, NOTE_RANGE
  }
  
  private static final String WHOLE_KEYBOARD_DISP = "all keys";
  
  private final Keyboard _keyboard;
  
  /** The type of range (whole, single note, note range) */
  private final RangeType _type;
  
  /** If this is a single note, then that note */
  private final Note _single;
  
  /** If this is a range, then the lower bound (inclusive) */
  private final Note _lower;
  
  /** If this is a range, then the upper bound (inclusive) */
  private final Note _upper;
  
  private Location(Keyboard keyboard, RangeType type, Note single, Note lower, Note upper) {
    _keyboard = keyboard;
    _type = type;
    _single = single;
    _lower = lower;
    _upper = upper;
  }
  
  public Location(Location template, Keyboard newKeyboard) {
    _keyboard = newKeyboard;
    _type = template._type;
    _single = template._single;
    _lower = template._lower;
    _upper = template._upper;
  }
  
  public Keyboard getKeyboard() {
    return _keyboard;
  }
  
  /**
   * @return the type of range (whole, single note, note range)
   */
  public RangeType getType() {
    return _type;
  }
  
  /**
   * @return the single note, if a SINGLE_NOTE type
   * @throws IllegalStateException if not a SINGLE_NOTE range
   */
  public Note getSingle() {
    if (_type != RangeType.SINGLE_NOTE) throw new IllegalStateException("Wrong type");
    else return _single;
  }
  
  /**
   * @return the lower bound, if a NOTE_RANGE type
   * @throws IllegalStateException if not a NOTE_RANGE range
   */
  public Note getLowerOfRange() {
    if (_type != RangeType.NOTE_RANGE) throw new IllegalStateException("Wrong type");
    else return _lower;
  }
  
  /**
   * @return the upper bound, if a NOTE_RANGE type
   * @throws IllegalStateException if not a NOTE_RANGE range
   */
  public Note getUpperOfRange() {
    if (_type != RangeType.NOTE_RANGE) throw new IllegalStateException("Wrong type");
    else return _upper;
  }
  
  @Override
  public String toString() {
    return toString(true);
  }
  
  public String toString(boolean includeKeyboardInfo) {
    final String suffix = (_keyboard == null || !includeKeyboardInfo) ? "" : " on " + _keyboard.name;
    switch (_type) {
      case WHOLE_KEYBOARD: return WHOLE_KEYBOARD_DISP + suffix;
      case SINGLE_NOTE: return _single.toString() + suffix;
      case NOTE_RANGE: return (_lower == null ? "" : _lower.toString()) + "-" +
                  (_upper == null ? "" : _upper.toString()) + suffix;
      default: throw new IllegalStateException("Unknown note type!");
    }
  }
  
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    final Location nro = (Location) obj;
    if (nro._type != _type) return false;
    switch (_type) {
      case WHOLE_KEYBOARD: return true;
      case SINGLE_NOTE: return _single == nro._single;
      case NOTE_RANGE: return _lower == nro._lower && _upper == nro._upper;
      default: return false;
    }
  }
  
  @Override
  public int hashCode() {
    switch (_type) {
      case WHOLE_KEYBOARD: return 0;
      case SINGLE_NOTE: return _single.hashCode();
      case NOTE_RANGE: return _lower.hashCode() + 127*_upper.hashCode();
      default: throw new IllegalStateException("Unknown note type!");
    }
  }
  
  /**
   * Determines whether or not the given note name (e.g., A4) falls within this range
   * @param noteName - the note to check, in scientific pitch notation
   * @param keyboard - the Keyboard this note was pressed from
   * @return <tt>true</tt> iff the pitch is within this range
   */
  public boolean contains(String noteName) {
    return contains(MidiUtilities.noteNameToNumber(noteName));
  }
  
  /**
   * Determines whether or not the given MIDI note number falls within this range
   * @param midiNumber - the MIDI number of the pitch to check
   * @param keyboard - the Keyboard this note was pressed from
   * @return <tt>true</tt> iff the pitch is within this range
   */
  public boolean contains(int midiNumber) {
    if (_type == RangeType.SINGLE_NOTE)
      return midiNumber == _single.getMidiNumber();
    
    if (_type == RangeType.WHOLE_KEYBOARD)
      return _keyboard.soundingLow.getMidiNumber() <= midiNumber && midiNumber <= _keyboard.soundingHigh.getMidiNumber();
    
    // _type == Type.NOTE_RANGE
    if (_lower == null) {
      return _keyboard.soundingLow.getMidiNumber() <= midiNumber && midiNumber <= _upper.getMidiNumber();
    } else if (_upper == null) {
      return _lower.getMidiNumber() <= midiNumber && midiNumber <= _keyboard.soundingHigh.getMidiNumber();
    } else {
      return _lower.getMidiNumber() <= midiNumber && midiNumber <= _upper.getMidiNumber();
    }
  }
  
  public Note getLowest() {
    switch (_type) {
      case SINGLE_NOTE:    return _single;
      case NOTE_RANGE:     return _lower;
      case WHOLE_KEYBOARD: return _keyboard.low;
      default: throw new IllegalStateException("Unknown Location type!");
    }
  }
  
  public Note getHighest() {
    switch (_type) {
      case SINGLE_NOTE:    return _single;
      case NOTE_RANGE:     return _upper;
      case WHOLE_KEYBOARD: return _keyboard.high;
      default: throw new IllegalStateException("Unknown Location type!");
    }
  }
  
  public Note getLowestSounding() {
    switch(_type) {
      case SINGLE_NOTE:    return _single;
      case NOTE_RANGE:     return _lower;
      case WHOLE_KEYBOARD: return _keyboard.soundingLow;
      default: throw new IllegalStateException("Unknown Location type!");
    }
  }
  
  public Note getHighestSounding() {
    switch (_type) {
      case SINGLE_NOTE:    return _single;
      case NOTE_RANGE:     return _upper;
      case WHOLE_KEYBOARD: return _keyboard.soundingHigh;
      default: throw new IllegalStateException("Unknown Location type!");
    }
  }
  
  /**
   * @return a Location representing the whole keyboard
   */
  public static Location wholeKeyboard(Keyboard keyboard) {
    return new Location(keyboard, RangeType.WHOLE_KEYBOARD, null, null, null);
  }
  
  /**
   * Creates a Location representing a single note
   * @param note - the note to match by this range
   * @return a single-note Location for the given note
   */
  public static Location singleNote(Keyboard keyboard, Note note) {
    return new Location(keyboard, RangeType.SINGLE_NOTE, note, null, null);
  }
  
  /**
   * Creates a Location representing a range of notes
   * @param lower - the lower bound of the range (inclusive), or null to leave open-ended
   * @param upper - the upper bound of the range (inclusive), or null to leave open-ended
   * @return a note range Location for the given range
   */
  public static Location range(Keyboard keyboard, Note lower, Note upper) {
    return new Location(keyboard, RangeType.NOTE_RANGE, null, lower, upper);
  }
  
}
