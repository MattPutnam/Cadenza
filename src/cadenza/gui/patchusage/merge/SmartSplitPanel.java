package cadenza.gui.patchusage.merge;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import cadenza.core.Keyboard;
import cadenza.core.Location;
import cadenza.core.Note;
import cadenza.core.patchmerge.SplitPatchMerge;
import cadenza.core.patchusage.PatchUsage;
import cadenza.gui.common.HelpButton;
import cadenza.gui.keyboard.KeyboardAdapter;
import cadenza.gui.keyboard.SingleKeyboardPanel;
import common.Utils;
import common.swing.IntField;
import common.swing.SwingUtils;

@SuppressWarnings("serial")
public class SmartSplitPanel extends MergePanel<SplitPatchMerge> {
  private static final String BUFFER_HELP_TEXT = Utils.renderForSwingHTML(
      "The size of the buffer used to group key presses on either side of the split.\n\n"
      + "Smaller values track your notes more closely, larger values cause the split\n"
      + "point to change more gradually."
      );
  
  private JComboBox<PatchUsage> _patchUsageCombo;
  private JCheckBox _aboveBox;
  private IntField _bufferField;
  private SingleKeyboardPanel _keyboardPanel;
  private JPanel _rangePanel;
  
  private PatchUsage _other;
  private Location _union;
  private Note _currentSplit;
  
  public SmartSplitPanel(PatchUsage primary, List<PatchUsage> others) {
    super(primary, others);
    
    _patchUsageCombo = new JComboBox<>(others.toArray(new PatchUsage[others.size()]));
    _patchUsageCombo.addActionListener(e -> notifyChange());
    
    final Keyboard kbd = primary.location.getKeyboard();
    
    _aboveBox = new JCheckBox("Make this the top patch");
    _aboveBox.addActionListener(e -> update());
    
    _bufferField = new IntField(SplitPatchMerge.DEFAULT_BUFFER_SIZE, 1, Integer.MAX_VALUE);
    _bufferField.setColumns(6);
    SwingUtils.freezeSize(_bufferField);
    
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
        _union = new Location(kbd, lower, upper);
        _currentSplit = Note.valueOf((lower.getMidiNumber() + upper.getMidiNumber()) / 2);
        update();
      };
    });
    
    _rangePanel = new JPanel(null);
    SwingUtils.freezeHeight(_rangePanel, 26); // placeholder so dialog allocates enough height
    
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    add(SwingUtils.buildCenteredRow(new JLabel("Merge with:"), _patchUsageCombo));
    add(SwingUtils.buildCenteredRow(_aboveBox));
    add(SwingUtils.buildCenteredRow(new JLabel("Buffer size:"), _bufferField, new HelpButton(BUFFER_HELP_TEXT)));
    add(SwingUtils.buildCenteredRow(new JLabel("Click to select initial split point, drag to define total range:")));
    add(SwingUtils.buildCenteredRow(_keyboardPanel));
    add(SwingUtils.buildCenteredRow(_rangePanel));
    add(Box.createVerticalGlue());
    
    // put this on the end of the event queue so that we wait for some other
    // initialization to finish first
    SwingUtilities.invokeLater(() -> {
      SwingUtils.freezeSize(_rangePanel, _keyboardPanel.getWidth(), 26);
      notifyChange();
    });
  }

  @Override
  public void initialize(SplitPatchMerge initial) {
    if (initial.getLower() == accessPrimary()) {
      _aboveBox.setSelected(false);
      _patchUsageCombo.setSelectedItem(initial.getUpper());
    } else {
      _aboveBox.setSelected(true);
      _patchUsageCombo.setSelectedItem(initial.getLower());
    }
    
    // put this on the end of the event queue so that some other initialization
    // that would blast this happens first
    SwingUtilities.invokeLater(() -> {
      _union = initial.accessLocation();
      _currentSplit = Note.valueOf(initial.getStartSplit());
      _bufferField.setInt(initial.getBufferSize());
      
      update();
    });
  }

  @Override
  public SplitPatchMerge getPatchMerge() {
    final boolean above = _aboveBox.isSelected();
    final PatchUsage lower = above ? _other : accessPrimary();
    final PatchUsage upper = above ? accessPrimary() : _other;
    return new SplitPatchMerge(lower, upper, _currentSplit.getMidiNumber(), _bufferField.getInt());
  }
  
  private PatchUsage getSelectedItem() {
    return (PatchUsage) _patchUsageCombo.getSelectedItem();
  }
  
  private void notifyChange() {
    _other = getSelectedItem();
    _union = Location.union(accessPrimary().location, _other.location);
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
    
    PatchUsage pu = _aboveBox.isSelected() ? _other : accessPrimary();
    lowerPanel.setBounds(x, 1, width, 24);
    lowerPanel.setBackground(pu.patch.getDisplayColor());
    lowerPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    
    final JLabel lowerLabel = new JLabel("<html>" + pu.toString(false, false, true) + "</html>", JLabel.CENTER);
    lowerLabel.setForeground(pu.patch.getTextColor());
    lowerLabel.setBounds(0, 0, width, 24);
    lowerPanel.add(lowerLabel);
    
    final JPanel upperPanel = new JPanel(null);
    x = r.x;
    final int extent = _keyboardPanel.accessKeyboardPanel().getKeyPosition(_union.getUpper()).x;
    width = extent - x + (int) r.getWidth();
    
    pu = _aboveBox.isSelected() ? accessPrimary() : _other;
    upperPanel.setBounds(x, 1, width, 24);
    upperPanel.setBackground(pu.patch.getDisplayColor());
    upperPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    
    final JLabel upperLabel = new JLabel("<html>" + pu.toString(false, false, true) + "</html>", JLabel.CENTER);
    upperLabel.setForeground(pu.patch.getTextColor());
    upperLabel.setBounds(0, 0, width, 24);
    upperPanel.add(upperLabel);
    
    _rangePanel.add(lowerPanel);
    _rangePanel.add(upperPanel);
    _rangePanel.revalidate();
    _rangePanel.repaint();
  }

}
