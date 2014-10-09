package cadenza.core.tracker;

import java.util.HashMap;
import java.util.Map;

import common.collection.buffer.FixedSizeIntBuffer;

/**
 * Tracks the average velocity from each input channel.  This is used to send
 * volume changes for patches that aren't velocity-sensitive so they can
 * somewhat pretend to be velocity-sensitive, instead of just always playing
 * at max volume.
 * 
 * @author Matt Putnam
 */
public final class VelocityTracker {
  private static final int BUFFER_SIZE = 15;
  
  private static final VelocityTracker INSTANCE = new VelocityTracker();
  public static VelocityTracker getInstance() {
    return INSTANCE;
  }
  
  private final Map<Integer, FixedSizeIntBuffer> _buffers;
  
  private VelocityTracker() {
    _buffers = new HashMap<>();
  }
  
  /**
   * Notifies the VelocityTracker that a note with the given velocity was
   * played on the given input channel
   * @param inputChannel the input channel
   * @param velocity the velocity of the note played
   */
  public void notify(int inputChannel, int velocity) {
    final Integer key = Integer.valueOf(inputChannel);
    FixedSizeIntBuffer buffer = _buffers.get(key);
    if (buffer == null) {
      buffer = new FixedSizeIntBuffer(BUFFER_SIZE);
      _buffers.put(key, buffer);
    }
    
    buffer.add(velocity);
  }
  
  /**
   * Clears all buffers
   */
  public void clear() {
    _buffers.clear();
  }
  
  /**
   * Gets the average of the most recently received velocities from the
   * given channel.  The size of the buffer is a constant.  If the given
   * input channel has not yet received any input, -1 is returned.
   * @param inputChannel the input channel
   * @return the average of the most recently received velocities from
   *     <tt>inputChannel</tt>, or -1 if none has been received.
   */
  public int getAverage(int inputChannel) {
    final Integer key = Integer.valueOf(inputChannel);
    final FixedSizeIntBuffer buffer = _buffers.get(key);
    if (buffer == null)
      return -1;
    else
      return average(buffer.getValues());
  }
  
  private static int average(int[] values) {
    int sum = 0;
    for (final int value : values)
      sum += value;
    return Math.round(sum / ((float) values.length));
  }
}
