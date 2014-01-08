package cadenza.gui.keyboard;

import cadenza.core.Note;


public interface MultipleKeyboardListener
{
	public void keyPressed(Note note, KeyboardPanel source);
	
	public void keyReleased(Note note, KeyboardPanel source);
	
	public void keyDragged(Note startNote, KeyboardPanel startSource, Note endNote, KeyboardPanel endSource);
	
	public void keyClicked(Note note, KeyboardPanel source);
}
