package cadenza.gui.common;

import java.util.Arrays;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import common.swing.IntField;
import common.swing.SwingUtils;

@SuppressWarnings("serial")
public class TranspositionEditor extends JPanel {
  private static final String[] DIRECTIONS = new String[] {
    "Up",
    "Down"
  };
  
  private static final String[] INTERVALS = new String[] {
    "0  (Unison)",
    "1  (m2)",
    "2  (M2)",
    "3  (m3)",
    "4  (M3)",
    "5  (P4)",
    "6  (TT)",
    "7  (P5)",
    "8  (m6)",
    "9  (M6)",
    "10 (m7)",
    "11 (M7)"
  };
  
  private final JComboBox<String> _direction;
  private final JComboBox<String> _interval;
  private final IntField _octaveField;

  public TranspositionEditor(int initialValue) {
    _direction = new JComboBox<>(DIRECTIONS);
    _interval = new JComboBox<>(INTERVALS);
    _octaveField = new IntField(initialValue, 0, 10);
    _octaveField.setColumns(4);
    SwingUtils.freezeSize(_octaveField);
    
    setTransposition(initialValue);
    
    final List<JComponent> components = Arrays.asList(new JLabel("Transpose"),
        _direction, _interval, new JLabel(" plus "), _octaveField, new JLabel(" octaves"),
        SwingUtils.button("Clear", e -> setTransposition(0)));
    
    final GroupLayout layout = new GroupLayout(this);
    setLayout(layout);
    
    final SequentialGroup sg = layout.createSequentialGroup();
    components.forEach(sg::addComponent);
    final ParallelGroup pg = layout.createParallelGroup(Alignment.BASELINE);
    components.forEach(pg::addComponent);
    
    layout.setHorizontalGroup(sg);
    layout.setVerticalGroup(pg);
  }
  
  public void setTransposition(int value) {
    _direction.setSelectedIndex(value < 0 ? 1 : 0);
    value = Math.abs(value);
    _interval.setSelectedIndex(value % 12);
    _octaveField.setInt(value / 12);
  }
  
  public int getTransposition() {
    int transposition = 12*_octaveField.getInt() + _interval.getSelectedIndex();
    if (_direction.getSelectedIndex() == 1)
      transposition *= -1;
    
    return transposition;
  }

}
