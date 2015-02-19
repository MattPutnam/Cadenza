package cadenza.core.metronome;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Implementation of a simple metronome.</p>
 * 
 * <p>The metronome sends 12 subdivisions of the beat numbered 0-11, so
 * listeners can pick which subdivision to respond to:</p>
 * <ul>
 *   <li>0 only &mdash; just the main beat</li>
 *   <li>0 and 6 &mdash; 2 subdivisions (8th notes)</li>
 *   <li>0, 4, and 8 &mdash; 3 subdivisions (triplets)</li>
 *   <li>0, 3, 6, and 9 &mdash; 4 subdivisions (16th notes)</li>
 * </ul>
 * 
 * This class is fully threadsafe.
 * 
 * @author Matt Putnam
 */
public class Metronome {
  private static final int MILLIS_IN_MINUTE = 1000*60;
  
  /**
   * Convenience for selecting and responding only to a desired subdivision
   * of the metronome.
   * 
   * @author Matt Putnam
   */
  public static enum Subdivision {
    /** Respond to every main beat */
    QUARTERS("Quarter Notes", 12),
    /** Respond to every 6 subdivisions, or half of each main beat */
    EIGHTHS("Eighth Notes", 6),
    /** Respond to every 4 subdivisions, or 1/3 of each main beat */
    TRIPLETS("Triplet Eighth Notes", 4),
    /** Respond to every 3 subdivisions, or 1/4 of each main beat */
    SIXTEENTHS("Sixteenth Notes", 3);
    
    private final String _displayName;
    private final int _twelfths;
    
    private Subdivision(String displayName, int twelfths) {
      _displayName = displayName;
      _twelfths = twelfths;
    }
    
    /**
     * MetronomeListeners can use this method to see if the given click
     * corresponds to this subdivision.  Example:
     * 
     * <pre>public void metronomeClicked(int subdivision) {
     *     if (SIXTEENTHS.matches(subdivision) {
     *         // this code runs every 16th note
     *     }
     * }
     * 
     * @param subdivisionNumber the subdivision number given by
     * {@link MetronomeListener#metronomeClicked(int)}
     * @return whether the metronome click corresponds to the subdivision
     */
    public boolean matches(int subdivisionNumber) {
      return subdivisionNumber % _twelfths == 0;
    }
    
    @Override
    public String toString() {
      return _displayName;
    }
  }
  
  private static final Metronome INSTANCE = new Metronome();
  public static Metronome getInstance() {
    return INSTANCE;
  }
  
  private volatile int _bpm;
  private boolean _running;
  private Thread _metronomeThread;
  
  private List<MetronomeListener> _listeners;
  
  private Metronome() {
    _bpm = 120;
    _running = false;
    _listeners = new ArrayList<>();
  }
  
  /**
   * Adds a listener to the metronome
   * @param listener the listener to add
   */
  public synchronized void addMetronomeListener(MetronomeListener listener) {
    _listeners.add(listener);
  }
  
  /**
   * Removes a listener from the metronome
   * @param listener the listener to remove
   */
  public synchronized void removeMetronomeListener(MetronomeListener listener) {
    _listeners.remove(listener);
  }
  
  /**
   * Set the BPM to the given value.  BPM must be 1 or greater.
   * @param bpm the BPM to set
   */
  public synchronized void setBPM(int bpm) {
    if (bpm < 1) {
      throw new IllegalArgumentException("BPM must be 1 or greater");
    }
    
    if (bpm != _bpm) {
      _bpm = bpm;
      for (final MetronomeListener listener : _listeners)
        listener.bpmSet(_bpm);
    }
  }
  
  /**
   * @return the current BPM of the metronome
   */
  public synchronized int getBPM() {
    return _bpm;
  }
  
  /**
   * @return whether or not the metronome is currently running
   */
  public synchronized boolean isRunning() {
    return _running;
  }
  
  /**
   * Starts the metronome.  Does nothing if the metronome is already running.
   * Sends a {@link MetronomeListener#metronomeStarted()} message, then will
   * periodically send {@link MetronomeListener#metronomeClicked(int)}
   * messages as the metronome clicks.  The metronome will click immediately
   * after this call.  The metronomeStarted() message will be called in the
   * current thread, but the metronomeClicked() messages will be called from
   * a separate thread (not the Swing thread).
   */
  public synchronized void start() {
    if (_running)
      return;
    _running = true;
    
    startMetronome();
    
    for (final MetronomeListener listener : _listeners)
      listener.metronomeStarted();
  }
  
  private void startMetronome() {
    _metronomeThread = new Thread(() -> {
      boolean alive = true;
      int counter = 0;
      while (alive) {
        for (final MetronomeListener listener : _listeners)
          listener.metronomeClicked(counter);
        counter = (counter+1) % 12;
        
        try {
          Thread.sleep((long) ((MILLIS_IN_MINUTE / 12.0) / _bpm));
        } catch (InterruptedException e) {
          alive = false;
        }
      }
    });
    _metronomeThread.start();
  }
  
  /**
   * Stops the metronome.  Does nothing if the metronome is already stopped.
   * Sends a {@link MetronomeListener#metronomeStopped()} message in the
   * current thread.
   */
  public synchronized void stop() {
    if (!_running)
      return;
    _running = false;
    
    _metronomeThread.interrupt();
    
    for (final MetronomeListener listener : _listeners)
      listener.metronomeStopped();
  }
  
  /**
   * Stops and then restarts the metronome, if it is running.  Does nothing
   * if the metronome is not running.  This has the effect of causing the
   * metronome to immediately click and then continue from there with the
   * existing tempo.  No start/stop messages are sent.
   */
  public synchronized void restart() {
    if (!_running)
      return;
    
    _metronomeThread.interrupt();
    startMetronome();
  }
}
