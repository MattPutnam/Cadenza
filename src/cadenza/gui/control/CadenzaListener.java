package cadenza.gui.control;

import java.util.List;

import cadenza.core.Patch;

public interface CadenzaListener {
	/**
	 * Notification that Cadenza is now performing at the given location
	 * @param position the cue number
	 */
	public void updatePerformanceLocation(int position);
	
	/**
	 * Notification that Cadenza is now previewing the given patches
	 * @param patch the preview patch
	 */
	public void updatePreviewPatches(List<Patch> patches);
	
	/**
	 * Notification that an exception has been thrown
	 * @param e the thrown exception
	 */
	public void handleException(Exception e);
}
