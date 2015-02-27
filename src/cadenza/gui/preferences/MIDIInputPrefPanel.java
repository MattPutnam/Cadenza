package cadenza.gui.preferences;

import java.awt.BorderLayout;

import javax.swing.Box;
import javax.swing.JCheckBox;
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
  
  private CollapsiblePanel _patchUsagePanel;
  private JCheckBox _puSingleCheckBox;
  private JCheckBox _puRangeCheckBox;
  private JCheckBox _puWholeCheckBox;
  
  public MIDIInputPrefPanel() {
    super();
    
    _strictButton = new JRadioButton("Only respond to volume control signal (CC7)");
    _lenientButton = new JRadioButton("Respond to any control signal");
    SwingUtils.group(_strictButton, _lenientButton);
    
    _puSingleCheckBox = new JCheckBox("Press a single key to add a patch to that key");
    _puRangeCheckBox = new JCheckBox("Press two keys to add a patch to that range of keys");
    _puWholeCheckBox = new JCheckBox("Press any 3 keys to add a patch to the whole keyboard");
    
    final Box volumeContent = Box.createVerticalBox();
    volumeContent.add(_strictButton);
    volumeContent.add(_lenientButton);
    
    final Box puContent = Box.createVerticalBox();
    puContent.add(_puSingleCheckBox);
    puContent.add(_puRangeCheckBox);
    puContent.add(_puWholeCheckBox);
    
    _volumePanel = new CollapsiblePanel(indent(volumeContent), Orientation.VERTICAL, Icon.CHECKBOX, "Allow input to volume fields", null);
    _patchUsagePanel = new CollapsiblePanel(indent(puContent), Orientation.VERTICAL, Icon.CHECKBOX, "Allow input to patch edit dialog", null);
    
    final Box mainContent = Box.createVerticalBox();
    mainContent.add(_volumePanel);
    mainContent.add(_patchUsagePanel);
    _mainPanel = new CollapsiblePanel(indent(mainContent), Orientation.VERTICAL, Icon.CHECKBOX, "Allow MIDI instrument input", null);
    
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
    
    _patchUsagePanel.setExpanded(options[3]);
    _puSingleCheckBox.setSelected(options[4]);
    _puRangeCheckBox.setSelected(options[5]);
    _puWholeCheckBox.setSelected(options[6]);
  }
  
  public boolean[] getSelectedOptions() {
    return new boolean[] {
        _mainPanel.isExpanded(),
        
        _volumePanel.isExpanded(),
        _strictButton.isSelected(),
        
        _patchUsagePanel.isExpanded(),
        _puSingleCheckBox.isSelected(),
        _puRangeCheckBox.isSelected(),
        _puWholeCheckBox.isSelected(),
    };
  }
  
  private static JComponent indent(JComponent content) {
    return SwingUtils.buildLeftAlignedRow(Box.createHorizontalStrut(16), content);
  }
}
