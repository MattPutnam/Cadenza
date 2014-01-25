package cadenza.gui;

/**
 * Delegate that handles platform-specific UI work
 * 
 * @author Matt Putnam
 */
public interface CadenzaDelegate {
	/**
	 * Called after the home window is shown.
	 */
	public void doAfterShowHome();
	
	/**
	 * Called after a CadenzaFrame is created
	 * @param frame
	 */
	public void setupFrame(CadenzaFrame frame);
}
