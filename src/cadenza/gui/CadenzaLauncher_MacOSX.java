package cadenza.gui;

import it.sauronsoftware.junique.AlreadyLockedException;
import it.sauronsoftware.junique.JUnique;

import java.awt.Component;

import cadenza.gui.preferences.PreferencesDialog;

import com.apple.eawt.Application;
import common.swing.dialog.OKCancelDialog;

public class CadenzaLauncher_MacOSX {
private static final String APP_ID = "cadenzasoftware.cadenza";
  
  public static void main(String[] args) throws Exception {
    System.setProperty("apple.laf.useScreenMenuBar", "true");
    System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Cadenza");
    
    try {
      JUnique.acquireLock(APP_ID);
    } catch (AlreadyLockedException e) {
      System.err.println("An instance of Cadenza is already running");
      System.exit(-1);
    }
    
    final Application app = Application.getApplication();
    
    app.setAboutHandler(e -> new AboutDialog());
    
    app.setPreferencesHandler(e -> {
    	final Component parent = (e.getSource() instanceof Component) ? (Component) e.getSource() : null;
    	OKCancelDialog.showDialog(new PreferencesDialog(parent), dialog -> dialog.commitPreferences());
    });
    
    Cadenza.setDelegate(new MacOSXDelegate());
    Cadenza.showHome();
  }
}
