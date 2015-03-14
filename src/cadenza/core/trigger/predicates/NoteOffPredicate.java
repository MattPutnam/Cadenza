package cadenza.core.trigger.predicates;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;

import cadenza.core.Location;

import common.midi.MidiUtilities;

public class NoteOffPredicate implements TriggerPredicate, HasLocation {
  private static final long serialVersionUID = 2L;
  
  private Location _location;

  public NoteOffPredicate(Location location) {
    _location = location;
  }

  @Override
  public boolean receive(MidiMessage message) {
    if (message instanceof ShortMessage) {
      final ShortMessage sm = (ShortMessage) message;
      if (!MidiUtilities.isNoteOff(sm))
        return false;
      
      final int channel = sm.getChannel();
      final int midiNumber = sm.getData1();
      
      return _location.getKeyboard().channel == channel && _location.contains(midiNumber);
    } else {
      return false;
    }
  }
  
  @Override
  public String toString() {
    return _location.toString() + " released";
  }
  
  @Override
  public Location getLocation() {
    return _location;
  }
  
  @Override
  public void setLocation(Location newLocation) {
    _location = newLocation;
  }
}