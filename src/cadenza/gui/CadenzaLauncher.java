package cadenza.gui;

import it.sauronsoftware.junique.AlreadyLockedException;
import it.sauronsoftware.junique.JUnique;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.apache.commons.lang3.SystemUtils;

import com.apple.eawt.AboutHandler;
import com.apple.eawt.AppEvent.AboutEvent;
import com.apple.eawt.Application;

public class CadenzaLauncher {
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
		
		if (SystemUtils.IS_OS_MAC_OSX) {
			final Application app = Application.getApplication();
			
			app.setAboutHandler(new AboutHandler() {
				@Override
				public void handleAbout(AboutEvent _) {
					new AboutDialog();
				}
			});
			
			app.setPreferencesHandler(null);
		} else {
			// Let MacOS have its LAF, but force Windows/Linux to use Nimbus instead of their fugly shit
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
		}
		
		Cadenza.showHome();
	}
}
