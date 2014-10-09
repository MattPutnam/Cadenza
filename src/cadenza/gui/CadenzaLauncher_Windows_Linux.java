package cadenza.gui;

import it.sauronsoftware.junique.AlreadyLockedException;
import it.sauronsoftware.junique.JUnique;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

public class CadenzaLauncher_Windows_Linux {
  private static final String APP_ID = "cadenzasoftware.cadenza";
  
  public static void main(String[] args) throws Exception {
    try {
      JUnique.acquireLock(APP_ID);
    } catch (AlreadyLockedException e) {
      System.err.println("An instance of Cadenza is already running");
      System.exit(-1);
    }
    
    // force Windows/Linux to use Nimbus instead of their fugly shit
    try {
      for (final LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
        if ("Nimbus".equals(info.getName())) {
          UIManager.setLookAndFeel(info.getClassName());
          break;
        }
      }
    } catch (Exception e) {
      System.err.println("Failed to set LAF, Nimbus not installed");
    }
    
    Cadenza.showHome();
  }
}
