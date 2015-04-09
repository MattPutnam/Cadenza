package cadenza.core.patchmerge;

import cadenza.core.PatchAssignmentEntity;

/**
 * PatchMerge implementation that splits based on input velocity.  Velocities
 * below a threshold are played on the primary PatchUsage, while velocities
 * above the threshold are played on the secondary PatchUsage with a specified
 * reduction.
 *  
 * @author Matt Putnam
 */
public class VelocityPatchMerge extends PatchMerge {
  private static final long serialVersionUID = 2L;
  
  private final int _threshold;
  private final int _reduction;
  
  /**
   * Creates a VelocityPatchMerge
   * @param main the main PatchUsage, which is played for notes with velocity
   *             below the threshold
   * @param high the secondary PatchUsage, which is played for notes with
   *             velocity above (or including) the threshold
   * @param threshold the input velocity to split around
   * @param reduction the volume to reduce the upper PatchUsage by
   */
  public VelocityPatchMerge(PatchAssignmentEntity main, PatchAssignmentEntity high, int threshold, int reduction) {
    super(main, high);
    _threshold = threshold;
    _reduction = reduction;
  }

  /**
   * @return the primary PatchUsage, below the threshold
   */
  @Override
  public PatchAssignmentEntity accessPrimary() {
    return accessPatchAssignmentEntities().get(0);
  }
  
  /**
   * @return the upper PatchUsage, above the threshold
   */
  public PatchAssignmentEntity accessSecondary() {
    return accessPatchAssignmentEntities().get(1);
  }
  
  /**
   * @return the velocity threshold
   */
  public int getThreshold() {
    return _threshold;
  }
  
  /**
   * @return the amount to reduce the upper PatchUsage by
   */
  public int getReduction() {
    return _reduction;
  }

  @Override
  public Response receive(int midiNumber, int velocity) {
    if (velocity < _threshold) {
      return accessPrimary().receive(midiNumber, velocity);
    } else {
      return accessSecondary().receive(midiNumber, velocity);
    }
  }

  @Override
  protected String toString_additional() {
    return " Velocity split at " + _threshold;
  }

}
