package cadenza.gui.patchusage.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import cadenza.core.Note;
import cadenza.core.Note.PitchClass;
import cadenza.core.Scale;
import cadenza.core.patchusage.CustomScalePatchUsage;
import cadenza.gui.common.ScaleSelector;
import cadenza.gui.keyboard.KeyboardPanel;
import cadenza.gui.keyboard.MultipleKeyboardAdapter;
import cadenza.gui.keyboard.MultipleKeyboardPanel;

import common.swing.GraphicsUtils;
import common.swing.SwingUtils;

@SuppressWarnings("serial")
public class CustomScalePatchUsageEditor extends JPanel {
  @SuppressWarnings("rawtypes")
  private static final List[] SCALES = new List[] {
      Scale.Diatonic.ALL_MAJOR,
      Scale.Diatonic.ALL_MINOR,
      Scale.Diatonic.ALL_HARMONIC,
      Scale.WholeTone.ALL };
  private static final String[] NAMES = new String[] {
      "Major", "Minor", "Harmonic", "Whole Tone"
  };
  
  private static final Note B4 = new Note(PitchClass.B, 4);
  private static final Note C3 = new Note(PitchClass.C, 3);
  private static final Note C6 = new Note(PitchClass.C, 6);
  
  private static final int GAP = 40;
  
  private final Map<PitchClass, Integer> _map;
  private Scale _selectedScale;
  
  private final KeyboardPanel _srcPanel;
  private final KeyboardPanel _destPanel;
  private final KeyboardArea _keyboardArea;
  
  private ScaleSelector _scaleSelector;
  
  public CustomScalePatchUsageEditor() {
    _map = new IdentityHashMap<>();
    
    _srcPanel = new KeyboardPanel(Note.C4, B4);
    _destPanel = new KeyboardPanel(C3, C6);
    _keyboardArea = new KeyboardArea();
    
    setLayout(new BorderLayout());
    add(_keyboardArea, BorderLayout.WEST);
    add(buildSelectors(), BorderLayout.CENTER);
    
    _keyboardArea.repaint();
  }
  
  public void populate(CustomScalePatchUsage template) {
    _map.clear();
    _map.putAll(template.map);
    _selectedScale = template.scale;
    
    _scaleSelector.setSelectedScale(_selectedScale);
  }
  
  public Map<PitchClass, Integer> getMap() {
    return _map;
  }
  
  public Scale getSelectedScale() {
    return _selectedScale;
  }
  
  public boolean isScaleSelected() {
    return _selectedScale != null;
  }
  
  @SuppressWarnings("unchecked")
  private JComponent buildSelectors() {
    final JPanel result = new JPanel(new BorderLayout());
    
    _scaleSelector = new ScaleSelector(SCALES, NAMES, null, "Custom");
    _scaleSelector.addListener(scale -> {
      _selectedScale = scale;
      
      _map.clear();
      if (_selectedScale != null)
        _map.putAll(_selectedScale.buildMapFromNaturals());
      
      revalidate();
      repaint();
    });
    
    final JLabel instructionLabel = new JLabel("Drag from the top keyboard " +
        "to the bottom keyboard or select a predefined scale");
    
    result.add(instructionLabel, BorderLayout.NORTH);
    result.add(SwingUtils.hugNorth(_scaleSelector), BorderLayout.CENTER);
    
    return result;
  }
  
  private class KeyboardArea extends MultipleKeyboardPanel {
    public KeyboardArea() {
      super(GAP, _srcPanel, _destPanel);
      
      addMultipleKeyboardListener(new MultipleKeyboardAdapter() {
        @Override
        public void keyClicked(Note note, KeyboardPanel source) {
          if (source == _srcPanel) {
            _map.remove(note.getPitchClass());
            _selectedScale = null;
            repaint();
          }
        }
        
        @Override
        public void keyDragged(Note startNote, KeyboardPanel startSource,
                     Note endNote, KeyboardPanel endSource) {
          if (startSource == _srcPanel && endSource == _destPanel && startNote.getPitchClass().isWhite()) {
            _map.put(startNote.getPitchClass(), Integer.valueOf(endNote.getMidiNumber() - startNote.getMidiNumber()));
            _selectedScale = null;
            repaint();
          }
        }
      });
    }
    
    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      
      g.setColor(Color.BLACK);
      
      for (Entry<PitchClass, Integer> entry : _map.entrySet()) {
        final Note start = new Note(entry.getKey(), 4);
        final Rectangle startRect = _srcPanel.getKeyPosition(start);
        final int x1 = startRect.x + (startRect.width/2) + _srcPanel.getLocation().x;
        final int y1 = _srcPanel.getHeight();
        
        final Note end = Note.valueOf(start.getMidiNumber() + entry.getValue().intValue());
        final Rectangle endRect = _destPanel.getKeyPosition(end);
        final int x2 = endRect.x + (endRect.width/2);
        final int y2 = _srcPanel.getHeight() + GAP;
        
        final Graphics2D g2d = (Graphics2D) g.create();
        GraphicsUtils.antialias(g2d);
        GraphicsUtils.drawArrow(g2d, x1, y1, x2, y2);
      }
    }
  }
}
