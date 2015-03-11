package cadenza.control.midiinput;

import java.awt.AWTException;
import java.awt.Component;
import java.awt.Robot;
import java.awt.Window;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Stack;
import java.util.stream.IntStream;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cadenza.core.Note;

import com.sun.glass.events.KeyEvent;
import common.midi.MidiUtilities;

public class MIDIInputControlCenter {
  private static final Logger LOG = LogManager.getLogger(MIDIInputControlCenter.class);
  
  private static volatile MIDIInputControlCenter INSTANCE;
  public static MIDIInputControlCenter getInstance() {
    if (INSTANCE == null) {
      synchronized (MIDIInputControlCenter.class) {
        if (INSTANCE == null)
          INSTANCE = new MIDIInputControlCenter();
      }
    }
    return INSTANCE;
  }
  
  private Stack<AcceptsKeyboardInput> _stack;
  
  private MIDIInputControlCenter() {
    _stack = new Stack<>();
  }
  
  public boolean isActive() {
    return !_stack.isEmpty();
  }
  
  public void grabFocus(AcceptsKeyboardInput component) {
    synchronized (_stack) {
      if (_stack.contains(component) && _stack.peek() != component) {
        _stack.remove(component);
        _stack.push(component);
      } else {
        _stack.push(component);
      }
    }
  }
  
  public void relinquishFocus(AcceptsKeyboardInput component) {
    synchronized (_stack) {
      _stack.remove(component);
    }
  }
  
  public void send(MidiMessage message) {
    if (isActive()) {
      final AcceptsKeyboardInput comp = _stack.peek();
      if (message instanceof ShortMessage) {
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
          getInstance().grabFocus(component);
        }
        
        @Override
        public void focusLost(FocusEvent e) {
          getInstance().relinquishFocus(component);
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
        public void windowActivated(WindowEvent e) {
          getInstance().grabFocus(component);
        }
        
        @Override
        public void windowDeactivated(WindowEvent e) {
          getInstance().relinquishFocus(component);
        }
      });
    } else {
      throw new IllegalArgumentException("Component must be a Window");
    }
  }
  
  public static void installKeyboardArrowSpoofer(Component component) {
    final KeyboardArrowSpoofer spoofer = new KeyboardArrowSpoofer();
    component.addFocusListener(new FocusListener() {
      @Override
      public void focusGained(FocusEvent e) {
        getInstance().grabFocus(spoofer);
      }
      
      @Override
      public void focusLost(FocusEvent e) {
        getInstance().relinquishFocus(spoofer);
      }
    });
  }
  
  private static class KeyboardArrowSpoofer implements AcceptsKeyboardInput {
    private final Map<Integer, Integer> _lastReceivedCCValues;
    
    public KeyboardArrowSpoofer() {
      _lastReceivedCCValues = new IdentityHashMap<>();
      IntStream.rangeClosed(0, 127).boxed().forEach(i -> _lastReceivedCCValues.put(i, Integer.valueOf(0)));
    }
    
    @Override
    public void keyPressed(int channel, int midiNumber, int velocity) {
      Robot robot;
      try {
        robot = new Robot();
      } catch (AWTException e) {
        LOG.error("Unable to set up key spoofer:", e);
        return;
      }
      
      if (Note.valueOf(midiNumber).getPitchClass().isWhite())
        robot.keyPress(KeyEvent.VK_DOWN);
      else
        robot.keyPress(KeyEvent.VK_UP);
    }
    
    @Override
    public void controlReceived(int channel, int ccNumber, int value) {
      Robot robot;
      try {
        robot = new Robot();
      } catch (AWTException e) {
        LOG.error("Unable to set up key spoofer:", e);
        return;
      }
      
      final Integer key = Integer.valueOf(ccNumber);
      final int last = _lastReceivedCCValues.get(key).intValue();
      if (value > last)
        robot.keyPress(KeyEvent.VK_UP);
      else
        robot.keyPress(KeyEvent.VK_DOWN);
      _lastReceivedCCValues.put(key, Integer.valueOf(value));
    }
  }
}
