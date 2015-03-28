package cadenza.core.trigger.actions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cadenza.control.PerformanceController;
import cadenza.core.metronome.Metronome;
import cadenza.core.metronome.Metronome.Subdivision;
import cadenza.core.metronome.MetronomeListener;

public class WaitAction implements TriggerAction {
  private static final Logger LOG = LogManager.getLogger(WaitAction.class);
  
  private static final long serialVersionUID = 2L;
  
  private final int _num;
  private final boolean _isMillis;
  private final Subdivision _subdivision;
  
  private volatile int _beatCounter;
  
  private WaitAction(int num, boolean isMillis, Subdivision subdivision) {
    _num = num;
    _isMillis = isMillis;
    _subdivision = subdivision;
  }
  
  public static WaitAction millis(int millis) {
    return new WaitAction(millis, true, null);
  }
  
  public static WaitAction beats(int num, Subdivision subdivision) {
    return new WaitAction(num, false, subdivision);
  }
  
  public Subdivision getSubdivision() {
    return _subdivision;
  }

  @Override
  public void takeAction(PerformanceController controller) {
    if (_isMillis) {
      try {
        Thread.sleep(_num);
      } catch (InterruptedException e) {
        LOG.warn("Exception during wait action", e);
      }
    } else {
      Metronome.getInstance().start();
      _beatCounter = 0;
      final MetronomeListener clickCounter = subdivision -> {
        if (_subdivision.matches(subdivision))
          ++_beatCounter;
      };
      Metronome.getInstance().addMetronomeListener(clickCounter);
      while (_beatCounter < _num)
        Thread.yield();
      Metronome.getInstance().removeMetronomeListener(clickCounter);
    }
  }
  
  @Override
  public String toString() {
    return "wait " + _num + (_isMillis ? " milliseconds" : " " + _subdivision.toString());
  }
  
  public int getNum() {
    return _num;
  }
  
  public boolean isMillis() {
    return _isMillis;
  }

}
