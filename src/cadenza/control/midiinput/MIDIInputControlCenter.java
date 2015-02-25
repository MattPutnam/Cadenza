package cadenza.control.midiinput;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Stack;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;

import common.midi.MidiUtilities;

public class MIDIInputControlCenter {
  private static final MIDIInputControlCenter INSTANCE = new MIDIInputControlCenter();
  public static MIDIInputControlCenter getInstance() {
    return INSTANCE;
  }
  
  private Stack<AcceptsKeyboardInput> _stack;
  private volatile boolean _active;
  
  private MIDIInputControlCenter() {
    _stack = new Stack<>();
    _active = true;
  }
  
  public synchronized void setActive(boolean active) {
    _active = active;
  }
  
  public synchronized void grabFocus(AcceptsKeyboardInput component) {
    _stack.push(component);
  }
  
  public synchronized void relinquishFocus(AcceptsKeyboardInput component) {
    _stack.remove(component);
  }
  
  public synchronized void send(MidiMessage message) {
    if (!_stack.isEmpty()) {
      final AcceptsKeyboardInput comp = _stack.peek();
      if (_active && (message instanceof ShortMessage)) {
        final ShortMessage sm = (ShortMessage) message;
        if (MidiUtilities.isNoteOn(sm))
          comp.keyPressed(sm.getChannel(), sm.getData1(), sm.getData2());
        else if (MidiUtilities.isNoteOff(sm))
          comp.keyReleased(sm.getChannel(), sm.getData1());
        else if (MidiUtilities.isControlChange(sm))
          comp.controlReceived(sm.getChannel(), sm.getData1(), sm.getData2());
      }
    }
  }
  
  public static void installFocusGrabber(AcceptsKeyboardInput component) {
    if (component instanceof Component) {
      final Component comp = (Component) component;
      comp.addFocusListener(new FocusListener() {
        @Override
        public void focusGained(FocusEvent e) {
          MIDIInputControlCenter.getInstance().grabFocus(component);
        }
        
        @Override
        public void focusLost(FocusEvent e) {
          MIDIInputControlCenter.getInstance().relinquishFocus(component);
        }
      });
    } else {
      throw new IllegalArgumentException("Component must be a Component");
    }
  }
}
