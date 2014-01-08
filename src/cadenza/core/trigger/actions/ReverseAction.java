package cadenza.core.trigger.actions;

import cadenza.control.CadenzaController;

/**
 * Action to send control to the previous Cue
 * 
 * @author Matt Putnam
 */
public class ReverseAction implements TriggerAction {
	@Override
	public void takeAction(CadenzaController controller) {
		controller.reverse();
	}
	
	@Override
	public String toString() {
		return "reverse cue";
	}

}
