package cadenza.control;

import java.util.LinkedList;
import java.util.List;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;

import cadenza.core.CadenzaData;
import cadenza.core.Patch;
import cadenza.gui.control.CadenzaListener;

/**
 * Interface defining operations for a controller that receives raw MIDI
 * messages from the input source, processes them according to the Cadenza
 * data, and sends the appropriate output MIDI.
 * 
 * @author Matt Putnam
 */
public abstract class CadenzaController {
  private final CadenzaData _data;
  private final List<CadenzaListener> _listeners;
  
  private Receiver _receiver;
  
  public CadenzaController(CadenzaData data) {
    _data = data;
    _listeners = new LinkedList<>();
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
  
  protected final Receiver getReceiver() {
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
  
  public synchronized final void addCadenzaListener(CadenzaListener listener) {
    _listeners.add(listener);
    initializeListener(listener);
  }
  
  /**
   * Called by {@link #addCadenzaListener(CadenzaListener)} after the listener is added.
   * Subclasses should use this to re-send setup information.
   * @param listener the listener that was just added
   */
  protected abstract void initializeListener(CadenzaListener listener);
  
  public synchronized final void removeCadenzaListener(CadenzaListener listener) {
    _listeners.remove(listener);
  }
  
  protected synchronized final void notifyListeners(Exception e) {
    for (final CadenzaListener listener : _listeners) {
      listener.handleException(e);
    }
  }
  
  protected synchronized final void notifyListeners(List<Patch> patches) {
    for (final CadenzaListener listener : _listeners) {
      listener.updatePreviewPatches(patches);
    }
  }
  
  protected synchronized final void notifyListeners(int position) {
    for (final CadenzaListener listener : _listeners) {
      listener.updatePerformanceLocation(position);
    }
  }
}
