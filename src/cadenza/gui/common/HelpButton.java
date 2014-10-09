package cadenza.gui.common;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import net.java.balloontip.BalloonTip;
import cadenza.gui.ImageStore;

@SuppressWarnings("serial")
public class HelpButton extends JButton {
  public HelpButton(final String message) {
    super(ImageStore.HELP);
    
    setBorder(null);
    addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent _) {
        new BalloonTip(HelpButton.this, message);
      }
    });
  }
}
