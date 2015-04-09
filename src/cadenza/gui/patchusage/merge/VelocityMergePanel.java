package cadenza.gui.patchusage.merge;

import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;

import cadenza.core.NoteRange;
import cadenza.core.PatchAssignment;
import cadenza.core.patchmerge.VelocityPatchMerge;

import common.swing.IntField;
import common.swing.SimpleGrid;
import common.swing.SwingUtils;

@SuppressWarnings("serial")
public class VelocityMergePanel extends MergePanel<VelocityPatchMerge> {
  private final JComboBox<PatchAssignment> _patchUsageCombo;
  private final IntField _thresholdField;
  private final IntField _reductionField;
  private final MergeRangePanel _rangePanel;

  public VelocityMergePanel(PatchAssignment primary, List<PatchAssignment> others) {
    super(primary, others);
    
    _patchUsageCombo = buildComboForOthers();
    _thresholdField = new IntField(100, 0, 127);
    _reductionField = new IntField(40, 0, 127);
    _rangePanel = new MergeRangePanel(primary.getNoteRange());
    
    _patchUsageCombo.addActionListener(e ->
        _rangePanel.setNoteRange(NoteRange.union(primary.getNoteRange(),
                                                 ((PatchAssignment) _patchUsageCombo.getSelectedItem()).getNoteRange())));
    
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    add(SwingUtils.buildCenteredRow(new SimpleGrid(new JComponent[][] {
        {new JLabel(         "Play patch:"), _patchUsageCombo},
        {new JLabel("For velocities over:"), _thresholdField },
        {new JLabel(      "And reduce by:"), _reductionField }
    }, Alignment.BASELINE, Alignment.TRAILING)));
    add(SwingUtils.buildCenteredRow(_rangePanel));
  }

  @Override
  public void initialize(VelocityPatchMerge initial) {
    _patchUsageCombo.setSelectedItem(initial.accessPrimary());
    _thresholdField.setInt(initial.getThreshold());
    _reductionField.setInt(initial.getReduction());
  }

  @Override
  public VelocityPatchMerge getPatchMerge() {
    final VelocityPatchMerge result = new VelocityPatchMerge(accessPrimary(),
        (PatchAssignment) _patchUsageCombo.getSelectedItem(),
        _thresholdField.getInt(), _reductionField.getInt());
    result.setNoteRange(_rangePanel.getNoteRange());
    return result;
  }

}
