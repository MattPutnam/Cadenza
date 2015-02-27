package cadenza.gui.preferences;

import java.awt.BorderLayout;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import common.swing.CollapsiblePanel;
import common.swing.CollapsiblePanel.Icon;
import common.swing.CollapsiblePanel.Orientation;
import common.swing.SwingUtils;

@SuppressWarnings("serial")
public class MIDIInputPrefPanel extends JPanel {
  private CollapsiblePanel _mainPanel;
  
  private CollapsiblePanel _volumePanel;
  private JRadioButton _strictButton;
  private JRadioButton _lenientButton;
  
  public MIDIInputPrefPanel() {
    super();
    _strictButton = new JRadioButton("Only respond to volume control signal (CC7)");
    _lenientButton = new JRadioButton("Respond to any control signal");
    SwingUtils.group(_strictButton, _lenientButton);
    
    final Box volumeContent = Box.createVerticalBox();
    volumeContent.add(_strictButton);
    volumeContent.add(_lenientButton);
    
    _volumePanel = new CollapsiblePanel(indent(volumeContent), Orientation.VERTICAL, Icon.CHECKBOX, "Allow input to volume fields", null);
    _mainPanel = new CollapsiblePanel(indent(_volumePanel), Orientation.VERTICAL, Icon.CHECKBOX, "Allow MIDI instrument input", null);
    
    setLayout(new BorderLayout());
    add(SwingUtils.hugWest(_mainPanel), BorderLayout.NORTH);
  }
  
  public void match(boolean[] options) {
    _mainPanel.setExpanded(options[0]);
    
    _volumePanel.setExpanded(options[1]);
    if (options[2])
      _strictButton.setSelected(true);
    else
      _lenientButton.setSelected(true);
  }
  
  public boolean[] getSelectedOptions() {
    return new boolean[] {
        _mainPanel.isExpanded(),
        _volumePanel.isExpanded(),
        _strictButton.isSelected(),
    };
  }
  
  private static JComponent indent(JComponent content) {
    return SwingUtils.buildLeftAlignedRow(Box.createHorizontalStrut(16), content);
  }
}
