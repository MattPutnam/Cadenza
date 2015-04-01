package cadenza.gui.patchusage.merge;

import java.util.List;

import javax.swing.JPanel;

import cadenza.core.patchmerge.PatchMerge;
import cadenza.core.patchusage.PatchUsage;

import common.swing.VerificationException;

@SuppressWarnings("serial")
public abstract class MergePanel<T extends PatchMerge> extends JPanel {
  private final PatchUsage _primary;
  private final List<PatchUsage> _others;
  
  public MergePanel(PatchUsage primary, List<PatchUsage> others) {
    _primary = primary;
    _others = others;
  }
  
  protected PatchUsage accessPrimary() {
    return _primary;
  }
  
  protected List<PatchUsage> accessOthers() {
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
}
