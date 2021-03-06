package cadenza.core.trigger.actions;

import cadenza.control.PerformanceController;
import cadenza.core.LocationNumber;
import cadenza.core.Song;

/**
 * Action to go to a specific point in the performance 
 * 
 * @author Matt Putnam
 */
public class GotoAction implements TriggerAction {
  private static final long serialVersionUID = 2L;
  
  private final Song _song;
  private final LocationNumber _measure;
  
  public GotoAction(Song song, LocationNumber measure) {
    _song = song;
    _measure = measure;
  }

  @Override
  public void takeAction(PerformanceController controller) {
    controller.goTo(_song, _measure);
  }
  
  @Override
  public String toString() {
    return "go to " + _song + " m. " + _measure;
  }
  
  public Song getSong() {
    return _song;
  }
  
  public LocationNumber getMeasure() {
    return _measure;
  }

}
