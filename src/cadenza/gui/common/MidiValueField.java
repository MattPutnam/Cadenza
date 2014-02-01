package cadenza.gui.common;

import common.swing.NonNegativeIntField;

@SuppressWarnings("serial")
public class MidiValueField extends NonNegativeIntField {
	public MidiValueField() {
		super();
	}
	
	public MidiValueField(int value) {
		super(value);
	}
	
	@Override
	public int getInt() {
		final int val = super.getInt();
		if (val > 127) {
			setText("127");
			return 127;
		}
		return val;
	}
}
