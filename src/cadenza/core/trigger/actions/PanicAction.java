package cadenza.core.trigger.actions;

import cadenza.control.PerformanceController;

public class PanicAction implements TriggerAction {
  private static final long serialVersionUID = 2L;
  
  @Override
  public void takeAction(PerformanceController controller) {
    controller.allNotesOff();
  }
  
  @Override
  public String toString() {
    return "panic";
  }
}
