package cadenza.core.plugins;

import java.io.Serializable;
import java.text.DecimalFormat;

import cadenza.gui.plugins.view.PluginView;

/**
 * Interface for MIDI-level "plugins" that operate on MIDI values
 * 
 * @author Matt Putnam
 */
public interface Plugin extends Serializable {
	public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");
	
	/**
	 * Takes a MIDI note number and velocity and processes it to a new velocity
	 * @param midiNumber the MIDI number of the note
	 * @param velocity the volume of the note
	 * @return the velocity post processing
	 */
	public int process(int midiNumber, int velocity);
	
	/**
	 * @return a PluginView for this Plugin
	 */
	public PluginView createView();
	
	/**
	 * @return a copy of this Plugin
	 */
	public Plugin copy();
}
