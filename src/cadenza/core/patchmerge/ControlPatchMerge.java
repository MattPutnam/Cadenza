package cadenza.core.patchmerge;

import java.util.Arrays;

import cadenza.control.PerformanceController;
import cadenza.core.PatchAssignment;

public class ControlPatchMerge extends PatchMerge {
  private static final long serialVersionUID = 2L;
  
  private final PatchAssignment[] _patchUsages;
  private final int[] _breakpoints;
  private final int _ccNum;
  
  private transient PatchAssignment _active;
  
  public ControlPatchMerge(PatchAssignment[] patchUsages, int[] breakpoints, int ccNum) {
    super(patchUsages);
    
    if (patchUsages.length != breakpoints.length+1)
      throw new IllegalArgumentException("Must have one more PatchUsage than breakpoints");
    
    _patchUsages = patchUsages;
    _breakpoints = breakpoints;
    _ccNum = ccNum;
  }

  @Override
  public PatchAssignment accessPrimary() {
    return accessPatchAssignments().get(0);
  }
  
  public PatchAssignment[] getPatchUsages() {
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
    return _active.receive(midiNumber, velocity);
  }

  @Override
  protected String toString_additional() {
    return " CC split at " + Arrays.toString(_breakpoints);
  }

}
