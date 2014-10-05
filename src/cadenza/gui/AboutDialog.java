package cadenza.gui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JDialog;
import javax.swing.JLabel;

import cadenza.Version;

@SuppressWarnings("serial")
public class AboutDialog extends JDialog {
	private static final String WEBSITE = "http://www.cadenzasoftware.com";
	
	private static final Font TITLE_FONT = Font.decode("Lucida bold 18");
	private static final Font NORMAL_FONT = Font.decode("Lucida 12");
	
	public AboutDialog() {
		final JLabel icon = new JLabel(ImageStore.APPLICATION_ICON, JLabel.CENTER);
		
		final JLabel title = new JLabel("Cadenza", JLabel.CENTER);
		title.setFont(TITLE_FONT);
		
		final JLabel version = new JLabel("Version " + Version.getVersion(), JLabel.CENTER);
		version.setFont(NORMAL_FONT);
		
		final JLabel URL = new JLabel("<html><a href='dummyval'>" + WEBSITE + "</a></html>");
		URL.setFont(NORMAL_FONT);
		URL.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		URL.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent _) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
          try {
            desktop.browse(new URI(WEBSITE));
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
		});
		
		final Box box = Box.createVerticalBox();
		box.add(icon);
		box.add(title);
		box.add(Box.createVerticalStrut(8));
		box.add(version);
		box.add(Box.createVerticalStrut(20));
		box.add(URL);
		
		box.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
		
		setLayout(new BorderLayout());
		add(box, BorderLayout.CENTER);
		
		pack();
		setLocationRelativeTo(null);
		setModal(true);
		setResizable(false);
		setVisible(true);
	}
}
