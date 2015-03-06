package cadenza.gui.cue;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import cadenza.control.midiinput.AcceptsKeyboardInput;
import cadenza.control.midiinput.LocationEntryTracker;
import cadenza.control.midiinput.MIDIInputControlCenter;
import cadenza.core.CadenzaData;
import cadenza.core.ControlMapEntry;
import cadenza.core.ControlMapProvider;
import cadenza.core.Cue;
import cadenza.core.Keyboard;
import cadenza.core.Location;
import cadenza.core.Note;
import cadenza.core.patchusage.PatchUsage;
import cadenza.gui.controlmap.ControlMapPanel;
import cadenza.gui.effects.edit.EffectChainViewerEditor;
import cadenza.gui.patchusage.PatchUsagePanel;
import cadenza.gui.song.SongPanel;
import cadenza.gui.trigger.TriggerPanel;
import cadenza.preferences.Preferences;

import common.collection.NotifyingList;
import common.swing.CollapsiblePanel;
import common.swing.CollapsiblePanel.Icon;
import common.swing.CollapsiblePanel.Orientation;
import common.swing.SwingUtils;
import common.swing.VerificationException;
import common.swing.dialog.OKCancelDialog;

@SuppressWarnings("serial")
public class CueEditDialog extends OKCancelDialog implements ControlMapProvider, AcceptsKeyboardInput {
  private final Cue _cue;
  private final NotifyingList<Cue> _otherCues;
  private final CadenzaData _data;
  
  private SongPanel _songPanel;
  private JTextField _measureField;
  
  private PatchUsagePanel _patchUsagePanel;
  private TriggerPanel _triggerPanel;
  private ControlMapPanel _controlPanel;
  private EffectChainViewerEditor _effectsPanel;
  
  private JCheckBox _disableGlobalTriggersCheckBox;
  private JCheckBox _disableGlobalControlCheckBox;
  private JCheckBox _disableGlobalEffectsCheckBox;
  
  private PatchEnterer _patchEnterer;
  
  public CueEditDialog(Component parent, Cue cue, CadenzaData data) {
    super(parent);
    _cue = cue;
    _data = data;
    
    _otherCues = new NotifyingList<>();
    for (Cue c : data.cues) {
      if (c != cue) {
        _otherCues.add(c);
      }
    }
    
    if (Preferences.allowPatchUsageInput()) {
      MIDIInputControlCenter.installWindowFocusGrabber(this);
      _patchEnterer = new PatchEnterer();
    }
  }
  
  @Override
  protected JComponent buildContent() {
    _songPanel = new SongPanel(_data.songs, true);
    if (_cue.song != null)
      _songPanel.setSelectedSong(_cue.song);
    
    _measureField = new JTextField(_cue.measureNumber, 8);
    
    _patchUsagePanel = new PatchUsagePanel(_cue, _data);
    _triggerPanel = new TriggerPanel(_cue, _data);
    _controlPanel = new ControlMapPanel(_cue);
    _effectsPanel = new EffectChainViewerEditor(_cue.effects, true);
    
    _disableGlobalTriggersCheckBox = new JCheckBox("Disable global triggers", _cue.disableGlobalTriggers);
    _disableGlobalControlCheckBox = new JCheckBox("Disable global control map", _cue.disableGlobalControlMap);
    _disableGlobalEffectsCheckBox = new JCheckBox("Disable global effects", _cue.disableGlobalEffects);
    
    _measureField.addFocusListener(new FocusAdapter() {
      @Override
      public void focusLost(FocusEvent e) {
        if (_measureField.getText().isEmpty())
          _measureField.setText("1");
      }
    });
    
    final JPanel tp = new JPanel(new BorderLayout());
    tp.add(_disableGlobalTriggersCheckBox, BorderLayout.NORTH);
    tp.add(_triggerPanel, BorderLayout.CENTER);
    
    final JPanel cp = new JPanel(new BorderLayout());
    cp.add(_disableGlobalControlCheckBox, BorderLayout.NORTH);
    cp.add(_controlPanel, BorderLayout.CENTER);
    
    final JPanel pp = new JPanel(new BorderLayout());
    pp.add(_disableGlobalEffectsCheckBox, BorderLayout.NORTH);
    pp.add(_effectsPanel, BorderLayout.CENTER);
    
    final CollapsiblePanel collapsePatches = new CollapsiblePanel(
        _patchUsagePanel, Orientation.VERTICAL, Icon.ARROW,"Patches", null);
    final CollapsiblePanel collapseTrigger = new CollapsiblePanel(
        tp, Orientation.VERTICAL, Icon.ARROW, "Triggers", null);
    final CollapsiblePanel collapseControl = new CollapsiblePanel(
        cp, Orientation.VERTICAL, Icon.ARROW,"Control Overrides", null);
    final CollapsiblePanel collapseEffects = new CollapsiblePanel(
        pp, Orientation.VERTICAL, Icon.ARROW,"Effects", null);
    
    collapseTrigger.setExpanded(!_cue.triggers.isEmpty() || _cue.disableGlobalTriggers);
    collapseControl.setExpanded(!_cue.getControlMap().isEmpty() || _cue.disableGlobalControlMap);
    collapseEffects.setExpanded(!_cue.effects.isEmpty() || _cue.disableGlobalEffects);
    
    final Box measure = Box.createHorizontalBox();
    measure.add(new JLabel("Measure: "));
    measure.add(_measureField);
    measure.add(Box.createHorizontalGlue());
    SwingUtils.freezeHeight(measure);
    
    _songPanel.setBorder(new EmptyBorder(0, 8, 0, 0));
    measure.setBorder(new EmptyBorder(0, 8, 0, 0));
    
    final Box result = Box.createVerticalBox();
    result.add(_songPanel);
    result.add(measure);
    result.add(collapsePatches);
    result.add(collapseTrigger);
    result.add(collapseControl);
    result.add(collapseEffects);
    
    return result;
  }
  
  @Override
  protected void initialize() {
    setSize(new Dimension(getSize().width, 600));
  }
  
  @Override
  protected String declareTitle() {
    return (_cue == null) ? "Create Cue" : "Edit Cue";
  }
  
  @Override
  protected void takeActionOnOK() {
    _cue.song = _songPanel.getSelectedSong();
    _cue.measureNumber = _measureField.getText().trim();
    _cue.patches = _patchUsagePanel.getPatchUsages();
    _cue.triggers = _triggerPanel.getTriggers();
    _cue.disableGlobalTriggers = _disableGlobalTriggersCheckBox.isSelected();
    _cue.setControlMap(_controlPanel.getMapping());
    _cue.disableGlobalControlMap = _disableGlobalControlCheckBox.isSelected();
    _cue.effects = _effectsPanel.getEffects();
    _cue.disableGlobalEffects = _disableGlobalEffectsCheckBox.isSelected();
  }
  
  @Override
  public List<ControlMapEntry> getControlMap() {
    return _controlPanel == null ? new ArrayList<>() : _controlPanel.getMapping();
  };
  
  @Override
  public List<PatchUsage> getPatchUsages() {
    return _cue.patches;
  }
  
  @Override
  protected void verify() throws VerificationException {
    _songPanel.verify();
    
    final String measure = _measureField.getText().trim();
    
    if (measure.isEmpty())
      throw new VerificationException("Please specify a measure number", _measureField);
    
    for (Cue cue : _otherCues) {
      if (_songPanel.getSelectedSong().equals(cue.song) && measure.equals(cue.measureNumber)) {
        throw new VerificationException("A Cue with this song/measure already exists");
      }
    }
  }

  @Override
  public void keyPressed(int channel, int midiNumber, int velocity) {
    final Keyboard kbd = _patchEnterer.keyPressed(channel, midiNumber);
    _patchUsagePanel.highlightKey(_data.keyboards.indexOf(kbd), midiNumber);
  }

  @Override
  public void keyReleased(int channel, int midiNumber) {
    final Keyboard kbd = _patchEnterer.keyReleased(channel, midiNumber);
    _patchUsagePanel.unHighlightKey(_data.keyboards.indexOf(kbd), midiNumber);
  }
  
  private class PatchEnterer extends LocationEntryTracker {
    public PatchEnterer() {
      super(_data.keyboards);
    }
    
    @Override
    protected void singlePressed(Keyboard keyboard, int noteNumber) {
      _patchUsagePanel.addPatchUsage(Location.singleNote(keyboard, new Note(noteNumber)));
    }
    
    @Override
    protected void rangePressed(Keyboard keyboard, int lowNumber, int highNumber) {
      _patchUsagePanel.addPatchUsage(Location.range(keyboard, new Note(lowNumber), new Note(highNumber)));
    }
    
    @Override
    protected void wholePressed(Keyboard keyboard) {
      _patchUsagePanel.addPatchUsage(Location.wholeKeyboard(keyboard));
    }
  }
}
