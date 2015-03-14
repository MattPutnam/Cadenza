package cadenza.core.trigger.actions;

import cadenza.control.PerformanceController;

/**
 * Action to send control to the previous Cue
 * 
 * @author Matt Putnam
 */
public class ReverseAction implements TriggerAction {
  private static final long serialVersionUID = 2L;
  
  @Override
  public void takeAction(PerformanceController controller) {
    controller.reverse();
  }
  
  @Override
  public String toString() {
    return "reverse cue";
  }

}
