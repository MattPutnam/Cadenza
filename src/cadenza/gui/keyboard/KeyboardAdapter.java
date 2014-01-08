package cadenza.gui.keyboard;

import cadenza.core.Note;

public class KeyboardAdapter implements KeyboardListener {

	@Override
	public void keyPressed(Note note) {}

	@Override
	public void keyReleased(Note note) {}

	@Override
	public void keyDragged(Note startNote, Note endNote) {}

	@Override
	public void keyClicked(Note note) {}

}
