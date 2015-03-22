package cadenza.core.midimessage;

import cadenza.core.Keyboard;

public abstract class MIDIMessage {
  public static final int NOTE_ON = 1;
  public static final int NOTE_OFF = 2;
  public static final int CONTROL = 3;
  
  private final Keyboard _keyboard;
  
  public MIDIMessage(Keyboard keyboard) {
    _keyboard = keyboard;
  }
  
  public final Keyboard getKeyboard() {
    return _keyboard;
  }
  
  public abstract int getType();
}
