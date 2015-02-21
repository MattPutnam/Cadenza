package cadenza.gui.patch;

import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import cadenza.core.Patch;
import cadenza.core.Synthesizer;

import common.swing.SwingUtils;
import common.swing.dialog.OKCancelDialog;


@SuppressWarnings("serial")
public class PatchSelector extends JPanel {
  private final JComboBox<Patch> _patchList;
  
  public PatchSelector(final List<Patch> patches, final List<Synthesizer> synthesizer, Patch selected) {
    super();

    _patchList = new JComboBox<>(patches.toArray(new Patch[patches.size()]));
    if (selected != null)
      _patchList.setSelectedItem(selected);
    _patchList.setRenderer(new PatchRenderer());
    
    add(new JLabel("Select existing patch:"));
    add(_patchList);
    add(new JLabel("or"));
    add(SwingUtils.button("Select New Patch", e ->
      OKCancelDialog.showDialog(new PatchPickerDialog(PatchSelector.this, synthesizer), dialog -> {
        patches.add(dialog.getSelectedPatch());
        patches.sort(null);
        _patchList.insertItemAt(dialog.getSelectedPatch(), 0);
        _patchList.setSelectedIndex(0);
      })
    ));
  }
  
  public JComboBox<Patch> accessCombo() {
    return _patchList;
  }
  
  public Patch getSelectedPatch() {
    return (Patch) _patchList.getSelectedItem();
  }

}
