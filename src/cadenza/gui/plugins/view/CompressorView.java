package cadenza.gui.plugins.view;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.text.DecimalFormat;
import java.util.Optional;

import cadenza.core.plugins.Compressor;
import cadenza.gui.plugins.edit.CompressorEditor;
import cadenza.gui.plugins.edit.PluginEditor;

import common.swing.GraphicsUtils;
import common.swing.SwingUtils;
import common.swing.Tooltip;

@SuppressWarnings("serial")
public class CompressorView extends PluginView {
  private static final int SIZE = 128 + MARGIN + MARGIN;
  private static final DecimalFormat FORMAT = new DecimalFormat("0.000");
  
  private final Compressor _compressor;
  
  public CompressorView(Compressor compressor) {
    super();
    _compressor = compressor;
    
    SwingUtils.freezeSize(this, SIZE, SIZE);
    Tooltip.registerTooltip(this, e -> {
      final int x = e.getPoint().x;
      if (x >= MARGIN && x < SIZE-MARGIN) {
        final int velocity = x - MARGIN;
        final int output = _compressor.process(0, velocity);
        return Optional.of("<html>Threshold=" + _compressor.getThreshold() + " / Ratio=" +
            FORMAT.format(_compressor.getRatio()) +
            "<br>Input=" + velocity + " / Output=" + output);
      } else {
        return Optional.empty();
      }
    });
  }
  
  @Override
  protected void paintComponent(Graphics g) {
    final Graphics2D g2d = (Graphics2D) g;
    
    final int threshold = _compressor.getThreshold();
    final int t127 = _compressor.process(0, 127);
    
    g2d.setColor(BACKGROUND);
    g2d.fillRect(0, 0, SIZE, SIZE);
    
    g2d.setColor(AXES);
    g2d.drawLine(MARGIN, MARGIN, MARGIN, SIZE-MARGIN);
    g2d.drawLine(MARGIN, SIZE-MARGIN, SIZE-MARGIN, SIZE-MARGIN);
    
    g2d.setColor(DATA);
    final int[] xPoints = new int[] {MARGIN,      MARGIN+threshold,      SIZE-MARGIN};
    final int[] yPoints = new int[] {SIZE-MARGIN, SIZE-MARGIN-threshold, SIZE-MARGIN-t127};
    g2d.drawPolyline(xPoints, yPoints, 3);
    
    if (_velocity != -1) {
      g2d.setColor(_velocity > threshold ? INPUT_GR : INPUT);
      g2d.drawLine(MARGIN+_velocity, SIZE-MARGIN, MARGIN+_velocity, SIZE-MARGIN-_compressor.process(0, _velocity));
    }
    
    g2d.setColor(AXES);
    g2d.setFont(AXIS_FONT);
    final FontMetrics metrics = g2d.getFontMetrics();
    int width = (int) metrics.getStringBounds(AXIS_LABEL_INPUT_VELOCITY, g2d).getWidth();
    g2d.drawString(AXIS_LABEL_INPUT_VELOCITY, SIZE-MARGIN-width, SIZE-3);
    
    final AffineTransform saved = g2d.getTransform();
    g2d.rotate(-Math.PI/2, 0, SIZE);
    width = (int) metrics.getStringBounds(AXIS_LABEL_OUTPUT_VELOCITY, g2d).getWidth();
    GraphicsUtils.drawString(g2d, AXIS_LABEL_OUTPUT_VELOCITY, SIZE-MARGIN-width, SIZE+10);
    g2d.setTransform(saved);
  }
  
  @Override
  public PluginEditor createEditor() {
    return new CompressorEditor(_compressor);
  }
}

