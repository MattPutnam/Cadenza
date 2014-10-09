package cadenza.gui.plugins.edit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.DecimalFormat;

import javax.swing.BoxLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cadenza.core.plugins.Compressor;
import cadenza.gui.plugins.view.CompressorView;

import common.swing.DoubleField;
import common.swing.IntField;
import common.swing.SimpleGrid;
import common.swing.SwingUtils;

@SuppressWarnings("serial")
public class CompressorEditor extends PluginEditor {
  private static final int RATIO_SLIDER_STEPS = 100000;
  private static final DecimalFormat RATIO_FORMAT = new DecimalFormat("0.000");
  
  private final Compressor _compressor;
  private final CompressorView _compressorView;
  
  private final JSlider _thresholdSlider;
  private final JSlider _ratioSlider;
  
  private final IntField _thresholdField;
  private final DoubleField _ratioField;
  
  public CompressorEditor(Compressor initial) {
    super();
    _compressor = new Compressor(initial.getThreshold(), initial.getRatio());
    _compressorView = new CompressorView(_compressor);
    
    _thresholdSlider = new JSlider(0, 127, initial.getThreshold());
    _ratioSlider = new JSlider(0, RATIO_SLIDER_STEPS, calculateSliderValueFromRatio(initial.getRatio()));
    
    _thresholdField = new IntField(initial.getThreshold(), 0, 127);
    _thresholdField.setColumns(5);
    _ratioField = new DoubleField(1.0, 1.0, Double.POSITIVE_INFINITY);
    _ratioField.setText(RATIO_FORMAT.format(initial.getRatio()));
    _ratioField.setColumns(5);
    
    SwingUtils.freezeSize(_thresholdField);
    SwingUtils.freezeSize(_ratioField);
    
    addListeners();
    
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    add(_compressorView);
    add(new SimpleGrid(new JComponent[][]
    {
      { new JLabel("  Threshold:"), _thresholdSlider, _thresholdField },
      { new JLabel("Ratio:"),     _ratioSlider,     _ratioField     }
    }, Alignment.CENTER, Alignment.TRAILING));
  }
  
  @Override
  public Compressor getPlugin() {
    return _compressor;
  }
  
  private void addListeners() {
    _thresholdSlider.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent _) {
        updateThreshold(_thresholdSlider.getValue());
      }
    });
    _thresholdField.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent _) {
        int fieldVal = _thresholdField.getInt();
        if (fieldVal > 127) {
          fieldVal = 127;
          _thresholdField.setText("127");
        }
        updateThreshold(fieldVal);
      }
    });
    _thresholdField.addFocusListener(new FocusAdapter() {
      @Override
      public void focusLost(FocusEvent _) {
        updateThreshold(_thresholdField.getInt());
      }
    });
    
    _ratioSlider.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent _) {
        updateRatio(_ratioSlider.getValue());
      }
    });
    _ratioField.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent _) {
        double fieldVal = _ratioField.getDouble();
        if (fieldVal < 1.0) {
          fieldVal = 1.0;
          _ratioField.setText("1.0");
        }
        updateRatio(fieldVal);
      }
    });
    _ratioField.addFocusListener(new FocusAdapter() {
      @Override
      public void focusLost(FocusEvent _) {
        double fieldVal = _ratioField.getDouble();
        if (fieldVal < 1.0) {
          fieldVal = 1.0;
          _ratioField.setText("1.0");
        }
        updateRatio(fieldVal);
      }
    });
  }
  
  private static int calculateSliderValueFromRatio(double ratio) {
    if (ratio == Double.POSITIVE_INFINITY)
      return RATIO_SLIDER_STEPS;
    else
      return (int) (RATIO_SLIDER_STEPS * (ratio - 1) / ratio);
  }
  
  private static double calculateRatioFromSliderValue(int sliderValue) {
    return ((double) RATIO_SLIDER_STEPS) / (RATIO_SLIDER_STEPS - sliderValue);
  }
  
  private void updateThreshold(int threshold) {
    _thresholdSlider.setValue(threshold);
    _thresholdField.setText(String.valueOf(threshold));
    _compressor.setThreshold(threshold);
    _compressorView.repaint();
  }
  
  private void updateRatio(double ratio) {
    _ratioSlider.setValue(calculateSliderValueFromRatio(ratio));
    _compressor.setRatio(ratio);
    _compressorView.repaint();
  }
  
  private void updateRatio(int sliderPosition) {
    final double ratio = calculateRatioFromSliderValue(sliderPosition);
    _ratioField.setText(RATIO_FORMAT.format(ratio));
    _compressor.setRatio(ratio);
    _compressorView.repaint();
  }
}
