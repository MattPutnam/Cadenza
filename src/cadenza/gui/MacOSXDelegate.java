package cadenza.gui;

import com.apple.eawt.Application;
import common.swing.dialog.Dialog;

/**
 * Delegate for Mac OS X
 * 
 * @author Matt Putnam
 */
public class MacOSXDelegate implements CadenzaDelegate {
  
  @Override
  public void doAfterShowHome() {
    final Application app = Application.getApplication();

    app.setQuitHandler((event, response) -> response.performQuit());
  }
  
  @Override
  public void setupFrame(final CadenzaFrame frame) {
    final Application app = Application.getApplication();

    app.setQuitHandler((event, response) -> {
      if (frame.isDirty() && !Dialog.confirm(frame, "Are you sure you want to quit? "
          + "Unsaved changes will be lost."))
        response.cancelQuit();
      else
        response.performQuit();
    });
  }
  
}
