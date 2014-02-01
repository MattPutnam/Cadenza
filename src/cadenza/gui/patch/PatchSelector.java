package cadenza.gui.patch;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import cadenza.core.Patch;
import cadenza.core.Synthesizer;

@SuppressWarnings("serial")
public class PatchSelector extends JPanel {
	private final JComboBox<Patch> _patchList;
	
	public PatchSelector(final List<Patch> patches, final List<Synthesizer> synthesizer, Patch selected) {
		super();

		_patchList = new JComboBox<>(patches.toArray(new Patch[patches.size()]));
		if (selected != null)
			_patchList.setSelectedItem(selected);
		_patchList.setRenderer(new PatchRenderer());
		
		final JButton pickNewPatchButton = new JButton("Select New Patch");
		pickNewPatchButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final PatchPickerDialog dialog = new PatchPickerDialog(PatchSelector.this, synthesizer);
				dialog.showDialog();
				if (dialog.okPressed()) {
					patches.add(dialog.getSelectedPatch());
					Collections.sort(patches);
					_patchList.insertItemAt(dialog.getSelectedPatch(), 0);
					_patchList.setSelectedIndex(0);
				}
			}
		});
		
		add(new JLabel("Select existing patch:"));
		add(_patchList);
		add(new JLabel("or"));
		add(pickNewPatchButton);
	}
	
	public JComboBox<Patch> accessCombo() {
		return _patchList;
	}
	
	public Patch getSelectedPatch() {
		return (Patch) _patchList.getSelectedItem();
	}

}
