package cadenza.core;

public interface PatchAssignmentEntity {
  public Location getLocation();
  
  public String toString(boolean includeKeyboardInfo, boolean highlightPatchName);
}
