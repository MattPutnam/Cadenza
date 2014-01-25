package cadenza.gui;

import it.sauronsoftware.junique.AlreadyLockedException;
import it.sauronsoftware.junique.JUnique;

import com.apple.eawt.AboutHandler;
import com.apple.eawt.AppEvent.AboutEvent;
import com.apple.eawt.Application;

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
		
		app.setAboutHandler(new AboutHandler() {
			@Override
			public void handleAbout(AboutEvent _) {
				new AboutDialog();
			}
		});
		
		app.setPreferencesHandler(null);
		
		Cadenza.setDelegate(new MacOSXDelegate());
		Cadenza.showHome();
	}
}
