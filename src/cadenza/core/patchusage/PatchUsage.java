package cadenza.core.patchusage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cadenza.core.Bank;
import cadenza.core.NoteRange;
import cadenza.core.Patch;
import cadenza.core.PatchAssignment;
import cadenza.core.Synthesizer;
import cadenza.core.effects.Effect;

import common.swing.ColorUtils;

/**
 * The usage of a patch as part of a cue.  This base class defines the patch,
 * the {@link NoteRange}, and the volume level to set.
 * 
 * @author Matt Putnam
 */
public abstract class PatchUsage extends PatchAssignment implements Serializable {
  private static final long serialVersionUID = 2L;
  
  /** The patch to play */
  public Patch patch;
  
  /** The volume to play the patch */
  public int volume;
  
  /** 
   * A list of effects to use for this patch only.  Comes before cue and
   * global effects
   */
  public List<Effect> effects = new ArrayList<>();
  
  /**
   * The mapping of control change number -> control value that
   * should be sent when this patch is first loaded
   */
  public Map<Integer, Integer> initialControlSends = new HashMap<>();
  
  /**
   * A PatchUsage singleton representing all patch usages, used for
   * the global mapping editor
   */
  public static final PatchUsage ALL = new SimplePatchUsage(
      new Patch(Synthesizer.TEMP, "ALL", new Bank("TEMP"), 0),
            new NoteRange(null, null, null), 100, 0, false);
  
  public PatchUsage(Patch patch, NoteRange noteRange, int volume) {
    super(noteRange);
    this.patch = patch;
    this.volume = volume;
  }
  
  @Override
  public final boolean contains(Patch other) {
    return other == patch;
  }
  
  @Override
  public final boolean replace(Patch target, Patch replacement) {
    if (target == patch) {
      patch = replacement;
      return true;
    } else {
      return false;
    }
  }
  
  @Override
  public final Response receive(int midiNumber, int velocity) {
    return new Response(this, getNotes(midiNumber, velocity));
  }
  
  public abstract int[][] getNotes(int midiNumber, int velocity);
  
  /**
   * Determines if this patch usage should play the given note.  Uses the
   * note range and defers to the patch for further consideration
   * @param midiNumber the input note
   * @param velocity the input velocity
   * @return whether or not this patch usage plays the note
   */
  public final boolean respondsTo(int midiNumber, int velocity) {
    if (!getNoteRange().contains(midiNumber))
      return false;
    
    return respondsTo_additional(midiNumber, velocity);
  }
  
  /**
   * Additional logic for the patch usage implementation to determine if it
   * should play the given note.  Default implementation returns <tt>true</tt>.
   * @param midiNumber the input note
   * @param velocity the input velocity
   * @return whether or not this patch usage plays the note
   */
  boolean respondsTo_additional(int midiNumber, int velocity) {
    return true;
  }
  
  @Override
  public final String toString() {
    return toString(true, true, false);
  }
  
  @Override
  public final String toString(boolean includeNoteRange, boolean includeKeyboardInfo, boolean highlightPatchName) {
    if (this.equals(ALL))
      return "ALL";
    
    final String bgColorHTML = ColorUtils.getHTMLColorString(patch.getDisplayColor());
    final String fgColorHTML = ColorUtils.getHTMLColorString(patch.getTextColor());
    
    final StringBuilder sb = new StringBuilder();
    
    if (highlightPatchName) sb.append("<html><span style='color:" + fgColorHTML + ";background:" + bgColorHTML + "'>");
    sb.append(patch.name);
    if (highlightPatchName) sb.append("</span>");
    
    if (includeNoteRange) sb.append(" ").append(getNoteRange().toString(includeKeyboardInfo));
    if (volume != patch.defaultVolume)
      sb.append(" at " + volume);
    
    return sb.toString() + toString_additional();
  }
  
  /**
   * Appended to the end of the baseline toString information.
   * @return additional info for the toString value
   */
  abstract String toString_additional();
}
