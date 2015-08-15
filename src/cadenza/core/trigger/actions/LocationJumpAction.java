package cadenza.core.trigger.actions;

import cadenza.control.PerformanceController;

public class LocationJumpAction implements TriggerAction {
  private static final long serialVersionUID = 1L;
  
  private static volatile transient int _index;
  
  public static enum Type {
    SAVE("Save current location"),
    LOAD("Load saved location");
    
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
  
  public LocationJumpAction(Type type) {
    _type = type;
  }
  
  public Type getType() {
    return _type;
  }
  
  @Override
  public void takeAction(PerformanceController controller) {
    if (_type == Type.SAVE)
      _index = controller.getCurrentCueIndex();
    else if (_type == Type.LOAD)
      controller.goTo(_index);
  }
  
  @Override
  public String toString() {
    return _type.toString();
  }
}
