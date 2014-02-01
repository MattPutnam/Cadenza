package cadenza.core.trigger.predicates;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;

import cadenza.core.Keyboard;

import common.Utils;
import common.midi.MidiUtilities;
import common.tuple.Pair;

public class ChordPredicate implements TriggerPredicate {
	private static final long serialVersionUID = 1L;
	
	private final List<Pair<Integer, Integer>> _notes;
	
	private final List<Pair<Keyboard, Integer>> _originalInput;
	
	private final String _chordDescription;
	
	private final List<Pair<Integer, Integer>> _currentNotes;
	
	public ChordPredicate(List<Pair<Keyboard, Integer>> notes) {
		_notes = new ArrayList<>();
		
		_originalInput = new ArrayList<>(notes);
		
		final List<String> noteNames = new LinkedList<>();
		for (Pair<Keyboard, Integer> p : notes) {
			_notes.add(Pair.make(Integer.valueOf(p._1().channel), p._2()));
			noteNames.add(MidiUtilities.noteNumberToName(p._2().intValue()));
		}
		_chordDescription = Utils.mkString(noteNames, "[", ", ", "]");
		
		_currentNotes = new ArrayList<>();
	}

	@Override
	public boolean receive(MidiMessage message) {
		if (message instanceof ShortMessage) {
			final ShortMessage sm = (ShortMessage) message;
			final int channel = sm.getChannel() + 1; // libraries use 0-indexing, this application uses 1-indexing
			
			final int midiNumber = sm.getData1();
			final Pair<Integer, Integer> entry = Pair.make(Integer.valueOf(channel), Integer.valueOf(midiNumber));
			
			if (MidiUtilities.isNoteOn(sm))
				_currentNotes.add(entry);
			else if (MidiUtilities.isNoteOff(sm))
				_currentNotes.remove(entry);
		}
		
		return _currentNotes.containsAll(_notes);
	}
	
	@Override
	public String toString() {
		return "chord " + _chordDescription + " played";
	}

	public List<Pair<Keyboard, Integer>> getNotes() {
		return new ArrayList<>(_originalInput);
	}

}
