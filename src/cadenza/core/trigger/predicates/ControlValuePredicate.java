package cadenza.core.trigger.predicates;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;

import cadenza.core.ControlNames;
import cadenza.core.Keyboard;

import common.midi.MidiUtilities;

public class ControlValuePredicate implements TriggerPredicate {
  private static final long serialVersionUID = 2L;
  
  private final Keyboard _keyboard;
  private final int _cc;
  private final int _low;
  private final int _high;
  
  public ControlValuePredicate(Keyboard keyboard, int cc, int value) {
    this(keyboard, cc, value, value);
  }
  
  public ControlValuePredicate(Keyboard keyboard, int cc, int low, int high) {
    _keyboard = keyboard;
    _cc = cc;
    _low = low;
    _high = high;
  }

  @Override
  public boolean receive(MidiMessage message) {
    if (message instanceof ShortMessage) {
      final ShortMessage sm = (ShortMessage) message;
      if (!MidiUtilities.isControlChange(sm))
        return false;
      
      final int channel = sm.getChannel();
      final int control = sm.getData1();
      final int value = sm.getData2();
      
      return _keyboard.channel == channel &&
          control == _cc &&
          _low <= value && value <= _high;
    } else {
      return false;
    }
  }
  
  @Override
  public String toString() {
    final String prefix = "control " + _cc + " (" + ControlNames.getName(_cc) + ") on kbd '" + _keyboard.name;
    if (_low == _high)
      return prefix + "' with value " + _low;
    else
      return prefix + "' in range [" + _low + ", " + _high + "]";
  }
  
  public Keyboard getKeyboard() {
    return _keyboard;
  }
  
  public int getControlNumber() {
    return _cc;
  }
  
  public int getLow() {
    return _low;
  }
  
  public int getHigh() {
    return _high;
  }

}
