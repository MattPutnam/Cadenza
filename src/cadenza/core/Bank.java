package cadenza.core;

import java.io.Serializable;
import java.util.Objects;

public class Bank implements Serializable {
  private static final long serialVersionUID = 2L;
  
  private final String _name;
  private final String _selector;
  
  public Bank(String name) {
    this(name, name);
  }
  
  public Bank(String name, String selector) {
    _name = name;
    _selector = selector;
  }
  
  public String getName() {
    return _name;
  }
  
  public String getSelector() {
    return _selector;
  }
  
  @Override
  public String toString() {
    return _name;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (obj == this) return true;
    final Bank other = (Bank) obj;
    return _name.equals(other._name) && _selector.equals(other._selector);
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(_name, _selector);
  }
}
