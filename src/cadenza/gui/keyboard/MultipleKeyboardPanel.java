package cadenza.gui.keyboard;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;

import cadenza.core.Note;


/**
 * Class that displays multiple KeyboardPanels vertically, with corresponding
 * keys lined up (if possible).  Also provides utilities for clicking/dragging
 * between multiple keyboards.
 * 
 * @author Matt Putnam
 */
public class MultipleKeyboardPanel extends JPanel {
  private static final long serialVersionUID = 2L;
  
  private final KeyboardPanel[] _keyboards;
  private final int _spacing;
  private final Dimension _size;
  
  private transient final List<MultipleKeyboardListener> _listeners;
  
  private transient PanelNotePair _pressed;
  private transient PanelNotePair _dragged;
  
  /**
   * Creates a new MultipleKeyboardPanel with the given keyboards and vertical
   * spacing between them
   * @param spacing - the distance between keyboards
   * @param keyboards - the number of keyboards to display (must be at least 2)
   */
  public MultipleKeyboardPanel(int spacing, KeyboardPanel... keyboards) {
    super();
    if (keyboards.length < 2)
      throw new IllegalArgumentException("Must have at least 2 keyboards");
    
    _spacing = spacing;
    _keyboards = keyboards;
    _listeners = new LinkedList<>();
    
    final Mouser mouser = new Mouser();
    addMouseListener(mouser);
    addMouseMotionListener(mouser);
    
    _size = layoutKeyboards();
  }
  
  /**
   * Registers a new MultipleKeyboardListener to this component
   * @param listener - the listener to register
   */
  public void addMultipleKeyboardListener(MultipleKeyboardListener listener) {
    _listeners.add(listener);
  }
  
  private Dimension layoutKeyboards() {
    int width = 0;
    
    KeyboardPanel leftMost = _keyboards[0];
    final int len = _keyboards.length;
    for (int i = 1; i < len; ++i) {
      final KeyboardPanel k = _keyboards[i];
      if (k.getLowNote().compareTo(leftMost.getLowNote()) < 0)
        leftMost = k;
    }
    
    final Box content = Box.createVerticalBox();
    for (int i = 0; i < len; ++i) {
      final KeyboardPanel panel = _keyboards[i];
      if (panel == leftMost) {
        final Box hBox = Box.createHorizontalBox();
        hBox.add(panel);
        hBox.add(Box.createHorizontalGlue());
        content.add(hBox);
      } else {
        final Rectangle pos = leftMost.getKeyPosition(panel.getLowNote());
        if (pos == null) {
          content.add(panel);
        } else {
          final Box hBox = Box.createHorizontalBox();
          hBox.add(Box.createHorizontalStrut(pos.x));
          hBox.add(panel);
          hBox.add(Box.createHorizontalGlue());
          content.add(hBox);
        }
      }
      
      if (panel.getWidth() > width)
        width = panel.getWidth();
      
      if (i < len-1)
        content.add(Box.createVerticalStrut(_spacing));
    }
    
    setLayout(new BorderLayout());
    add(content, BorderLayout.NORTH);
    
    return new Dimension(width, len*(KeyboardPanel.WHITE_HEIGHT+1) + (len-1)*_spacing);
  }
  
  private final class Mouser extends MouseInputAdapter {
    @Override
    public void mousePressed(MouseEvent e) {
      final PanelNotePair pair = findHit(e);
      if (pair != null) {
        pair.highlight();
        _pressed = pair;
        
        for (final MultipleKeyboardListener listener : _listeners)
          listener.keyPressed(pair.note, pair.panel);
        
        return;
      }
    }
    
    @Override
    public void mouseReleased(MouseEvent e) {
      if (_pressed != null)
        _pressed.unhighlight();
      if (_dragged != null)
        _dragged.unhighlight();
      
      final PanelNotePair released = findHit(e);
      if (released != null) {
        for (final MultipleKeyboardListener listener : _listeners)
          listener.keyReleased(released.note, released.panel);
        
        if (_pressed != null) {
          if (_pressed.equals(released))
            for (final MultipleKeyboardListener listener : _listeners)
              listener.keyClicked(released.note, released.panel);
          else
            for (final MultipleKeyboardListener listener : _listeners)
              listener.keyDragged(_pressed.note, _pressed.panel, released.note, released.panel);
        }
      }
      
      _pressed = null;
      _dragged = null;
    }
    
    @Override
    public void mouseDragged(MouseEvent e) {
      final PanelNotePair hit = findHit(e);
      if (hit == null)
        return;
      
      if (_pressed == null || !hit.equals(_dragged)) {
        if (_dragged != null && (_pressed == null || !_pressed.equals(_dragged)))
          _dragged.unhighlight();
        hit.highlight();
      }
      
      _dragged = hit;
    }
    
    @Override
    public void mouseMoved(MouseEvent e) {
      final PanelNotePair hit = findHit(e);
      setToolTipText(hit == null ? null : hit.note.toString());
    }
  }
  
  private static final class PanelNotePair {
    final KeyboardPanel panel;
    final Note note;
    public PanelNotePair(KeyboardPanel panel, Note note) {
      this.panel = panel;
      this.note = note;
    }
    
    public void highlight() {
      panel.highlightNote(note, KeyboardPanel.HIGHLIGHT_COLOR);
    }
    
    public void unhighlight() {
      panel.unhighlightNote(note);
    }
    
    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof PanelNotePair))
        return false;
      final PanelNotePair p = (PanelNotePair) obj;
      return panel == p.panel && note.equals(p.note);
    }
    
    @Override
    public int hashCode() {
      return 31*panel.hashCode() + note.hashCode();
    }
  }
  
  private PanelNotePair findHit(MouseEvent e) {
    for (final KeyboardPanel panel : _keyboards) {
      final Point actual = e.getLocationOnScreen();
      final Point pos = panel.getLocationOnScreen();
      actual.translate(-pos.x, -pos.y);
      final Note hit = panel.getNoteAt(actual);
      if (hit != null)
        return new PanelNotePair(panel, hit);
    }
    return null;
  }
  
  @Override
  public Dimension getSize() {
    return _size;
  }
  
  @Override
  public Dimension getMinimumSize() {
    return _size;
  }
  
  @Override
  public Dimension getMaximumSize() {
    return _size;
  }
  
  @Override
  public Dimension getPreferredSize() {
    return _size;
  }
  
}

