package cadenza.gui.common;

import java.awt.Cursor;

import javax.swing.JButton;

import net.java.balloontip.BalloonTip;
import cadenza.gui.ImageStore;

@SuppressWarnings("serial")
public class HelpButton extends JButton {
  public HelpButton(final String message) {
    super(ImageStore.HELP);
    
    setBorder(null);
    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    addActionListener(e -> new BalloonTip(HelpButton.this, message));
  }
}
