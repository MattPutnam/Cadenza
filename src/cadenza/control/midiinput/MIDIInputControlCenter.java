package cadenza.control.midiinput;

import java.awt.KeyboardFocusManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;

import common.midi.MidiUtilities;

public class MIDIInputControlCenter {
	private volatile AcceptsKeyboardInput _componentWithFocus;
	private volatile boolean _active;
	
	public MIDIInputControlCenter() {
		_active = true;
		
		final KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		manager.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				final String prop = evt.getPropertyName();
				if ("focusOwner".equals(prop)) {
					if (evt.getNewValue() instanceof AcceptsKeyboardInput)
						_componentWithFocus = (AcceptsKeyboardInput) evt.getNewValue();
					else
						_componentWithFocus = null;
				}
			}
		});
	}
	
	public void setActive(boolean active) {
		_active = active;
	}
	
	public void send(MidiMessage message) {
		if (_active && (_componentWithFocus != null) && (message instanceof ShortMessage)) {
			final ShortMessage sm = (ShortMessage) message;
			if (MidiUtilities.isNoteOn(sm))
				_componentWithFocus.keyPressed(sm.getChannel(), sm.getData1(), sm.getData2());
			else if (MidiUtilities.isNoteOff(sm))
				_componentWithFocus.keyReleased(sm.getChannel(), sm.getData1());
			else if (MidiUtilities.isControlChange(sm))
				_componentWithFocus.controlReceived(sm.getChannel(), sm.getData1(), sm.getData2());
		}
	}
}
