package cadenza.gui;

import java.awt.Component;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;

import cadenza.core.Keyboard;
import cadenza.gui.keyboard.KeyboardEditPanel;

import common.io.PropertiesFileReader;
import common.swing.BlockingTask;
import common.swing.CardPanel;
import common.swing.SwingUtils;
import common.swing.VerificationException;
import common.swing.dialog.OKCancelDialog;
import common.tuple.Pair;

@SuppressWarnings("serial")
public class PreferencesDialog extends OKCancelDialog {
  private static final File PREFERENCES_FILE = new File("resources/preferences.txt");
  
  private KeyboardEditPanel _keyboardEditPanel;

  public PreferencesDialog(Component parent) {
    super(parent);
  }

  @Override
  protected JComponent buildContent() {
    _keyboardEditPanel = new KeyboardEditPanel(new Keyboard(1));
    
    loadPreferences();
    
    return new CardPanel(Arrays.<Component>asList(_keyboardEditPanel), Arrays.asList("Keyboard"));
  }
  
  private void loadPreferences() {
    new BlockingTask(this, new Runnable() {
      @Override
      public void run() {
        final Map<String, String> prefMap = new HashMap<>();
        
        try (PropertiesFileReader reader = new PropertiesFileReader(PREFERENCES_FILE)) {
          while (reader.hasNext()) {
            final Pair<String, String> entry = reader.next();
            prefMap.put(entry._1().toLowerCase(), entry._2());
          }
        } catch (Exception e) {
          System.err.println("Exception while trying to read preferences file:");
          e.printStackTrace();
        }
        
        SwingUtils.doInSwing(new Runnable() {
          @Override
          public void run() {
            final String keyboardName = prefMap.get("keyboard.name");
            if (keyboardName != null)
              _keyboardEditPanel.setKeyboardName(keyboardName);
            
            final String keyboardChannel = prefMap.get("keyboard.channel");
            if (keyboardChannel != null)
              _keyboardEditPanel.setChannel(Integer.parseInt(keyboardChannel));
            
            final String fullRange = prefMap.get("keyboard.range");
            if (fullRange != null)
              _keyboardEditPanel.setRange(fullRange);
            
            final String soundingRange = prefMap.get("keyboard.soundingrange");
            if (soundingRange != null)
              _keyboardEditPanel.setSoundingRange(soundingRange);
          }
        }, true);
      }
    }).start();
  }
  
  public void commitPreferences() {
    new Thread(new Runnable() {
      @Override
      public void run() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PREFERENCES_FILE))) {
          final Keyboard keyboard = _keyboardEditPanel.getKeyboard();
          
          writer.write("keyboard.name = " + keyboard.name); writer.newLine();
          writer.write("keyboard.channel = " + keyboard.channel); writer.newLine();
          writer.write("keyboard.range = " + keyboard.low + "-" + keyboard.high); writer.newLine();
          writer.write("keyboard.soundingrange = " + keyboard.soundingLow + "-" + keyboard.soundingHigh); writer.newLine();
        } catch (IOException e) {
          System.err.println("Exception while trying to write preferences file:");
          e.printStackTrace();
        }
      }
    }).start();
  }

  @Override
  protected String declareTitle() {
    return "Preferences";
  }

  @Override
  protected void verify() throws VerificationException {
    // no-op
  }
  
}
