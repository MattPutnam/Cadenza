package cadenza.core;

import cadenza.control.PerformanceController;

/**
 * Joins PatchUsage and PatchMerge for display purposes.
 * 
 * @author Matt Putnam
 */
public interface PatchAssignmentEntity {
  /**
   * @return the location of this PatchUsage/PatchMerge
   */
  public Location getLocation();
  
  /**
   * Displays this PatchUsage/PatchMerge
   * @param includeLocation whether or not to include the location
   * @param includeKeyboardInfo whether or not to include the keyboard in the
   *                            location; ignored if <tt>includeLocation</tt>
   *                            is false.
   * @param highlightPatchName whether or not to mark up the patch name with
   *                           HTML spans to highlight it with its selected color
   * @return a display String
   */
  public String toString(boolean includeLocation, boolean includeKeyboardInfo, boolean highlightPatchName);
  
  /**
   * Called by the PerformanceController when the PatchUsage is loaded.
   * Default implementation does nothing, override to perform any needed setup.
   * @param controller the controller
   */
  public default void prepare(PerformanceController controller) {}
  
  /**
   * Called by the PerformanceController when the PatchUsage is left.
   * Default implementation does nothing, override to perform any needed cleanup.
   * @param controller the controller
   */
  public default void cleanup(PerformanceController controller) {}
  
  /**
   * Called by the PerformanceController when a note from this PatchUsage
   * is released.  Default implementation does nothing.
   * @param midiNumber the MIDI number of the released note
   */
  public default void noteReleased(int midiNumber) {}
  
  /**
   * Called by the PerformanceController when a control change mapping to this
   * PatchUsage/PatchMerge changes value.  Default implementation does nothing.
   * @param ccNum the control change number.  This is the raw CC#; if there is
   *              a control mapping present, the value received in this method
   *              is the pre-mapped value.  It is also received no matter what,
   *              even if mapping would cause it to not take effect in the
   *              performance.
   * @param ccVal the new control value
   */
  public default void controlChanged(int ccNum, int ccVal) {}
}
