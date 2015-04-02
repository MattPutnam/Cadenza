package cadenza.core.patchusage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cadenza.control.PerformanceController;
import cadenza.core.Bank;
import cadenza.core.Location;
import cadenza.core.Patch;
import cadenza.core.PatchAssignmentEntity;
import cadenza.core.Synthesizer;
import cadenza.core.effects.Effect;
import common.swing.ColorUtils;

/**
 * The usage of a patch as part of a cue.  This base class defines the patch,
 * the {@link Location}, and the volume level to set.
 * 
 * @author Matt Putnam
 */
public abstract class PatchUsage implements PatchAssignmentEntity, Serializable {
  private static final long serialVersionUID = 2L;
  
  /** The patch to play */
  public Patch patch;
  
  /** The location for the patch */
  public Location location;
  
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
            new Location(null, null, null), 100, 0, false, -1, true, 0);
  
  public PatchUsage(Patch patch, Location location, int volume) {
    this.patch = patch;
    this.location = location;
    this.volume = volume;
  }
  
  /**
   * Returns an array of [note, velocity] pairs that should be played given an
   * input note and velocity
   * @param midiNumber - the note number of the input note
   * @param velocity - the velocity of the input note, before effects
   * @return an array of [note, velocity] pairs to be played
   */
  public abstract int[][] getNotes(int midiNumber, int velocity);
  
  /**
   * Determines if this patch usage should play the given note.  Uses the
   * location and defers to the patch for further consideration
   * @param midiNumber the input note
   * @param velocity the input velocity
   * @return whether or not this patch usage plays the note
   */
  public final boolean respondsTo(int midiNumber, int velocity) {
    if (!location.contains(midiNumber))
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
    return toString(true, false);
  }

  public final String toString(boolean includeKeyboardInfo) {
    return toString(includeKeyboardInfo, false);
  }
  
  /**
   * Returns a String representation of this PatchUsage.  Contains the patch
   * name, the location, and the volume if it differs from the patch default.
   * @param includeKeyboardInfo whether or not to include the keyboard names
   * in the location info
   * @param highlightPatchName whether or not to highlight the patch name
   * with its display color
   * @return a String representation of this PatchUsage
   */
  @Override
  public final String toString(boolean includeKeyboardInfo, boolean highlightPatchName) {
    if (this.equals(ALL))
      return "ALL";
    
    final String bgColorHTML = ColorUtils.getHTMLColorString(patch.getDisplayColor());
    final String fgColorHTML = ColorUtils.getHTMLColorString(patch.getTextColor());
    
    final StringBuilder sb = new StringBuilder();
    
    if (highlightPatchName) sb.append("<span style='color:" + fgColorHTML + ";background:" + bgColorHTML + "'>");
    sb.append(patch.name);
    if (highlightPatchName) sb.append("</span>");
    
    sb.append(" ").append(location.toString(includeKeyboardInfo));
    if (volume != patch.defaultVolume)
      sb.append(" at " + volume);
    
    return sb.toString() + toString_additional();
  }
  
  /**
   * Appended to the end of the baseline toString information.
   * @return additional info for the toString value
   */
  abstract String toString_additional();
  
  /**
   * Called by the CadenzaController when the PatchUsage is loaded.
   * Default implementation does nothing, override to perform any needed setup.
   * @param controller the controller
   */
  public void prepare(PerformanceController controller) {}
  
  /**
   * Called by the CadenzaController when the PatchUsage is left.
   * Default implementation does nothing, override to perform any needed cleanup.
   * @param controller the controller
   */
  public void cleanup(PerformanceController controller) {}
  
  /**
   * Called by the CadenzaController when a note from this PatchUsage
   * is released.  Default implementation does nothing.
   * @param midiNumber the MIDI number of the released note
   */
  public void noteReleased(int midiNumber) {}
  
  // Compliance
  @Override
  public Location getLocation() {
    return location;
  }
}
