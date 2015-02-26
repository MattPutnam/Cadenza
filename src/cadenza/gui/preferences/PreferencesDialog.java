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
import cadenza.preferences.Preferences;

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
  
  private KeyboardEditPanel _keyboardEditPanel;
  private SynthConfigPanel _synthConfigPanel;
  private DefaultMIDIPortsPanel _midiPortsPanel;
  private MIDIInputPrefPanel _midiInputPrefPanel;

  public PreferencesDialog(Component parent) {
    super(parent);
  }

  @Override
  protected JComponent buildContent() {
    _keyboardEditPanel = new KeyboardEditPanel(new Keyboard(1));
    _synthConfigPanel = new SynthConfigPanel(new ArrayList<Synthesizer>(0), null);
    _midiPortsPanel = new DefaultMIDIPortsPanel();
    _midiInputPrefPanel = new MIDIInputPrefPanel();
    
    loadPreferences();
    
    return new CardPanel(
        Arrays.asList(SwingUtils.hugNorth(_keyboardEditPanel), _synthConfigPanel, _midiPortsPanel, _midiInputPrefPanel),
        Arrays.asList("Default Keyboard", "Default Synthesizer", "Default MIDI Ports", "MIDI Input Preferences"));
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
      
      final Keyboard kbd = Preferences.buildDefaultKeyboard(_preferences);
      final Synthesizer synth = Preferences.buildDefaultSynthesizer(_preferences);
      final String[] midiPorts = Preferences.buildDefaultMIDIPorts(_preferences);
      final boolean[] inputPrefs = Preferences.buildMIDIInputOptions(_preferences);
      
      SwingUtils.doInSwing(() -> {
        _keyboardEditPanel.match(kbd);
        _synthConfigPanel.match(synth);
        _midiPortsPanel.match(midiPorts);
        _midiInputPrefPanel.match(inputPrefs);
      }, true);
    }).start();
  }
  
  public void commitPreferences() {
    new Thread(() -> {
      Preferences.commitDefaultKeyboard(_preferences, _keyboardEditPanel.getKeyboard());
      Preferences.commitDefaultSynthesizer(_preferences, _synthConfigPanel.getSynthesizer());
      Preferences.commitDefaultMIDIPorts(_preferences, _midiPortsPanel.getSelectedPorts());
      Preferences.commitInputOptions(_preferences, _midiInputPrefPanel.getSelectedOptions());
      
      try {
        Preferences.writePreferences(_preferences);
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
    _keyboardEditPanel.verify();
    _synthConfigPanel.verify();
  }
  
}
