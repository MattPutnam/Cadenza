package cadenza.gui.controlmap;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import cadenza.core.ControlNames;

@SuppressWarnings("serial")
public class ControlListRenderer extends DefaultListCellRenderer {
	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		final JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
				cellHasFocus);
		final Integer i = (Integer) value;
		
		label.setText(i + ": " + ControlNames.getName(i));
		
		return label;
	}
}