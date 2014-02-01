package cadenza.core.trigger.actions;

import cadenza.control.CadenzaController;

public class PanicAction implements TriggerAction {
	private static final long serialVersionUID = 1L;
	
	@Override
	public void takeAction(CadenzaController controller) {
		controller.allNotesOff();
	}
	
	@Override
	public String toString() {
		return "panic";
	}
}
