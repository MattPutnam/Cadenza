package cadenza.gui.common;

import javax.swing.JButton;

import net.java.balloontip.BalloonTip;
import cadenza.gui.ImageStore;

@SuppressWarnings("serial")
public class HelpButton extends JButton {
  public HelpButton(final String message) {
    super(ImageStore.HELP);
    
    setBorder(null);
    addActionListener(e -> new BalloonTip(HelpButton.this, message));
  }
}
