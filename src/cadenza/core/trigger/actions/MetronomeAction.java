package cadenza.core.trigger.actions;

import cadenza.control.PerformanceController;
import cadenza.core.metronome.Metronome;
import cadenza.core.metronome.TempoTapper;

public class MetronomeAction implements TriggerAction {
  private static final long serialVersionUID = 1L;
  
  public static enum Type { START, STOP, SET_BPM, TAP }
  
  private final Type _type;
  private final int _bpm;
  
  public MetronomeAction(Type type) {
    if (type == Type.SET_BPM)
      throw new IllegalArgumentException("SET_BPM must be set with other constructor");
    _type = type;
    _bpm = 0;
  }
  
  public MetronomeAction(int bpm) {
    _type = Type.SET_BPM;
    _bpm = bpm;
  }

  @Override
  public void takeAction(PerformanceController controller) {
    switch (_type) {
      case START:   Metronome.getInstance().start();      break;
      case STOP:    Metronome.getInstance().stop();       break;
      case SET_BPM: Metronome.getInstance().setBPM(_bpm); break;
      case TAP:     TempoTapper.getInstance().tap();      break;
    }
  }
  
  @Override
  public String toString() {
    switch (_type) {
      case START:   return "start metronome";
      case STOP:    return "stop metronome";
      case SET_BPM: return "set metronome BPM to " + _bpm;
      case TAP:     return "tempo tap";
      default: throw new IllegalStateException("Unknown Type!");
    }
  }
  
  public Type getType() {
    return _type;
  }
  
  public int getBPM() {
    return _bpm;
  }

}
