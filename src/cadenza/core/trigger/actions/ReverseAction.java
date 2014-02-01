package cadenza.core.trigger.actions;

import cadenza.control.CadenzaController;

/**
 * Action to send control to the previous Cue
 * 
 * @author Matt Putnam
 */
public class ReverseAction implements TriggerAction {
	private static final long serialVersionUID = 1L;
	
	@Override
	public void takeAction(CadenzaController controller) {
		controller.reverse();
	}
	
	@Override
	public String toString() {
		return "reverse cue";
	}

}
