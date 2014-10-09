package cadenza.gui.keyboard;

import cadenza.core.Note;

public class MultipleKeyboardAdapter implements MultipleKeyboardListener {

  @Override
  public void keyPressed(Note note, KeyboardPanel source) {}

  @Override
  public void keyReleased(Note note, KeyboardPanel source) {}

  @Override
  public void keyDragged(Note startNote, KeyboardPanel startSource,
      Note endNote, KeyboardPanel endSource) {}

  @Override
  public void keyClicked(Note note, KeyboardPanel source) {}

}
