package cadenza.gui.preferences;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import cadenza.preferences.Preferences;
import common.swing.SwingUtils;
import common.tuple.Pair;

@SuppressWarnings("serial")
public class PatchSearchOptionsPanel extends JPanel {
  private final JRadioButton _simpleButton;
  private final JRadioButton _pipesButton;
  private final JRadioButton _regexButton;
  
  private final JCheckBox _caseSensitiveBox;
  
  public PatchSearchOptionsPanel() {
    _simpleButton = new JRadioButton("Simple search - search the given text verbatim");
    _pipesButton = new JRadioButton("Multiple search - separate multiple terms with a pipe (|)");
    _regexButton = new JRadioButton("Regex search - use a Java formatted regex (advanced)");
    SwingUtils.group(_simpleButton, _pipesButton, _regexButton);
    
    _caseSensitiveBox = new JCheckBox("Case sensitive (not applicable for regex search)");
    
    setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
    add(_simpleButton);
    add(_pipesButton);
    add(_regexButton);
    add(Box.createVerticalStrut(16));
    add(_caseSensitiveBox);
  }
  
  public void setSelectedOptions(Pair<Integer, Boolean> options) {
    switch (options._1().intValue()) {
      case Preferences.SIMPLE: _simpleButton.setSelected(true); break;
      case Preferences.PIPES:  _pipesButton.setSelected(true);  break;
      case Preferences.REGEX:  _regexButton.setSelected(true);  break;
    }
    
    _caseSensitiveBox.setSelected(options._2().booleanValue());
  }
  
  public Pair<Integer, Boolean> getSelectedOptions() {
    final int mode;
    if (_simpleButton.isSelected()) mode = Preferences.SIMPLE;
    else if (_pipesButton.isSelected()) mode = Preferences.PIPES;
    else mode = Preferences.REGEX;
    
    return Pair.make(Integer.valueOf(mode), Boolean.valueOf(_caseSensitiveBox.isSelected()));
  }
}
