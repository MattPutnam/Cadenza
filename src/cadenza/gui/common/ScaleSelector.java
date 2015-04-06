package cadenza.gui.common;

import java.awt.CardLayout;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import cadenza.core.Scale;
import common.swing.SwingUtils;

@SuppressWarnings("serial")
public class ScaleSelector extends JPanel {
  private final JRadioButton _chromaticRadioButton;
  private final JRadioButton _scaleRadioButton;
  
  private final JComboBox<String> _master;
  private final List<JComboBox<Scale>> _scaleCombos;
  
  public ScaleSelector(List<Scale>[] scales, String[] names, Scale initial) {
    _chromaticRadioButton = new JRadioButton("None [chromatic]");
    _scaleRadioButton = new JRadioButton("Predefined Scale");
    SwingUtils.groupAndSelectFirst(_chromaticRadioButton, _scaleRadioButton);
    
    _scaleCombos = Arrays.stream(scales)
                         .map(group -> new JComboBox<>(group.toArray(new Scale[0])))
                         .collect(Collectors.toList());
    
    final CardLayout layout = new CardLayout();
    final JPanel scalePanel = new JPanel(layout);
    for (int i = 0; i < scales.length; ++i)
      scalePanel.add(_scaleCombos.get(i), String.valueOf(i));
    
    _master = new JComboBox<>(names);
    _master.addActionListener(e -> layout.show(scalePanel, String.valueOf(_master.getSelectedIndex())));
    
    if (initial != null) {
      _scaleRadioButton.setSelected(true);
      
      for (int i = 0; i < scales.length; ++i) {
        if (scales[i].contains(initial)) {
          _master.setSelectedIndex(i);
          _scaleCombos.get(i).setSelectedItem(initial);
          break;
        }
      }
    }
    
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    add(SwingUtils.hugWest(new JLabel("Scale:")));
    add(SwingUtils.hugWest(_chromaticRadioButton));
    add(SwingUtils.hugWest(_scaleRadioButton));
    add(SwingUtils.buildLeftAlignedRow(Box.createHorizontalStrut(16), _master));
    add(SwingUtils.buildLeftAlignedRow(Box.createHorizontalStrut(32), scalePanel));
  }

  public Scale getSelectedScale() {
    return _chromaticRadioButton.isSelected() ? null : (Scale) _scaleCombos.get(_master.getSelectedIndex()).getSelectedItem();
  }
}
