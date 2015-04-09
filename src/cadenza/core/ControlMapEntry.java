package cadenza.core;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import cadenza.core.patchusage.PatchUsage;

public class ControlMapEntry implements Serializable {
  private static final long serialVersionUID = 2L;
  
  public int sourceCC;
  public List<Integer> destCCs;
  public List<PatchUsage> destPatches;
  
  public ControlMapEntry(int sourceCC, List<Integer> destCCs, List<PatchUsage> destPatches) {
    this.sourceCC = sourceCC;
    this.destCCs = destCCs;
    this.destPatches = destPatches;
  }
  
  public String getDestCCString() {
    return destCCs.stream()
                  .map(i -> i + ": " + ControlNames.getName(i))
                  .collect(Collectors.joining(", "));
  }
  
  public String getDestPatchesString() {
    if (destPatches.get(0).equals(PatchUsage.ALL))
      return "ALL";
    
    return destPatches.stream()
                      .map(pu -> pu.patch.name + " " + pu.getNoteRange().toString(true))
                      .collect(Collectors.joining(", "));
  }
  
  @Override
  public String toString() {
    return sourceCC + ": " + ControlNames.getName(sourceCC) + " -> " + getDestCCString() + " for " + getDestPatchesString();
  }
}
