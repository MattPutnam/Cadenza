package cadenza.core.patchusage;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

import cadenza.core.Location;
import cadenza.core.Note;
import cadenza.core.Note.PitchClass;
import cadenza.core.Patch;
import cadenza.core.Scale;

/**
 * A PatchUsage that maps the white keys (or a subset of them) to other notes.
 * This allows the performer to easily play complicated scales or arpeggios by
 * simply performing a glissando on the white keys.
 * 
 * @author Matt Putnam
 */
public class CustomScalePatchUsage extends PatchUsage {
  private static final long serialVersionUID = 2L;
  
  /**
   * Maps the pitch class of the input note to the number of half steps to
   * sound the pitch.  For example, the G major scale would map F to 1 (+1
   * to F#) and all other white keys to 0.
   */
  public final Map<PitchClass, Integer> map;
  
  /**
   * The Scale that is being used, if any.  This is used for display
   * purposes only.
   */
  public final Scale scale;

  /**
   * Creates a CustomScalePatchUsage with the given base PatchUsage info and
   * the given PitchClass map.  If this map corresponds to any scale, that
   * scale name will not be reflected.
   * @param patch the patch
   * @param location the location
   * @param volume the volume
   * @param map the map of PitchClass to transposition
   */
  public CustomScalePatchUsage(Patch patch, Location location, int volume, Map<PitchClass, Integer> map) {
    super(patch, location, volume);
    this.map = Collections.unmodifiableMap(new IdentityHashMap<>(map));
    scale = null;
  }
  
  /**
   * Creates a CustomScalePatchUsage from the given Scale.  All black keys
   * are mapped to their appropriate white keys.  This works very well for
   * any diatonic scale, and reasonably well for whole tone scales and other
   * scales with fewer than 7 notes.  Doesn't work right for octatonic or
   * other types of scales with more than 7 notes.  You're free to try it
   * but you'll be missing some notes.
   * @param patch the patch
   * @param location the location
   * @param volume the volume
   * @param scale the Scale to derive the PitchClass->transposition map from
   */
  public CustomScalePatchUsage(Patch patch, Location location, int volume, Scale scale) {
    super(patch, location, volume);
    map = Collections.unmodifiableMap(scale.buildMapFromNaturals());
    this.scale = scale;
  }

  @Override
  public int[][] getNotes(int midiNumber, int velocity) {
    final PitchClass pc = Note.valueOf(midiNumber).getPitchClass();
    if (map.containsKey(pc)) {
      return new int[][] {{midiNumber + map.get(pc).intValue(), velocity}};
    } else {
      return new int[][] {};
    }
  }
  
  @Override
  boolean respondsTo_additional(int midiNumber, int velocity) {
    return map.containsKey(Note.valueOf(midiNumber).getPitchClass());
  }
  
  @Override
  String toString_additional() {
    return scale == null ? " with custom scale" : (" using scale " + scale.getName());
  }

}
