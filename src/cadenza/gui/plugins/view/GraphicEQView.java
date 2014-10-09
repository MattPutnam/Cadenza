package cadenza.gui.plugins.view;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;

import cadenza.core.Note;
import cadenza.core.plugins.GraphicEQ;
import cadenza.gui.plugins.edit.GraphicEQEditor;
import cadenza.gui.plugins.edit.PluginEditor;

import common.swing.GraphicsUtils;
import common.swing.SwingUtils;

@SuppressWarnings("serial")
public class GraphicEQView extends PluginView {
  private static final int BAND_WIDTH = 3;
  private static final int THROW_HEIGHT = 64;
  
  private static final int WIDTH = 128*BAND_WIDTH + 2*MARGIN;
  private static final int HEIGHT = 2*THROW_HEIGHT + 2*MARGIN;
  private static final int MID_HEIGHT = THROW_HEIGHT + MARGIN;
  
  private final GraphicEQ _geq;
  
  public GraphicEQView(GraphicEQ geq) {
    super();
    _geq = geq;
    
    SwingUtils.freezeSize(this, WIDTH, HEIGHT);
    addMouseMotionListener(new ToolTipGenerator());
  }
  
  @Override
  protected void paintComponent(Graphics g) {
    final Graphics2D g2d = (Graphics2D) g;
    final int max = findMax();
    
    g2d.setColor(BACKGROUND);
    g2d.fillRect(0, 0, WIDTH, HEIGHT);
    
    g2d.setColor(DATA);
    final double scalingFactor = -((double) THROW_HEIGHT) / max;
    int x = MARGIN;
    int h;
    for (int i = 0; i < 128; ++i) {
      h = (int) (_geq.getLevels()[i] * scalingFactor);
      if (i == _midiNum && h != 0) {
        if (h > 0) {
          g2d.setColor(INPUT);
          g2d.fillRect(x, MID_HEIGHT+h, BAND_WIDTH, h);
        } else {
          g2d.setColor(INPUT_GR);
          g2d.fillRect(x, MID_HEIGHT, BAND_WIDTH, -h);
        }
        g2d.setColor(DATA);
      }
      
      g2d.drawLine(x, MID_HEIGHT, x, MID_HEIGHT+h);
      g2d.drawLine(x, MID_HEIGHT+h, x+BAND_WIDTH, MID_HEIGHT+h);
      g2d.drawLine(x+BAND_WIDTH, MID_HEIGHT+h, x+BAND_WIDTH, MID_HEIGHT);
      x += BAND_WIDTH;
    }
    
    g2d.setColor(AXES);
    g2d.drawLine(MARGIN, MARGIN, MARGIN, HEIGHT-MARGIN);
    g2d.drawLine(MARGIN, MID_HEIGHT, WIDTH-MARGIN, MID_HEIGHT);
    
    g2d.setFont(AXIS_FONT);
    final FontMetrics metrics = g2d.getFontMetrics();
    int width = (int) metrics.getStringBounds(AXIS_LABEL_INPUT_MIDINUM, g2d).getWidth();
    g2d.drawString(AXIS_LABEL_INPUT_MIDINUM, WIDTH-MARGIN-width, HEIGHT-3);
    
    final AffineTransform saved = g2d.getTransform();
    g2d.rotate(-Math.PI/2, 0, HEIGHT);
    final String topLabel = "+" + max;
    width = (int) metrics.getStringBounds(topLabel, g).getWidth();
    GraphicsUtils.drawString(g2d, topLabel, HEIGHT-MARGIN-width, HEIGHT+10);
    
    width = (int) metrics.getStringBounds(AXIS_LABEL_OUTPUT_VELOCITY, g2d).getWidth();
    GraphicsUtils.drawString(g2d, AXIS_LABEL_OUTPUT_VELOCITY, MID_HEIGHT-(width/2), HEIGHT+10);
    
    GraphicsUtils.drawString(g2d, "-" + max, MARGIN, HEIGHT+10);
    
    g2d.setTransform(saved);
  }
  
  private int findMax() {
    int max = 3;
    for (int i : _geq.getLevels())
      max = Math.max(max, Math.abs(i));
    return max;
  }
  
  @Override
  public PluginEditor createEditor() {
    return new GraphicEQEditor(_geq);
  }
  
  private class ToolTipGenerator extends MouseMotionAdapter {
    @Override
    public void mouseMoved(MouseEvent e) {
      final int midiNum = (e.getPoint().x - MARGIN) / BAND_WIDTH;
      if (0 <= midiNum && midiNum <= 127) {
        final int gain = _geq.process(midiNum, 0);
        final String text = new Note(midiNum).toString() + ": " + (gain >= 0 ? "+" : "") + gain;
        setToolTipText(text);
      }
    }
  }
}
