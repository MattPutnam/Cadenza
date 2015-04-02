package cadenza.core;

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
}
