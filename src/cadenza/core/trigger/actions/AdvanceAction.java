package cadenza.core.trigger.actions;

import cadenza.control.CadenzaController;

/**
 * Action to send control to the next Cue
 * 
 * @author Matt Putnam
 */
public class AdvanceAction implements TriggerAction {
	private static final long serialVersionUID = 1L;
	
	@Override
	public void takeAction(CadenzaController controller) {
		controller.advance();
	}
	
	@Override
	public String toString() {
		return "advance cue";
	}

}
