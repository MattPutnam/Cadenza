package cadenza.core.trigger.actions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cadenza.control.PerformanceController;
import cadenza.core.metronome.Metronome;
import cadenza.core.metronome.MetronomeListener;

public class WaitAction implements TriggerAction {
  private static final Logger LOG = LogManager.getLogger(WaitAction.class);
  
  private static final long serialVersionUID = 1L;
  
  private final int _num;
  private final boolean _isMillis;
  
  private volatile int _beatCounter;
  
  public WaitAction(int num, boolean isMillis) {
    _num = num;
    _isMillis = isMillis;
  }

  @Override
  public void takeAction(PerformanceController controller) {
    if (_isMillis) {
      try {
        Thread.sleep(_num);
      } catch (InterruptedException e) {
        LOG.warn("Exception during wait action", e);
      }
    } else if (Metronome.getInstance().isRunning()) {
      _beatCounter = 0;
      final MetronomeListener clickCounter = subdivision -> {
        if (subdivision == 0)
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
    return "wait " + _num + (_isMillis ? " milliseconds" : " beats");
  }
  
  public int getNum() {
    return _num;
  }
  
  public boolean isMillis() {
    return _isMillis;
  }

}
