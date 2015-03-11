package cadenza.control.midiinput;

/**
 * <p>Interface for components that should be able to interpret input from a
 * MIDI instrument.  All methods have default no-op implementations.</p>
 * 
 * <p>Be aware that the methods in this class will be called from the thread
 * handling all MIDI events.  Any GUI updates should be fired off in the
 * Swing Event thread, and all heavy lifting should be done in another
 * separate thread.</p>
 * 
 * @author Matt Putnam
 */
public interface AcceptsKeyboardInput {
  /**
   * Called when a key is pressed.
   * @param channel the MIDI channel
   * @param midiNumber the MIDI number of the pressed key
   * @param velocity the velocity of the key press
   */
  public default void keyPressed(int channel, int midiNumber, int velocity) {}
  
  /**
   * Called when a key is released.
   * @param channel the MIDI channel
   * @param midiNumber the MIDI number of the pressed key
   */
  public default void keyReleased(int channel, int midiNumber) {}
  
  /**
   * Called when a control change value is received
   * @param channel the MIDI channel
   * @param ccNumber the control change number
   * @param value the control change value
   */
  public default void controlReceived(int channel, int ccNumber, int value) {}
}
