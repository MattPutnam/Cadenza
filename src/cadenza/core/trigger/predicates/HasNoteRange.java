package cadenza.core.trigger.predicates;

import cadenza.core.NoteRange;

public interface HasNoteRange {
  public NoteRange getNoteRange();
  public void setNoteRange(NoteRange newNoteRange);
}
