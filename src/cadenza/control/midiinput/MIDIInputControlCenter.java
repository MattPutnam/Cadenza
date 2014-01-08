package cadenza.control.midiinput;

import java.awt.Component;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;

import common.midi.MidiUtilities;

public class MIDIInputControlCenter {
	private AcceptsKeyboardInput _componentWithFocus;
	private boolean _active;
	
	private MIDIInputControlCenter() {}
	private static final MIDIInputControlCenter INSTANCE = new MIDIInputControlCenter();
	public static MIDIInputControlCenter getInstance() { return INSTANCE; }
	
	public void registerComponent(final AcceptsKeyboardInput component) {
		if (!(component instanceof Component))
			throw new IllegalArgumentException("Argument must be a Component");
		
		((Component) component).addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent _) {
				_componentWithFocus = component;
			}
		});
	}
	
	public void setActive(boolean active) {
		_active = active;
	}
	
	public void send(MidiMessage message) {
		if (_active && (message instanceof ShortMessage)) {
			final ShortMessage sm = (ShortMessage) message;
			if (MidiUtilities.isNoteOn(sm))
				_componentWithFocus.keyPressed(null, sm.getData1(), sm.getData2());
			else if (MidiUtilities.isNoteOff(sm))
				_componentWithFocus.keyReleased(null, sm.getData1());
			else if (MidiUtilities.isControlChange(sm))
				_componentWithFocus.controlReceived(null, sm.getData1(), sm.getData2());
		}
	}
}
