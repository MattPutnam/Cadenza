package cadenza.gui.preferences;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import cadenza.preferences.Preferences;
import common.swing.SwingUtils;

@SuppressWarnings("serial")
public class PatchSearchModePanel extends JPanel {
  private final JRadioButton _simpleButton;
  private final JRadioButton _pipesButton;
  private final JRadioButton _regexButton;
  
  public PatchSearchModePanel() {
    _simpleButton = new JRadioButton("Simple search - search the given text verbatim");
    _pipesButton = new JRadioButton("Multiple search - separate multiple terms with a pipe (|)");
    _regexButton = new JRadioButton("Regex search - use a Java formatted regex (advanced)");
    SwingUtils.group(_simpleButton, _pipesButton, _regexButton);
    
    setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
    add(_simpleButton);
    add(_pipesButton);
    add(_regexButton);
  }
  
  public void setSelectedOption(int option) {
    switch (option) {
      case Preferences.SIMPLE: _simpleButton.setSelected(true); break;
      case Preferences.PIPES:  _pipesButton.setSelected(true); break;
      case Preferences.REGEX:  _regexButton.setSelected(true);
    }
  }
  
  public int getSelectedOption() {
    if (_simpleButton.isSelected()) return Preferences.SIMPLE;
    else if (_pipesButton.isSelected()) return Preferences.PIPES;
    else return Preferences.REGEX;
  }
}
