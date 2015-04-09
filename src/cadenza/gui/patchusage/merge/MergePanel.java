package cadenza.gui.patchusage.merge;

import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import cadenza.core.PatchAssignment;
import cadenza.core.patchmerge.PatchMerge;

import common.swing.VerificationException;

@SuppressWarnings("serial")
public abstract class MergePanel<T extends PatchMerge> extends JPanel {
  private final PatchAssignment _primary;
  private final List<PatchAssignment> _others;
  
  public MergePanel(PatchAssignment primary, List<PatchAssignment> others) {
    _primary = primary;
    _others = others;
  }
  
  protected PatchAssignment accessPrimary() {
    return _primary;
  }
  
  protected List<PatchAssignment> accessOthers() {
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
  
  protected final JComboBox<PatchAssignment> buildComboForOthers() {
    return new JComboBox<>(_others.toArray(new PatchAssignment[_others.size()]));
  }
  
  protected final JComboBox<PatchAssignment> buildComboForOthers(PatchAssignment initial) {
    final JComboBox<PatchAssignment> result = buildComboForOthers();
    result.setSelectedItem(initial);
    return result;
  }
}
