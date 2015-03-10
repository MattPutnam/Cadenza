package cadenza.gui.effects.view;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.Optional;

import cadenza.core.Note;
import cadenza.core.effects.ParametricEQ;
import cadenza.gui.effects.edit.EffectEditor;
import cadenza.gui.effects.edit.ParametricEQEditor;
import common.swing.GraphicsUtils;
import common.swing.SwingUtils;
import common.swing.Tooltip;


@SuppressWarnings("serial")
public class ParametricEQView extends EffectView {
  private static final int BAND_WIDTH = 3;
  private static final int THROW_HEIGHT = 64;
  
  private static final int WIDTH = 128*BAND_WIDTH + 2*MARGIN;
  private static final int HEIGHT = 2*THROW_HEIGHT + 2*MARGIN;
  private static final int MID_HEIGHT = THROW_HEIGHT + MARGIN;
  
  private final ParametricEQ _peq;
  
  public ParametricEQView(ParametricEQ peq) {
    super();
    _peq = peq;
    
    SwingUtils.freezeSize(this, WIDTH, HEIGHT);
    Tooltip.registerTooltip(this, e -> {
      final int midiNum = (e.getPoint().x - MARGIN) / BAND_WIDTH;
      if (0 <= midiNum && midiNum <= 127) {
        final int gain = _peq.process(midiNum, 0);
        return Optional.of(Note.valueOf(midiNum).toString() + ": " + (gain >= 0 ? "+" : "") + gain);
      } else {
        return Optional.empty();
      }
    });
  }
  
  @Override
  protected void paintComponent(Graphics g) {
    final Graphics2D g2d = (Graphics2D) g;
    final int max = findMax();
    
    g2d.setColor(BACKGROUND);
    g2d.fillRect(0, 0, WIDTH, HEIGHT);
    
    g2d.setColor(DATA);
    final double scalingFactor = -((double) THROW_HEIGHT) / max;
    final double[] fineLevels = _peq.getFineLevels();
    int x = MARGIN;
    int h;
    final int[] xPoints = new int[128];
    final int[] yPoints = new int[128];
    for (int i = 0; i < 128; ++i) {
      h = (int) (fineLevels[i] * scalingFactor);
      xPoints[i] = x;
      yPoints[i] = MID_HEIGHT + h;
      x += BAND_WIDTH;
    }
    g2d.drawPolyline(xPoints, yPoints, 128);
    
    // draw ticks for A0, C4, and C8
    for (Note note : new Note[] { Note.A0, Note.C4, Note.C8 }) {
      int midiNum = note.getMidiNumber();
      x = MARGIN+midiNum*BAND_WIDTH;
      g2d.drawLine(x, MID_HEIGHT-3, x, MID_HEIGHT+3);
    }
    
    if (_midiNum != -1) {
      double fine = _peq.getFineLevels()[_midiNum];
      g2d.setColor(fine > 0.0 ? INPUT : INPUT_GR);
      x = MARGIN + _midiNum*BAND_WIDTH;
      g2d.drawLine(x, MID_HEIGHT, x, MID_HEIGHT+(int)(fine*scalingFactor));
    }
    
    g2d.setColor(AXES);
    g2d.drawLine(MARGIN, MARGIN, MARGIN, HEIGHT-MARGIN);
    g2d.drawLine(MARGIN, MID_HEIGHT, WIDTH-MARGIN, MID_HEIGHT);
    
    g2d.setFont(AXIS_FONT);
    final FontMetrics metrics = g.getFontMetrics();
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
    for (int i = 0; i < 128; ++i)
      max = Math.max(max, (int) Math.ceil(Math.abs(_peq.getFineLevels()[i])));
    return max;
  }
  
  @Override
  public EffectEditor createEditor() {
    return new ParametricEQEditor(_peq);
  }
}
