package cadenza.gui.trigger;

import java.awt.Component;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;

import cadenza.core.CadenzaData;
import cadenza.core.metronome.Metronome.Subdivision;
import cadenza.core.trigger.actions.CueStepAction;
import cadenza.core.trigger.actions.GotoAction;
import cadenza.core.trigger.actions.LocationJumpAction;
import cadenza.core.trigger.actions.MetronomeAction;
import cadenza.core.trigger.actions.PanicAction;
import cadenza.core.trigger.actions.TriggerAction;
import cadenza.core.trigger.actions.WaitAction;
import cadenza.gui.common.LocationField;
import cadenza.gui.song.SongPanel;
import common.swing.IntField;
import common.swing.RadioButtonPanel;
import common.swing.SimpleGrid;
import common.swing.SwingUtils;
import common.swing.VerificationException;
import common.swing.dialog.OKCancelDialog;

@SuppressWarnings("serial")
public class TriggerActionEditDialog extends OKCancelDialog {
  private final CadenzaData _data;
  private final TriggerAction _initial;
  
  private JTabbedPane _tabbedPane;
  private StepPane _stepPane;
  private GotoPane _gotoPane;
  private WaitPane _waitPane;
  private PanicPane _panicPane;
  private MetronomePane _metronomePane;
  private LocationJumpPane _locationJumpPane;

  public TriggerActionEditDialog(Component parent, CadenzaData data, TriggerAction initial) {
    super(parent);
    _data = data;
    _initial = initial;
  }

  @Override
  protected JComponent buildContent() {
    _tabbedPane = new JTabbedPane();
    _stepPane = new StepPane();
    _gotoPane = new GotoPane();
    _waitPane = new WaitPane();
    _panicPane = new PanicPane();
    _metronomePane = new MetronomePane();
    _locationJumpPane = new LocationJumpPane();
    
    _tabbedPane.addTab("Step cue", _stepPane);
    _tabbedPane.addTab("Go To", _gotoPane);
    _tabbedPane.addTab("Wait", _waitPane);
    _tabbedPane.addTab("Panic", _panicPane);
    _tabbedPane.addTab("Metronome", _metronomePane);
    _tabbedPane.addTab("Location Jump", _locationJumpPane);
    
    return _tabbedPane;
  }
  
  @Override
  protected void initialize() {
    if (_initial == null)
      return;
    
    if (_initial instanceof CueStepAction) {
      _tabbedPane.setSelectedComponent(_stepPane);
      _stepPane.initialize((CueStepAction) _initial);
    } else if (_initial instanceof GotoAction) {
      _tabbedPane.setSelectedComponent(_gotoPane);
      _gotoPane.initialize((GotoAction) _initial);
    } else if (_initial instanceof WaitAction) {
      _tabbedPane.setSelectedComponent(_waitPane);
      _waitPane.initialize((WaitAction) _initial);
    } else if (_initial instanceof PanicAction) {
      _tabbedPane.setSelectedComponent(_panicPane);
      _panicPane.initialize((PanicAction) _initial);
    } else if (_initial instanceof MetronomeAction) {
      _tabbedPane.setSelectedComponent(_metronomePane);
      _metronomePane.initialize((MetronomeAction) _initial);
    } else if (_initial instanceof LocationJumpAction) {
      _tabbedPane.setSelectedComponent(_locationJumpPane);
      _locationJumpPane.initialize((LocationJumpAction) _initial);
    }
  }
  
  public TriggerAction getAction() {
    return ((ActionPane<?>) _tabbedPane.getSelectedComponent()).createAction();
  }

  @Override
  protected String declareTitle() {
    return "Create Action";
  }

  @Override
  protected void verify() throws VerificationException {
    ((ActionPane<?>) _tabbedPane.getSelectedComponent()).verify();
  }
  
  private abstract class ActionPane<T extends TriggerAction> extends JPanel {
    /**
     * Verifies the action UI
     * @throws VerificationException if a UI element has a bad value
     */
    public void verify() throws VerificationException {}
    
    /**
     * Initialize the tab with the given action
     * @param initial the action to imitate
     */
    public void initialize(T initial) {}
    
    public abstract T createAction();
  }
  
  private class StepPane extends ActionPane<CueStepAction> {
    private RadioButtonPanel<CueStepAction.Type> _buttonPanel;
    
    public StepPane() {
      _buttonPanel = new RadioButtonPanel<>(CueStepAction.Type.values(), CueStepAction.Type.ADVANCE, true);
      
      add(_buttonPanel);
    }
    
    @Override
    public void initialize(CueStepAction initial) {
      _buttonPanel.setSelectedValue(initial.getType());
    }
    
    @Override
    public CueStepAction createAction() {
      return new CueStepAction(_buttonPanel.getSelectedValue());
    }
  }
  
  private class GotoPane extends ActionPane<GotoAction> {
    private SongPanel _songPanel;
    private LocationField _measureField;
    
    public GotoPane() {
      _songPanel = new SongPanel(_data.songs, true);
      _measureField = new LocationField();
      
      add(new JLabel("Go to "));
      add(_songPanel);
      add(new JLabel("  Measure #"));
      add(_measureField);
    }
    
    @Override
    public void initialize(GotoAction initial) {
      _songPanel.setSelectedSong(initial.getSong());
      _measureField.setLocationNumber(initial.getMeasure());
    }
    
    @Override
    public void verify() throws VerificationException {
      _songPanel.verify();
      _measureField.verify();
    }
    
    @Override
    public GotoAction createAction() {
      return new GotoAction(_songPanel.getSelectedSong(), _measureField.getLocationNumber());
    }
  }
  
  private class WaitPane extends ActionPane<WaitAction> {
    private IntField _field;
    
    private JRadioButton _millisButton;
    private JRadioButton _beatsButton;
    
    private JComboBox<Subdivision> _subdivisionCombo;
    
    public WaitPane() {
      _field = new IntField(1, 0, Integer.MAX_VALUE);
      _field.setColumns(6);
      
      _millisButton = new JRadioButton("milliseconds");
      _beatsButton = new JRadioButton();
      SwingUtils.groupAndSelectFirst(_millisButton, _beatsButton);
      
      _subdivisionCombo = new JComboBox<>(Subdivision.values());
      final Box beatsBox = SwingUtils.buildLeftAlignedRow(_beatsButton, _subdivisionCombo);
      
      final Box box = Box.createVerticalBox();
      box.add(SwingUtils.buildLeftAlignedRow(_millisButton));
      box.add(beatsBox);
      
      add(new JLabel("Wait "));
      add(_field);
      add(box);
    }
    
    @Override
    public void initialize(WaitAction initial) {
      _field.setInt(initial.getNum());
      if (initial.isMillis()) {
        _millisButton.setSelected(true);
      } else {
        _beatsButton.setSelected(true);
        _subdivisionCombo.setSelectedItem(initial.getSubdivision());
      }
    }
    
    @Override
    public WaitAction createAction() {
      if (_millisButton.isSelected()) {
        return WaitAction.millis(_field.getInt());
      } else {
        return WaitAction.beats(_field.getInt(), (Subdivision) _subdivisionCombo.getSelectedItem());
      }
    }
  }
  
  private class PanicPane extends ActionPane<PanicAction> {
    public PanicPane() {
      add(new JLabel("Panic (all notes off)"));
    }
    
    @Override
    public PanicAction createAction() {
      return new PanicAction();
    }
  }
  
  private class LocationJumpPane extends ActionPane<LocationJumpAction> {
    private RadioButtonPanel<LocationJumpAction.Type> _rbp;
    
    public LocationJumpPane() {
      _rbp = new RadioButtonPanel<>(LocationJumpAction.Type.values(), LocationJumpAction.Type.SAVE, true);
      add(_rbp);
    }
    
    @Override
    public LocationJumpAction createAction() {
      return new LocationJumpAction(_rbp.getSelectedValue());
    }
    
    @Override
    public void initialize(LocationJumpAction initial) {
      _rbp.setSelectedValue(initial.getType());
    }
  }
  
  private class MetronomePane extends ActionPane<MetronomeAction> {
    private JRadioButton _startButton;
    private JRadioButton _stopButton;
    private JRadioButton _setBPMButton;
    private JRadioButton _tapButton;
    
    private IntField _bpmField;
    
    public MetronomePane() {
      _startButton = new JRadioButton("Start");
      _stopButton = new JRadioButton("Stop");
      _setBPMButton = new JRadioButton("Set BPM = ");
      _tapButton = new JRadioButton("Tempo Tap");
      SwingUtils.groupAndSelectFirst(_startButton, _stopButton, _setBPMButton, _tapButton);
      
      _bpmField = new IntField(120, 1, 500);
      _bpmField.setColumns(6);
      
      add(new SimpleGrid(new JComponent[][] {
          { _startButton,  null      },
          { _stopButton,   null      },
          { _setBPMButton, _bpmField },
          { _tapButton,    null      }
      }));
    }
    
    @Override
    public void initialize(MetronomeAction initial) {
      switch (initial.getType()) {
        case START:   _startButton.setSelected(true);  break;
        case STOP:    _stopButton.setSelected(true);   break;
        case SET_BPM: _setBPMButton.setSelected(true); _bpmField.setInt(initial.getBPM()); break;
        case TAP:     _tapButton.setSelected(true);
      }
    }
    
    @Override
    public MetronomeAction createAction() {
      if (_startButton.isSelected())
        return new MetronomeAction(MetronomeAction.Type.START);
      else if (_stopButton.isSelected())
        return new MetronomeAction(MetronomeAction.Type.STOP);
      else if (_setBPMButton.isSelected())
        return new MetronomeAction(_bpmField.getInt());
      else
        return new MetronomeAction(MetronomeAction.Type.TAP);
    }
  }

}
