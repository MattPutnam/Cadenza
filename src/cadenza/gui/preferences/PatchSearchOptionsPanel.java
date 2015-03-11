package cadenza.gui.preferences;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import cadenza.preferences.Preferences.PatchSearchMode;

import common.swing.RadioButtonPanel;
import common.tuple.Pair;

@SuppressWarnings("serial")
public class PatchSearchOptionsPanel extends JPanel {
  private final RadioButtonPanel<PatchSearchMode> _radioButtonPanel;
  private final JCheckBox _caseSensitiveBox;
  
  public PatchSearchOptionsPanel() {
    _radioButtonPanel = new RadioButtonPanel<>(PatchSearchMode.values(), PatchSearchMode.PIPES, true);
    _caseSensitiveBox = new JCheckBox("Case sensitive (not applicable for regex search)");
    
    setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
    add(_radioButtonPanel);
    add(Box.createVerticalStrut(16));
    add(_caseSensitiveBox);
  }
  
  public void setSelectedOptions(Pair<PatchSearchMode, Boolean> options) {
    _radioButtonPanel.setSelectedValue(options._1());
    _caseSensitiveBox.setSelected(options._2().booleanValue());
  }
  
  public Pair<PatchSearchMode, Boolean> getSelectedOptions() {
    return Pair.make(_radioButtonPanel.getSelectedValue(), Boolean.valueOf(_caseSensitiveBox.isSelected()));
  }
}