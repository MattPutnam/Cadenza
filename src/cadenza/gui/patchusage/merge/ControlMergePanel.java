package cadenza.gui.patchusage.merge;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.GroupLayout.Alignment;

import cadenza.core.patchmerge.ControlPatchMerge;
import cadenza.core.patchusage.PatchUsage;
import cadenza.gui.common.ControlCombo;
import common.swing.IntField;
import common.swing.SimpleGrid;
import common.swing.SwingUtils;
import common.swing.VerificationException;

@SuppressWarnings("serial")
public class ControlMergePanel extends MergePanel<ControlPatchMerge> {

  private final List<JComboBox<PatchUsage>> _patchCombos;
  private final List<IntField> _splitFields;
  private final ControlCombo _ccNumCombo;
  
  private final JButton _addButton;
  private final JPanel _mainPanel;
  
  public ControlMergePanel(PatchUsage primary, List<PatchUsage> others) {
    super(primary, others);
    
    _patchCombos = new ArrayList<>();
    _splitFields = new ArrayList<>();
    _ccNumCombo = new ControlCombo(Integer.valueOf(1)); // mod wheel
    
    _addButton = SwingUtils.button("Add new", e -> {
      _patchCombos.add(buildComboForOthers());
      
      final int init = (_splitFields.get(_splitFields.size()-1).getInt() + 127) / 2;
      _splitFields.add(new IntField(init, 0, 127));
      
      update();
    });
    
    _mainPanel = new JPanel();
    
    final Box top = SwingUtils.buildCenteredRow(new JLabel("Select between patches using CC#"), _ccNumCombo);
    final JScrollPane center = new JScrollPane(_mainPanel);
    center.setBorder(null);
    
    // initializing for new merge:
    _patchCombos.add(buildComboForOthers());
    _splitFields.add(new IntField(64, 0, 127));
    
    setLayout(new BorderLayout());
    add(top, BorderLayout.NORTH);
    add(center, BorderLayout.CENTER);
    
    update();
  }
  
  private void update() {
    _mainPanel.removeAll();
    
    final int size = _patchCombos.size();
    
    final JComponent[][] grid = new JComponent[size+2][3];
    grid[0][1] = _addButton;
    for (int rowIndex = 1, i = size-1; i >= 0; --i, ++rowIndex) {
      grid[rowIndex][0] = SwingUtils.buildCenteredRow(new JLabel("When control value is over"),
                                                      _splitFields.get(i),
                                                      new JLabel("play:"));
      grid[rowIndex][1] = _patchCombos.get(i);
      if (i > 0) {
        final int fi = i;
        grid[rowIndex][2] = SwingUtils.button("Delete", e -> {
                              _patchCombos.remove(fi);
                              _splitFields.remove(fi);
                              update();
                            });
      }
    }
    grid[size+1][0] = SwingUtils.buildRightAlignedRow(new JLabel("When control value is below lowest, play:"));
    grid[size+1][1] = new JLabel(accessPrimary().toString(false, false, false));
    
    _mainPanel.add(new SimpleGrid(grid, Alignment.BASELINE, Alignment.CENTER));
    
    _mainPanel.revalidate();
    _mainPanel.repaint();
  }

  @Override
  public void initialize(ControlPatchMerge initial) {
    final PatchUsage[] usages = initial.getPatchUsages();
    final int[] breakpoints = initial.getBreakpoints();
    
    if (usages[0] != accessPrimary())
      throw new IllegalArgumentException("Primaries don't match");
    
    _patchCombos.clear();
    for (int i = 1; i < usages.length; ++i)
      _patchCombos.add(buildComboForOthers(usages[i]));
    
    _splitFields.clear();
    for (int i = 0; i < breakpoints.length; ++i)
      _splitFields.add(new IntField(breakpoints[i], 0, 127));
    
    _ccNumCombo.setSelectedIndex(initial.getCCNum());
    
    update();
  }
  
  @Override
  public void verify() throws VerificationException {
    for (int i = 0; i < _splitFields.size()-1; ++i)
      if (_splitFields.get(i).getInt() >= _splitFields.get(i+1).getInt())
        throw new VerificationException("Control values must be ascending", _splitFields.get(i+1));
  }

  @Override
  public ControlPatchMerge getPatchMerge() {
    final List<PatchUsage> patchUsages = new ArrayList<>();
    patchUsages.add(accessPrimary());
    _patchCombos.forEach(combo -> patchUsages.add((PatchUsage) combo.getSelectedItem()));
    final PatchUsage[] puArray = patchUsages.toArray(new PatchUsage[patchUsages.size()]);
    
    final int[] breakpoints = _splitFields.stream().mapToInt(IntField::getInt).toArray();
    
    return new ControlPatchMerge(puArray, breakpoints, _ccNumCombo.getSelectedIndex());
  }

}
