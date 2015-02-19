package cadenza.gui.controlmap;

import javax.swing.JLabel;
import javax.swing.JList;

import cadenza.core.ControlNames;

import common.swing.SimpleListCellRenderer;

@SuppressWarnings("serial")
public class ControlListRenderer extends SimpleListCellRenderer<Integer> {
  @Override
  protected void processLabel(JLabel label, JList<Integer> list, Integer value,
      int index, boolean isSelected, boolean cellHasFocus) {
    label.setText(value + ": " + ControlNames.getName(value));
  }
}