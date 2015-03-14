package cadenza.core.trigger.predicates;

import cadenza.core.Location;

public interface HasLocation {
  public Location getLocation();
  public void setLocation(Location newLocation);
}
