package cadenza.gui.preferences;

import java.awt.BorderLayout;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import cadenza.preferences.PatchSearchOptions;
import cadenza.preferences.PatchSearchOptions.PatchSearchMode;

import common.swing.RadioButtonPanel;
import common.swing.SwingUtils;

@SuppressWarnings("serial")
public class PatchSearchOptionsPanel extends JPanel {
  private final RadioButtonPanel<PatchSearchMode> _radioButtonPanel;
  private final JCheckBox _regexWrapBox;
  
  private final JCheckBox _caseSensitiveBox;
  private final JCheckBox _excludeUserBox;
  
  public PatchSearchOptionsPanel() {
    _radioButtonPanel = new RadioButtonPanel<>(PatchSearchMode.values(), PatchSearchMode.PIPES, true);
    _regexWrapBox = new JCheckBox("Automatically wrap search with .* to match any substring");
    
    _caseSensitiveBox = new JCheckBox("Case sensitive (not applicable for regex search)");
    _excludeUserBox = new JCheckBox("Exclude the 'user' bank from search results");
    
    _radioButtonPanel.addSelectionListener(e -> _regexWrapBox.setEnabled(e.getSelectedValue() == PatchSearchMode.REGEX));
    
    final Box box = Box.createVerticalBox();
    box.add(SwingUtils.buildLeftAlignedRow(_radioButtonPanel));
    box.add(SwingUtils.buildLeftAlignedRow(Box.createHorizontalStrut(16), _regexWrapBox));
    box.add(Box.createVerticalStrut(16));
    box.add(SwingUtils.buildLeftAlignedRow(_caseSensitiveBox));
    box.add(SwingUtils.buildLeftAlignedRow(_excludeUserBox));
    
    setLayout(new BorderLayout());
    add(SwingUtils.hugNorth(box), BorderLayout.WEST);
  }
  
  public void setSelectedOptions(PatchSearchOptions options) {
    _radioButtonPanel.setSelectedValue(options.getSearchMode());
    _regexWrapBox.setSelected(options.isRegexWrap());
    _caseSensitiveBox.setSelected(options.isCaseSensitive());
    _excludeUserBox.setSelected(options.isExcludeUser());
  }
  
  public PatchSearchOptions getSelectedOptions() {
    return new PatchSearchOptions(_radioButtonPanel.getSelectedValue(),
                                  _regexWrapBox.isSelected(),
                                  _caseSensitiveBox.isSelected(),
                                  _excludeUserBox.isSelected());
  }
}
