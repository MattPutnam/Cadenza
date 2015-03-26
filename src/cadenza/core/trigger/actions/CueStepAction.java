package cadenza.core.trigger.actions;

import cadenza.control.PerformanceController;

public class CueStepAction implements TriggerAction {
  private static final long serialVersionUID = 2L;
  
  public static enum Type {
    ADVANCE("Advance to the next cue"),
    REVERSE("Reverse to the previous cue");
    
    private final String _name;
    private Type(String name) {
      _name = name;
    }
    
    @Override
    public String toString() {
      return _name;
    }
  }
  
  private final Type _type;
  
  public CueStepAction(Type type) {
    _type = type;
  }
  
  public Type getType() {
    return _type;
  }

  @Override
  public void takeAction(PerformanceController controller) {
    switch (_type) {
      case ADVANCE: controller.advance(); break;
      case REVERSE: controller.reverse(); break;
    }
  }
  
  @Override
  public String toString() {
    return _type.toString();
  }

}
