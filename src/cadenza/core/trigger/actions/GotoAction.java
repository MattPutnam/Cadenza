package cadenza.core.trigger.actions;

import cadenza.control.CadenzaController;
import cadenza.core.Song;

/**
 * Action to go to a specific point in the performance 
 * 
 * @author Matt Putnam
 */
public class GotoAction implements TriggerAction {
	private final Song _song;
	private final String _measure;
	
	public GotoAction(Song song, String measure) {
		_song = song;
		_measure = measure;
	}

	@Override
	public void takeAction(CadenzaController controller) {
		controller.goTo(_song, _measure);
	}
	
	@Override
	public String toString() {
		return "go to " + _song + " m. " + _measure;
	}
	
	public Song getSong() {
		return _song;
	}
	
	public String getMeasure() {
		return _measure;
	}

}
