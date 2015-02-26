package cadenza.control.midiinput;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Stack;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;

import common.midi.MidiUtilities;

public class MIDIInputControlCenter {  
  private static Stack<AcceptsKeyboardInput> _stack = new Stack<>();
  private static volatile boolean _active = true;
  
  public synchronized static void setActive(boolean active) {
    _active = active;
  }
  
  public synchronized static void grabFocus(AcceptsKeyboardInput component) {
    _stack.push(component);
  }
  
  public synchronized static void relinquishFocus(AcceptsKeyboardInput component) {
    _stack.remove(component);
  }
  
  public synchronized static void send(MidiMessage message) {
    if (_active && !_stack.isEmpty()) {
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
          MIDIInputControlCenter.grabFocus(component);
        }
        
        @Override
        public void focusLost(FocusEvent e) {
          MIDIInputControlCenter.relinquishFocus(component);
        }
      });
    } else {
      throw new IllegalArgumentException("Component must be a Component");
    }
  }
  
  public static void installWindowFocusGrabber(AcceptsKeyboardInput component) {
    if (component instanceof Window) {
      final Window window = (Window) component;
      window.addWindowListener(new WindowAdapter() {
        @Override
        public void windowOpened(WindowEvent e) {
          MIDIInputControlCenter.grabFocus(component);
        }
        
        @Override
        public void windowClosed(WindowEvent e) {
          MIDIInputControlCenter.relinquishFocus(component);
        }
      });
    } else {
      throw new IllegalArgumentException("Component must be a Window");
    }
  }
}
