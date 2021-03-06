package cadenza.gui.keyboard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Rectangle;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import cadenza.core.Keyboard;
import cadenza.core.NoteRange;
import cadenza.core.Note;

import common.swing.IntField;
import common.swing.SwingUtils;
import common.swing.VerificationException;

@SuppressWarnings("serial")
public class KeyboardEditPanel extends JPanel {
  private final Keyboard _keyboard;
  
  private NoteRange _selectedFullRange;
  private NoteRange _selectedSoundingRange;
  
  private final JTextField _nameField;
  private final IntField _channelField;
  private final JComboBox<String> _combo;
  private final SingleKeyboardPanel _keyboardPanel;
  
  private JPanel _rangeArea;
  
  public KeyboardEditPanel(Keyboard keyboard) {
    super();
    
    _keyboard = keyboard;
    _selectedFullRange = new NoteRange(keyboard, keyboard.low, keyboard.high);
    _selectedSoundingRange = new NoteRange(keyboard, keyboard.soundingLow, keyboard.soundingHigh);
    
    _nameField = new JTextField(keyboard.name);
    _channelField = new IntField(keyboard.channel, 0, Integer.MAX_VALUE);
    SwingUtils.freezeWidth(_channelField, 50);
    
    _combo = new JComboBox<>(new String[] { "Specify Range", "Specify Sounding Range" });
    _keyboardPanel = new SingleKeyboardPanel(Note.MIN, Note.MAX);
    
    
    init();
  }
  
  private void init() {
    _rangeArea = new JPanel();
    _rangeArea.setLayout(null);
    SwingUtils.freezeSize(_rangeArea, _keyboardPanel.getSize().width, 50);
    
    _keyboardPanel.addKeyboardListener(new KeyboardAdapter() {
      @Override
      public void keyDragged(Note startNote, Note endNote) {
        applyNoteRange(new NoteRange(_keyboard, startNote, endNote));
      }
    });
    rebuildLabels();
    
    final Box content = Box.createVerticalBox();
    content.add(SwingUtils.buildRow(new JLabel("Name: "), _nameField, new JLabel("  Input channel: "), _channelField));
    content.add(_combo);
    content.add(_keyboardPanel);
    content.add(_rangeArea);
    
    setLayout(new BorderLayout());
    add(content, BorderLayout.CENTER);
  }
  
  public void applyNoteRange(NoteRange noteRange) {
    SwingUtils.throwIfNotEventThread();
    
    if (_combo.getSelectedIndex() == 0) {
      _selectedFullRange = noteRange;
      _combo.setSelectedIndex(1);
      rebuildLabels();
    } else {
      _selectedSoundingRange = noteRange;
      _combo.setSelectedIndex(0);
      rebuildLabels();
    }
  }
  
  KeyboardPanel accessKeyboardPanel() {
    return _keyboardPanel.accessKeyboardPanel();
  }
  
  private void rebuildLabels() {
    _rangeArea.removeAll();
    Rectangle r1 = _keyboardPanel.accessKeyboardPanel().getKeyPosition(_selectedFullRange.getLower());
    Rectangle r2 = _keyboardPanel.accessKeyboardPanel().getKeyPosition(_selectedFullRange.getUpper());
    int width = r2.x+r2.width-r1.x;
    final RangePanel fullPanel = new RangePanel("Physical Range", width);
    fullPanel.setBounds(r1.x, 1, width, 24);
    r1 = _keyboardPanel.accessKeyboardPanel().getKeyPosition(_selectedSoundingRange.getLower());
    r2 = _keyboardPanel.accessKeyboardPanel().getKeyPosition(_selectedSoundingRange.getUpper());
    width = r2.x+r2.width-r1.x;
    final RangePanel soundingPanel = new RangePanel("Sounding Range", width);
    soundingPanel.setBounds(r1.x, 26, width, 24);
    
    _rangeArea.add(fullPanel);
    _rangeArea.add(soundingPanel);
    
    final KeyboardPanel kp = _keyboardPanel.accessKeyboardPanel();
    kp.unlabelAll();
    kp.labelNote(Note.A0);
    kp.labelNote(Note.C4);
    kp.labelNote(Note.C8);
    kp.labelNote(_selectedFullRange.getLower());
    kp.labelNote(_selectedFullRange.getUpper());
    kp.labelNote(_selectedSoundingRange.getLower());
    kp.labelNote(_selectedSoundingRange.getUpper());
    
    revalidate();
    repaint();
  }
  
  public void verify() throws VerificationException {
    if (_nameField.getText().trim().isEmpty())
      throw new VerificationException("Please specify a name", _nameField);
  }
  
  public void verify(List<Keyboard> others) throws VerificationException {
    verify();
    
    for (final Keyboard keyboard : others) {
      if (keyboard != _keyboard) {
        if (getEnteredName().equals(keyboard.name))
          throw new VerificationException("A keyboard with this name already exists", _nameField);
        if (_channelField.getInt() == keyboard.channel)
          throw new VerificationException("Another keyboard has been assigned to this channel", _channelField);
      }
    }
  }
  
  private String getEnteredName() {
    return _nameField.getText().trim();
  }
  
  public Keyboard buildKeyboard() {
    return new Keyboard(_selectedFullRange.getLower(), _selectedFullRange.getUpper(),
        _selectedSoundingRange.getLower(), _selectedSoundingRange.getUpper(),
        getEnteredName(), _keyboard.isMain, _channelField.getInt());
  }
  
  private class RangePanel extends JPanel {
    public RangePanel(String text, int width) {
      super(null);
      setBackground(Color.WHITE);
      setBorder(BorderFactory.createLineBorder(Color.BLACK));
      final JLabel label = new JLabel(text, JLabel.CENTER);
      label.setBounds(0, 0, width, 24);
      add(label);
    }
  }
  
  public void match(Keyboard keyboard) {
    SwingUtils.throwIfNotEventThread();
    
    _nameField.setText(keyboard.name);
    _channelField.setInt(keyboard.channel);
    _selectedFullRange = new NoteRange(_keyboard, keyboard.low, keyboard.high);
    _selectedSoundingRange = new NoteRange(_keyboard, keyboard.soundingLow, keyboard.soundingHigh);
    rebuildLabels();
  }
}
