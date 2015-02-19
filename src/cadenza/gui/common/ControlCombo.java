package cadenza.gui.common;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;

import cadenza.core.ControlNames;

import common.swing.SimpleListCellRenderer;

@SuppressWarnings("serial")
public class ControlCombo extends JComboBox<Integer> {

  public ControlCombo(Integer initValue) {
    super();
    final Integer[] options = new Integer[128];
    for (int i = 0; i < 128; ++i)
      options[i] = Integer.valueOf(i);
    setModel(new DefaultComboBoxModel<>(options));
    
    if (initValue != null)
      setSelectedIndex(initValue.intValue());
    else
      setSelectedIndex(0);
    
    setRenderer(new ControlRenderer());
  }
  
  private class ControlRenderer extends SimpleListCellRenderer<Integer> {
    @Override
    protected void processLabel(JLabel label, JList<Integer> list,
        Integer value, int index, boolean isSelected, boolean cellHasFocus) {
      int ival = value.intValue();
      label.setText(ival + " -- " + ControlNames.getName(ival));
    }
  }

}
