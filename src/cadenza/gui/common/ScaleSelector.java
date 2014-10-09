package cadenza.gui.common;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
  private JRadioButton _chromaticRadioButton;
  private JRadioButton _scaleRadioButton;
  private JComboBox<Scale> _majorScales;
  private JComboBox<Scale> _minorScales;
  private JComboBox<Scale> _harmonicScales;
  private JComboBox<Scale> _modalScales;
  private JComboBox<Scale> _bluesScales;
  private JComboBox<Scale> _pentatonicScales;
  private JComboBox<Scale> _diminishedScales;
  private JComboBox<Scale> _wholeToneScales;
  
  private Scale _selectedScale;
  
  public ScaleSelector(Scale initial) {
    _chromaticRadioButton = new JRadioButton("None [chromatic]");
    _scaleRadioButton = new JRadioButton("Predefined Scale");
    SwingUtils.groupAndSelectFirst(_chromaticRadioButton, _scaleRadioButton);
    
    _majorScales = new JComboBox<>(Scale.Diatonic.ALL_MAJOR.toArray(new Scale[0]));
    _minorScales = new JComboBox<>(Scale.Diatonic.ALL_MINOR.toArray(new Scale[0]));
    _harmonicScales = new JComboBox<>(Scale.Diatonic.ALL_HARMONIC.toArray(new Scale[0]));
    _modalScales = new JComboBox<>(Scale.Modal.ALL.toArray(new Scale[0]));
    _bluesScales = new JComboBox<>(Scale.Blues.ALL.toArray(new Scale[0]));
    _pentatonicScales = new JComboBox<>(Scale.Pentatonic.ALL.toArray(new Scale[0]));
    _diminishedScales = new JComboBox<>(Scale.Diminished.ALL.toArray(new Scale[0]));
    _wholeToneScales = new JComboBox<>(Scale.WholeTone.ALL.toArray(new Scale[0]));
    
    final ActionListener scaleSelector = new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        _selectedScale = (Scale) ((JComboBox<?>) e.getSource()).getSelectedItem();
      }
    };
    _majorScales.addActionListener(scaleSelector);
    _minorScales.addActionListener(scaleSelector);
    _harmonicScales.addActionListener(scaleSelector);
    _modalScales.addActionListener(scaleSelector);
    _bluesScales.addActionListener(scaleSelector);
    _pentatonicScales.addActionListener(scaleSelector);
    _diminishedScales.addActionListener(scaleSelector);
    _wholeToneScales.addActionListener(scaleSelector);
    
    final ActionListener comboEnabler = new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        final boolean enabled = e.getSource() == _scaleRadioButton;
        _majorScales.setEnabled(enabled);
        _minorScales.setEnabled(enabled);
        _harmonicScales.setEnabled(enabled);
        _modalScales.setEnabled(enabled);
        _bluesScales.setEnabled(enabled);
        _pentatonicScales.setEnabled(enabled);
        _diminishedScales.setEnabled(enabled);
        _wholeToneScales.setEnabled(enabled);
      }
    };
    _chromaticRadioButton.addActionListener(comboEnabler);
    _scaleRadioButton.addActionListener(comboEnabler);
    _majorScales.setEnabled(false);
    _minorScales.setEnabled(false);
    _harmonicScales.setEnabled(false);
    _modalScales.setEnabled(false);
    _bluesScales.setEnabled(false);
    _pentatonicScales.setEnabled(false);
    _diminishedScales.setEnabled(false);
    _wholeToneScales.setEnabled(false);
    
    if (initial != null) {
      _scaleRadioButton.setSelected(true);
      _selectedScale = initial;
      
      if (Scale.Diatonic.ALL_MAJOR.contains(initial))
        _majorScales.setSelectedItem(initial);
      else if (Scale.Diatonic.ALL_MINOR.contains(initial))
        _minorScales.setSelectedItem(initial);
      else if (Scale.Diatonic.ALL_HARMONIC.contains(initial))
        _harmonicScales.setSelectedItem(initial);
      else if (Scale.Diminished.ALL.contains(initial))
        _diminishedScales.setSelectedItem(initial);
      else if (Scale.WholeTone.ALL.contains(initial))
        _wholeToneScales.setSelectedItem(initial);
      
      _majorScales.setEnabled(true);
      _minorScales.setEnabled(true);
      _harmonicScales.setEnabled(true);
      _modalScales.setEnabled(true);
      _bluesScales.setEnabled(true);
      _pentatonicScales.setEnabled(true);
      _diminishedScales.setEnabled(true);
      _wholeToneScales.setEnabled(true);
    }
    
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    add(SwingUtils.hugWest(new JLabel("Scale:")));
    add(SwingUtils.hugWest(_chromaticRadioButton));
    add(SwingUtils.hugWest(_scaleRadioButton));
    add(SwingUtils.buildRow(Box.createHorizontalStrut(8), _majorScales));
    add(SwingUtils.buildRow(Box.createHorizontalStrut(8), _minorScales));
    add(SwingUtils.buildRow(Box.createHorizontalStrut(8), _harmonicScales));
    add(SwingUtils.buildRow(Box.createHorizontalStrut(8), _modalScales));
    add(SwingUtils.buildRow(Box.createHorizontalStrut(8), _bluesScales));
    add(SwingUtils.buildRow(Box.createHorizontalStrut(8), _pentatonicScales));
    add(SwingUtils.buildRow(Box.createHorizontalStrut(8), _diminishedScales));
    add(SwingUtils.buildRow(Box.createHorizontalStrut(8), _wholeToneScales));
    add(Box.createVerticalGlue());
  }

  public Scale getSelectedScale() {
    return _chromaticRadioButton.isSelected() ? null : _selectedScale;
  }
}
