package cadenza.core.trigger.actions;

import cadenza.control.CadenzaController;

/**
 * Action to send control to the next Cue
 * 
 * @author Matt Putnam
 */
public class AdvanceAction implements TriggerAction {
	@Override
	public void takeAction(CadenzaController controller) {
		controller.advance();
	}
	
	@Override
	public String toString() {
		return "advance cue";
	}

}
