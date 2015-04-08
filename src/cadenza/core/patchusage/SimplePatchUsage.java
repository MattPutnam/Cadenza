package cadenza.core.patchusage;

import cadenza.control.PerformanceController;
import cadenza.core.NoteRange;
import cadenza.core.Patch;

/**
 * A PatchUsage which is just a simple range of notes, possibly transposed
 * 
 * @author Matt Putnam
 */
public class SimplePatchUsage extends PatchUsage {
  private static final long serialVersionUID = 2L;
  
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
  
  private transient int _currentNote;
  private transient PerformanceController _controller;

  public SimplePatchUsage(Patch patch, NoteRange noteRange, int volume,
      int transposition, boolean monophonic) {
    super(patch, noteRange, volume);
    this.transposition = transposition;
    this.monophonic = monophonic;
  }
  
  public SimplePatchUsage(Patch patch, NoteRange noteRange) {
    this(patch, noteRange, patch.defaultVolume, 0, false);
  }
  
  @Override
  public void prepare(PerformanceController controller) {
    _controller = controller;
  }

  @Override
  public int[][] getNotes(int midiNumber, int velocity) {
    if (monophonic) {
      _controller.sendNoteOff(_currentNote, this);
    }
    
    _currentNote = midiNumber + transposition;
    
    return new int[][] {{_currentNote, velocity}};
  }
  
  @Override
  String toString_additional() {
    final StringBuilder sb = new StringBuilder();
    
    if (monophonic)
      sb.append(" monophonic");
    
    if (transposition != 0)
      sb.append(" transposed ").append(transposition > 0 ? "+" : "").append(transposition);
    
    return sb.toString();
  }
  
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    else if (!(obj instanceof SimplePatchUsage)) return false;
    
    final SimplePatchUsage spu = (SimplePatchUsage) obj;
    return this.patch.equals(spu.patch) &&
         this.noteRange.equals(spu.noteRange) &&
         this.volume == spu.volume &&
         this.initialControlSends.equals(spu.initialControlSends) &&
         this.transposition == spu.transposition &&
         this.monophonic == spu.monophonic;
  }
  
  @Override
  public int hashCode() {
    int hashCode = patch.hashCode();
    hashCode = 31*hashCode + noteRange.hashCode();
    hashCode = 31*hashCode + volume;
    hashCode = 31*hashCode + initialControlSends.hashCode();
    hashCode = 31*hashCode + transposition;
    hashCode = 2*hashCode + (monophonic ? 1 : 0);
    
    return hashCode;
  }

}
