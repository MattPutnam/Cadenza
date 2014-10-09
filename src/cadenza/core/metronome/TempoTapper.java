package cadenza.core.metronome;

import org.apache.commons.math3.stat.StatUtils;

import common.collection.buffer.FixedSizeDoubleBuffer;

/**
 * <p>Utility that allows the user to set the metronome's BPM by tapping the
 * desired tempo.  The taps are averaged out over a short buffer to smoothe
 * the effects of human inaccuracy, and the BPM is reset as the average
 * changes.</p>
 * 
 * <p>The tapper makes a best-effort attempt to sync the metronome clicks with
 * the user's taps, but in the interest of simplicity and efficiency (and
 * application responsiveness), it doesn't do a perfect job.  The tapper can
 * generally handle any accelerando and slight ritardando, but can't handle
 * dramatic drops in tempo.  If you know your tempo is going to be dropping
 * severely, it would be wise to set up a trigger that sets the tempo to the
 * approximate target tempo first.</p>
 * 
 * @author Matt Putnam
 */
public class TempoTapper extends MetronomeAdapter {
  private static final int MILLIS_IN_MINUTE = 1000*60;
  private static final int BUFFER_SIZE = 10;
  private static final int NEW_TEMPO_THRESHOLD = 250;
  private static final int CLICK_SAFETY_DELAY = 250;
  
  private static final TempoTapper INSTANCE = new TempoTapper();
  public static TempoTapper getInstance() {
    return INSTANCE;
  }
  
  private double _runningMean;
  
  private long _lastTap;
  private long _lastClick;
  private final FixedSizeDoubleBuffer _buffer;
  
  private TempoTapper() {
    _buffer = new FixedSizeDoubleBuffer(BUFFER_SIZE);
    Metronome.getInstance().addMetronomeListener(this);
  }
  
  public synchronized void tap() {
    final long now = System.currentTimeMillis();
    final int diff = (int) (now - _lastTap);
    _lastTap = now;
    
    if (Math.abs(diff - _runningMean) > NEW_TEMPO_THRESHOLD) {
      // this will also handle long delays
      _buffer.clear();
    }
    _buffer.add(diff);
    
    _runningMean = StatUtils.mean(_buffer.getValues());
    final int bpm = (int) (MILLIS_IN_MINUTE / _runningMean);
    if (bpm > 1) {
      Metronome.getInstance().setBPM(bpm);
      if (now - _lastClick > CLICK_SAFETY_DELAY) {
        Metronome.getInstance().restart();
      }
    }
  }
  
  @Override
  public void metronomeClicked(int subdivision) {
    if (subdivision == 0) {
      _lastClick = System.currentTimeMillis();
    }
  }
}
