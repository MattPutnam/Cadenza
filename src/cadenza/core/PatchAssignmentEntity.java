package cadenza.core;

import cadenza.control.PerformanceController;
import cadenza.core.patchusage.PatchUsage;

/**
 * Joins PatchUsage and PatchMerge for display purposes.
 * 
 * @author Matt Putnam
 */
public interface PatchAssignmentEntity {
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
  
  /**
   * @return the range of this PatchUsage/PatchMerge
   */
  public NoteRange getNoteRange();
  
  /**
   * Sets the note range
   * @param newNoteRange the new note range
   */
  public void setNoteRange(NoteRange newNoteRange);
  
  /**
   * Determines the notes that should be played in response to a note being
   * pressed.  This response consists of the (single) PatchUsage that ends
   * up receiving the event, and the array of [note, velocity] pairs that
   * the underlying PatchUsage returns.
   * @param midiNumber the input MIDI note number
   * @param velocity the input note velocity
   * @return a Response containing the result notes
   */
  public Response receive(int midiNumber, int velocity);
  
  /**
   * Displays this PatchUsage/PatchMerge
   * @param includeRange whether or not to include the range
   * @param includeKeyboardInfo whether or not to include the keyboard in the
   *                            range; ignored if <tt>includeRange</tt>
   *                            is false.
   * @param highlightPatchName whether or not to mark up the patch name with
   *                           HTML spans to highlight it with its selected color
   * @return a display String
   */
  public String toString(boolean includeRange, boolean includeKeyboardInfo, boolean highlightPatchName);
  
  public boolean contains(Patch patch);
  
  public boolean replace(Patch target, Patch replacement);
  
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
