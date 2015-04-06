package cadenza.core.patchusage;

import java.util.Collections;
import java.util.List;
import java.util.OptionalInt;

import cadenza.control.PerformanceController;
import cadenza.core.Location;
import cadenza.core.Patch;
import cadenza.core.metronome.Metronome;
import cadenza.core.metronome.MetronomeListener;
import cadenza.core.sequencer.Sequencer;
import cadenza.core.sequencer.Sequencer.NoteChangeBehavior;

public class SequencerPatchUsage extends PatchUsage implements MetronomeListener {
  private static final long serialVersionUID = 2L;
  
  public final Sequencer sequencer;
  
  private transient PerformanceController _controller;
  private transient boolean _turnOffMetronomeOnExit;
  
  private transient volatile boolean _waitingForDownbeat;
  private transient volatile OptionalInt _activeDepressedNote;
  private transient volatile OptionalInt _noteForSounding;
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
    _activeDepressedNote = OptionalInt.of(midiNumber);
    
    if (!_noteForSounding.isPresent() || isChangeNote())
      _noteForSounding = _activeDepressedNote;
    
    if (isRestart())
      _index = 0;
    
    Metronome.getInstance().start();
    return new int[][] {};
  }
  
  @Override
  public void noteReleased(int midiNumber) {
    _activeDepressedNote.ifPresent(m -> {
      if (m == midiNumber) {
        _activeDepressedNote = OptionalInt.empty();
        _noteForSounding = OptionalInt.empty();
        _index = 0;
      }
    });
  }
  
  @Override
  public void prepare(PerformanceController controller) {
    Metronome.getInstance().addMetronomeListener(this);
    _controller = controller;
    _turnOffMetronomeOnExit = !Metronome.getInstance().isRunning();
    
    _waitingForDownbeat = sequencer.isStartOnDownbeat();
    _activeDepressedNote = OptionalInt.empty();
    _noteForSounding = OptionalInt.empty();
    _currentSoundingNotes = Collections.emptyList();
  }
  
  @Override
  public void cleanup(PerformanceController controller) {
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
    
    if (_waitingForDownbeat) {
      if (subdivision == 0)
        _waitingForDownbeat = false;
      else
        return;
    }
    
    if (sequencer.getSubdivision().matches(subdivision) && _activeDepressedNote.isPresent()) {
      _currentSoundingNotes = sequencer.receive(isContinue() ? _noteForSounding.getAsInt()
                                                             : _activeDepressedNote.getAsInt(),
                                                _index);
      sendNotesOn();
      
      _index = (_index+1) % sequencer.getLength();
      if (_index == 0 && isContinue())
        _noteForSounding = _activeDepressedNote;
    }
  }
  
  private void sendNotesOn() {
    _currentSoundingNotes.forEach(i -> _controller.sendNoteOn(i.intValue(), volume, this));
  }
  
  private void sendNotesOff() {
    _currentSoundingNotes.forEach(i -> _controller.sendNoteOff(i.intValue(), this));
  }
  
  private boolean isChangeNote() {
    return sequencer.getNoteChangeBehavior() == NoteChangeBehavior.CHANGE_NOTE;
  }
  
  private boolean isContinue() {
    return sequencer.getNoteChangeBehavior() == NoteChangeBehavior.CONTINUE_SEQUENCE;
  }
  
  private boolean isRestart() {
    return sequencer.getNoteChangeBehavior() == NoteChangeBehavior.RESTART_SEQUENCE;
  }
}
