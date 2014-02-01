package cadenza.gui.common;

import javax.swing.JComboBox;

import cadenza.synths.Synthesizers;


@SuppressWarnings("serial")
public class DeviceCombo extends JComboBox<String> {
	public DeviceCombo(String device) {
		super(Synthesizers.SYNTH_NAMES.toArray(new String[Synthesizers.SYNTH_NAMES.size()]));
		if (device != null) {
			setSelectedItem(device);
		}
	}
	
	public String getDevice() {
		return getItemAt(getSelectedIndex());
	}
}
