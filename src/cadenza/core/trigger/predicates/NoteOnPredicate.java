package cadenza.core.trigger.predicates;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;

import cadenza.core.Location;

import common.midi.MidiUtilities;

public class NoteOnPredicate implements TriggerPredicate {
	private final Location _location;

	public NoteOnPredicate(Location location) {
		_location = location;
	}

	@Override
	public boolean receive(MidiMessage message) {
		if (message instanceof ShortMessage) {
			final ShortMessage sm = (ShortMessage) message;
			if (!MidiUtilities.isNoteOn(sm))
				return false;
			
			final int channel = sm.getChannel() + 1; // libraries use 0-indexing, this application uses 1-indexing
			final int midiNumber = sm.getData1();
			
			return _location.getKeyboard().channel == channel && _location.contains(midiNumber);
		} else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		return _location.toString() + " pressed";
	}
	
	public Location getLocation() {
		return _location;
	}
}
