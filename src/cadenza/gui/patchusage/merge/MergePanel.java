package cadenza.gui.patchusage.merge;

import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import cadenza.core.PatchAssignmentEntity;
import cadenza.core.patchmerge.PatchMerge;

import common.swing.VerificationException;

@SuppressWarnings("serial")
public abstract class MergePanel<T extends PatchMerge> extends JPanel {
  private final PatchAssignmentEntity _primary;
  private final List<PatchAssignmentEntity> _others;
  
  public MergePanel(PatchAssignmentEntity primary, List<PatchAssignmentEntity> others) {
    _primary = primary;
    _others = others;
  }
  
  protected PatchAssignmentEntity accessPrimary() {
    return _primary;
  }
  
  protected List<PatchAssignmentEntity> accessOthers() {
    return _others;
  }
  
  /**
   * Initialize the UI with the given existing PatchMerge
   * @param initial the PatchMerge to mimic
   */
  public abstract void initialize(T initial);
  
  /**
   * Verify the UI
   * @throws VerificationException if there is any malformed input
   */
  public void verify() throws VerificationException {}
  
  /**
   * @return the PatchMerge represented by this panel
   */
  public abstract T getPatchMerge();
  
  protected final JComboBox<PatchAssignmentEntity> buildComboForOthers() {
    return new JComboBox<>(_others.toArray(new PatchAssignmentEntity[_others.size()]));
  }
  
  protected final JComboBox<PatchAssignmentEntity> buildComboForOthers(PatchAssignmentEntity initial) {
    final JComboBox<PatchAssignmentEntity> result = buildComboForOthers();
    result.setSelectedItem(initial);
    return result;
  }
}
