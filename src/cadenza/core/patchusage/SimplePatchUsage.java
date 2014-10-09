package cadenza.core.patchusage;

import cadenza.control.PerformanceController;
import cadenza.core.Location;
import cadenza.core.Patch;

/**
 * A PatchUsage which is just a simple range of notes, possibly transposed
 * 
 * @author Matt Putnam
 */
public class SimplePatchUsage extends PatchUsage {
  private static final long serialVersionUID = 1L;
  
  /**
   * The transposition, in half steps to add to the sounding note
   * (i.e. +2 to sound a whole step higher than played)
   */
  public final int transposition;
  
  /**
   * Whether or not this PatchUsage is monophonic.  Monophonic patch usages
   * will send a note off for the current note when a new note is received.
   * Useful for analog synth and solo instruments.
   */
  public final boolean monophonic;
  
  /** The volume limit, or -1 to not limit the volume */
  public final int volumeLimit;
  
  /**
   * True if the patch should sound below the limit,
   * false if it should sound at or above the limit
   */
  public final boolean isLimitToBelow;
  
  /**
   * The amount to reduce the volume, only valid when isLimitToBelow==false
   */
  public final int volumeReduction;
  
  private transient int _currentNote;
  private transient PerformanceController _controller;
  private transient int _channel;

  public SimplePatchUsage(Patch patch, Location location, int volume,
      int transposition, boolean monophonic,
      int volumeLimit, boolean isLimitToBelow, int volumeReduction) {
    super(patch, location, volume);
    this.transposition = transposition;
    this.monophonic = monophonic;
    this.volumeLimit = volumeLimit;
    this.isLimitToBelow = isLimitToBelow;
    this.volumeReduction = volumeReduction;
  }
  
  @Override
  public void prepare(PerformanceController controller) {
    _controller = controller;
    _channel = _controller.getCurrentlyAssignedChannel(this);
  }

  @Override
  public int[][] getNotes(int midiNumber, int velocity) {
    if (monophonic) {
      _controller.sendNoteOff(_currentNote, _channel);
    }
    
    _currentNote = midiNumber + transposition;
    
    if (volumeLimit == -1)
      return new int[][] {{_currentNote, velocity}};
    else {
      if (isLimitToBelow && velocity < volumeLimit)
        return new int[][] {{_currentNote, velocity}};
      else if (!isLimitToBelow && velocity >= volumeLimit)
        return new int[][] {{_currentNote, velocity - volumeReduction}};
    }
    
    return new int[][] {};
  }
  
  @Override
  String toString_additional() {
    final StringBuilder sb = new StringBuilder();
    
    if (monophonic)
      sb.append(" monophonic");
    
    if (transposition != 0)
      sb.append(" transposed ").append(transposition > 0 ? "+" : "").append(transposition);
    
    if (volumeLimit != -1)
      sb.append(" limited to ").append(isLimitToBelow ? "below " : "above (and incl.) ").append(volumeLimit);
    if (volumeLimit != -1 && !isLimitToBelow)
      sb.append(" reduced by ").append(volumeReduction);
    
    return sb.toString();
  }
  
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    else if (!(obj instanceof SimplePatchUsage)) return false;
    
    final SimplePatchUsage spu = (SimplePatchUsage) obj;
    return this.patch.equals(spu.patch) &&
         this.location.equals(spu.location) &&
         this.volume == spu.volume &&
         this.initialControlSends.equals(spu.initialControlSends) &&
         this.transposition == spu.transposition &&
         this.monophonic == spu.monophonic &&
         this.volumeLimit == spu.volumeLimit &&
         this.isLimitToBelow == spu.isLimitToBelow &&
         this.volumeReduction == spu.volumeReduction;
  }
  
  @Override
  public int hashCode() {
    int hashCode = patch.hashCode();
    hashCode = 31*hashCode + location.hashCode();
    hashCode = 31*hashCode + volume;
    hashCode = 31*hashCode + initialControlSends.hashCode();
    hashCode = 31*hashCode + transposition;
    hashCode = 2*hashCode + (monophonic ? 1 : 0);
    hashCode = 31*hashCode + volumeLimit;
    hashCode = 2*hashCode + (isLimitToBelow ? 1 : 0);
    hashCode = 31*hashCode + volumeReduction;
    
    return hashCode;
  }

}
