package cadenza.core.effects;

import java.io.Serializable;
import java.text.DecimalFormat;

import cadenza.gui.effects.view.EffectView;

/**
 * Interface for MIDI-level "effects" that operate on MIDI values
 * 
 * @author Matt Putnam
 */
public interface Effect extends Serializable {
  public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");
  
  /**
   * Takes a MIDI note number and velocity and processes it to a new velocity
   * @param midiNumber the MIDI number of the note
   * @param velocity the volume of the note
   * @return the velocity post processing
   */
  public int process(int midiNumber, int velocity);
  
  /**
   * @return an EffectView for this Effect
   */
  public EffectView createView();
  
  /**
   * @return a copy of this Effect
   */
  public Effect copy();
}
