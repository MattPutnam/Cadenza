package cadenza.gui.common;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

import cadenza.core.Patch;
import cadenza.core.Synthesizer;
import cadenza.gui.CadenzaFrame;
import cadenza.gui.patch.PatchPickerDialog;

import common.swing.SwingUtils;
import common.swing.VerificationException;
import common.swing.dialog.OKCancelDialog;

@SuppressWarnings("serial")
public class SinglePatchSelectionDialog extends OKCancelDialog {
  private final CadenzaFrame _frame;
  private final List<Patch> _patches;
  private final List<Synthesizer> _synthesizers;
  
  private List<JRadioButton> _radioButtons;
  private ButtonGroup _radioButtonGroup;
  private JButton _pickNewButton;
  
  private Patch _selectedPatch;
  
  public SinglePatchSelectionDialog(CadenzaFrame frame, Patch selected, List<Patch> patches, List<Synthesizer> synthesizers) {
    super(frame);
    _frame = frame;
    _synthesizers = synthesizers;
    
    if (selected == null)
      _patches = patches;
    else {
      _patches = new ArrayList<>(patches.size());
      for (final Patch p : patches)
        if (!p.equals(selected))
          _patches.add(p);
    }
  }
  
  @Override
  protected JComponent buildContent() {
    _radioButtons = new ArrayList<>(_patches.size());
    _radioButtonGroup = new ButtonGroup();
    
    final Box patchButtons = Box.createVerticalBox();
    for (final Patch patch : _patches) {
      final JRadioButton patchButton = new JRadioButton(patch.toString());
      _radioButtonGroup.add(patchButton);
      _radioButtons.add(patchButton);
      patchButtons.add(patchButton);
      
      patchButton.addActionListener(e -> _selectedPatch = patch);
    }
    
    final JScrollPane scrollPane = new JScrollPane(patchButtons);
    
    _pickNewButton = SwingUtils.button("Pick new patch...", e -> {
      OKCancelDialog.showDialog(new PatchPickerDialog(_frame, _synthesizers), dialog -> {
        _selectedPatch = dialog.getSelectedPatch();
        pressOK();
      });
    });
    
    final JPanel panel = new JPanel();
    panel.add(scrollPane, BorderLayout.CENTER);
    panel.add(_pickNewButton, BorderLayout.SOUTH);
    
    return panel;
  }
  
  @Override
  protected void initialize() {
    setSize(400, 300);
  }
  
  @Override
  protected String declareTitle() {
    return "Select Patch";
  }
  
  @Override
  protected void verify() throws VerificationException {
    if (_selectedPatch == null)
      throw new VerificationException("Please select a patch", null);
  }
  
  public Patch getSelectedPatch() {
    return _selectedPatch;
  }
  
}
