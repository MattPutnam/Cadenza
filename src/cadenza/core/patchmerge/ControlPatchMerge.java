package cadenza.core.patchmerge;

import java.util.Arrays;

import cadenza.control.PerformanceController;
import cadenza.core.patchusage.PatchUsage;

public class ControlPatchMerge extends PatchMerge {
  private static final long serialVersionUID = 2L;
  
  private final PatchUsage[] _patchUsages;
  private final int[] _breakpoints;
  private final int _ccNum;
  
  private transient PatchUsage _active;
  
  public ControlPatchMerge(PatchUsage[] patchUsages, int[] breakpoints, int ccNum) {
    super(patchUsages);
    
    if (patchUsages.length != breakpoints.length+1)
      throw new IllegalArgumentException("Must have one more PatchUsage than breakpoints");
    
    _patchUsages = patchUsages;
    _breakpoints = breakpoints;
    _ccNum = ccNum;
  }

  @Override
  public PatchUsage accessPrimary() {
    return accessPatchUsages().get(0);
  }
  
  public PatchUsage[] getPatchUsages() {
    return _patchUsages;
  }
  
  public int[] getBreakpoints() {
    return _breakpoints;
  }
  
  public int getCCNum() {
    return _ccNum;
  }
  
  @Override
  protected void prepare_additional(PerformanceController controller) {
    // TODO: use CC tracker to set this correctly initially
    _active = _patchUsages[0];
  }
  
  @Override
  protected void controlChanged_additional(int ccNum, int ccVal) {
    if (ccNum == _ccNum) {
      int i;
      for (i = 0; i < _breakpoints.length && ccVal > _breakpoints[i]; ++i);
      _active = _patchUsages[i];
    }
  }

  @Override
  public Response receive(int midiNumber, int velocity) {
    return new Response(_active, _active.getNotes(midiNumber, velocity));
  }

  @Override
  protected String toString_additional() {
    return " CC split at " + Arrays.toString(_breakpoints);
  }

}
