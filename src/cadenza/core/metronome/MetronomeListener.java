package cadenza.core.metronome;

/**
 * Listener interface for the {@link Metronome}.  Clients can attack this
 * listener to receive events from the metronome.
 * 
 * @author Matt Putnam
 */
public interface MetronomeListener {
	/**
	 * Notification that the Metronome's BPM has been set
	 * @param bpm the BPM value that has been set
	 */
	public void bpmSet(int bpm);
	
	/**
	 * Notification that the metronome has started
	 */
	public void metronomeStarted();
	
	/**
	 * Notification that the metronome has clicked
	 * @param subdivision which subdivision, ranges 0-11
	 */
	public void metronomeClicked(int subdivision);
	
	/**
	 * Notification that the metronome has stopped
	 */
	public void metronomeStopped();
}
