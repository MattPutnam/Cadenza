package cadenza.gui.wizard;

import java.awt.BorderLayout;
import java.util.List;

import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;

import cadenza.core.CadenzaData;
import cadenza.gui.trigger.TriggerPanel;

public class GlobalTriggerWizardPage extends WizardPage {
	public static final String GLOBAL_TRIGGERS_KEY = "Global Triggers";
	
	private final CadenzaData _data;
	private final TriggerPanel _triggerPanel;
	
	public GlobalTriggerWizardPage(CadenzaData data) {
		super("Global Triggers", "Set up the global triggers used in this performance");
		_data = data;
		_triggerPanel = new TriggerPanel(data, data);
		_triggerPanel.setName("TriggerPanel");
		
		setLayout(new BorderLayout());
		add(_triggerPanel, BorderLayout.CENTER);
	}
	
	@Override
	public void rendering(List<WizardPage> path, WizardSettings settings) {
		setNextEnabled(true);
		setFinishEnabled(true);
	}
	
	@Override
	public void updateSettings(WizardSettings settings) {
		super.updateSettings(settings);
		_data.globalTriggers.clear();
		_data.globalTriggers.addAll(_triggerPanel.getTriggers());
	}
}
