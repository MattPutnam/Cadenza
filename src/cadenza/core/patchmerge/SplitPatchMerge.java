package cadenza.core.patchmerge;

import java.util.Arrays;

import cadenza.core.Location;
import cadenza.core.Note;
import cadenza.core.patchusage.PatchUsage;
import common.collection.buffer.FixedSizeIntBuffer;

public class SplitPatchMerge extends PatchMerge {
  private final int _bufferSize;
  private final FixedSizeIntBuffer _lowerBuffer;
  private final FixedSizeIntBuffer _upperBuffer;
  
  private int _startSplit;
  private int _currentSplit;
  private int _lowerCenter;
  private int _upperCenter;
  
  public SplitPatchMerge(PatchUsage lower, PatchUsage upper, int startSplit, int bufferSize) {
    super(lower, upper);
    _startSplit = startSplit;
    
    _bufferSize = bufferSize;
    _lowerBuffer = new FixedSizeIntBuffer(_bufferSize);
    _upperBuffer = new FixedSizeIntBuffer(_bufferSize);
  }
  
  public PatchUsage getLower() {
    return accessPatchUsages().get(0);
  }
  
  public PatchUsage getUpper() {
    return accessPatchUsages().get(1);
  }
  
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
