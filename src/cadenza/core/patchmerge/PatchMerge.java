package cadenza.core.patchmerge;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import cadenza.control.PerformanceController;
import cadenza.core.Location;
import cadenza.core.PatchAssignmentEntity;
import cadenza.core.patchusage.PatchUsage;

/**
 * Represents a merging of two or more patch usages.  Merged patch usages share
 * the same Location, and an incoming message gets dispatched to one of the
 * constituent patch usages based on the subclass's specification.
 * 
 * @author Matt Putnam
 */
public abstract class PatchMerge implements PatchAssignmentEntity, Serializable {
  private static final long serialVersionUID = 2L;

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
   * Each subclass must declare one of its constituent PatchUsages to be the "primary",
   * for editing purposes.
   * @return the primary PatchUsage
   */
  public abstract PatchUsage accessPrimary();
  
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
   * Called by the CadenzaController when the PatchMerge is loaded.  Prepares
   * the underlying PatchUsages and defers to
   * {@link #prepare_additional(PerformanceController)} for any additional
   * work needed.
   * @param controller the PerformanceController
   */
  public final void prepare(PerformanceController controller) {
    _patchUsages.forEach(pu -> pu.prepare(controller));
    prepare_additional(controller);
  }
  
  /**
   * Do any additional preparation beyond individual PatchUsage preparation.
   * Default implementation does nothing.
   * @param controller the PerformanceController
   */
  protected void prepare_additional(PerformanceController controller) {}
  
  /**
   * Called by the CadenzaController when the PatchMerge is left.  Cleans up
   * the underlying PatchUsages and defers to
   * {@link #cleanup_additional(PerformanceController)} for any additional work
   * needed.
   * @param controller the PerformanceController
   */
  public final void cleanup(PerformanceController controller) {
    _patchUsages.forEach(pu -> pu.cleanup(controller));
    cleanup_additional(controller);
  }
  
  /**
   * Do any additional cleanup beyond individual PatchUsage cleanup.  Default
   * implementation does nothing.
   * @param controller the PerformanceController
   */
  protected void cleanup_additional(PerformanceController controller) {}
  
  /**
   * Called by the CadenzaController when a note from this PatchMerge
   * is released.  Calls {@link PatchUsage#noteReleased(int)} on the underlying
   * PatchUsages and defers to {@link #noteReleased_additional(int)} for any
   * additional work needed.
   * @param midiNumber the MIDI number of the released note
   */
  public void noteReleased(int midiNumber) {
    _patchUsages.forEach(pu -> pu.noteReleased(midiNumber));
    noteReleased_additional(midiNumber);
  }
  
  /**
   * Do any additional work to process a note release.  Default implementation
   * does nothing.
   * @param midiNumber the MIDI number of the released note
   */
  protected void noteReleased_additional(int midiNumber) {}
  
  @Override
  public final String toString() {
    return toString(true, false, false);
  }
  
  @Override
  public final String toString(boolean includeLocation, boolean includeKeyboardInfo, boolean highlightPatchNames) {
    return _patchUsages.stream()
                       .map(pu -> pu.toString(false, false, highlightPatchNames))
                       .collect(Collectors.joining(" / ")) + 
           toString_additional() +
           (includeLocation ? " " + accessLocation().toString(includeKeyboardInfo) : "");
  }
  
  protected abstract String toString_additional();
  
  // Compliance
  @Override
  public Location getLocation() {
    return accessLocation();
  }
}
