package cadenza.core.effects;

import cadenza.gui.effects.view.CompressorView;
import cadenza.gui.effects.view.EffectView;

/**
 * <p>A compressor/limiter.  Reduces any input volume above the <b>threshold</b>
 * by the <b>ratio</b>.  Make it a limiter by setting a ratio of Infinity.</p>
 * 
 * <p>For audio engineers: the attack and release times are 0 and it's a hard knee.</p>
 * 
 * @author Matt Putnam
 */
public class Compressor implements Effect {
  private static final long serialVersionUID = 2L;
  
  private int _threshold;
  private double _ratio;
  
  private int[] _values;
  
  /**
   * Creates a new Compressor with the given threshold and ratio
   * @param threshold the threshold, must be between 0 and 127 (inclusive)
   * @param ratio the ratio, must be between 1.0 and +Inf
   */
  public Compressor(int threshold, double ratio) {
    _threshold = threshold;
    _ratio = ratio;
    
    _values = new int[128];
    recalculate();
  }
  
  /**
   * @return the threshold
   */
  public int getThreshold() {
    return _threshold;
  }
  
  /**
   * Sets the threshold
   * @param threshold the new threshold
   */
  public void setThreshold(int threshold) {
    if (threshold != _threshold) {
      _threshold = threshold;
      recalculate();
    }
  }
  
  /**
   * @return the ratio
   */
  public double getRatio() {
    return _ratio;
  }
  
  /**
   * Sets the ratio
   * @param ratio the new ratio
   */
  public void setRatio(double ratio) {
    if (ratio != _ratio) {
      _ratio = ratio;
      recalculate();
    }
  }
  
  private void recalculate() {
    for (int velocity = 0; velocity < 128; ++velocity) {
      _values[velocity] = velocity <= _threshold
          ? velocity
          : (int) (_threshold + ((velocity - _threshold) / _ratio));
    }
  }
  
  /**
   * If <tt>velocity</tt> is over the threshold, then the velocity is reduced by
   * the compression ratio.  <tt>midiNumber</tt> is unused.
   */
  @Override
  public int process(int midiNumber, int velocity) {
    return _values[velocity];
  }
  
  @Override
  public Effect copy() {
    return new Compressor(_threshold, _ratio);
  }
  
  @Override
  public EffectView createView() {
    return new CompressorView(this);
  }
  
  @Override
  public String toString() {
    return "Compressor threshold="+_threshold + " ratio="+DECIMAL_FORMAT.format(_ratio);
  }

}
