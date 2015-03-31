package cadenza.core.patchmerge;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import cadenza.core.Location;
import cadenza.core.patchusage.PatchUsage;

public abstract class PatchMerge {
  public static class Response {
    private final PatchUsage _patchUsage;
    private final int[][] _notes;
    
    public Response(PatchUsage patchUsage, int[][] notes) {
      _patchUsage = patchUsage;
      _notes = notes;
    }
    
    public PatchUsage getPatchUsage() {
      return _patchUsage;
    }
    
    public int[][] getNotes() {
      return _notes;
    }
  }
  
  private final List<PatchUsage> _patchUsages;
  
  public PatchMerge(List<PatchUsage> patchUsages) {
    if (patchUsages.size() < 2)
      throw new IllegalArgumentException("Can only merge 2 or more patches");
    
    _patchUsages = patchUsages;
    
    final Iterator<PatchUsage> iterator = _patchUsages.iterator();
    Location location = iterator.next().location;
    while (iterator.hasNext())
      location = Location.union(location, iterator.next().location);
    
    final Location l = location;
    _patchUsages.forEach(pu -> pu.location = l);
  }
  
  public PatchMerge(PatchUsage... patchUsages) {
    this(Arrays.asList(patchUsages));
  }
  
  public List<PatchUsage> accessPatchUsages() {
    return _patchUsages;
  }
  
  public Location accessLocation() {
    return _patchUsages.get(0).location;
  }
  
  public abstract Response receive(int midiNumber, int velocity);
  
  public void reset() {}
  
  @Override
  public final String toString() {
    return toString(true, false);
  }
  
  public final String toString(boolean includeLocationInfo) {
    return toString(includeLocationInfo, false);
  }
  
  public final String toString(boolean includeLocationInfo, boolean highlightPatchNames) {
    return _patchUsages.stream()
                       .map(pu -> pu.toString(false, highlightPatchNames))
                       .collect(Collectors.joining(" / ")) +
           toString_additional() +
           (includeLocationInfo ? " " + accessLocation().toString(true) : "");
  }
  
  protected abstract String toString_additional();
}
