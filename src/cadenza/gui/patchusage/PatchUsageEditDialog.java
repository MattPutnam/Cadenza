package cadenza.gui.patchusage;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;

import cadenza.core.CadenzaData;
import cadenza.core.Location;
import cadenza.core.Patch;
import cadenza.core.effects.Effect;
import cadenza.core.metronome.Metronome.Subdivision;
import cadenza.core.patchusage.ArpeggiatorPatchUsage;
import cadenza.core.patchusage.ArpeggiatorPatchUsage.Pattern;
import cadenza.core.patchusage.CustomScalePatchUsage;
import cadenza.core.patchusage.GhostNotePatchUsage;
import cadenza.core.patchusage.PatchUsage;
import cadenza.core.patchusage.SequencerPatchUsage;
import cadenza.core.patchusage.SimplePatchUsage;
import cadenza.gui.common.LocationEditPanel;
import cadenza.gui.common.LocationListener;
import cadenza.gui.common.TranspositionEditor;
import cadenza.gui.common.VolumeField;
import cadenza.gui.effects.edit.EffectChainViewerEditor;
import cadenza.gui.patch.PatchSelector;
import cadenza.gui.patchusage.editor.CustomScalePatchUsageEditor;
import cadenza.gui.patchusage.editor.GhostNotePatchUsageEditor;
import cadenza.gui.patchusage.editor.SequencerPatchUsageEditor;
import common.swing.IntField;
import common.swing.SwingUtils;
import common.swing.VerificationException;
import common.swing.dialog.OKCancelDialog;

@SuppressWarnings("serial")
public class PatchUsageEditDialog extends OKCancelDialog {
  private final PatchUsage _startingPatchUsage;
  private final CadenzaData _data;
  
  private PatchSelector _patchSelector;
  private VolumeField _volumeField;
  private LocationEditPanel _locationSelector;
  private EffectChainViewerEditor _effectPanel;
  
  private JTabbedPane _tabbedPane;
  private SimplePatchUsagePane _simplePane;
  private GhostNotePatchUsagePane _ghostPane;
  private CustomScalePatchUsagePane _scalePane;
  private ArpeggiatorPatchUsagePane _arpeggiatorPane;
  private SequencerPatchUsagePane _sequencerPane;

  public PatchUsageEditDialog(Component parent, PatchUsage startingPatchUsage,
      CadenzaData data) {
    super(parent);
    _startingPatchUsage = startingPatchUsage;
    
    _data = data;
  }

  @Override
  protected JComponent buildContent() {
    if (_startingPatchUsage == null) {
      _patchSelector = new PatchSelector(_data.patches, _data.synthesizers, null);
      _volumeField = new VolumeField(100);
      _locationSelector = new LocationEditPanel(_data.keyboards, null);
      _effectPanel = new EffectChainViewerEditor(new ArrayList<Effect>(), true);
    } else {
      _patchSelector = new PatchSelector(_data.patches, _data.synthesizers, _startingPatchUsage.patch);
      _volumeField = new VolumeField(_startingPatchUsage.volume);
      _locationSelector = new LocationEditPanel(_data.keyboards, _startingPatchUsage.location);
      _effectPanel = new EffectChainViewerEditor(_startingPatchUsage.effects, true);
    }
    
    _patchSelector.accessCombo().addActionListener(e -> _volumeField.setVolume(_patchSelector.getSelectedPatch().defaultVolume));
    _locationSelector.setBorder(BorderFactory.createTitledBorder("Location"));
    _effectPanel.setBorder(BorderFactory.createTitledBorder("Effects"));
    
    _tabbedPane = new JTabbedPane();
    _simplePane = new SimplePatchUsagePane();
    _ghostPane = new GhostNotePatchUsagePane(_startingPatchUsage, _locationSelector);
    _scalePane = new CustomScalePatchUsagePane();
    _arpeggiatorPane = new ArpeggiatorPatchUsagePane();
    _sequencerPane = new SequencerPatchUsagePane();
    
    _tabbedPane.addTab("Simple Patch", _simplePane);
    _tabbedPane.addTab("Ghost Note Patch", _ghostPane);
    _tabbedPane.addTab("Custom Scale", _scalePane);
    _tabbedPane.addTab("Arpeggiator", _arpeggiatorPane);
    _tabbedPane.addTab("Sequencer", _sequencerPane);
    
    if (_startingPatchUsage instanceof SimplePatchUsage) {
      _simplePane.populate((SimplePatchUsage) _startingPatchUsage);
      _tabbedPane.setSelectedIndex(0);
    } else if (_startingPatchUsage instanceof GhostNotePatchUsage) {
      _ghostPane.populate((GhostNotePatchUsage) _startingPatchUsage);
      _tabbedPane.setSelectedIndex(1);
    } else if (_startingPatchUsage instanceof CustomScalePatchUsage) {
      _scalePane.populate((CustomScalePatchUsage) _startingPatchUsage);
      _tabbedPane.setSelectedIndex(2);
    } else if (_startingPatchUsage instanceof ArpeggiatorPatchUsage) {
      _arpeggiatorPane.populate((ArpeggiatorPatchUsage) _startingPatchUsage);
      _tabbedPane.setSelectedIndex(3);
    } else if (_startingPatchUsage instanceof SequencerPatchUsage) {
      _sequencerPane.populate((SequencerPatchUsage) _startingPatchUsage);
      _tabbedPane.setSelectedIndex(4);
    }
    
    _tabbedPane.setBorder(BorderFactory.createTitledBorder("Patch Type"));
    
    final Box box = Box.createVerticalBox();
    box.add(SwingUtils.buildCenteredRow(_patchSelector, new JLabel("Volume: "), _volumeField));
    box.add(_locationSelector);
    box.add(_effectPanel);
    box.add(_tabbedPane);
    return box;
  }

  @Override
  protected String declareTitle() {
    return _startingPatchUsage == null ? "Create Patch Usage" : "Edit Patch Usage";
  }

  @Override
  protected void verify() throws VerificationException {
    if (_patchSelector.getSelectedPatch() == null)
      throw new VerificationException("Please select a patch");
    
    switch (_tabbedPane.getSelectedIndex()) {
      case 0: _simplePane.verify(); break;
      case 1: _ghostPane.verify(); break;
      case 2: _scalePane.verify(); break;
      case 3: _arpeggiatorPane.verify(); break;
      case 4: _sequencerPane.verify(); break;
    }
  }
  
  public PatchUsage getPatchUsage() {
    final Patch patch = _patchSelector.getSelectedPatch();
    final Location location = _locationSelector.getSelectedLocation();
    final int volume = _volumeField.getVolume();
    
    final PatchUsage newPatchUsage;
    switch (_tabbedPane.getSelectedIndex()) {
      case 0: newPatchUsage = _simplePane.getPatchUsage(patch, location, volume); break;
      case 1: newPatchUsage = _ghostPane.getPatchUsage(patch, location, volume); break;
      case 2: newPatchUsage = _scalePane.getPatchUsage(patch, location, volume); break;
      case 3: newPatchUsage = _arpeggiatorPane.getPatchUsage(patch, location, volume); break;
      case 4: newPatchUsage = _sequencerPane.getPatchUsage(patch, location, volume); break;
      default: throw new IllegalStateException("Unknown Tab!");
    }
    
    newPatchUsage.effects = _effectPanel.getEffects();
    return newPatchUsage;
  }
  
  private static abstract class PatchUsageEditPane<T extends PatchUsage> extends JPanel {
    /**
     * Populate the editor with the values from an existing patch usage
     * @param initialPatchUsage the patch usage to mimic
     */
    public abstract void populate(T initialPatchUsage);
    
    /**
     * Verifies the patch for correctness
     * @throws VerificationException if a UI element has a bad value
     */
    public void verify() throws VerificationException {}
    
    /**
     * Gets the patch usage as defined by the GUI 
     * @param patch the selected patch, passed in from the general controls
     * @param location the selected location, passed in from the general controls
     * @param volume the selected volume, passed in from the general controls
     * @return the patch usage as defined by the GUI
     */
    public abstract T getPatchUsage(Patch patch, Location location, int volume);
  }
  
  private static class SimplePatchUsagePane extends PatchUsageEditPane<SimplePatchUsage> {
    private final TranspositionEditor _transpositionEditor;
    
    private final JCheckBox _isMonophonicCheckBox;
    
    private final JCheckBox _limitVolumeCheckBox;
    private final JRadioButton _belowRadioButton;
    private final JRadioButton _aboveRadioButton;
    private final JLabel _reduceLabel;
    private VolumeField _volumeLimitField;
    private VolumeField _volumeReductionField;
    
    public SimplePatchUsagePane() {
      _transpositionEditor = new TranspositionEditor(0);
      
      _isMonophonicCheckBox = new JCheckBox("Monophonic");
      
      _limitVolumeCheckBox = new JCheckBox("Limit only to velocities");
      _belowRadioButton = new JRadioButton("below");
      _aboveRadioButton = new JRadioButton("above (and incl.)");
      SwingUtils.groupAndSelectFirst(_belowRadioButton, _aboveRadioButton);
      _reduceLabel = new JLabel(" and reduce by ");
      _volumeLimitField = new VolumeField(120);
      _volumeReductionField = new VolumeField(0);
      
      final ActionListener listener = e -> updateEnabledStates();
      _limitVolumeCheckBox.addActionListener(listener);
      _belowRadioButton.addActionListener(listener);
      _aboveRadioButton.addActionListener(listener);
      updateEnabledStates();
      
      final Box buttons = Box.createVerticalBox();
      buttons.add(_belowRadioButton); buttons.add(_aboveRadioButton);
      
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      add(SwingUtils.buildCenteredRow(_transpositionEditor));
      add(Box.createVerticalStrut(24));
      add(SwingUtils.buildCenteredRow(_isMonophonicCheckBox));
      add(Box.createVerticalStrut(24));
      add(SwingUtils.buildCenteredRow(_limitVolumeCheckBox, buttons, _volumeLimitField));
      add(SwingUtils.buildCenteredRow(_reduceLabel, _volumeReductionField));
      add(Box.createVerticalGlue());
    }
    
    @Override
    public void populate(SimplePatchUsage initialPatchUsage) {
      _transpositionEditor.setTransposition(initialPatchUsage.transposition);
      
      _isMonophonicCheckBox.setSelected(initialPatchUsage.monophonic);
      
      if (initialPatchUsage.volumeLimit != -1) {
        _limitVolumeCheckBox.setSelected(true);
        if (initialPatchUsage.isLimitToBelow)
          _belowRadioButton.setSelected(true);
        else
          _aboveRadioButton.setSelected(true);
        _volumeLimitField.setVolume(initialPatchUsage.volumeLimit);
        _volumeReductionField.setVolume(initialPatchUsage.volumeReduction);
      }
      
      updateEnabledStates();
    }
    
    private void updateEnabledStates() {
      final boolean limitEnabled = _limitVolumeCheckBox.isSelected();
      final boolean reduceEnabled = _aboveRadioButton.isSelected();
      
      _belowRadioButton.setEnabled(limitEnabled);
      _aboveRadioButton.setEnabled(limitEnabled);
      _reduceLabel.setEnabled(limitEnabled && reduceEnabled);
      _volumeLimitField.setEnabled(limitEnabled);
      _volumeReductionField.setEnabled(limitEnabled && reduceEnabled);
    }
    
    @Override
    public SimplePatchUsage getPatchUsage(Patch patch, Location location, int volume) {
      return new SimplePatchUsage(patch, location, volume,
          _transpositionEditor.getTransposition(),
          _isMonophonicCheckBox.isSelected(),
          _limitVolumeCheckBox.isSelected() ? _volumeLimitField.getVolume() : -1,
          _belowRadioButton.isSelected(),
          _volumeReductionField.getVolume());
    }
  }
  
  private static class GhostNotePatchUsagePane extends PatchUsageEditPane<GhostNotePatchUsage> {
    private final GhostNotePatchUsageEditor _editor;
    
    public GhostNotePatchUsagePane(PatchUsage startingPatchUsage, LocationEditPanel locationSelector) {
      _editor = new GhostNotePatchUsageEditor(startingPatchUsage);
      
      locationSelector.addLocationListener(new LocationListener() {
        @Override
        public void locationChanged(Location newLocation) {
          _editor.setLocation(newLocation);
        }
      });
      
      add(_editor);
    }
    
    @Override
    public void populate(GhostNotePatchUsage initialPatchUsage) {
      _editor.setPatchUsage(initialPatchUsage);
    }
    
    @Override
    public GhostNotePatchUsage getPatchUsage(Patch patch, Location location, int volume) {
      return new GhostNotePatchUsage(patch, location, volume,  _editor.getMap());
    }
  }
  
  private class CustomScalePatchUsagePane extends PatchUsageEditPane<CustomScalePatchUsage> {
    private final CustomScalePatchUsageEditor _editor;
    
    public CustomScalePatchUsagePane() {
      _editor = new CustomScalePatchUsageEditor();
      
      add(_editor);
    }
    
    @Override
    public void populate(CustomScalePatchUsage initialPatchUsage) {
      _editor.populate(initialPatchUsage);
    }
    
    @Override
    public CustomScalePatchUsage getPatchUsage(Patch patch, Location location, int volume) {
      if (_editor.isScaleSelected())
        return new CustomScalePatchUsage(patch, location, volume, _editor.getSelectedScale());
      else
        return new CustomScalePatchUsage(patch, location, volume, _editor.getMap());
    }
  }
  
  private class ArpeggiatorPatchUsagePane extends PatchUsageEditPane<ArpeggiatorPatchUsage> {
    private final JComboBox<Pattern> _patternCombo;
    private final JComboBox<Subdivision> _subdivisionCombo;
    private final IntField _minNotesField;
    
    public ArpeggiatorPatchUsagePane() {
      _patternCombo = new JComboBox<>(Pattern.values());
      _subdivisionCombo = new JComboBox<>(Subdivision.values());
      _minNotesField = new IntField(3, 2, Integer.MAX_VALUE);
      _minNotesField.setColumns(4);
      
      add(_patternCombo);
      add(_subdivisionCombo);
      add(new JLabel("Minimum number of notes:"));
      add(_minNotesField);
    }
    
    @Override
    public void populate(ArpeggiatorPatchUsage initialPatchUsage) {
      _patternCombo.setSelectedItem(initialPatchUsage.pattern);
      _subdivisionCombo.setSelectedItem(initialPatchUsage.subdivision);
      _minNotesField.setInt(initialPatchUsage.minSize);
    }
    
    @Override
    public void verify() throws VerificationException {
      if (_minNotesField.getInt() < 2)
        throw new VerificationException("Minimum number of notes must be at least 2", _minNotesField);
    }
    
    @Override
    public ArpeggiatorPatchUsage getPatchUsage(Patch patch, Location location, int volume) {
      return new ArpeggiatorPatchUsage(patch, location, volume,
          (Pattern) _patternCombo.getSelectedItem(),
          (Subdivision) _subdivisionCombo.getSelectedItem(),
          _minNotesField.getInt());
    }
  }
  
  private class SequencerPatchUsagePane extends PatchUsageEditPane<SequencerPatchUsage> {
    private final SequencerPatchUsageEditor _editor;
    
    public SequencerPatchUsagePane() {
      _editor = new SequencerPatchUsageEditor(_data);
      final Dimension pref = _editor.getPreferredSize();
      pref.height = 180;
      _editor.setPreferredSize(pref);
      
      setLayout(new BorderLayout());
      add(_editor, BorderLayout.CENTER);
    }
    
    @Override
    public void populate(SequencerPatchUsage initialPatchUsage) {
      _editor.populate(initialPatchUsage);
    }
    
    @Override
    public void verify() throws VerificationException {
      _editor.verify();
    }
    
    @Override
    public SequencerPatchUsage getPatchUsage(Patch patch, Location location, int volume) {
      return new SequencerPatchUsage(patch, location, volume, _editor.getSequencer());
    }
  }

}
