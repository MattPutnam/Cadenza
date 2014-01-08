package cadenza.gui.common;

import java.awt.Font;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import cadenza.control.midiinput.AcceptsKeyboardInput;
import cadenza.core.Keyboard;

import common.swing.NonNegativeIntField;
import common.swing.SwingUtils;
import common.swing.VerificationException;

public class VolumeField extends JPanel implements AcceptsKeyboardInput {
	private final NonNegativeIntField _field;
	private final JLabel _label;
	
	public VolumeField(int volume) {
		super();
		
		_field = new NonNegativeIntField(volume);
		_field.setColumns(3);
		SwingUtils.freezeSize(_field);
		
		_label = new JLabel("Volume [0-127]:");
		
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		add(_label);
		add(_field);
	}
	
	public void verify() throws VerificationException {
		if (_field.getText().isEmpty())
			throw new VerificationException("Volume must be 0-127", _field);
		
		final int val = getVolume();
		if (val < 0 || val > 127)
			throw new VerificationException("Volume must be 0-127", _field);
	}
	
	public void setVolume(int volume) {
		if (volume < 0 || volume > 127)
			throw new IllegalArgumentException("Volume must be 0-127");
		_field.setInt(volume);
	}
	
	public int getVolume() {
		return _field.getInt();
	}
	
	public NonNegativeIntField accessField() {
		return _field;
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		_field.setEnabled(enabled);
		_label.setEnabled(enabled);
	}
	
	@Override
	public void setFont(Font font) {
		super.setFont(font);
		if (_field != null)
			_field.setFont(font);
		if (_label != null)
			_label.setFont(font);
	}
	
	@Override
	public void controlReceived(Keyboard source, int ccNumber, int value) {
		_field.setInt(value);
	}
	
	@Override
	public void keyPressed(Keyboard source, int midiNumber, int velocity) { /* ignore */ }
	@Override
	public void keyReleased(Keyboard source, int midiNumber) { /* ignore */ }

}
