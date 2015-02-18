package cadenza.gui.effects.edit;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;

import cadenza.core.effects.ParametricEQ;
import cadenza.core.effects.ParametricEQ.Band;
import cadenza.gui.effects.view.ParametricEQView;

import common.swing.DoubleField;
import common.swing.IntField;
import common.swing.SimpleGrid;
import common.swing.SwingUtils;
import common.swing.icon.DeleteIcon;

@SuppressWarnings("serial")
public class ParametricEQEditor extends EffectEditor {
  private final List<Band> _bands;
  private final ParametricEQ _peq;
  private final ParametricEQView _peqView;
  
  private final JPanel _bandPanel;
  
  public ParametricEQEditor(ParametricEQ initial) {
    _bands = initial.getBands();
    _peq = new ParametricEQ(_bands);
    _peqView = new ParametricEQView(_peq);
    
    _bandPanel = new JPanel(new FlowLayout());
    refreshBands();
    
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    add(_peqView);
    add(new JScrollPane(_bandPanel, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
    add(SwingUtils.buildRow(SwingUtils.button("Add New Band", e -> {
                              _bands.add(new Band(64, 3.0, 1.0));
                              refreshBands();
                            }),
                            SwingUtils.button("Sort Bands", e -> {
                              Collections.sort(_bands, (b1, b2) -> b1.getFrequency() - b2.getFrequency());
                              refreshBands();
                            })));
  }
  
  @Override
  public ParametricEQ getEffect() {
    return _peq;
  }
  
  private void refreshBands() {
    _bandPanel.removeAll();
    for (final Band band : _bands)
      _bandPanel.add(new BandEditor(band));
    _bandPanel.repaint();
    _peq.update();
    revalidate();
    repaint();
  }
  
  private class BandEditor extends JPanel {
    private final Band _band;
    
    private final JSlider _frequencySlider;
    private final IntField _frequencyField;
    
    private final JSlider _gainSlider;
    private final DoubleField _gainField;
    
    private final JSlider _qualitySlider;
    private final DoubleField _qualityField;
    
    private final JCheckBox _highShelfCheckBox;
    private final JCheckBox _lowShelfCheckBox;
    
    public BandEditor(Band band) {
      super();
      _band = band;
      
      _frequencySlider = new JSlider(JSlider.VERTICAL, 0, 127, band.getFrequency());
      _frequencyField = new IntField(band.getFrequency(), 0, 127);
      _frequencyField.setColumns(3);
      
      _gainSlider = new JSlider(JSlider.VERTICAL, -10000, 10000, (int) (1000*band.getGain()));
      _gainField = new DoubleField(band.getGain());
      _gainField.setColumns(3);
      
      _qualitySlider = new JSlider(JSlider.VERTICAL, 200, 10000, (int) (1000*band.getQuality()));
      _qualityField = new DoubleField(band.getQuality(), 0.0, Double.POSITIVE_INFINITY);
      _qualityField.setColumns(3);
      
      _highShelfCheckBox = new JCheckBox("High Shelf");
      _lowShelfCheckBox = new JCheckBox("Low Shelf");
      
      addListeners();
      
      setBorder(BorderFactory.createLineBorder(Color.BLACK));
      
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      add(SwingUtils.buildRow(Box.createHorizontalGlue(), new DeleteButton(_band)));
      add(new SimpleGrid(new JComponent[][]
      {
        { new JLabel("f"),  new JLabel("G"), new JLabel("Q") },
        { _frequencySlider,   _gainSlider,   _qualitySlider },
        { _frequencyField,    _gainField,    _qualityField}
      }, Alignment.LEADING, Alignment.CENTER));
      add(SwingUtils.hugWest(_highShelfCheckBox));
      add(SwingUtils.hugWest(_lowShelfCheckBox));
    }
    
    private void addListeners() {
      _frequencySlider.addChangeListener(e -> {
        final int freq = _frequencySlider.getValue();
        _frequencyField.setText(String.valueOf(freq));
        _band.setFrequency(freq);
        _peq.update();
        _peqView.repaint();
      });
      _frequencyField.addActionListener(e -> {
        int freq = _frequencyField.getInt();
        if (freq > 127) {
          freq = 127;
          _frequencyField.setText("127");
        }
        _frequencySlider.setValue(freq);
        _band.setFrequency(freq);
        _peq.update();
        _peqView.repaint();
      });
      _frequencyField.addFocusListener(new FocusAdapter() {
        @Override
        public void focusLost(FocusEvent e) {
          int freq = _frequencyField.getInt();
          if (freq > 127) {
            freq = 127;
            _frequencyField.setText("127");
          }
          _frequencySlider.setValue(freq);
          _band.setFrequency(freq);
          _peq.update();
          _peqView.repaint();
        }
      });
      
      _gainSlider.addChangeListener(e -> {
        final double gain = _gainSlider.getValue() / 1000.0;
        _gainField.setText(String.valueOf(gain));
        _band.setGain(gain);
        _peq.update();
        _peqView.repaint();
      });
      _gainField.addActionListener(e -> {
        double gain = _gainField.getDouble();
        if (gain < -10.0) {
          gain = -10.0;
          _gainField.setText("-10.0");
        } else if (gain > 10.0) {
          gain = 10.0;
          _gainField.setText("10.0");
        }
        _gainSlider.setValue((int) (gain * 1000));
        _band.setGain(gain);
        _peq.update();
        _peqView.repaint();
      });
      _gainField.addFocusListener(new FocusAdapter() {
        @Override
        public void focusLost(FocusEvent e) {
          double gain = _gainField.getDouble();
          if (gain < -10.0) {
            gain = -10.0;
            _gainField.setText("-10.0");
          } else if (gain > 10.0) {
            gain = 10.0;
            _gainField.setText("10.0");
          }
          _gainSlider.setValue((int) (gain * 1000));
          _band.setGain(gain);
          _peq.update();
          _peqView.repaint();
        }
      });
      
      _qualitySlider.addChangeListener(e -> {
        double quality = _qualitySlider.getValue() / 1000.0;
        _qualityField.setText(String.valueOf(quality));
        _band.setQuality(quality);
        _peq.update();
        _peqView.repaint();
      });
      _qualityField.addActionListener(e -> {
        double quality = _qualityField.getDouble();
        if (quality > 10.0) {
          quality = 10.0;
          _qualityField.setText("10.0");
        }
        _qualitySlider.setValue((int) (quality*1000));
        _band.setQuality(quality);
        _peq.update();
        _peqView.repaint();
      });
      _qualityField.addFocusListener(new FocusAdapter() {
        @Override
        public void focusLost(FocusEvent e) {
          double quality = _qualityField.getDouble();
          if (quality > 10.0) {
            quality = 10.0;
            _qualityField.setText("10.0");
          }
          _qualitySlider.setValue((int) (quality*1000));
          _band.setQuality(quality);
          _peq.update();
          _peqView.repaint();
        }
      });
      
      _highShelfCheckBox.addActionListener(e -> {
        final boolean isSelected = _highShelfCheckBox.isSelected();
        if (isSelected)
          _lowShelfCheckBox.setSelected(false);
        _band.setHighShelf(isSelected);
        _peq.update();
        _peqView.repaint();
      });
      _lowShelfCheckBox.addActionListener(e -> {
        final boolean isSelected = _lowShelfCheckBox.isSelected();
        if (isSelected)
          _highShelfCheckBox.setSelected(false);
        _band.setLowShelf(isSelected);
        _peq.update();
        _peqView.repaint();
      });
    }
  }
  
  private class DeleteButton extends JButton {
    public DeleteButton(final Band band) {
      super(new DeleteIcon(10));
      setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
      addActionListener(e -> {
        _bands.remove(band);
        refreshBands();
      });
      setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
  }
}
