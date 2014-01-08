package cadenza.gui.wizard;

import java.awt.BorderLayout;
import java.util.List;

import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;

import cadenza.core.CadenzaData;
import cadenza.core.Keyboard;
import cadenza.gui.keyboard.KeyboardListEditor;

public class KeyboardWizardPage extends WizardPage {
	public static final String KEYBOARDS_KEY = "Keyboards";
	
	private final CadenzaData _data;
	private final KeyboardListEditor _keyboardListEditor;
	
	public KeyboardWizardPage(CadenzaData data) {
		super("Keyboards", "Set up the keyboards used in this performance");
		_data = data;
		
		final Keyboard keyboard = new Keyboard(1);
		keyboard.isMain = true;
		_data.keyboards.add(keyboard);
		
		_keyboardListEditor = new KeyboardListEditor(_data);
		_keyboardListEditor.setName("KeyboardListEditor");
		
		setLayout(new BorderLayout());
		add(_keyboardListEditor, BorderLayout.CENTER);
	}
	
	@Override
	public void rendering(List<WizardPage> path, WizardSettings settings) {
		setFinishEnabled(false);
		setNextEnabled(_keyboardListEditor.getKeyboards().size() > 0);
	}
	
	@Override
	public void updateSettings(WizardSettings settings) {
		super.updateSettings(settings);
		_data.keyboards.clear();
		_data.keyboards.addAll(_keyboardListEditor.getKeyboards());
	}
}
