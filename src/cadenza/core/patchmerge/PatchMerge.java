package cadenza.core.patchmerge;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import cadenza.core.Location;
import cadenza.core.patchusage.PatchUsage;

/**
 * Represents a merging of two or more patch usages.  Merged patch usages share
 * the same Location, and an incoming message gets dispatched to one of the
 * constituent patch usages based on the subclass's specification.
 * 
 * @author Matt Putnam
 */
public abstract class PatchMerge {
  /**
   * Response object for determining which constituent PatchUsage should
   * receive an incoming MIDI message.
   * 
   * @see PatchUsage#getNotes(int, int)
   * @author Matt Putnam
   */
  public static class Response {
    private final PatchUsage _patchUsage;
    private final int[][] _notes;
    
    /**
     * Creates a Response with the given PatchUsage and notes
     * @param patchUsage the PatchUsage that gets played
     * @param notes an array of [note, velocity] pairs to be played
     */
    public Response(PatchUsage patchUsage, int[][] notes) {
      _patchUsage = patchUsage;
      _notes = notes;
    }
    
    /**
     * @return the PatchUsage that gets played
     */
    public PatchUsage getPatchUsage() {
      return _patchUsage;
    }
    
    /**
     * @return an array of [note, velocity] pairs to be played
     */
    public int[][] getNotes() {
      return _notes;
    }
  }
  
  private final List<PatchUsage> _patchUsages;
  
  /**
   * Creates a PatchMerge of the given patch usages.  The list must have at
   * least 2 elements.
   * @param patchUsages the PatchUsages to merge
   * @throws IllegalArgumentException if the list has less than 2 elements
   */
  public PatchMerge(List<PatchUsage> patchUsages) {
    if (patchUsages.size() < 2)
      throw new IllegalArgumentException("Can only merge 2 or more patches");
    
    _patchUsages = patchUsages;
    
    final Location location = _patchUsages.stream()
                                          .map(pu -> pu.location)
                                          .reduce(Location::union)
                                          .get();
    _patchUsages.forEach(pu -> pu.location = location);
  }
  
  /**
   * Creates a PatchMerge of the given patch usages.  Must specify at least 2
   * elements.
   * @param patchUsages the PatchUsages to merge
   * @throws IllegalArgumentException if given less than 2 elements
   */
  public PatchMerge(PatchUsage... patchUsages) {
    this(Arrays.asList(patchUsages));
  }
  
  /**
   * @return the list of PatchUsages contained in this merge
   */
  public List<PatchUsage> accessPatchUsages() {
    return _patchUsages;
  }
  
  /**
   * @return the merged Location for this merge
   */
  public Location accessLocation() {
    return _patchUsages.get(0).location;
  }
  
  /**
   * Determines the notes that should be played in response to a note being
   * pressed.  This response consists of the (single) PatchUsage that ends
   * up receiving the event, and the array of [note, velocity] pairs that
   * the underlying PatchUsage returns.
   * @param midiNumber the input MIDI note number
   * @param velocity the input note velocity
   * @return a Response containing the result notes
   */
  public abstract Response receive(int midiNumber, int velocity);
  
  /**
   * Called by the PerformanceController when the cue containing this merge is
   * loaded.  Default implementation does nothing, subclasses can override to
   * do any needed setup.
   */
  public void reset() {}
  
  @Override
  public final String toString() {
    return toString(true, false);
  }
  
  public final String toString(boolean includeLocationInfo) {
    return toString(includeLocationInfo, false);
  }
  
  public final String toString(boolean includeLocationInfo, boolean highlightPatchNames) {
    return _patchUsages.stream()
                       .map(pu -> pu.toString(false, highlightPatchNames))
                       .collect(Collectors.joining(" / ")) +
           toString_additional() +
           (includeLocationInfo ? " " + accessLocation().toString(true) : "");
  }
  
  protected abstract String toString_additional();
}
