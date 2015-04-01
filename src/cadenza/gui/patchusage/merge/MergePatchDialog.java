package cadenza.gui.patchusage.merge;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import cadenza.core.patchmerge.PatchMerge;
import cadenza.core.patchmerge.SplitPatchMerge;
import cadenza.core.patchusage.PatchUsage;

import common.swing.VerificationException;
import common.swing.dialog.OKCancelDialog;

@SuppressWarnings("serial")
public class MergePatchDialog extends OKCancelDialog {
  private static final String TOP_TEXT = "Merge two patches into one by selecting a split rule:";
  
  private final PatchMerge _initial;
  private final PatchUsage _primary;
  private final List<PatchUsage> _others;
  
  private JTabbedPane _tabbedPane;

  public MergePatchDialog(Component parent, PatchMerge patchMerge, PatchUsage primary, List<PatchUsage> others) {
    super(parent);
    _initial = patchMerge;
    _primary = primary;
    _others = others;
  }

  @Override
  protected JComponent buildContent() {
    _tabbedPane = new JTabbedPane();
    
    final SmartSplitPanel ssp = new SmartSplitPanel(_primary, _others);
    
    _tabbedPane.addTab("Smart Split Point", ssp);
    
    if (_initial instanceof SplitPatchMerge)
      ssp.initialize((SplitPatchMerge) _initial);
    
    final JPanel panel = new JPanel(new BorderLayout());
    panel.add(new JLabel(TOP_TEXT), BorderLayout.NORTH);
    panel.add(_tabbedPane, BorderLayout.CENTER);
    return panel;
  }

  @Override
  protected String declareTitle() {
    return _initial == null ? "Create Merged Patch" : "Edit Merged Patch";
  }

  @Override
  protected void verify() throws VerificationException {
    ((MergePanel<?>) _tabbedPane.getSelectedComponent()).verify();
  }
  
  public PatchMerge getPatchMerge() {
    return ((MergePanel<?>) _tabbedPane.getSelectedComponent()).getPatchMerge();
  }

}
