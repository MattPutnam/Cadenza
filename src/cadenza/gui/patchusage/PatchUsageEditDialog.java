package cadenza.gui.patchusage;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import cadenza.control.midiinput.AcceptsKeyboardInput;
import cadenza.control.midiinput.MIDIInputControlCenter;
import cadenza.control.midiinput.NoteRangeEntryTracker;
import cadenza.core.CadenzaData;
import cadenza.core.Keyboard;
import cadenza.core.Note;
import cadenza.core.NoteRange;
import cadenza.core.Patch;
import cadenza.core.metronome.Metronome.Subdivision;
import cadenza.core.patchusage.ArpeggiatorPatchUsage;
import cadenza.core.patchusage.ArpeggiatorPatchUsage.Pattern;
import cadenza.core.patchusage.CustomScalePatchUsage;
import cadenza.core.patchusage.GhostNotePatchUsage;
import cadenza.core.patchusage.PatchUsage;
import cadenza.core.patchusage.SequencerPatchUsage;
import cadenza.core.patchusage.SimplePatchUsage;
import cadenza.gui.CadenzaFrame;
import cadenza.gui.common.HelpButton;
import cadenza.gui.common.NoteRangeEditPanel;
import cadenza.gui.common.TranspositionEditor;
import cadenza.gui.common.VolumeField;
import cadenza.gui.effects.edit.EffectChainViewerEditor;
import cadenza.gui.patch.PatchSelector;
import cadenza.gui.patchusage.editor.CustomScalePatchUsageEditor;
import cadenza.gui.patchusage.editor.GhostNotePatchUsageEditor;
import cadenza.gui.patchusage.editor.SequencerPatchUsageEditor;
import cadenza.preferences.Preferences;

import common.swing.IntField;
import common.swing.SwingUtils;
import common.swing.VerificationException;
import common.swing.dialog.OKCancelDialog;

@SuppressWarnings("serial")
public class PatchUsageEditDialog extends OKCancelDialog implements AcceptsKeyboardInput {
  private final CadenzaFrame _frame;
  private final PatchUsage _startingPatchUsage;
  private final CadenzaData _data;
  private final boolean _allowRangeEdit;
  
  private PatchSelector _patchSelector;
  private VolumeField _volumeField;
  private NoteRangeEditPanel _noteRangeSelector;
  private EffectChainViewerEditor _effectPanel;
  
  private JTabbedPane _tabbedPane;
  private SimplePatchUsagePane _simplePane;
  private GhostNotePatchUsagePane _ghostPane;
  private CustomScalePatchUsagePane _scalePane;
  private ArpeggiatorPatchUsagePane _arpeggiatorPane;
  private SequencerPatchUsagePane _sequencerPane;
  
  private NoteRangeEnterer _noteRangeEnterer;

  public PatchUsageEditDialog(CadenzaFrame frame, PatchUsage startingPatchUsage,
      CadenzaData data, boolean allowRangeEdit) {
    super(frame);
    _frame = frame;
    _startingPatchUsage = startingPatchUsage;
    _data = data;
    _allowRangeEdit = allowRangeEdit;
    
    if (Preferences.getMIDIInputOptions().allowMIDIInput()) {
      MIDIInputControlCenter.installWindowFocusGrabber(this);
      _noteRangeEnterer = new NoteRangeEnterer();
    }
  }

  @Override
  protected JComponent buildContent() {
    _patchSelector = new PatchSelector(_frame, _data.patches, _data.synthesizers, _startingPatchUsage.patch);
    _volumeField = new VolumeField(_startingPatchUsage.volume);
    _noteRangeSelector = new NoteRangeEditPanel(_data.keyboards, _startingPatchUsage.getNoteRange(), true);
    _effectPanel = new EffectChainViewerEditor(_startingPatchUsage.effects, true);
    
    _patchSelector.accessCombo().addActionListener(e -> _volumeField.setVolume(_patchSelector.getSelectedPatch().defaultVolume));
    _noteRangeSelector.setBorder(BorderFactory.createTitledBorder("Note Range"));
    _effectPanel.setBorder(BorderFactory.createTitledBorder("Effects"));
    
    _tabbedPane = new JTabbedPane();
    _simplePane = new SimplePatchUsagePane();
    _ghostPane = new GhostNotePatchUsagePane(_startingPatchUsage, _noteRangeSelector);
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
    if (_allowRangeEdit) box.add(_noteRangeSelector);
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
    final NoteRange noteRange = _noteRangeSelector.getSelectedNoteRange();
    final int volume = _volumeField.getVolume();
    
    final PatchUsage newPatchUsage;
    switch (_tabbedPane.getSelectedIndex()) {
      case 0: newPatchUsage = _simplePane.getPatchUsage(patch, noteRange, volume); break;
      case 1: newPatchUsage = _ghostPane.getPatchUsage(patch, noteRange, volume); break;
      case 2: newPatchUsage = _scalePane.getPatchUsage(patch, noteRange, volume); break;
      case 3: newPatchUsage = _arpeggiatorPane.getPatchUsage(patch, noteRange, volume); break;
      case 4: newPatchUsage = _sequencerPane.getPatchUsage(patch, noteRange, volume); break;
      default: throw new IllegalStateException("Unknown Tab!");
    }
    
    newPatchUsage.effects = _effectPanel.getEffects();
    return newPatchUsage;
  }
  
  @Override
  public void keyPressed(int channel, int midiNumber, int velocity) {
    SwingUtilities.invokeLater(() -> {
      final Keyboard kbd = _noteRangeEnterer.keyPressed(channel, midiNumber);
      _noteRangeSelector.highlightKey(kbd, midiNumber);
    });
  }
  
  @Override
  public void keyReleased(int channel, int midiNumber) {
    SwingUtilities.invokeLater(() -> {
      final Keyboard kbd = _noteRangeEnterer.keyReleased(channel, midiNumber);
      _noteRangeSelector.unhighlightKey(kbd, midiNumber);
    });
  }
  
  private class NoteRangeEnterer extends NoteRangeEntryTracker {
    public NoteRangeEnterer() {
      super(_data.keyboards);
    }
    
    @Override
    protected void singlePressed(Keyboard keyboard, int noteNumber) {
      _noteRangeSelector.setSelectedNoteRange(new NoteRange(keyboard, Note.valueOf(noteNumber)));
    }
    
    @Override
    protected void rangePressed(Keyboard keyboard, int lowNumber, int highNumber) {
      _noteRangeSelector.setSelectedNoteRange(new NoteRange(keyboard, Note.valueOf(lowNumber), Note.valueOf(highNumber)));
    }
    
    @Override
    protected void wholePressed(Keyboard keyboard) {
      _noteRangeSelector.setSelectedNoteRange(new NoteRange(keyboard, true));
    }
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
     * @param noteRange the selected note range, passed in from the general controls
     * @param volume the selected volume, passed in from the general controls
     * @return the patch usage as defined by the GUI
     */
    public abstract T getPatchUsage(Patch patch, NoteRange noteRange, int volume);
  }
  
  private static class SimplePatchUsagePane extends PatchUsageEditPane<SimplePatchUsage> {
    private static final String MONOPHONIC_HELP = "<html>Monophonic patches play only one note at a time.<br>"
        + "When a new note is played, the old note is terminated.</html>";
    
    private final TranspositionEditor _transpositionEditor;
    private final JCheckBox _isMonophonicCheckBox;
    
    public SimplePatchUsagePane() {
      _transpositionEditor = new TranspositionEditor(0);
      
      _isMonophonicCheckBox = new JCheckBox("Monophonic");
      
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      add(SwingUtils.buildCenteredRow(_transpositionEditor));
      add(Box.createVerticalStrut(8));
      add(SwingUtils.buildCenteredRow(_isMonophonicCheckBox, new HelpButton(MONOPHONIC_HELP)));
      add(Box.createVerticalGlue());
    }
    
    @Override
    public void populate(SimplePatchUsage initialPatchUsage) {
      _transpositionEditor.setTransposition(initialPatchUsage.transposition);
      
      _isMonophonicCheckBox.setSelected(initialPatchUsage.monophonic);
    }
    
    @Override
    public SimplePatchUsage getPatchUsage(Patch patch, NoteRange noteRange, int volume) {
      return new SimplePatchUsage(patch, noteRange, volume,
          _transpositionEditor.getTransposition(),
          _isMonophonicCheckBox.isSelected());
    }
  }
  
  private static class GhostNotePatchUsagePane extends PatchUsageEditPane<GhostNotePatchUsage> {
    private final GhostNotePatchUsageEditor _editor;
    
    public GhostNotePatchUsagePane(PatchUsage startingPatchUsage, NoteRangeEditPanel noteRangeSelector) {
      _editor = new GhostNotePatchUsageEditor(startingPatchUsage);
      
      noteRangeSelector.addNoteRangeListener(_editor::setNoteRange);
      
      add(_editor);
    }
    
    @Override
    public void populate(GhostNotePatchUsage initialPatchUsage) {
      _editor.setPatchUsage(initialPatchUsage);
    }
    
    @Override
    public GhostNotePatchUsage getPatchUsage(Patch patch, NoteRange noteRange, int volume) {
      return new GhostNotePatchUsage(patch, noteRange, volume,  _editor.getMap());
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
    public CustomScalePatchUsage getPatchUsage(Patch patch, NoteRange noteRange, int volume) {
      if (_editor.isScaleSelected())
        return new CustomScalePatchUsage(patch, noteRange, volume, _editor.getSelectedScale());
      else
        return new CustomScalePatchUsage(patch, noteRange, volume, _editor.getMap());
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
    public ArpeggiatorPatchUsage getPatchUsage(Patch patch, NoteRange noteRange, int volume) {
      return new ArpeggiatorPatchUsage(patch, noteRange, volume,
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
    public SequencerPatchUsage getPatchUsage(Patch patch, NoteRange noteRange, int volume) {
      return new SequencerPatchUsage(patch, noteRange, volume, _editor.getSequencer());
    }
  }

}
