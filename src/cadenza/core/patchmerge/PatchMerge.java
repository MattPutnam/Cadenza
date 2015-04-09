package cadenza.core.patchmerge;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import cadenza.control.PerformanceController;
import cadenza.core.NoteRange;
import cadenza.core.PatchAssignmentEntity;
import cadenza.core.patchusage.PatchUsage;

/**
 * Represents a merging of two or more patch usages.  Merged patch usages share
 * the same NoteRange, and an incoming message gets dispatched to one of the
 * constituent patch usages based on the subclass's specification.
 * 
 * @author Matt Putnam
 */
public abstract class PatchMerge implements PatchAssignmentEntity, Serializable {
  private static final long serialVersionUID = 2L;
  
  private final List<PatchAssignmentEntity> _patchEntities;
  
  /**
   * Creates a PatchMerge of the given entities.  The list must have at
   * least 2 elements.
   * @param entities the PatchAssignmentEntities to merge
   * @throws IllegalArgumentException if the list has less than 2 elements
   */
  public PatchMerge(List<PatchAssignmentEntity> entities) {
    if (entities.size() < 2)
      throw new IllegalArgumentException("Can only merge 2 or more patches");
    
    _patchEntities = entities;
    
    final NoteRange range = _patchEntities.stream()
                                          .map(PatchAssignmentEntity::getNoteRange)
                                          .reduce(NoteRange::union)
                                          .get();
    _patchEntities.forEach(pu -> pu.setNoteRange(range));
  }
  
  /**
   * Creates a PatchMerge of the given entities.  Must specify at least 2
   * elements.
   * @param entities the PatchAssignmentEntities to merge
   * @throws IllegalArgumentException if given less than 2 elements
   */
  public PatchMerge(PatchAssignmentEntity... entities) {
    this(Arrays.asList(entities));
  }
  
  /**
   * @return the list of PatchAssignmentEntities contained in this merge
   */
  public List<PatchAssignmentEntity> accessPatchAssignmentEntities() {
    return _patchEntities;
  }
  
  /**
   * Each subclass must declare one of its constituent PatchAssignmentEntities
   * to be the "primary", for editing purposes.
   * @return the primary PatchUsage
   */
  public abstract PatchAssignmentEntity accessPrimary();
  
  /**
   * @return the merged NoteRange for this merge
   */
  public NoteRange accessNoteRange() {
    return _patchEntities.get(0).getNoteRange();
  }
  
  public void performReplace(PatchAssignmentEntity original, PatchAssignmentEntity replacement) {
    final int index = _patchEntities.indexOf(original);
    if (index == -1)
      throw new IllegalStateException("Replacement failed, original was not a constituent of this");
    
    _patchEntities.set(index, replacement);
  }
  
  @Override
  public final void prepare(PerformanceController controller) {
    _patchEntities.forEach(pae -> pae.prepare(controller));
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
    _patchEntities.forEach(pae -> pae.cleanup(controller));
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
    _patchEntities.forEach(pae -> pae.noteReleased(midiNumber));
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
    _patchEntities.forEach(pae -> pae.controlChanged(ccNum, ccVal));
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
    return _patchEntities.stream()
                         .map(pae -> {
                           final String s = pae.toString(false, false, highlightPatchNames);
                           return (pae instanceof PatchUsage) ? s : "(" + s + ")";
                         })
                         .collect(Collectors.joining(" / ")) + 
           toString_additional() +
           (includeNoteRange ? " " + accessNoteRange().toString(includeKeyboardInfo) : "");
  }
  
  protected abstract String toString_additional();
  
  // Compliance
  @Override
  public NoteRange getNoteRange() {
    return accessNoteRange();
  }
  
  @Override
  public void setNoteRange(NoteRange newNoteRange) {
    _patchEntities.forEach(pae -> pae.setNoteRange(newNoteRange));
  }
}
