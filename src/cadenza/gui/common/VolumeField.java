package cadenza.gui.common;

import cadenza.control.midiinput.AcceptsKeyboardInput;
import cadenza.control.midiinput.MIDIInputControlCenter;
import common.swing.IntField;
import common.swing.SwingUtils;

/**
 * A field for specifying a MIDI volume value [0-127].  Can accept MIDI input,
 * either strictly (only volume CC signals recognized (CC#7)), or leniently
 * (any CC value recognized).
 * 
 * @author Matt Putnam
 */
@SuppressWarnings("serial")
public class VolumeField extends IntField implements AcceptsKeyboardInput {
  private final boolean _lenient;
  
  /**
   * Creates a new VolumeField with the given value and strict input.
   * @param volume the starting volume
   */
  public VolumeField(int volume) {
    this (volume, false);
  }
  
  /**
   * Creates a new VolumeField with the given value and strictness
   * @param volume the starting volume
   * @param strict <tt>true</tt> to be strict, <tt>false</tt> to be lenient
   */
  public VolumeField(int volume, boolean strict) {
    super(volume, 0, 127);
    _lenient = !strict;
    
    setColumns(3);
    SwingUtils.freezeSize(this);
    
    MIDIInputControlCenter.installFocusGrabber(this);
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
    SwingUtils.doInSwing(() -> {
      if (_lenient || ccNumber == 7) {
        setInt(value);
        selectAll();
      }
    }, false);
  }
}
