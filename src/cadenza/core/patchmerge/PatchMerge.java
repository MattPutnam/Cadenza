package cadenza.core.patchmerge;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import cadenza.control.PerformanceController;
import cadenza.core.NoteRange;
import cadenza.core.Patch;
import cadenza.core.PatchAssignment;
import cadenza.core.patchusage.PatchUsage;

/**
 * Represents a merging of two or more patch usages.  Merged patch usages share
 * the same NoteRange, and an incoming message gets dispatched to one of the
 * constituent patch usages based on the subclass's specification.
 * 
 * @author Matt Putnam
 */
public abstract class PatchMerge extends PatchAssignment {
  private static final long serialVersionUID = 2L;
  
  private final List<PatchAssignment> _patchAssignments;
  
  /**
   * Creates a PatchMerge of the given assignments.  The list must have at
   * least 2 elements.
   * @param assignments the PatchAssignments to merge
   * @throws IllegalArgumentException if the list has less than 2 elements
   */
  public PatchMerge(List<PatchAssignment> assignments) {
    super(assignments.get(0).getNoteRange());
    
    if (assignments.size() < 2)
      throw new IllegalArgumentException("Can only merge 2 or more patches");
    
    _patchAssignments = assignments;
    
    final NoteRange range = _patchAssignments.stream()
                                             .map(PatchAssignment::getNoteRange)
                                             .reduce(NoteRange::union)
                                             .get();
    _patchAssignments.forEach(pa -> pa.setNoteRange(range));
  }
  
  /**
   * Creates a PatchMerge of the given assignments.  Must specify at least 2
   * elements.
   * @param assignments the PatchAssignments to merge
   * @throws IllegalArgumentException if given less than 2 elements
   */
  public PatchMerge(PatchAssignment... assignments) {
    this(Arrays.asList(assignments));
  }
  
  @Override
  public void setNoteRange(NoteRange newNoteRange) {
    super.setNoteRange(newNoteRange);
    _patchAssignments.forEach(pa -> pa.setNoteRange(newNoteRange));
  }
  
  @Override
  public final boolean contains(Patch patch) {
    return _patchAssignments.stream().anyMatch(pa -> pa.contains(patch));
  }
  
  @Override
  public boolean replace(Patch target, Patch replacement) {
    return _patchAssignments.stream().anyMatch(pa -> pa.replace(target, replacement));
  }
  
  /**
   * @return the list of PatchAssignments contained in this merge
   */
  public List<PatchAssignment> accessPatchAssignments() {
    return _patchAssignments;
  }
  
  /**
   * Each subclass must declare one of its constituent PatchAssignments
   * to be the "primary", for editing purposes.
   * @return the primary PatchUsage
   */
  public abstract PatchAssignment accessPrimary();
  
  public void performReplace(PatchAssignment original, PatchAssignment replacement) {
    final int index = _patchAssignments.indexOf(original);
    if (index == -1)
      throw new IllegalStateException("Replacement failed, original was not a constituent of this");
    
    _patchAssignments.set(index, replacement);
  }
  
  @Override
  public final void prepare(PerformanceController controller) {
    _patchAssignments.forEach(pa -> pa.prepare(controller));
    prepare_additional(controller);
  }
  
  /**
   * Do any additional preparation beyond individual PatchUsage preparation.
   * Default implementation does nothing.
   * @param controller the PerformanceController
   */
  protected void prepare_additional(PerformanceController controller) {}
  
  @Override
  public final void cleanup(PerformanceController controller) {
    _patchAssignments.forEach(pa -> pa.cleanup(controller));
    cleanup_additional(controller);
  }
  
  /**
   * Do any additional cleanup beyond individual PatchUsage cleanup.  Default
   * implementation does nothing.
   * @param controller the PerformanceController
   */
  protected void cleanup_additional(PerformanceController controller) {}
  
  @Override
  public final void noteReleased(int midiNumber) {
    _patchAssignments.forEach(pa -> pa.noteReleased(midiNumber));
    noteReleased_additional(midiNumber);
  }
  
  /**
   * Do any additional work to process a note release.  Default implementation
   * does nothing.
   * @param midiNumber the MIDI number of the released note
   */
  protected void noteReleased_additional(int midiNumber) {}
  
  @Override
  public final void controlChanged(int ccNum, int ccVal) {
    _patchAssignments.forEach(pa -> pa.controlChanged(ccNum, ccVal));
    controlChanged_additional(ccNum, ccVal);
  }
  
  /**
   * Do any additional work to handle a control change.  Default implementation
   * does nothing.
   * @param ccNum the control change number
   * @param ccVal the new control value
   */
  protected void controlChanged_additional(int ccNum, int ccVal) {}
  
  @Override
  public final String toString() {
    return toString(true, false, false);
  }
  
  @Override
  public final String toString(boolean includeNoteRange, boolean includeKeyboardInfo, boolean highlightPatchNames) {
    return _patchAssignments.stream()
                            .map(pa -> {
                              final String s = pa.toString(false, false, highlightPatchNames);
                              return (pa instanceof PatchUsage) ? s : "(" + s + ")";
                            })
                            .collect(Collectors.joining(" / ")) + 
           toString_additional() +
           (includeNoteRange ? " " + getNoteRange().toString(includeKeyboardInfo) : "");
  }
  
  protected abstract String toString_additional();
}
