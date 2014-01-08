package cadenza.gui.common;

import java.awt.Component;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;

import cadenza.core.ControlNames;

public class ControlCombo extends JComboBox<Integer> {

	public ControlCombo(Integer initValue) {
		super();
		final Integer[] options = new Integer[128];
		for (int i = 0; i < 128; ++i)
			options[i] = Integer.valueOf(i);
		setModel(new DefaultComboBoxModel<>(options));
		
		if (initValue != null)
			setSelectedIndex(initValue);
		else
			setSelectedIndex(0);
		
		setRenderer(new ControlRenderer());
	}
	
	private class ControlRenderer extends DefaultListCellRenderer {
		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value,
				int index, boolean isSelected, boolean hasFocus) {
			final JLabel label = (JLabel) super.getListCellRendererComponent(
					list, value, index, isSelected, hasFocus);
			int ival = ((Integer) value).intValue();
			label.setText(ival + " -- " + ControlNames.getName(ival));
			return label;
		}
	}

}
