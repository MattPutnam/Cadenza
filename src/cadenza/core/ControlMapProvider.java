package cadenza.core;

import java.util.List;

import cadenza.core.patchusage.PatchUsage;

/**
 * Interface for an object which can provide information to the control mapping
 * UI elements.
 * 
 * @author Matt Putnam
 */
public interface ControlMapProvider {
  /**
   * @return the control mapping to use to populate the UI
   */
  public List<ControlMapEntry> getControlMap();
  
  /**
   * @return the list of available PatchUsages
   */
  public List<PatchUsage> getPatchUsages();
}
