package cadenza.gui.patchusage.merge;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import cadenza.core.Note;
import cadenza.core.NoteRange;
import cadenza.gui.keyboard.KeyboardAdapter;
import cadenza.gui.keyboard.SingleKeyboardPanel;

import common.swing.SwingUtils;

@SuppressWarnings("serial")
public class MergeRangePanel extends JPanel {
  private final SingleKeyboardPanel _keyboardPanel;
  private final JPanel _rangePanel;
  
  private NoteRange _noteRange;
  
  public MergeRangePanel(NoteRange initial) {
    _noteRange = Objects.requireNonNull(initial);
    
    _keyboardPanel = new SingleKeyboardPanel(initial.getKeyboard().soundingLow, initial.getKeyboard().soundingHigh);
    _keyboardPanel.addKeyboardListener(new KeyboardAdapter() {
      @Override
      public void keyDragged(Note startNote, Note endNote) {
        final Note lower = Note.min(startNote, endNote);
        final Note upper = Note.max(startNote, endNote);
        _noteRange = new NoteRange(_noteRange.getKeyboard(), lower, upper);
        update();
      }
    });
    
    _rangePanel = new JPanel(null);
    SwingUtils.freezeHeight(_rangePanel, 26);
    
    final JLabel label = new JLabel("Drag to define merged range:");
    label.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
    
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    add(SwingUtils.buildCenteredRow(label));
    add(SwingUtils.buildCenteredRow(_keyboardPanel));
    add(SwingUtils.buildCenteredRow(_rangePanel));
    
    // put this on the end of the event queue so that we wait for some other
    // initialization to finish first
    SwingUtilities.invokeLater(() -> {
      SwingUtils.freezeSize(_rangePanel, _keyboardPanel.getWidth(), 26);
      update();
    });
  }
  
  public void setNoteRange(NoteRange range) {
    _noteRange = range;
    update();
  }
  
  public NoteRange getNoteRange() {
    return _noteRange;
  }
  
  private void update() {
    SwingUtils.throwIfNotEventThread();
    
    _rangePanel.removeAll();
    
    final Rectangle r1 = _keyboardPanel.accessKeyboardPanel().getKeyPosition(_noteRange.getLower());
    final Rectangle r2 = _keyboardPanel.accessKeyboardPanel().getKeyPosition(_noteRange.getUpper());
    final int width = r2.x + r2.width - r1.x;
    
    final JPanel panel = new JPanel(null);
    panel.setBounds(r1.x, 1, width, 24);
    panel.setBackground(Color.WHITE);
    panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    
    final JLabel label = new JLabel("Range", JLabel.CENTER);
    label.setBounds(0, 0, width, 24);
    panel.add(label);
    
    _rangePanel.add(panel);
    _rangePanel.revalidate();
    _rangePanel.repaint();
  }
}
