package cadenza.gui.wizard;

import java.awt.BorderLayout;
import java.util.List;

import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;

import cadenza.core.CadenzaData;
import cadenza.gui.controlmap.ControlMapPanel;

public class GlobalControlMapWizardPage extends WizardPage {
	public static final String GLOBAL_CONTROL_KEY = "Global Controls";
	
	private final CadenzaData _data;
	private final ControlMapPanel _controlMapPanel;
	
	public GlobalControlMapWizardPage(CadenzaData data) {
		super("Global Control Overrides", "Set up any global control overrides used in this performance");
		_data = data;
		_controlMapPanel = new ControlMapPanel(_data);
		_controlMapPanel.setName("Control Map Panel");
		
		setLayout(new BorderLayout());
		add(_controlMapPanel, BorderLayout.CENTER);
	}
	
	@Override
	public void rendering(List<WizardPage> path, WizardSettings settings) {
		setNextEnabled(false);
		setFinishEnabled(true);
	}
	
	@Override
	public void updateSettings(WizardSettings settings) {
		super.updateSettings(settings);
		_data.globalControlMap.clear();
		_data.globalControlMap.addAll(_controlMapPanel.getMapping());
	}
}
