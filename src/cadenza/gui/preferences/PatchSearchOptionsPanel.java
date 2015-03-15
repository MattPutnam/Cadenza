package cadenza.gui.preferences;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import cadenza.preferences.PatchSearchOptions;
import cadenza.preferences.PatchSearchOptions.PatchSearchMode;

import common.swing.RadioButtonPanel;

@SuppressWarnings("serial")
public class PatchSearchOptionsPanel extends JPanel {
  private final RadioButtonPanel<PatchSearchMode> _radioButtonPanel;
  private final JCheckBox _caseSensitiveBox;
  private final JCheckBox _excludeUserBox;
  
  public PatchSearchOptionsPanel() {
    _radioButtonPanel = new RadioButtonPanel<>(PatchSearchMode.values(), PatchSearchMode.PIPES, true);
    _caseSensitiveBox = new JCheckBox("Case sensitive (not applicable for regex search)");
    _excludeUserBox = new JCheckBox("Exclude the 'user' bank from search results");
    
    setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
    add(_radioButtonPanel);
    add(Box.createVerticalStrut(16));
    add(_caseSensitiveBox);
    add(_excludeUserBox);
  }
  
  public void setSelectedOptions(PatchSearchOptions options) {
    _radioButtonPanel.setSelectedValue(options.getSearchMode());
    _caseSensitiveBox.setSelected(options.isCaseSensitive());
    _excludeUserBox.setSelected(options.isExcludeUser());
  }
  
  public PatchSearchOptions getSelectedOptions() {
    return new PatchSearchOptions(_radioButtonPanel.getSelectedValue(),
                                  _caseSensitiveBox.isSelected(),
                                  _excludeUserBox.isSelected());
  }
}
