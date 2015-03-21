package cadenza.gui.common;

import cadenza.core.Location;

@FunctionalInterface
public interface LocationListener {
  public void locationChanged(Location newLocation);
}
