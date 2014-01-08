package cadenza.core.trigger.actions;

import java.io.Serializable;

import cadenza.control.CadenzaController;

public interface TriggerAction extends Serializable {
	public void takeAction(CadenzaController controller);
}
