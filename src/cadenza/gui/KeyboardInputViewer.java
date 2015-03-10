package cadenza.gui;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import cadenza.core.Keyboard;
import cadenza.core.Note;
import cadenza.gui.keyboard.KeyboardPanel;
import common.collection.ListAdapter;
import common.collection.ListEvent;
import common.collection.NotifyingList;
import common.midi.MidiUtilities;
import common.swing.SwingUtils;

@SuppressWarnings("serial")
public class KeyboardInputViewer extends JPanel {
  private final NotifyingList<Keyboard> _keyboards;
  private final List<KeyboardPanel> _keyboardPanels;
  
  private final Map<Integer, Integer> _channelToIndex;
  
  public KeyboardInputViewer(NotifyingList<Keyboard> keyboards) {
    _keyboards = keyboards;
    _keyboardPanels = new ArrayList<>(_keyboards.size());
    
    _channelToIndex = new IdentityHashMap<>();
    
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    
    _keyboards.addListener(new ListAdapter<Keyboard>() {
      @Override
      public void anyChange(ListEvent<Keyboard> event) {
        rebuildKeyboards();
      }
    });
    rebuildKeyboards();
  }
  
  private void rebuildKeyboards() {
    _channelToIndex.clear();
    _keyboardPanels.clear();
    removeAll();
    
    for (int i = 0; i < _keyboards.size(); ++i) {
      final Keyboard k = _keyboards.get(i);
      final KeyboardPanel kp = new KeyboardPanel(k);
      
      _channelToIndex.put(Integer.valueOf(k.channel), Integer.valueOf(i));
      _keyboardPanels.add(kp);
      
      add(SwingUtils.buildCenteredRow(new JLabel(k.name)));
      add(kp);
      add(Box.createVerticalStrut(8));
    }
  }
  
  public void receive(MidiMessage mm) {
    if (mm instanceof ShortMessage) {
      final ShortMessage sm = (ShortMessage) mm;
      final int index = _channelToIndex.get(Integer.valueOf(sm.getChannel())).intValue();
      final Note note = Note.valueOf(sm.getData1());
      if (MidiUtilities.isNoteOn(sm)) {
        _keyboardPanels.get(index).highlightNote(note);
      } else if (MidiUtilities.isNoteOff(sm)) {
        _keyboardPanels.get(index).unhighlightNote(note);
      }
    }
  }
}
