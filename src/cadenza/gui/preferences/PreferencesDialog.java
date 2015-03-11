package cadenza.gui.preferences;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JComponent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cadenza.core.Keyboard;
import cadenza.core.Synthesizer;
import cadenza.gui.common.SynthConfigPanel;
import cadenza.gui.keyboard.KeyboardEditPanel;
import cadenza.preferences.Preferences.PatchSearchMode;
import cadenza.preferences.PreferencesLoader;

import common.io.PropertiesFileReader;
import common.swing.BlockingTask;
import common.swing.CardPanel;
import common.swing.SwingUtils;
import common.swing.VerificationException;
import common.swing.dialog.OKCancelDialog;
import common.tuple.Pair;

@SuppressWarnings("serial")
public class PreferencesDialog extends OKCancelDialog {
  private static final Logger LOG = LogManager.getLogger(PreferencesDialog.class);
  
  private static final File PREFERENCES_FILE = new File("resources/preferences.txt");
  
  private Map<String, String> _preferences;
  
  private KeyboardEditPanel _defaultKeyboardPanel;
  private SynthConfigPanel _defaultSynthPanel;
  private DefaultMIDIPortsPanel _midiPortsPanel;
  private MIDIInputPrefPanel _midiInputPrefPanel;
  private PatchSearchOptionsPanel _patchSearchModePanel;

  public PreferencesDialog(Component parent) {
    super(parent);
  }

  @Override
  protected JComponent buildContent() {
    _defaultKeyboardPanel = new KeyboardEditPanel(new Keyboard(1));
    _defaultSynthPanel = new SynthConfigPanel(new ArrayList<Synthesizer>(0), null);
    _midiPortsPanel = new DefaultMIDIPortsPanel();
    _midiInputPrefPanel = new MIDIInputPrefPanel();
    _patchSearchModePanel = new PatchSearchOptionsPanel();
    
    loadPreferences();
    
    return new CardPanel(
        Arrays.asList(SwingUtils.hugNorth(_defaultKeyboardPanel), _defaultSynthPanel, _midiPortsPanel, _midiInputPrefPanel, _patchSearchModePanel),
        Arrays.asList("Default Keyboard", "Default Synthesizer", "Default MIDI Ports", "MIDI Input Preferences", "Patch Search Mode"));
  }
  
  private void loadPreferences() {
    new BlockingTask(this, () -> {
      _preferences = new LinkedHashMap<>();
      try {
        _preferences.putAll(PropertiesFileReader.readAll(PREFERENCES_FILE));
      } catch (Exception e) {
        LOG.warn("Exception while trying to read preferences file", e);
        return;
      }
      
      final Keyboard kbd = PreferencesLoader.buildDefaultKeyboard(_preferences);
      final Synthesizer synth = PreferencesLoader.buildDefaultSynthesizer(_preferences);
      final String[] midiPorts = PreferencesLoader.buildDefaultMIDIPorts(_preferences);
      final boolean[] inputPrefs = PreferencesLoader.buildMIDIInputOptions(_preferences);
      final Pair<PatchSearchMode, Boolean> patchSearchOptions = PreferencesLoader.buildPatchSearchOptions(_preferences);
      
      SwingUtils.doInSwing(() -> {
        _defaultKeyboardPanel.match(kbd);
        _defaultSynthPanel.match(synth);
        _midiPortsPanel.match(midiPorts);
        _midiInputPrefPanel.match(inputPrefs);
        _patchSearchModePanel.setSelectedOptions(patchSearchOptions);
      }, true);
    }).start();
  }
  
  public void commitPreferences() {
    new Thread(() -> {
      PreferencesLoader.commitDefaultKeyboard(_preferences, _defaultKeyboardPanel.buildKeyboard());
      PreferencesLoader.commitDefaultSynthesizer(_preferences, _defaultSynthPanel.getSynthesizer());
      PreferencesLoader.commitDefaultMIDIPorts(_preferences, _midiPortsPanel.getSelectedPorts());
      PreferencesLoader.commitInputOptions(_preferences, _midiInputPrefPanel.getSelectedOptions());
      PreferencesLoader.commitPatchSearchOptions(_preferences, _patchSearchModePanel.getSelectedOptions());
      
      try {
        PreferencesLoader.writePreferences(_preferences);
      } catch (IOException e) {
        LOG.warn("Exception trying to commit preferences", e);
      }
    }).start();
  }

  @Override
  protected String declareTitle() {
    return "Preferences";
  }

  @Override
  protected void verify() throws VerificationException {
    _defaultKeyboardPanel.verify();
    _defaultSynthPanel.verify();
  }
  
}
