package cadenza.gui.patch;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import cadenza.core.Patch;
import cadenza.core.Synthesizer;

import common.swing.ColorPreviewPanel;
import common.swing.VerificationException;
import common.swing.dialog.OKCancelDialog;

@SuppressWarnings("serial")
public class PatchEditDialog extends OKCancelDialog {
	private JComboBox<Synthesizer> _synthesizerCombo;
	private JTextField _nameField;
	private JTextField _bankField;
	private JTextField _numField;
	private JTextField _volumeField;
	private ColorPreviewPanel _colorChooser;
	
	private final Patch _patch;
	private final List<Patch> _otherPatches;
	private final List<Synthesizer> _synthesizers;
	
	public PatchEditDialog(Component parent, List<Synthesizer> synthesizers, Patch patch, List<Patch> patches) {
		super(parent);
		_patch = patch;
		if (patch == null) { // new patch
			_otherPatches = patches;
		} else { // editing patch
			_otherPatches = new LinkedList<>();
			for (Patch p : patches) {
				if (p != patch) {
					_otherPatches.add(p);
				}
			}
		}
		_synthesizers = synthesizers;
	}
	
	@Override
	protected JComponent buildContent() {
		_synthesizerCombo = new JComboBox<>(_synthesizers.toArray(new Synthesizer[_synthesizers.size()]));
		if (_patch != null) _synthesizerCombo.setSelectedItem(_patch.getSynthesizer());
		_nameField = new JTextField(16);
		_bankField = new JTextField(3);
		_numField = new JTextField(3);
		_volumeField = new JTextField(3);
		_colorChooser = new ColorPreviewPanel(_patch == null ? Color.WHITE : _patch.getDisplayColor());
		
		final Box box = Box.createHorizontalBox();
		box.add(new JLabel("Synth:"));
		box.add(_synthesizerCombo);
		box.add(Box.createHorizontalStrut(8));
		box.add(new JLabel("Name:"));
		box.add(_nameField);
		box.add(Box.createHorizontalStrut(8));
		box.add(new JLabel("Bank:"));
		box.add(_bankField);
		box.add(Box.createHorizontalStrut(8));
		box.add(new JLabel("Num:"));
		box.add(_numField);
		box.add(Box.createHorizontalStrut(8));
		box.add(new JLabel("Volume (0-127):"));
		box.add(_volumeField);
		box.add(new JLabel("  Display Color: "));
		box.add(_colorChooser);
		box.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		
		final JPanel panel = new JPanel();
		panel.add(box, BorderLayout.NORTH);
		
		return panel;
	}
	
	@Override
	protected void initialize() {
		if (_patch == null) {
			_volumeField.setText("100");
		} else {
			_nameField.setText(_patch.name);
			_bankField.setText(_patch.bank);
			_numField.setText(String.valueOf(_patch.number));
			_volumeField.setText(String.valueOf(_patch.defaultVolume));
		}
		
		_nameField.requestFocus();
	}
	
	@Override
	protected String declareTitle() {
		return (_patch == null) ? "Create Patch" : "Edit Patch";
	}
	
	@Override
	protected void verify() throws VerificationException {
		final String name = _nameField.getText().trim();
		
		if (name.isEmpty())
			throw new VerificationException("Please specify a Patch name", _nameField);
		
		for (Patch patch : _otherPatches) {
			if (name.equals(patch.name)) {
				throw new VerificationException("Another patch already has this name", _nameField);
			}
		}
		
		if (_bankField.getText().trim().isEmpty())
			throw new VerificationException("Please specify a Patch bank", _bankField);
		
		try {
			int num = Integer.parseInt(_numField.getText());
			if (num < 0)
				throw new VerificationException("Patch number must be a positive integer", _numField);
		} catch (NumberFormatException nfe) {
			throw new VerificationException("Patch number must be a positive integer", _numField);
		}
		
		try {
			int num = Integer.parseInt(_volumeField.getText());
			if (num < 0)
				throw new VerificationException("Patch volume must be an integer 1 to 100", _volumeField);
		} catch (NumberFormatException nfe) {
			throw new VerificationException("Patch volume must be an integer 1 to 100", _volumeField);
		}
	}
	
	public Patch getPatch() {
		final Patch result = new Patch((Synthesizer) _synthesizerCombo.getSelectedItem(),
									   _nameField.getText(),
									   _bankField.getText(),
									   Integer.parseInt(_numField.getText()),
									   Integer.parseInt(_volumeField.getText()));
		result.setDisplayColor(_colorChooser.getSelectedColor());
		return result;
	}
}
