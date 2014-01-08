package cadenza.gui.control;

import cadenza.control.CadenzaController.Mode;
import cadenza.core.Patch;


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
	 * the preview patch has changed
	 * @param patch the preview patch
	 */
	public void updatePreviewPatch(Patch patch);
	
	/**
	 * Notification that an exception has been thrown
	 * @param e the thrown exception
	 */
	public void handleException(Exception e);
}
