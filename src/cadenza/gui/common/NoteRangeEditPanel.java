package cadenza.gui.common;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import cadenza.core.Keyboard;
import cadenza.core.NoteRange;
import cadenza.core.Note;
import cadenza.gui.keyboard.KeyboardAdapter;
import cadenza.gui.keyboard.SingleKeyboardPanel;

import common.swing.SwingUtils;

@SuppressWarnings("serial")
public class NoteRangeEditPanel extends JPanel {
  private final List<Keyboard> _keyboards;
  private final List<SingleKeyboardPanel> _keyboardPanels;
  private final JPanel _rangePanel;
  
  private final KeyboardSelector _keyboardSelector;
  private final JButton _resetButton;
  
  private final List<NoteRangeListener> _listeners = new LinkedList<>();
  
  private NoteRange _selectedNoteRange;
  
  public NoteRangeEditPanel(List<Keyboard> keyboards, NoteRange initialNoteRange, boolean soundingOnly) {
    _keyboards = keyboards;
    _keyboardPanels = new ArrayList<>();
    
    final List<JComponent> components = new LinkedList<>();
    final Updater updater = new Updater();
    
    if (_keyboards == null) {
      _keyboardSelector = null;
      final SingleKeyboardPanel skp = new SingleKeyboardPanel(Note.MIN, Note.MAX);
      skp.addKeyboardListener(updater);
      components.add(skp);
      _keyboardPanels.add(skp);
    } else if (_keyboards.size() == 1) {
      _keyboardSelector = null;
      final Keyboard kbd = _keyboards.get(0);
      final SingleKeyboardPanel skp;
      if (soundingOnly)
        skp = new SingleKeyboardPanel(kbd.soundingLow, kbd.soundingHigh);
      else
        skp = new SingleKeyboardPanel(kbd.low, kbd.high);
      
      skp.addKeyboardListener(updater);
      components.add(new JLabel("Keyboard: " + kbd.name));
      components.add(skp);
      _keyboardPanels.add(skp);
    } else {
      _keyboardSelector = new KeyboardSelector(_keyboards);
      final JPanel keyboardPanel = new JPanel();
      int maxWidth = 0;
      for (final Keyboard keyboard : _keyboards) {
        final SingleKeyboardPanel skp;
        if (soundingOnly)
          skp = new SingleKeyboardPanel(keyboard.soundingLow, keyboard.soundingHigh);
        else
          skp = new SingleKeyboardPanel(keyboard.low, keyboard.high);
        
        maxWidth = Math.max(maxWidth, skp.getSize().width);
        skp.addKeyboardListener(updater);
        _keyboardPanels.add(skp);
      }
      _keyboardSelector.addActionListener(e -> {
        keyboardPanel.removeAll();
        keyboardPanel.add(_keyboardPanels.get(_keyboardSelector.getSelectedIndex()));
        keyboardPanel.revalidate();
        updateRangeDisplay();
        repaint();
      });
      keyboardPanel.add(_keyboardPanels.get(_keyboardSelector.getSelectedIndex()));
      keyboardPanel.setMinimumSize(new Dimension(maxWidth, keyboardPanel.getMinimumSize().height));
      
      components.add(_keyboardSelector);
      components.add(keyboardPanel);
    }
    
    _rangePanel = new JPanel(null);
    setSelectedNoteRange(initialNoteRange == null ? new NoteRange(getSelectedKeyboard(), false) : initialNoteRange);
    
    _resetButton = SwingUtils.button("Use Entire Keyboard", e -> setSelectedNoteRange(new NoteRange(getSelectedKeyboard(), true)));
    
    components.add(_rangePanel);
    components.add(_resetButton);
    
    final GroupLayout layout = new GroupLayout(this);
    setLayout(layout);
    
    final GroupLayout.ParallelGroup pg = layout.createParallelGroup(Alignment.CENTER);
    final GroupLayout.SequentialGroup sg = layout.createSequentialGroup();
    for (final JComponent component : components) {
      pg.addComponent(component);
      sg.addComponent(component);
    }
    
    layout.setVerticalGroup(sg);
    layout.setHorizontalGroup(pg);
    
    SwingUtilities.invokeLater(() -> updateRangeDisplay());
  }
  
  public void setSelectedNoteRange(NoteRange noteRange) {
    _selectedNoteRange = noteRange;
    if (_keyboardSelector != null)
      _keyboardSelector.setSelectedItem(noteRange.getKeyboard());
    
    updateRangeDisplay();
    
    _listeners.forEach(l -> l.noteRangeChanged(_selectedNoteRange));
  }
  
  public NoteRange getSelectedNoteRange() {
    return _selectedNoteRange;
  }
  
  public void addNoteRangeListener(NoteRangeListener listener) {
    _listeners.add(listener);
  }
  
  public void removeNoteRangeListener(NoteRangeListener listener) {
    _listeners.remove(listener);
  }
  
  public void highlightKey(Keyboard keyboard, int noteNumber) {
    if (_keyboardSelector != null)
      _keyboardSelector.setSelectedItem(keyboard);
    _keyboardPanels.get(_keyboards.indexOf(keyboard)).accessKeyboardPanel().highlightNote(Note.valueOf(noteNumber));
  }
  
  public void unhighlightKey(Keyboard keyboard, int noteNumber) {
    _keyboardPanels.get(_keyboards.indexOf(keyboard)).accessKeyboardPanel().unhighlightNote(Note.valueOf(noteNumber));
  }
  
  private Keyboard getSelectedKeyboard() {
    if (_keyboards == null || _keyboards.isEmpty())
      return null;
    else if (_keyboards.size() == 1)
      return _keyboards.get(0);
    else
      return _keyboardSelector.getSelectedKeyboard();
  }
  
  private void updateRangeDisplay() {
    final SingleKeyboardPanel skp;
    if (_keyboards == null || _keyboards.size() == 1) {
      skp = _keyboardPanels.get(0);
    } else {
      skp = _keyboardPanels.get(_keyboards.indexOf(_selectedNoteRange.getKeyboard()));
    }
    
    SwingUtils.freezeSize(_rangePanel, skp.getWidth(), 26);
    _rangePanel.removeAll();
    
    final JPanel range = new JPanel(null);
    final int x = skp.accessKeyboardPanel().getKeyPosition(_selectedNoteRange.getLower()).x;
    final Rectangle r = skp.accessKeyboardPanel().getKeyPosition(_selectedNoteRange.getUpper());
    final int width = r.x+r.width-x;
    range.setBounds(x, 1, width, 24);
    range.setBackground(Color.WHITE);
    range.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    
    final JLabel label = new JLabel(_selectedNoteRange.toString(false, true), JLabel.CENTER);
    label.setBounds(0, 0, width, 24);
    range.add(label);
    
    _rangePanel.add(range);
    _rangePanel.revalidate();
    _rangePanel.repaint();
  }
  
  private class Updater extends KeyboardAdapter {
    @Override
    public void keyClicked(Note note) {
      setSelectedNoteRange(new NoteRange(getSelectedKeyboard(), note));
    }
    
    @Override
    public void keyDragged(Note startNote, Note endNote) {
      final Note high, low;
      if (startNote.compareTo(endNote) < 0) {
        low = startNote;
        high = endNote;
      } else {
        low = endNote;
        high = startNote;
      }
      
      setSelectedNoteRange(new NoteRange(getSelectedKeyboard(), low, high));
    }
  }
}
