package cadenza.core.midimessage;

import cadenza.core.Keyboard;
import cadenza.core.Note;

public final class NoteOn extends MIDIMessage {
  private final Note _note;
  private final int _velocity;
  
  public NoteOn(Keyboard keyboard, Note note, int velocity) {
    super(keyboard);
    _note = note;
    _velocity = velocity;
  }
  
  public Note getNote() {
    return _note;
  }
  
  public int getVelocity() {
    return _velocity;
  }

  @Override
  public int getType() {
    return NOTE_ON;
  }
}
