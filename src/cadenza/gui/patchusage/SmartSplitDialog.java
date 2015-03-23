package cadenza.gui.patchusage;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import cadenza.core.Keyboard;
import cadenza.core.Location;
import cadenza.core.Note;
import cadenza.core.patchusage.PatchUsage;
import cadenza.gui.keyboard.KeyboardAdapter;
import cadenza.gui.keyboard.SingleKeyboardPanel;

import common.swing.SwingUtils;
import common.swing.VerificationException;
import common.swing.dialog.OKCancelDialog;

@SuppressWarnings("serial")
public class SmartSplitDialog extends OKCancelDialog {
  private final PatchUsage _patchUsage;
  private final List<PatchUsage> _others;
  
  private JComboBox<PatchUsage> _patchUsageCombo;
  private JCheckBox _aboveBox;
  private SingleKeyboardPanel _keyboardPanel;
  private JPanel _rangePanel;
  
  private PatchUsage _other;
  private Location _union;
  private Note _currentSplit;

  public SmartSplitDialog(Component parent, PatchUsage patchUsage, List<PatchUsage> others) {
    super(parent);
    _patchUsage = patchUsage;
    _others = others;
  }

  @Override
  protected JComponent buildContent() {
    _patchUsageCombo = new JComboBox<>(_others.toArray(new PatchUsage[_others.size()]));
    if (_patchUsage.isSplit())
      _patchUsageCombo.setSelectedItem(_patchUsage.splitTwin);
    _patchUsageCombo.addActionListener(e -> notifyChange());
    
    final Keyboard kbd = _patchUsage.location.getKeyboard();
    
    _aboveBox = new JCheckBox("Make this the top patch");
    _aboveBox.addActionListener(e -> update());
    
    _keyboardPanel = new SingleKeyboardPanel(kbd.low, kbd.high);
    _keyboardPanel.addKeyboardListener(new KeyboardAdapter() {
      @Override
      public void keyClicked(Note note) {
        if (_union.contains(note.getMidiNumber())) {
          _currentSplit = note;
          update();
        }
      };
      
      @Override
      public void keyDragged(Note startNote, Note endNote) {
        final Note lower = Note.min(startNote, endNote);
        final Note upper = Note.max(startNote, endNote);
        _union = new Location(_patchUsage.location.getKeyboard(), lower, upper);
        _currentSplit = Note.valueOf((lower.getMidiNumber() + upper.getMidiNumber()) / 2);
        update();
      };
    });
    
    _rangePanel = new JPanel(null);
    SwingUtils.freezeHeight(_rangePanel, 26); // placeholder so dialog allocates enough height
    
    Box box = Box.createVerticalBox();
    box.add(SwingUtils.buildCenteredRow(new JLabel("Merge with:"), _patchUsageCombo));
    box.add(SwingUtils.buildCenteredRow(_aboveBox));
    box.add(SwingUtils.buildCenteredRow(new JLabel("Click to select initial split point, drag to define total range:")));
    box.add(SwingUtils.buildCenteredRow(_keyboardPanel));
    box.add(SwingUtils.buildCenteredRow(_rangePanel));
    
    SwingUtilities.invokeLater(() -> {
      SwingUtils.freezeSize(_rangePanel, _keyboardPanel.getWidth(), 26);
      
      if (_patchUsage.isSplit()) {
        // editing
        _aboveBox.setSelected(_patchUsage.splitAbove);
        _other = _patchUsage.splitTwin;
        _union = _patchUsage.location;
        _currentSplit = Note.valueOf(_patchUsage.startSplit);
        
        update();
      } else {
        // new
        notifyChange();
      }
    });
    
    return box;
  }
  
  private PatchUsage getSelectedItem() {
    return (PatchUsage) _patchUsageCombo.getSelectedItem();
  }

  @Override
  protected String declareTitle() {
    return _patchUsage.isSplit() ? "Edit merged patch usage"
                                 : "Create merged patch usage";
  }

  @Override
  protected void verify() throws VerificationException {
    // no-op
  }
  
  @Override
  protected void takeActionOnOK() {
    if (_patchUsage.isSplit())
      _patchUsage.unsplit();
    
    _patchUsage.location = _union;
    _patchUsage.splitWith(_other, _aboveBox.isSelected(), _currentSplit.getMidiNumber());
  }
  
  private void notifyChange() {
    _other = getSelectedItem();
    _union = Location.union(_patchUsage.location, _other.location);
    _currentSplit = Note.valueOf((_union.getLower().getMidiNumber() + _union.getUpper().getMidiNumber()) / 2);
    update();
  }
  
  private void update() {
    SwingUtils.throwIfNotEventThread();
    
    _rangePanel.removeAll();
    
    final Rectangle r = _keyboardPanel.accessKeyboardPanel().getKeyPosition(_currentSplit);
    
    final JPanel lowerPanel = new JPanel(null);
    int x = _keyboardPanel.accessKeyboardPanel().getKeyPosition(_union.getLower()).x;
    int width = r.x - x;
    
    PatchUsage pu = _aboveBox.isSelected() ? _other : _patchUsage;
    lowerPanel.setBounds(x, 1, width, 24);
    lowerPanel.setBackground(pu.patch.getDisplayColor());
    lowerPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    
    final JLabel lowerLabel = new JLabel(pu.toString(false), JLabel.CENTER);
    lowerLabel.setForeground(pu.patch.getTextColor());
    lowerLabel.setBounds(0, 0, width, 24);
    lowerPanel.add(lowerLabel);
    
    final JPanel upperPanel = new JPanel(null);
    x = r.x;
    final int extent = _keyboardPanel.accessKeyboardPanel().getKeyPosition(_union.getUpper()).x;
    width = extent - x + (int) r.getWidth();
    
    pu =_aboveBox.isSelected() ? _patchUsage : _other;
    upperPanel.setBounds(x, 1, width, 24);
    upperPanel.setBackground(pu.patch.getDisplayColor());
    upperPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    
    final JLabel upperLabel = new JLabel(pu.toString(false), JLabel.CENTER);
    upperLabel.setForeground(pu.patch.getTextColor());
    upperLabel.setBounds(0, 0, width, 24);
    upperPanel.add(upperLabel);
    
    _rangePanel.add(lowerPanel);
    _rangePanel.add(upperPanel);
    _rangePanel.revalidate();
    _rangePanel.repaint();
  }
}
