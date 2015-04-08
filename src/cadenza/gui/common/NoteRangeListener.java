package cadenza.gui.common;

import cadenza.core.NoteRange;

@FunctionalInterface
public interface NoteRangeListener {
  public void noteRangeChanged(NoteRange newNoteRange);
}
