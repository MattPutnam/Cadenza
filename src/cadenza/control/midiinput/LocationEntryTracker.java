package cadenza.control.midiinput;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cadenza.core.Keyboard;
import cadenza.preferences.Preferences;

/**
 * Object that tracks key presses for the sake of generating Location entry
 * events.  Keeps track of simultaneous key presses and once all keys are
 * released, fires the appropriate event.  If key presses come in from
 * multiple sources, the event is aborted.
 * 
 * @author Matt Putnam
 */
public abstract class LocationEntryTracker {
  private final Map<Integer, Keyboard> _keyboardMap = new IdentityHashMap<>();
  private Keyboard _activeKeyboard = null;
  private final Set<Integer> _currentlyPressedKeys = new HashSet<>();
  private final Set<Integer> _accumulatedPressedKeys = new HashSet<>();
  
  public LocationEntryTracker(List<Keyboard> keyboards) {
    keyboards.forEach(kbd -> _keyboardMap.put(Integer.valueOf(kbd.channel), kbd));
  }
  
  /**
   * Notify this object that a key was pressed, and calculate what Keyboard
   * that press came from
   * @param channel the channel (0-based) of the pressed key
   * @param midiNumber the MIDI note number of the pressed key
   * @return the Keyboard object on which the key was pressed.
   */
  public Keyboard keyPressed(int channel, int midiNumber) {
    final Keyboard kbd = _keyboardMap.get(Integer.valueOf(channel));

    if (_activeKeyboard == null || kbd == _activeKeyboard) {
      _activeKeyboard = kbd;
      _accumulatedPressedKeys.add(Integer.valueOf(midiNumber));
      _currentlyPressedKeys.add(Integer.valueOf(midiNumber));
    } else {
      // key pressed on other keyboard
      _activeKeyboard = null;
      _accumulatedPressedKeys.clear();
      _currentlyPressedKeys.clear();
    }
    
    return kbd;
  }

  /**
   * Notify this object that a key was released, and calculate what Keyboard
   * that event came from
   * @param channel the channel(0-based) of the released key
   * @param midiNumber the MIDI note number of the released key
   */
  public Keyboard keyReleased(int channel, int midiNumber) {
    final Keyboard kbd = _keyboardMap.get(Integer.valueOf(channel));

    if (kbd == _activeKeyboard) {
      _currentlyPressedKeys.remove(Integer.valueOf(midiNumber));
      
      if (_currentlyPressedKeys.isEmpty()) {
        if (_accumulatedPressedKeys.size() == 1 && Preferences.getMIDIInputOptions().allowSinglePatchUsage()) {
          singlePressed(kbd, _accumulatedPressedKeys.iterator().next().intValue());
        } else if (_accumulatedPressedKeys.size() == 2 && Preferences.getMIDIInputOptions().allowRangePatchUsage()) {
          final Iterator<Integer> i = _accumulatedPressedKeys.iterator();
          int n1 = i.next().intValue();
          int n2 = i.next().intValue();
          if (n1 < n2)
            rangePressed(kbd, n1, n2);
          else
            rangePressed(kbd, n2, n1);
        } else if (_accumulatedPressedKeys.size() >= 3 && Preferences.getMIDIInputOptions().allowWholePatchUsage()) {
          wholePressed(kbd);
        }
        _accumulatedPressedKeys.clear();
      }
    }
    
    return kbd;
  }
  
  /**
   * Called once a single key has been pressed and released,
   * if single note entry is turned on.
   * @param keyboard the keyboard on which the note was pressed
   * @param noteNumber the MIDI note number of the pressed key
   */
  protected void singlePressed(Keyboard keyboard, int noteNumber) {}
  
  /**
   * Called once two different keys on one keyboard have been pressed and
   * released, if note range entry is turned on.
   * @param keyboard the keyboard on which the notes were pressed
   * @param lowNumber the MIDI note number of the lower key
   * @param highNumber the MIDI note number of the higher key
   */
  protected void rangePressed(Keyboard keyboard, int lowNumber, int highNumber) {}
  
  /**
   * Called once three or more different keys on one keyboard have been pressed
   * and released, if whole range entry is turned on.
   * @param keyboard the keyboard on which the notes were pressed.
   */
  protected void wholePressed(Keyboard keyboard) {}
}
