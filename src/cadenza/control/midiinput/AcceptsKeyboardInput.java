package cadenza.control.midiinput;

public interface AcceptsKeyboardInput {
  public void keyPressed(int channel, int midiNumber, int velocity);
  public void keyReleased(int channel, int midiNumber);
  public void controlReceived(int channel, int ccNumber, int value);
}
