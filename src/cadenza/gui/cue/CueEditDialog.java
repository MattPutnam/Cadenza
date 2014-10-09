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

import cadenza.core.CadenzaData;
import cadenza.core.ControlMapEntry;
import cadenza.core.ControlMapProvider;
import cadenza.core.Cue;
import cadenza.core.patchusage.PatchUsage;
import cadenza.gui.controlmap.ControlMapPanel;
import cadenza.gui.patchusage.PatchUsagePanel;
import cadenza.gui.plugins.edit.PluginChainViewerEditor;
import cadenza.gui.song.SongPanel;
import cadenza.gui.trigger.TriggerPanel;

import common.collection.NotifyingList;
import common.swing.CollapsiblePanel;
import common.swing.CollapsiblePanel.Orientation;
import common.swing.SwingUtils;
import common.swing.VerificationException;
import common.swing.dialog.OKCancelDialog;

@SuppressWarnings("serial")
public class CueEditDialog extends OKCancelDialog implements ControlMapProvider {
  private final Cue _cue;
  private final NotifyingList<Cue> _otherCues;
  private final CadenzaData _data;
  
  private SongPanel _songPanel;
  private JTextField _measureField;
  
  private PatchUsagePanel _patchUsagePanel;
  private TriggerPanel _triggerPanel;
  private ControlMapPanel _controlPanel;
  private PluginChainViewerEditor _pluginsPanel;
  
  private JCheckBox _disableGlobalTriggersCheckBox;
  private JCheckBox _disableGlobalControlCheckBox;
  private JCheckBox _disableGlobalPluginsCheckBox;
  
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
  }
  
  @Override
  protected JComponent buildContent() {
    _songPanel = new SongPanel(_data.songs, true);
    if (_cue.song != null)
      _songPanel.setSelectedSong(_cue.song);
    
    _measureField = new JTextField(_cue.measureNumber, 8);
    
    _patchUsagePanel = new PatchUsagePanel(_cue, _data);
    _triggerPanel = new TriggerPanel(_cue, _data);
    _controlPanel = new ControlMapPanel(_cue == null ? this : _cue);
    _pluginsPanel = new PluginChainViewerEditor(_cue.plugins, true);
    
    _disableGlobalTriggersCheckBox = new JCheckBox("Disable global triggers", _cue.disableGlobalTriggers);
    _disableGlobalControlCheckBox = new JCheckBox("Disable global control map", _cue.disableGlobalControlMap);
    _disableGlobalPluginsCheckBox = new JCheckBox("Disable global plugins", _cue.disableGlobalPlugins);
    
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
    pp.add(_disableGlobalPluginsCheckBox, BorderLayout.NORTH);
    pp.add(_pluginsPanel, BorderLayout.CENTER);
    
    final CollapsiblePanel collapsePatches = new CollapsiblePanel(
        _patchUsagePanel, Orientation.VERTICAL, "Patches", null);
    final CollapsiblePanel collapseTrigger = new CollapsiblePanel(
        tp, Orientation.VERTICAL, "Triggers", null);
    final CollapsiblePanel collapseControl = new CollapsiblePanel(
        cp, Orientation.VERTICAL, "Control Overrides", null);
    final CollapsiblePanel collapsePlugins = new CollapsiblePanel(
        pp, Orientation.VERTICAL, "Plugins", null);
    
    collapseTrigger.setExpanded(!_cue.triggers.isEmpty() || _cue.disableGlobalTriggers);
    collapseControl.setExpanded(!_cue.getControlMap().isEmpty() || _cue.disableGlobalControlMap);
    collapsePlugins.setExpanded(!_cue.plugins.isEmpty() || _cue.disableGlobalPlugins);
    
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
    result.add(collapsePlugins);
    
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
    _cue.plugins = _pluginsPanel.getPlugins();
    _cue.disableGlobalPlugins = _disableGlobalPluginsCheckBox.isSelected();
  }
  
  @Override
  public List<ControlMapEntry> getControlMap() {
    return _controlPanel == null ? new ArrayList<ControlMapEntry>() : _controlPanel.getMapping();
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
}
