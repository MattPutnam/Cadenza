package cadenza.core.patchusage;

import java.util.List;

import cadenza.control.CadenzaController;
import cadenza.core.Location;
import cadenza.core.Patch;
import cadenza.core.metronome.Metronome;
import cadenza.core.metronome.MetronomeListener;
import cadenza.core.sequencer.Sequencer;
import cadenza.core.sequencer.Sequencer.NoteChangeBehavior;

public class SequencerPatchUsage extends PatchUsage implements MetronomeListener {
	private static final long serialVersionUID = 1L;
	
	public final Sequencer sequencer;
	
	private transient CadenzaController _controller;
	private transient int _channel;
	private transient boolean _turnOffMetronomeOnExit;
	
	private transient volatile int _activeDepressedNote;
	private transient volatile int _noteForSounding;
	private transient volatile List<Integer> _currentSoundingNotes;
	private transient volatile int _index;

	public SequencerPatchUsage(Patch patch, Location location, int volume,
			Sequencer sequencer) {
		super(patch, location, volume);
		this.sequencer = sequencer;
	}

	@Override
	public int[][] getNotes(int midiNumber, int velocity) {
		// abuse this just to get note input
		_activeDepressedNote = midiNumber;
		if (_noteForSounding == -1)
			_noteForSounding = midiNumber;
		if (isRestart()) {
			_index = 0;
			Metronome.getInstance().restart();
		}
		
		Metronome.getInstance().start();
		return new int[][] {};
	}
	
	@Override
	public void noteReleased(int midiNumber) {
		if (_activeDepressedNote == midiNumber) {
			_activeDepressedNote = -1;
			_noteForSounding = -1;
			_index = 0;
		}
	}
	
	@Override
	public void prepare(CadenzaController controller) {
		Metronome.getInstance().addMetronomeListener(this);
		_controller = controller;
		_channel = _controller.getCurrentlyAssignedChannel(this);
		_turnOffMetronomeOnExit = !Metronome.getInstance().isRunning();
		
		_activeDepressedNote = -1;
		_noteForSounding = -1;
		_currentSoundingNotes = null;
	}
	
	@Override
	public void cleanup(CadenzaController controller) {
		Metronome.getInstance().removeMetronomeListener(this);
		sendNotesOff();
		if (_turnOffMetronomeOnExit)
			Metronome.getInstance().stop();
	}

	@Override
	String toString_additional() {
		return " using sequencer " + sequencer.getName();
	}
	
	@Override
	public void metronomeClicked(int subdivision) {
		sendNotesOff();
		
		if (sequencer.getSubdivision().matches(subdivision) && _activeDepressedNote != -1) {
			_currentSoundingNotes = sequencer.receive(isContinue() ? _noteForSounding : _activeDepressedNote, _index);
			sendNotesOn();
			
			_index = (_index+1) % sequencer.getLength();
			if (_index == 0 && isContinue())
				_noteForSounding = _activeDepressedNote;
		}
	}
	
	private void sendNotesOn() {
		for (final Integer i : _currentSoundingNotes) {
			_controller.sendNoteOn(i.intValue(), volume, _channel);
		}
	}
	
	private void sendNotesOff() {
		if (_currentSoundingNotes != null) {
			for (final Integer i : _currentSoundingNotes) {
				_controller.sendNoteOff(i.intValue(), _channel);
			}
		}
	}
	
	private boolean isContinue() {
		return sequencer.getNoteChangeBehavior() == NoteChangeBehavior.CONTINUE_SEQUENCE;
	}
	
	private boolean isRestart() {
		return sequencer.getNoteChangeBehavior() == NoteChangeBehavior.RESTART_SEQUENCE;
	}
	
	@Override
	public void bpmSet(int bpm) { /* no op */ }
	@Override
	public void metronomeStarted() { /* no op */ }
	@Override
	public void metronomeStopped() { /* no op */ }
}
