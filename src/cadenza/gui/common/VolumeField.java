package cadenza.gui.common;

import cadenza.control.midiinput.AcceptsKeyboardInput;
import cadenza.core.Keyboard;

import common.swing.NonNegativeIntField;
import common.swing.SwingUtils;
import common.swing.VerificationException;

public class VolumeField extends NonNegativeIntField implements AcceptsKeyboardInput {
	public VolumeField(int volume) {
		super(volume);
		setColumns(3);
		SwingUtils.freezeSize(this);
	}
	
	public void verify() throws VerificationException {
		if (getText().isEmpty())
			throw new VerificationException("Volume must be 0-127", this);
		
		final int val = getVolume();
		if (val < 0 || val > 127)
			throw new VerificationException("Volume must be 0-127", this);
	}
	
	public void setVolume(int volume) {
		if (volume < 0 || volume > 127)
			throw new IllegalArgumentException("Volume must be 0-127");
		setInt(volume);
	}
	
	public int getVolume() {
		return getInt();
	}
	
	@Override
	public void controlReceived(Keyboard source, int ccNumber, final int value) { 
		SwingUtils.doInSwing(new Runnable() {
			@Override
			public void run() {
				setInt(value);
				selectAll();
			}
		}, false);
	}
	
	@Override
	public void keyPressed(Keyboard source, int midiNumber, int velocity) { /* ignore */ }
	@Override
	public void keyReleased(Keyboard source, int midiNumber) { /* ignore */ }

}
