package cadenza.core.trigger.actions;

import cadenza.control.CadenzaController;

public class PanicAction implements TriggerAction {
	@Override
	public void takeAction(CadenzaController controller) {
		controller.allNotesOff();
	}
	
	@Override
	public String toString() {
		return "panic";
	}
}
