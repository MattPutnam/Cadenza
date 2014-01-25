package cadenza.gui;

import com.apple.eawt.AppEvent.QuitEvent;
import com.apple.eawt.Application;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;
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

		app.setQuitHandler(new QuitHandler() {
			@Override
			public void handleQuitRequestWith(QuitEvent _, QuitResponse response) {
				response.performQuit();
			}
		});
	}
	
	@Override
	public void setupFrame(final CadenzaFrame frame) {
		final Application app = Application.getApplication();

		app.setQuitHandler(new QuitHandler() {
			@Override
			public void handleQuitRequestWith(QuitEvent _, QuitResponse response) {
				if (frame.isDirty() && !Dialog.confirm(frame, "Are you sure you want to quit? "
						+ "Unsaved changes will be lost."))
					response.cancelQuit();
				else
					response.performQuit();
			}
		});
	}
	
}
