package cadenza.gui.control;

import java.util.List;

import cadenza.core.Patch;
import cadenza.gui.CadenzaFrame.Mode;


public interface CadenzaListener {
	/**
	 * Notification that the mode has changed
	 * @param mode the new Mode
	 */
	public void updateMode(Mode mode);
	
	/**
	 * Notification that the controller is in performance mode and that it
	 * is at the given cue number
	 * @param position the cue number
	 */
	public void updatePerformanceLocation(int position);
	
	/**
	 * Notification that the controller is in preview mode and that
	 * the preview patches have changed
	 * @param patch the preview patch
	 */
	public void updatePreviewPatches(List<Patch> patches);
	
	/**
	 * Notification that an exception has been thrown
	 * @param e the thrown exception
	 */
	public void handleException(Exception e);
}
