package cadenza.gui.trigger;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import cadenza.control.midiinput.AcceptsKeyboardInput;
import cadenza.control.midiinput.LocationEntryTracker;
import cadenza.control.midiinput.MIDIInputControlCenter;
import cadenza.core.Keyboard;
import cadenza.core.Location;
import cadenza.core.Note;
import cadenza.core.trigger.predicates.ChordPredicate;
import cadenza.core.trigger.predicates.ControlValuePredicate;
import cadenza.core.trigger.predicates.NoteOffPredicate;
import cadenza.core.trigger.predicates.NoteOnPredicate;
import cadenza.core.trigger.predicates.TriggerPredicate;
import cadenza.gui.common.ControlCombo;
import cadenza.gui.common.KeyboardSelector;
import cadenza.gui.common.LocationEditPanel;
import cadenza.gui.keyboard.KeyboardPanel;
import cadenza.preferences.Preferences;
import common.swing.IntField;
import common.swing.SimpleGrid;
import common.swing.SwingUtils;
import common.swing.VerificationException;
import common.swing.dialog.OKCancelDialog;
import common.tuple.Pair;

@SuppressWarnings("serial")
public class TriggerPredicateEditDialog extends OKCancelDialog implements AcceptsKeyboardInput {
  private final List<Keyboard> _keyboards;
  private final TriggerPredicate _initial;
  
  private JTabbedPane _tabbedPane;
  private NoteOnPredicatePane _noteOnPane;
  private NoteOffPredicatePane _noteOffPane;
  private ChordPredicatePane _chordPane;
  private ControlValuePredicatePane _controlPane;
  
  private KeyPressTracker _keyPressTracker;
  
  public TriggerPredicateEditDialog(Component parent, List<Keyboard> keyboards, TriggerPredicate initial) {
    super(parent);
    _keyboards = keyboards;
    _initial = initial;
    
    if (Preferences.getMIDIInputOptions().allowMIDIInput()) {
      MIDIInputControlCenter.installWindowFocusGrabber(this);
      _keyPressTracker = new KeyPressTracker(keyboards);
    }
  }

  @Override
  protected JComponent buildContent() {
    _tabbedPane = new JTabbedPane();
    _noteOnPane = new NoteOnPredicatePane();
    _noteOffPane = new NoteOffPredicatePane();
    _chordPane = new ChordPredicatePane();
    _controlPane = new ControlValuePredicatePane();
    
    _tabbedPane.addTab("Note On", _noteOnPane);
    _tabbedPane.addTab("Note Off", _noteOffPane);
    _tabbedPane.addTab("Chord Played", _chordPane);
    _tabbedPane.addTab("Control Value", _controlPane);
    
    return _tabbedPane;
  }
  
  @Override
  protected void initialize() {
    if (_initial == null)
      return;
    
    if (_initial instanceof NoteOnPredicate) {
      _tabbedPane.setSelectedComponent(_noteOnPane);
      _noteOnPane.initialize((NoteOnPredicate) _initial);
    } else if (_initial instanceof NoteOffPredicate) {
      _tabbedPane.setSelectedComponent(_noteOffPane);
      _noteOffPane.initialize((NoteOffPredicate) _initial);
    } else if (_initial instanceof ChordPredicate) {
      _tabbedPane.setSelectedComponent(_chordPane);
      _chordPane.initialize((ChordPredicate) _initial);
    } else if (_initial instanceof ControlValuePredicate) {
      _tabbedPane.setSelectedComponent(_controlPane);
      _controlPane.initialize((ControlValuePredicate) _initial);
    }
  }
  
  @Override
  public void keyPressed(int channel, int midiNumber, int velocity) {
    SwingUtilities.invokeLater(() -> {
      final Keyboard kbd = _keyPressTracker.keyPressed(channel, midiNumber);
      getCurrentTab().notifyKeyPress(kbd, midiNumber);
    });
  }
  
  @Override
  public void keyReleased(int channel, int midiNumber) {
    SwingUtilities.invokeLater(() -> {
      final Keyboard kbd = _keyPressTracker.keyReleased(channel, midiNumber);
      getCurrentTab().notifyKeyRelease(kbd, midiNumber);
    });
  }
  
  @Override
  public void controlReceived(int channel, int ccNumber, int value) {
    SwingUtilities.invokeLater(() -> {
      final Component selected = _tabbedPane.getSelectedComponent();
      if (selected instanceof ControlValuePredicatePane) {
        ((ControlValuePredicatePane) selected).receive(channel, ccNumber, value);
      }
    });
  }
  
  public TriggerPredicate getPredicate() {
    return ((PredicatePane<?>) _tabbedPane.getSelectedComponent()).createPredicate();
  }
  
  private PredicatePane<?> getCurrentTab() {
    return (PredicatePane<?>) _tabbedPane.getSelectedComponent();
  }

  @Override
  protected String declareTitle() {
    return "Create Predicate";
  }

  @Override
  protected void verify() throws VerificationException {
    ((PredicatePane<?>) _tabbedPane.getSelectedComponent()).verify();
  }
  
  private class KeyPressTracker extends LocationEntryTracker {
    public KeyPressTracker(List<Keyboard> keyboards) {
      super(keyboards);
    }
    
    @Override
    protected void singlePressed(Keyboard keyboard, int noteNumber) {
      getCurrentTab().setLocation(Location.singleNote(keyboard, Note.valueOf(noteNumber)));
    }
    
    @Override
    protected void rangePressed(Keyboard keyboard, int lowNumber, int highNumber) {
      getCurrentTab().setLocation(Location.range(keyboard, Note.valueOf(lowNumber), Note.valueOf(highNumber)));
    }
    
    @Override
    protected void wholePressed(Keyboard keyboard) {
      getCurrentTab().setLocation(Location.wholeKeyboard(keyboard));
    }
  }
  
  private abstract class PredicatePane<T extends TriggerPredicate> extends JPanel {
    public abstract void initialize(T initial);
    
    /**
     * Verifies the Predicate UI
     * @throws VerificationException if a UI element has a bad value
     */
    public void verify() throws VerificationException {}
    
    public abstract T createPredicate();
    
    /**
     * Sets the selected location, if that makes sense for this predicate.
     * Default implementation does nothing.
     * @param location the location
     */
    public void setLocation(Location location) {}
    
    /**
     * Notification that a note has been pressed.  Default does nothing.
     * @param keyboard the keyboard
     * @param noteNumber the note number
     */
    public void notifyKeyPress(Keyboard keyboard, int noteNumber) {}
    
    /**
     * Notification that a note has been released.  Default does nothing.
     * @param keyboard the keyboard
     * @param noteNumber the note number
     */
    public void notifyKeyRelease(Keyboard keyboard, int noteNumber) {}
  }
  
  private class NoteOnPredicatePane extends PredicatePane<NoteOnPredicate> {
    private final LocationEditPanel _locationPanel;
    
    public NoteOnPredicatePane() {
      super();
      _locationPanel = new LocationEditPanel(_keyboards, null);
      add(_locationPanel);
    }
    
    @Override
    public void initialize(NoteOnPredicate initial) {
      _locationPanel.setSelectedLocation(initial.getLocation());
    }

    @Override
    public NoteOnPredicate createPredicate() {
      return new NoteOnPredicate(_locationPanel.getSelectedLocation());
    }
    
    @Override
    public void setLocation(Location location) {
      _locationPanel.setSelectedLocation(location);
    }
    
    @Override
    public void notifyKeyPress(Keyboard keyboard, int noteNumber) {
      _locationPanel.highlightKey(keyboard, noteNumber);
    }
    
    @Override
    public void notifyKeyRelease(Keyboard keyboard, int noteNumber) {
      _locationPanel.unhighlightKey(keyboard, noteNumber);
    }
  }
  
  private class NoteOffPredicatePane extends PredicatePane<NoteOffPredicate> {
    private final LocationEditPanel _locationPanel;
    
    public NoteOffPredicatePane() {
      super();
      _locationPanel = new LocationEditPanel(_keyboards, null);
      add(_locationPanel);
    }
    
    @Override
    public void initialize(NoteOffPredicate initial) {
      _locationPanel.setSelectedLocation(initial.getLocation());
    }

    @Override
    public NoteOffPredicate createPredicate() {
      return new NoteOffPredicate(_locationPanel.getSelectedLocation());
    }
    
    @Override
    public void setLocation(Location location) {
      _locationPanel.setSelectedLocation(location);
    }
    
    @Override
    public void notifyKeyPress(Keyboard keyboard, int noteNumber) {
      _locationPanel.highlightKey(keyboard, noteNumber);
    }
    
    @Override
    public void notifyKeyRelease(Keyboard keyboard, int noteNumber) {
      _locationPanel.unhighlightKey(keyboard, noteNumber);
    }
  }
  
  private class ChordPredicatePane extends PredicatePane<ChordPredicate> {
    final List<KeyboardPanel> _keyboardPanels;
    final List<Pair<Keyboard, Integer>> _notes;
    
    public ChordPredicatePane() {
      _keyboardPanels = new ArrayList<>(_keyboards.size());
      _notes = new ArrayList<>();
      
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      for (int i = 0; i < _keyboards.size(); ++i) {
        final Keyboard keyboard = _keyboards.get(i);
        final KeyboardPanel panel = new KeyboardPanel(keyboard);
        _keyboardPanels.add(panel);
        
        panel.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            final Pair<Keyboard, Integer> pair = Pair.make(
                keyboard, Integer.valueOf(panel.getNoteAt(e.getPoint()).getMidiNumber()));
            
            if (_notes.contains(pair))
              _notes.remove(pair);
            else
              _notes.add(pair);
            
            updateDisplay();
          }
        });
        
        add(new JLabel(keyboard.name));
        add(panel);
        add(Box.createVerticalStrut(8));
      }
    }
    
    @Override
    public void initialize(ChordPredicate initial) {
      _notes.addAll(initial.getNotes());
      
      updateDisplay();
    }
    
    @Override
    public void notifyKeyPress(Keyboard keyboard, int noteNumber) {
      final Optional<Pair<Keyboard, Integer>> opt = _notes.stream()
                                                          .filter(pair -> pair._1() == keyboard && pair._2().intValue() == noteNumber)
                                                          .findFirst();
      if (opt.isPresent()) {
        _notes.remove(opt.get());
      } else {
        _notes.add(Pair.make(keyboard, Integer.valueOf(noteNumber)));
      }
      updateDisplay();
    }
    
    private void updateDisplay() {
      for (final KeyboardPanel kp : _keyboardPanels)
        kp.unhighlightAll();
      
      for (final Pair<Keyboard, Integer> p : _notes)
        _keyboardPanels.get(_keyboards.indexOf(p._1())).
            highlightNote(Note.valueOf(p._2().intValue()), KeyboardPanel.HIGHLIGHT_COLOR);
    }
    
    @Override
    public ChordPredicate createPredicate() {
      return new ChordPredicate(_notes);
    }
  }
  
  private class ControlValuePredicatePane extends PredicatePane<ControlValuePredicate> {
    private final ControlCombo _controlCombo;
    private final IntField _valueField;
    private final KeyboardSelector _keyboardSelector;
    
    private int _lowCache;
    private int _highCache;
    
    public ControlValuePredicatePane() {
      super();
      
      _controlCombo = new ControlCombo(null);
      _valueField = new IntField(0, 0, 127);
      _valueField.setColumns(3);
      _keyboardSelector = new KeyboardSelector(_keyboards);
      
      add(new SimpleGrid(new JComponent[][] {
        { new JLabel("Control Change #"), _controlCombo },
        { new JLabel("With value"), _valueField },
        { new JLabel("From"), SwingUtils.hugWest(_keyboardSelector) }
      }, Alignment.CENTER, Alignment.LEADING));
    }

    @Override
    public void initialize(ControlValuePredicate initial) {
      _controlCombo.setSelectedIndex(initial.getControlNumber());
      _keyboardSelector.setSelectedKeyboard(initial.getKeyboard());
      
      final int low = initial.getLow();
      final int high = initial.getHigh();
      if (low == high) {
        _valueField.setText(String.valueOf(low));
      } else {
        _valueField.setText(low + "-" + high);
      }
    }
    
    public void receive(int channel, int ccNumber, int value) {
      _controlCombo.setSelectedIndex(ccNumber);
      _valueField.setInt(value);
      
      if (_keyboards.size() > 1) {
        _keyboards.stream()
                  .filter(kbd -> kbd.channel == channel)
                  .findFirst()
                  .ifPresent(kbd -> _keyboardSelector.setSelectedKeyboard(kbd));
      }
    }
    
    @Override
    public void verify() throws VerificationException {
      final String text = _valueField.getText().trim();
      final int hyphen = text.indexOf("-");
      if (hyphen == -1) {
        try {
          _lowCache = _highCache = Integer.parseInt(text);
        } catch (NumberFormatException e) {
          throw new VerificationException(
              "Enter a number or two numbers separated by a hyphen", _valueField);
        }
      } else {
        try {
          _lowCache = Integer.parseInt(text.substring(0, hyphen));
          _highCache = Integer.parseInt(text.substring(hyphen+1));
        } catch (NumberFormatException e) {
          throw new VerificationException(
              "Enter a number or two numbers separated by a hyphen", _valueField);
        }
      }
      if (_lowCache > _highCache || _lowCache < 0 || _highCache > 127)
        throw new VerificationException(
            "High must be greater than low, and both must be between 0 and 127", _valueField);
    }
    
    @Override
    public ControlValuePredicate createPredicate() {
      return new ControlValuePredicate(_keyboardSelector.getSelectedKeyboard(),
          _controlCombo.getSelectedIndex(), _lowCache, _highCache);
    }
  }
}
