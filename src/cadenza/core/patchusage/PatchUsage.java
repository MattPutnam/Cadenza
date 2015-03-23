package cadenza.core.patchusage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cadenza.control.PerformanceController;
import cadenza.core.Bank;
import cadenza.core.Location;
import cadenza.core.Patch;
import cadenza.core.Synthesizer;
import cadenza.core.effects.Effect;

import common.collection.buffer.FixedSizeIntBuffer;
import common.swing.ColorUtils;

/**
 * The usage of a patch as part of a cue.  This base class defines the patch,
 * the {@link Location}, and the volume level to set.
 * 
 * @author Matt Putnam
 */
public abstract class PatchUsage implements Serializable {
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
  
  /////////////////////////////////////////////////////////////////////////////
  // Smart split section
  /** The other PatchUsage in the split, or null if not splitting */
  public PatchUsage splitTwin;
  
  /** True for splitting above the smart split point, false for below */
  public boolean splitAbove;
  
  /** The starting split point (inclusive on upper split) */
  public int startSplit;
  
  /** The current split point (inclusive on upper split) */
  public int currentSplit;
  
  private transient FixedSizeIntBuffer _buffer = new FixedSizeIntBuffer(10);
  
  public boolean isSplit() {
    return splitTwin != null;
  }
  
  private int getCenter() {
    final int[] values = _buffer.getValues();
    return Arrays.stream(values).sum() / values.length;
  }
  
  public void copySplitDataFrom(PatchUsage other) {
    splitTwin = other.splitTwin;
    splitAbove = other.splitAbove;
    startSplit = other.startSplit;
    currentSplit = other.currentSplit;
  }
  // End smart split section
  /////////////////////////////////////////////////////////////////////////////
  
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
  
  public void splitWith(PatchUsage other, boolean thisIsAbove, int initialSplit) {
    if (isSplit())
      throw new IllegalStateException("This patch usage is already split");
    
    if (other.location.getKeyboard() != location.getKeyboard())
      throw new IllegalArgumentException("The two patch usages are on different keyboards");
    
    final Location newLocation = Location.union(location, other.location);
    location = other.location = newLocation;
    
    other.splitTwin = this;
    splitTwin = other;
    
    splitAbove = thisIsAbove;
    other.splitAbove = !thisIsAbove;
    
    startSplit = other.startSplit = initialSplit;
  }
  
  public void unsplit() {
    if (!isSplit())
      throw new IllegalStateException("Can only unsplit smart-split patches");
    
    splitTwin.splitTwin = null;
    splitTwin = null;
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
    
    if (isSplit()) {
      if (splitAbove && midiNumber < currentSplit)
        return false;
      else if (!splitAbove && midiNumber >= currentSplit)
        return false;
    }
    
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
  
  public final String buildSplitName(boolean includeKeyboardInfo, boolean highlightPatchName) {
    if (!isSplit())
      throw new IllegalStateException();
    
    if (!splitAbove) {
      final StringBuilder sb = new StringBuilder();
      
      String bgColorHTML = ColorUtils.getHTMLColorString(patch.getDisplayColor());
      String fgColorHTML = ColorUtils.getHTMLColorString(patch.getTextColor());
      
      if (highlightPatchName) sb.append("<span style='color:" + fgColorHTML + ";background:" + bgColorHTML + "'>");
      sb.append(patch.name);
      if (highlightPatchName) sb.append("</span>");
      if (volume != patch.defaultVolume)
        sb.append(" at " + volume);
      
      sb.append(" / ");
      
      final Patch twin = splitTwin.patch;
      
      bgColorHTML = ColorUtils.getHTMLColorString(twin.getDisplayColor());
      fgColorHTML = ColorUtils.getHTMLColorString(twin.getTextColor());
      
      if (highlightPatchName) sb.append("<span style='color:" + fgColorHTML + ";background:" + bgColorHTML + "'>");
      sb.append(twin.name);
      if (highlightPatchName) sb.append("</span>");
      if (splitTwin.volume != twin.defaultVolume)
        sb.append(" at " + splitTwin.volume);
      
      sb.append(" with smart split ").append(location.toString(includeKeyboardInfo));
      
      return sb.toString();
    } else {
      return splitTwin.buildSplitName(includeKeyboardInfo, highlightPatchName);
    }
  }
  
  /**
   * Appended to the end of the baseline toString information.
   * @return additional info for the toString value
   */
  abstract String toString_additional();
  
  /**
   * Called by the CadenzaController when the PatchUsage is loaded.
   * @param controller the controller
   */
  public final void prepare(PerformanceController controller) {
    if (isSplit()) {
      currentSplit = startSplit;
      
      final int center;
      if (splitAbove)
        center = (location.getLower().getMidiNumber() + startSplit) / 2;
      else
        center = (location.getUpper().getMidiNumber() + startSplit) / 2;
      
      _buffer = new FixedSizeIntBuffer(10);
      _buffer.fill(center);
    }
    
    prepare_additional(controller);
  }
  
  public final void notifyNotePressed(int noteNumber) {
    if (isSplit()) {
      _buffer.add(noteNumber);
      final int newSplit = (getCenter() + splitTwin.getCenter()) / 2;
      currentSplit = splitTwin.currentSplit = newSplit;
    }
  }
  
  /**
   * Called by the CadenzaController when the PatchUsage is loaded.
   * Default implementation does nothing, override to perform any needed setup.
   * @param controller the controller
   */
  void prepare_additional(PerformanceController controller) {}
  
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
}
