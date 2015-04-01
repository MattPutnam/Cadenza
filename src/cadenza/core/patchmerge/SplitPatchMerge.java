package cadenza.core.patchmerge;

import java.util.Arrays;

import cadenza.core.Location;
import cadenza.core.Note;
import cadenza.core.patchusage.PatchUsage;
import common.collection.buffer.FixedSizeIntBuffer;

/**
 * PatchMerge implementation of a smart split point.  This type of merge
 * entails exactly two PatchUsages, designated the lower and upper, and defers
 * to them via a floating split point.  The average location of notes in each
 * PatchUsage is tracked and the split point is adjusted to be the midpoint of
 * the two running averages.  The buffer size can be adjusted to determine how
 * much "memory" the clustering has.
 * 
 * @author Matt Putnam
 */
public class SplitPatchMerge extends PatchMerge {
  private final int _bufferSize;
  private final FixedSizeIntBuffer _lowerBuffer;
  private final FixedSizeIntBuffer _upperBuffer;
  
  private int _startSplit;
  private int _currentSplit;
  private int _lowerCenter;
  private int _upperCenter;
  
  /**
   * Creates a SplitPatchMerge
   * @param lower the lower PatchUsage
   * @param upper the upper Patchusage
   * @param startSplit the starting split point, which is set when the cue
   *                   containing this merge is loaded.
   * @param bufferSize the size of the buffer used to cluster notes into the
   *                   corresponding ranges.  Use a smaller buffer to track the
   *                   clusters more closely; use a larger buffer to make the
   *                   changes happen more gradually.
   */
  public SplitPatchMerge(PatchUsage lower, PatchUsage upper, int startSplit, int bufferSize) {
    super(lower, upper);
    _startSplit = startSplit;
    
    _bufferSize = bufferSize;
    _lowerBuffer = new FixedSizeIntBuffer(_bufferSize);
    _upperBuffer = new FixedSizeIntBuffer(_bufferSize);
  }
  
  /**
   * @return the lower PatchUsage
   */
  public PatchUsage getLower() {
    return accessPatchUsages().get(0);
  }
  
  /**
   * @return the upper PatchUsage
   */
  public PatchUsage getUpper() {
    return accessPatchUsages().get(1);
  }
  
  /**
   * @return the buffer size
   */
  public int getBufferSize() {
    return _bufferSize;
  }
  
  @Override
  public Response receive(int midiNumber, int velocity) {
    if (midiNumber < _currentSplit) {
      _lowerBuffer.add(midiNumber);
      final int[] values = _lowerBuffer.getValues();
      _lowerCenter = Arrays.stream(values).sum() / values.length;
      _currentSplit = (_lowerCenter + _upperCenter) / 2;
      
      final PatchUsage pu = getLower();
      return new Response(pu, pu.getNotes(midiNumber, velocity));
    } else {
      _upperBuffer.add(midiNumber);
      final int[] values = _upperBuffer.getValues();
      _upperCenter = Arrays.stream(values).sum() /  values.length;
      _currentSplit = (_lowerCenter + _upperCenter) / 2;
      
      final PatchUsage pu = getUpper();
      return new Response(pu, pu.getNotes(midiNumber, velocity));
    }
  }
  
  @Override
  public void reset() {
    _currentSplit = _startSplit;
    final Location l = accessLocation();
    _lowerCenter = (l.getLower().getMidiNumber() + _currentSplit) / 2;
    _upperCenter = (_currentSplit + l.getUpper().getMidiNumber()) / 2;
    
    _lowerBuffer.fill(_lowerCenter);
    _upperBuffer.fill(_upperCenter);
  }
  
  @Override
  protected String toString_additional() {
    return "Smart Split at " + Note.valueOf(_startSplit).toString();
  }
}