package cadenza.gui.effects.edit;

import java.util.stream.IntStream;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;

import cadenza.core.effects.Effect;
import cadenza.core.effects.GraphicEQ;
import cadenza.gui.effects.view.GraphicEQView;
import common.midi.MidiUtilities;
import common.swing.SimpleGrid;
import common.swing.SwingUtils;

@SuppressWarnings("serial")
public class GraphicEQEditor extends EffectEditor {
  private static final int SLIDER_MAX = 10;
  
  private final GraphicEQ _geq;
  private final GraphicEQView _geqView;
  
  private final JSlider[] _sliders;
  
  public GraphicEQEditor(GraphicEQ initial) {
    final int[] levels = initial.getLevels();
    _geq = new GraphicEQ(levels);
    _geqView = new GraphicEQView(_geq);
    
    _sliders = new JSlider[128];
    final JLabel[] labels = new JLabel[128];
    IntStream.range(0, 128).forEach(i -> {
      final JSlider slider = new JSlider(JSlider.VERTICAL, -SLIDER_MAX, SLIDER_MAX, levels[i]);
      slider.setMajorTickSpacing(SLIDER_MAX);
      slider.setMinorTickSpacing(1);
      slider.setPaintTicks(true);
      slider.setPaintLabels(true);
      _sliders[i] = slider;
      labels[i] = new JLabel(MidiUtilities.noteNumberToName(i));
      
      slider.addChangeListener(e -> {
        _geq.setLevel(i, slider.getValue());
        _geqView.repaint();
      });
    });
    
    final JScrollPane sPane = new JScrollPane(new SimpleGrid(new JComponent[][] { _sliders, labels }),
        JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    SwingUtils.freezeWidth(sPane, 500);
    
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    add(_geqView);
    add(sPane);
  }
  
  @Override
  public Effect getEffect() {
    return _geq;
  }
}
