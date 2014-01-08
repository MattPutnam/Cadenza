package cadenza.control.midiinput;

import cadenza.core.Keyboard;

public interface AcceptsKeyboardInput {
	public void keyPressed(Keyboard source, int midiNumber, int velocity);
	public void keyReleased(Keyboard source, int midiNumber);
	public void controlReceived(Keyboard source, int ccNumber, int value);
}
