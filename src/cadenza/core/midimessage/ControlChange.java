package cadenza.core.midimessage;

import cadenza.core.Keyboard;

public final class ControlChange extends MIDIMessage {
  private final int _ccNum;
  private final int _ccVal;
  
  public ControlChange(Keyboard keyboard, int ccNum, int ccVal) {
    super(keyboard);
    _ccNum = ccNum;
    _ccVal = ccVal;
  }
  
  public int getCCNumber() {
    return _ccNum;
  }
  
  public int getCCValue() {
    return _ccVal;
  }
  
  @Override
  public int getType() {
    return CONTROL;
  }
}
