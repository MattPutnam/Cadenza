package cadenza.core.patchusage;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cadenza.core.Location;
import cadenza.core.Patch;

/**
 * A PatchUsage which causes a set of notes to be played when a single note is
 * played.  Also called "slave notes".
 * 
 * @author Matt Putnam
 */
public class GhostNotePatchUsage extends PatchUsage {
  private static final long serialVersionUID = 2L;
  
  /** The map of notes to play when a note is pressed */
  public Map<Integer, List<Integer>> ghosts;

  public GhostNotePatchUsage(Patch patch, Location location,
      int volume, Map<Integer, List<Integer>> ghosts) {
    super(patch, location, volume);
    this.ghosts = Collections.unmodifiableMap(new LinkedHashMap<>(ghosts));
  }

  @Override
  public int[][] getNotes(int midiNumber, int velocity) {
    final List<Integer> ghostEntry = ghosts.get(Integer.valueOf(midiNumber));
    if (ghostEntry == null) {
      return new int[][] {};
    } else {
      final int size = ghostEntry.size();
      final int[][] result = new int[size][2];
      for (int i = 0; i < size; ++i) {
        result[i] = new int[] { ghostEntry.get(i).intValue(), velocity };
      }
      return result;
    }
  }
  
  @Override
  boolean respondsTo_additional(int midiNumber, int velocity) {
    return ghosts.containsKey(Integer.valueOf(midiNumber));
  }
  
  @Override
  String toString_additional() {
    return " with " + ghosts.size() + " ghost notes";
  }

}
