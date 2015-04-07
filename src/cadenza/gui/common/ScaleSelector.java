package cadenza.gui.common;

import java.awt.CardLayout;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import cadenza.core.Scale;

import common.swing.SwingUtils;

@SuppressWarnings("serial")
public class ScaleSelector extends JPanel {
  private final List<Scale>[] _scales;
  
  private final JRadioButton _chromaticRadioButton;
  private final JRadioButton _scaleRadioButton;
  
  private final JComboBox<String> _master;
  private final List<JComboBox<Scale>> _scaleCombos;
  
  private final List<ScaleSelectionListener> _listeners;
  
  public ScaleSelector(List<Scale>[] scales, String[] names, Scale initial, String chromaticLabel) {
    _scales = scales;
    
    _chromaticRadioButton = new JRadioButton(chromaticLabel);
    _scaleRadioButton = new JRadioButton("Predefined Scale");
    SwingUtils.groupAndSelectFirst(_chromaticRadioButton, _scaleRadioButton);
    
    _scaleCombos = Arrays.stream(scales)
                         .map(group -> new JComboBox<>(group.toArray(new Scale[group.size()])))
                         .collect(Collectors.toList());
    
    _listeners = new LinkedList<>();
    
    final CardLayout layout = new CardLayout();
    final JPanel scalePanel = new JPanel(layout);
    for (int i = 0; i < scales.length; ++i)
      scalePanel.add(_scaleCombos.get(i), String.valueOf(i));
    
    _master = new JComboBox<>(names);
    _master.addActionListener(e -> layout.show(scalePanel, String.valueOf(_master.getSelectedIndex())));
    
    setSelectedScale(initial);
    
    _chromaticRadioButton.addActionListener(e -> notifyListeners(null));
    final ActionListener notifier = e -> notifyListeners(getSelectedScale());
    _scaleRadioButton.addActionListener(notifier);
    _master.addActionListener(notifier);
    _scaleCombos.forEach(combo -> combo.addActionListener(notifier));
    
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    add(SwingUtils.hugWest(_chromaticRadioButton));
    add(SwingUtils.hugWest(_scaleRadioButton));
    add(SwingUtils.buildLeftAlignedRow(Box.createHorizontalStrut(16), _master));
    add(SwingUtils.buildLeftAlignedRow(Box.createHorizontalStrut(32), scalePanel));
  }

  public Scale getSelectedScale() {
    return _chromaticRadioButton.isSelected() ? null : (Scale) _scaleCombos.get(_master.getSelectedIndex()).getSelectedItem();
  }
  
  public void setSelectedScale(Scale scale) {
    if (scale == null) {
      _chromaticRadioButton.setSelected(true);
    } else {
      _scaleRadioButton.setSelected(true);
      
      for (int i = 0; i < _scales.length; ++i) {
        if (_scales[i].contains(scale)) {
          _master.setSelectedIndex(i);
          _scaleCombos.get(i).setSelectedItem(scale);
          break;
        }
      }
    }
  }
  
  public void addListener(ScaleSelectionListener listener) {
    _listeners.add(listener);
  }
  
  public void removeListener(ScaleSelectionListener listener) {
    _listeners.remove(listener);
  }
  
  private void notifyListeners(Scale scale) {
    _listeners.forEach(l -> l.scaleSelected(scale));
  }
  
  @FunctionalInterface
  public static interface ScaleSelectionListener {
    public void scaleSelected(Scale scale);
  }
}
