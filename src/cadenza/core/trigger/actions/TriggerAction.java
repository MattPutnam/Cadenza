package cadenza.core.trigger.actions;

import java.io.Serializable;

import cadenza.control.PerformanceController;

public interface TriggerAction extends Serializable {
	public void takeAction(PerformanceController controller);
}
