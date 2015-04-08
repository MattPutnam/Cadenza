package cadenza.core.trigger.predicates;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;

import cadenza.core.NoteRange;

import common.midi.MidiUtilities;

public class NoteOnPredicate implements TriggerPredicate, HasNoteRange {
  private static final long serialVersionUID = 2L;
  
  private NoteRange _noteRange;

  public NoteOnPredicate(NoteRange noteRange) {
    _noteRange = noteRange;
  }

  @Override
  public boolean receive(MidiMessage message) {
    if (message instanceof ShortMessage) {
      final ShortMessage sm = (ShortMessage) message;
      if (!MidiUtilities.isNoteOn(sm))
        return false;
      
      final int channel = sm.getChannel();
      final int midiNumber = sm.getData1();
      
      return _noteRange.getKeyboard().channel == channel && _noteRange.contains(midiNumber);
    } else {
      return false;
    }
  }
  
  @Override
  public String toString() {
    return _noteRange.toString() + " pressed";
  }
  
  @Override
  public NoteRange getNoteRange() {
    return _noteRange;
  }
  
  @Override
  public void setNoteRange(NoteRange newNoteRange) {
    _noteRange = newNoteRange;
  }
}
