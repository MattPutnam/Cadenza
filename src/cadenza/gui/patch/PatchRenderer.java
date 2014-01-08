package cadenza.gui.patch;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import cadenza.core.Patch;

public class PatchRenderer extends DefaultListCellRenderer {
	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		final Component c = super.getListCellRendererComponent(list, value, index, isSelected,
				cellHasFocus);
		final JLabel label = (JLabel) c;
		final Patch patch = (Patch) value;
		
		if (value != null)
			label.setText(patch.name + " (" + patch.getSynthesizer() + " " + patch.bank + " " + patch.number + ")");
		return label;
	}
}