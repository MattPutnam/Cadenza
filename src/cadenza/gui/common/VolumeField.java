package cadenza.gui.common;

import cadenza.control.midiinput.AcceptsKeyboardInput;

import common.swing.IntField;
import common.swing.SwingUtils;

@SuppressWarnings("serial")
public class VolumeField extends IntField implements AcceptsKeyboardInput {
  public VolumeField(int volume) {
    super(volume, 0, 127);
    setColumns(3);
    SwingUtils.freezeSize(this);
  }
  
  public void setVolume(int volume) {
    if (volume < 0 || volume > 127)
      throw new IllegalArgumentException("Volume must be 0-127");
    setInt(volume);
  }
  
  public int getVolume() {
    return getInt();
  }
  
  @Override
  public void controlReceived(int channel, int ccNumber, final int value) { 
    SwingUtils.doInSwing(new Runnable() {
      @Override
      public void run() {
        setInt(value);
        selectAll();
      }
    }, false);
  }
  
  @Override
  public void keyPressed(int channel, int midiNumber, int velocity) { /* ignore */ }
  @Override
  public void keyReleased(int channel, int midiNumber) { /* ignore */ }

}
