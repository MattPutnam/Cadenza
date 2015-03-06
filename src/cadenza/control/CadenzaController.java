package cadenza.control;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;

import cadenza.core.CadenzaData;

/**
 * Interface defining operations for a controller that receives raw MIDI
 * messages from the input source, processes them according to the Cadenza
 * data, and sends the appropriate output MIDI.
 * 
 * @author Matt Putnam
 */
public abstract class CadenzaController {
  private final CadenzaData _data;
  
  private Receiver _receiver;
  
  public CadenzaController(CadenzaData data) {
    _data = data;
  }
  
  protected final CadenzaData getData() {
    return _data;
  }
  
  /**
   * Sets the MIDI output connection
   * @param midiOut the new connection
   */
  public synchronized final void setReceiver(Receiver midiOut) {
    _receiver = midiOut;
    
    notifyReceiver();
  }
  
  protected synchronized final Receiver getReceiver() {
    return _receiver;
  }
  
  protected final boolean receiverReady() {
    return _receiver != null;
  }
  
  /**
   * Called by {@link #setReceiver(Receiver)} after the new receiver is set.
   * Subclasses should use this to re-send setup information.
   */
  protected abstract void notifyReceiver();
  
  /**
   * Receives a MidiMessage to process and potentially send to the receiver
   * @param message the message being received
   */
  public abstract void send(MidiMessage message);
}
