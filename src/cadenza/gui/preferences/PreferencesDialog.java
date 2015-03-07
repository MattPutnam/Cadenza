package cadenza.gui.preferences;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JComponent;

import cadenza.core.Keyboard;
import cadenza.core.Synthesizer;
import cadenza.gui.common.SynthConfigPanel;
import cadenza.gui.keyboard.KeyboardEditPanel;
import cadenza.preferences.PreferencesLoader;

import common.io.PropertiesFileReader;
import common.swing.BlockingTask;
import common.swing.CardPanel;
import common.swing.SwingUtils;
import common.swing.VerificationException;
import common.swing.dialog.OKCancelDialog;

@SuppressWarnings("serial")
public class PreferencesDialog extends OKCancelDialog {
  private static final File PREFERENCES_FILE = new File("resources/preferences.txt");
  
  private Map<String, String> _preferences;
  
  private KeyboardEditPanel _defaultKeyboardPanel;
  private SynthConfigPanel _defaultSynthPanel;
  private DefaultMIDIPortsPanel _midiPortsPanel;
  private MIDIInputPrefPanel _midiInputPrefPanel;
  private PatchSearchModePanel _patchSearchModePanel;

  public PreferencesDialog(Component parent) {
    super(parent);
  }

  @Override
  protected JComponent buildContent() {
    _defaultKeyboardPanel = new KeyboardEditPanel(new Keyboard(1));
    _defaultSynthPanel = new SynthConfigPanel(new ArrayList<Synthesizer>(0), null);
    _midiPortsPanel = new DefaultMIDIPortsPanel();
    _midiInputPrefPanel = new MIDIInputPrefPanel();
    _patchSearchModePanel = new PatchSearchModePanel();
    
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
        System.err.println("Exception while trying to read preferences file:");
        e.printStackTrace();
        return;
      }
      
      final Keyboard kbd = PreferencesLoader.buildDefaultKeyboard(_preferences);
      final Synthesizer synth = PreferencesLoader.buildDefaultSynthesizer(_preferences);
      final String[] midiPorts = PreferencesLoader.buildDefaultMIDIPorts(_preferences);
      final boolean[] inputPrefs = PreferencesLoader.buildMIDIInputOptions(_preferences);
      final int patchSearchMode = PreferencesLoader.buildPatchSearchMode(_preferences);
      
      SwingUtils.doInSwing(() -> {
        _defaultKeyboardPanel.match(kbd);
        _defaultSynthPanel.match(synth);
        _midiPortsPanel.match(midiPorts);
        _midiInputPrefPanel.match(inputPrefs);
        _patchSearchModePanel.setSelectedOption(patchSearchMode);
      }, true);
    }).start();
  }
  
  public void commitPreferences() {
    new Thread(() -> {
      PreferencesLoader.commitDefaultKeyboard(_preferences, _defaultKeyboardPanel.getKeyboard());
      PreferencesLoader.commitDefaultSynthesizer(_preferences, _defaultSynthPanel.getSynthesizer());
      PreferencesLoader.commitDefaultMIDIPorts(_preferences, _midiPortsPanel.getSelectedPorts());
      PreferencesLoader.commitInputOptions(_preferences, _midiInputPrefPanel.getSelectedOptions());
      PreferencesLoader.commitPatchSearchMode(_preferences, _patchSearchModePanel.getSelectedOption());
      
      try {
        PreferencesLoader.writePreferences(_preferences);
      } catch (IOException e) {
        System.err.println("Exception trying to commit preferences:");
        e.printStackTrace();
        // TODO: better error reporting
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
