package cadenza.gui.preferences;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class MIDIInputPrefPanel extends JPanel {
  private JCheckBox _mainCheckBox;
  
  public MIDIInputPrefPanel() {
    super();
    _mainCheckBox = new JCheckBox("Allow MIDI Instrument Input");
    
    add(_mainCheckBox);
  }
  
  public void match(boolean[] options) {
    _mainCheckBox.setSelected(options[0]);
  }
  
  public boolean[] getSelectedOptions() {
    return new boolean[] {
        _mainCheckBox.isSelected()
    };
  }
}
