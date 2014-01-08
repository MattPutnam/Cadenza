package cadenza.core.sequencer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JPanel;

import cadenza.core.Note;
import cadenza.core.Scale;
import cadenza.core.metronome.Metronome.Subdivision;
import cadenza.gui.sequencer.SequencerGridPreviewPanel;

public class Sequencer implements Serializable {
	public static enum NoteChangeBehavior {
		CONTINUE_SEQUENCE("Finish sequence"),
		RESTART_SEQUENCE("Restart sequence");
		
		private final String _displayName;
		
		private NoteChangeBehavior(String displayName) {
			_displayName = displayName;
		}
		
		@Override
		public String toString() {
			return _displayName;
		}
	}
	
	private String _name;
	private int _length;
	private int[] _notes;
	
	private final boolean[][] _grid;
	
	private final Scale _scale;
	private final Subdivision _subdivision;
	private final NoteChangeBehavior _noteChangeBehavior;
	
	private transient SequencerGridPreviewPanel _previewPanel;
	
	public static final Sequencer DEFAULT = new Sequencer("New Sequencer", 11,
			new int[] {5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5}, null,
			Subdivision.SIXTEENTHS, NoteChangeBehavior.CONTINUE_SEQUENCE);
	
	public Sequencer(String name, int length, int[] notes,
			Scale scale, Subdivision subdivision, NoteChangeBehavior noteChangeBehavior) {
		_name = name;
		_length = length;
		_notes = notes;
		_noteChangeBehavior = noteChangeBehavior;
		
		_scale = scale;
		_subdivision = subdivision;
		
		_grid = new boolean[_notes.length][length];
	}
	
	public Sequencer(String name, boolean[][] grid, int[] notes,
			Scale scale, Subdivision subdivision, NoteChangeBehavior noteChangeBehavior) {
		_name = name;
		_length = grid[0].length;
		_notes = notes;
		
		_scale = scale;
		_subdivision = subdivision;
		_noteChangeBehavior = noteChangeBehavior;
		
		_grid = new boolean[_notes.length][_length];
		for (int note = 0; note < _notes.length; ++note) {
			for (int index = 0; index < _length; ++index) {
				_grid[note][index] = grid[note][index];
			}
		}
	}
	
	public String getName() {
		return _name;
	}
	
	public Scale getScale() {
		return _scale;
	}
	
	public int getLength() {
		return _length;
	}
	
	public int[] getNotes() {
		return Arrays.copyOf(_notes, _notes.length);
	}
	
	public Subdivision getSubdivision() {
		return _subdivision;
	}
	
	public NoteChangeBehavior getNoteChangeBehavior() {
		return _noteChangeBehavior;
	}
	
	public boolean isOn(int note, int index) {
		return _grid[note][index];
	}
	
	public List<Integer> receive(int midiNum, int index) {
		final List<Integer> result = new ArrayList<>(_notes.length);
		
		for (int note = 0; note < _notes.length; ++note) {
			if (_grid[note][index]) {
				if (_notes[note] == 0)
					result.add(Integer.valueOf(midiNum));
				else if (_scale == null) { 
					result.add(Integer.valueOf(midiNum + _notes[note]));
				} else {
					result.add(Integer.valueOf(_scale.upDiatonic(new Note(midiNum), _notes[note]).getMidiNumber()));
				}
			}
		}
		
		return result;
	}
	
	public JPanel getPreviewPanel() {
		if (_previewPanel == null)
			_previewPanel = new SequencerGridPreviewPanel(this);
		return _previewPanel;
	}
}
