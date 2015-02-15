package cadenza.gui.keyboard;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;

import cadenza.core.Keyboard;
import cadenza.core.Note;


/**
 * Class that displays a single keyboard and provides utilities for clicking
 * and dragging on it.  Also provides tooltips for the hovered-over key.
 * 
 * @author Matt Putnam
 */
public final class SingleKeyboardPanel extends JPanel {
  private static final long serialVersionUID = 1L;

  private final KeyboardPanel _panel;
  
  private transient Note _pressedKey;
  private transient Note _draggedKey;
  
  private transient List<KeyboardListener> _listeners;
  
  public SingleKeyboardPanel(Keyboard keyboard) {
    this(keyboard.low, keyboard.high);
  }
  
  /**
   * Creates a new SingleKeyboardPanel with a keyboard with the specified range
   * @param low - the low note of the range
   * @param high - the high note of the range
   */
  public SingleKeyboardPanel(Note low, Note high) {
    super();
    
    setLayout(new BorderLayout());
    _panel = new KeyboardPanel(low, high);
    add(_panel, BorderLayout.CENTER);
    
    _listeners = new LinkedList<>();
    
    final Mouser mouser = new Mouser();
    addMouseListener(mouser);
    addMouseMotionListener(mouser);
  }
  
  public KeyboardPanel accessKeyboardPanel() {
    return _panel;
  }
  
  /**
   * Registers a KeyboardListener to this component
   * @param listener - the KeyboardListener to register
   */
  public void addKeyboardListener(KeyboardListener listener) {
    _listeners.add(listener);
  }
  
  private class Mouser extends MouseInputAdapter {
    @Override
    public void mousePressed(MouseEvent e) {
      _pressedKey = _panel.getNoteAt(e.getPoint());
      if (_pressedKey != null) {
        _panel.highlightNote(_pressedKey, KeyboardPanel.HIGHLIGHT_COLOR);
        
        for (final KeyboardListener listener : _listeners)
          listener.keyPressed(_pressedKey);
      }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
      final Note released = _panel.getNoteAt(e.getPoint());
      if (_pressedKey != null)
        _panel.unhighlightNote(_pressedKey);
      if (_draggedKey != null)
        _panel.unhighlightNote(_draggedKey);
      
      if (released != null) {
        for (final KeyboardListener listener : _listeners)
          listener.keyReleased(released);

        if (_pressedKey != null) {
          if (_pressedKey.equals(released))
            for (final KeyboardListener listener : _listeners)
              listener.keyClicked(released);
          else
            for (final KeyboardListener listener : _listeners)
              listener.keyDragged(_pressedKey, released);
        }
      }
      
      _pressedKey = null;
      _draggedKey = null;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
      final Note temp = _panel.getNoteAt(e.getPoint());
      if (temp == null)
        return;

      if (_pressedKey == null || _draggedKey == null || !temp.equals(_draggedKey)) {
        if (!eq(_pressedKey, _draggedKey))
          _panel.unhighlightNote(_draggedKey);
        _panel.highlightNote(temp, KeyboardPanel.HIGHLIGHT_COLOR);
      }

      _draggedKey = temp;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
      final Note note = _panel.getNoteAt(e.getPoint());
      setToolTipText(note == null ? null : note.toString());
    }
  }
  
  private static boolean eq(Note n1, Note n2) {
    if (n1 == null && n2 == null)
      return true;
    else if (n1 == null || n2 == null)
      return false;
    else
      return n1.equals(n2);
  }
  
  @Override
  public Dimension getSize() {
    return _panel.getSize();
  }

  @Override
  public Dimension getMinimumSize() {
    return _panel.getMinimumSize();
  }

  @Override
  public Dimension getMaximumSize() {
    return _panel.getMaximumSize();
  }
  
  @Override
  public Dimension getPreferredSize() {
  return _panel.getPreferredSize();
  }
}

