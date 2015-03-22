package cadenza.core.midimessage;

import cadenza.core.Keyboard;
import cadenza.core.Note;

public final class NoteOff extends MIDIMessage {
  private final Note _note;
  
  public NoteOff(Keyboard keyboard, Note note) {
    super(keyboard);
    _note = note;
  }
  
  public Note getNote() {
    return _note;
  }
  
  @Override
  public int getType() {
    return NOTE_OFF;
  }
}
