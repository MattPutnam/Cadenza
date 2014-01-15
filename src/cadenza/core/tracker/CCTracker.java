package cadenza.core.tracker;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Tracks the Control Change numbers that are sent from each input channel.
 * This is used to make sure that the synthesizer output channel has the
 * correct CC values after a cue change.
 * 
 * @author Matt Putnam
 */
public final class CCTracker {
	private static final CCTracker INSTANCE = new CCTracker();
	public static CCTracker getInstance() {
		return INSTANCE;
	}
	
	/*
	 * Implementation note: we use a Map<Integer, int[128]> instead of the
	 * faster int[16][128] because MIDI interfaces with multiple I/O ports
	 * can use more than 16 channels.
	 */
	private final Map<Integer, int[]> _values;
	
	private CCTracker() {
		_values = new HashMap<>();
	}
	
	/**
	 * Notifies the CCTracker that an input channel has sent a CC value
	 * @param channel the input channel
	 * @param ccNum the CC number
	 * @param value the CC value
	 */
	public void notify(int channel, int ccNum, int value) {
		final Integer key = Integer.valueOf(channel);
		int[] array = _values.get(key);
		if (array == null) {
			array = new int[128];
			_values.put(key, array);
		}
		
		array[ccNum] = value;
	}
	
	/**
	 * Get the current value of a CC number from a given channel, or -1 if one
	 * has not yet been received.
	 * @param channel the input channel
	 * @param ccNum the CC number to look up
	 * @return the last received value of <tt>ccNum</tt> from <tt>channel</tt>
	 */
	public int getValue(int channel, int ccNum) {
		final int[] array = _values.get(Integer.valueOf(channel));
		if (array == null)
			return -1;
		else
			return array[ccNum];
	}
	
	/**
	 * Gets all of the current CC numbers from a given channel
	 * @param channel the input channel
	 * @return all of the last received CC numbers from <tt>channel</tt>
	 */
	public int[] getValues(int channel) {
		return Arrays.copyOf(_values.get(Integer.valueOf(channel)), 128);
	}
}
