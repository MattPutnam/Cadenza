package cadenza.gui.patch;

import javax.swing.JLabel;
import javax.swing.JList;

import cadenza.core.Patch;

import common.swing.SimpleListCellRenderer;

@SuppressWarnings("serial")
public class PatchRenderer extends SimpleListCellRenderer<Patch> {
  @Override
  protected void processLabel(JLabel label, JList<Patch> list, Patch patch,
      int index, boolean isSelected, boolean cellHasFocus) {
    if (patch != null)
      label.setText(patch.name + " (" + patch.getSynthesizer() + " " + patch.bank + " " + patch.number + ")");
  }
}