package cadenza.control;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;

import cadenza.core.CadenzaData;
import cadenza.core.ControlMapEntry;
import cadenza.core.Cue;
import cadenza.core.Keyboard;
import cadenza.core.Patch;
import cadenza.core.Song;
import cadenza.core.Synthesizer;
import cadenza.core.patchusage.PatchUsage;
import cadenza.core.plugins.Plugin;
import cadenza.core.trigger.Trigger;
import cadenza.delegate.PatchChangeDelegate;
import cadenza.gui.CadenzaFrame;
import cadenza.gui.PluginMonitor;

import common.Debug;
import common.midi.MidiUtilities;
import common.tuple.Pair;

public final class PerformanceController extends CadenzaController {
  private final CadenzaFrame _cadenzaFrame;
  
  /**
   *  Maps channel numbers to the keyboards they originate from<br>
   *  Derivative of {@link CadenzaData#keyboardInputChannels}
   */
  private Map<Integer, Keyboard> _channelKeyboards;
  
  /**
   * Stores currently pressed notes.  Format:<br>
   * (Keyboard, MidiNumber) -&gt; {(MidiChannel, MidiNumber)}+
   */
  private Map<Pair<Keyboard, Integer>, Set<Pair<Integer, Integer>>> _currentNotes;
  
  /** The current assignments of patch usages to their output channels */
  private Map<PatchUsage, Integer> _currentAssignments;
  
  /** The current list of triggers, which is the globals plus cue-specific */
  private List<Trigger> _currentTriggers;
  
  /** The current list of global and cue-level plugins */
  private List<Plugin> _currentGlobalCuePlugins;
  
  /** The current cue, in perform mode */
  private Cue _currentCue;
  
  /** The cue number, in perform mode */
  private int _position = -1;
  
  public PerformanceController(CadenzaData data, CadenzaFrame cadenzaFrame) {
    super(data);
    _cadenzaFrame = cadenzaFrame;
    
    _currentAssignments = new HashMap<>();
    _currentNotes = new HashMap<>();
    
    updateKeyboardChannelMap();
  }
  
  public int getCurrentCueIndex() {
    return _position;
  }
  
  @Override
  protected void notifyReceiver() {
    if (_currentCue != null) {
      try {
        updatePosition(-1, getData().cues.indexOf(_currentCue));
      } catch (InvalidMidiDataException e) {
        e.printStackTrace();
      }
    }
  }
  
  public synchronized void updateKeyboardChannelMap() {
    _channelKeyboards = new HashMap<>();
    for (final Keyboard keyboard : getData().keyboards) {
      _channelKeyboards.put(Integer.valueOf(keyboard.channel), keyboard);
    }
  }
  
  public synchronized int getCurrentlyAssignedChannel(PatchUsage patch) {
    final Integer i = _currentAssignments.get(patch);
    if (i == null)
      return -1;
    else
      return i.intValue();
  }
  
  public synchronized void goTo(Song song, String measure) {
    final int oldIndex = _position;
    _position = Cue.findCueIndex(getData().cues, song, measure);
    
    if (receiverReady()) {
      try {
        updatePosition(oldIndex, _position);
      } catch (InvalidMidiDataException e) {
        e.printStackTrace();
      }
    }
    updatePerformanceLocation();
  }
  
  public synchronized void goTo(Cue cue) {
    final int oldIndex = _position;
    _position = getData().cues.indexOf(cue);
    
    if (receiverReady()) {
      try {
        updatePosition(oldIndex, _position);
      } catch (InvalidMidiDataException e) {
        e.printStackTrace();
      }
    }
    updatePerformanceLocation();
    
  }
  
  public synchronized void advance() {
    advance(true);
  }
  
  public synchronized void reverse() {
    advance(false);
  }
  
  private void advance(boolean forward) {
    final int oldPosition = _position;
    int newPosition = oldPosition + (forward ? 1 : -1);
    
    if (newPosition < 0)
      newPosition = 0;
    else if (newPosition > getData().cues.size()-1)
      newPosition = getData().cues.size()-1;
    
    _position = newPosition;
    
    if (receiverReady()) {
      try {
        updatePosition(oldPosition, newPosition);
      } catch (InvalidMidiDataException e) {
        e.printStackTrace();
      }
    }
    
    updatePerformanceLocation();
  }
  
  public synchronized void sendCC(int cc, int value, int channel) {
    if (!receiverReady())
      return;
    
    final ShortMessage sm = new ShortMessage();
    try {
      sm.setMessage(ShortMessage.CONTROL_CHANGE, channel, cc, value);
      getReceiver().send(sm, -1);
    } catch (InvalidMidiDataException e) {
      e.printStackTrace();
    }
  }
  
  public synchronized void sendCC(int cc, int value, PatchUsage patch) {
    sendCC(cc, value, _currentAssignments.get(patch).intValue());
  }
  
  public synchronized void sendNoteOn(int midiNumber, int velocity, int channel) {
    if (!receiverReady())
      return;
    
    final ShortMessage sm = new ShortMessage();
    try {
      sm.setMessage(ShortMessage.NOTE_ON, channel, midiNumber, velocity);
      getReceiver().send(sm, -1);
    } catch (InvalidMidiDataException e) {
      e.printStackTrace();
    }
  }
  
  public synchronized void sendNoteOn(int midiNumber, int velocity, PatchUsage patch) {
    sendNoteOn(midiNumber, velocity, _currentAssignments.get(patch).intValue());
  }
  
  public synchronized void sendNoteOff(int midiNumber, int channel) {
    if (!receiverReady())
      return;
    
    final ShortMessage sm = new ShortMessage();
    try {
      sm.setMessage(ShortMessage.NOTE_OFF, channel, midiNumber, 0);
      getReceiver().send(sm, -1);
    } catch (InvalidMidiDataException e) {
      e.printStackTrace();
    }
  }
  
  public synchronized void sendNoteOff(int midiNumber, PatchUsage patch) {
    sendNoteOff(midiNumber, _currentAssignments.get(patch).intValue());
  }
  
  /**
   * Send the necessary patch change events for changing from one cue to another.
   * If a patch from the old cue is reused in the new cue on the same keyboard, then it gets assigned
   * the same channel and no patch change is sent.  If there are more available channels than are needed,
   * new ones are used as much as possible to avoid sending patch changes on in-use channels
   * @param oldPosition - the index of the old cue, or -1 during initialization
   * @param newPosition - the index of the new cue
   * @throws InvalidMidiDataException if any exception occurs setting messages (shouldn't happen)
   */
  private void updatePosition(int oldPosition, int newPosition) throws InvalidMidiDataException {
    if (oldPosition == newPosition) {
      return;
    }
    
    for (final PatchUsage pu : _currentAssignments.keySet()) {
      pu.cleanup(this);
    }
    
    final Cue oldCue = oldPosition == -1 ? null : getData().cues.get(oldPosition);
    final Cue newCue = getData().cues.get(newPosition);
    
    final Map<PatchUsage, Integer> oldAssignments = _currentAssignments;
    final Map<PatchUsage, Integer> newAssignments = new HashMap<>();
    
    final List<PatchUsage> oldPatchUsages = oldCue == null ? Collections.<PatchUsage>emptyList()
                                 : oldCue.getPatchUsages();
    final List<PatchUsage> newPatchUsages = newCue.getPatchUsages();
    
    final Map<Synthesizer, List<Integer>> availableChannels = new HashMap<>();
    for (final Synthesizer synth : getData().synthesizers) {
      availableChannels.put(synth, new ArrayList<>(synth.getChannels()));
    }
    
    final List<PatchUsage> unassigned = new LinkedList<>();
    
    matchPatches:
    for (final PatchUsage newUsage : newPatchUsages) {
      final Patch patch = newUsage.patch;
      for (final PatchUsage oldUsage : oldPatchUsages) {
        if (patch == oldUsage.patch && oldAssignments.containsKey(oldUsage)) {
          final Integer channel = oldAssignments.get(oldUsage);
          Debug.println("Patch '" + patch.name + "' was already assigned, keeping on channel " + channel);
          newAssignments.put(newUsage, channel);
          availableChannels.get(patch.getSynthesizer()).remove(channel);
          continue matchPatches;
        }
      }
      
      // none found, reassign
      unassigned.add(newUsage);
    }
    
    for (final PatchUsage pu : unassigned) {
      final List<Integer> available = availableChannels.get(pu.patch.getSynthesizer());
      if (available.isEmpty()) {
        // TODO notify of error
        System.err.println("Not enough channels assigned to "
            + pu.patch.getSynthesizer().getName() + ", patch '" + pu.patch.name + "' not assigned.");
        continue;
      }
      
      final Integer channel = available.remove(0);
      Debug.println("Patch '" + pu.patch.name + "' assigned to channel " + channel);
      newAssignments.put(pu, channel);
      PatchChangeDelegate.performPatchChange(getReceiver(), pu.patch, channel.intValue());
    }
    
    for (final PatchUsage pu : newPatchUsages) {
      sendCC(7, pu.volume, newAssignments.get(pu).intValue());
    }
    
    _currentAssignments = newAssignments;
    _currentCue = newCue;
    
    for (final PatchUsage pu : _currentAssignments.keySet()) {
      pu.prepare(this);
    }
    
    _currentTriggers = new ArrayList<>();
    if (!newCue.disableGlobalTriggers)
      _currentTriggers.addAll(getData().globalTriggers);
    _currentTriggers.addAll(newCue.getTriggers());
    for (final Trigger t : _currentTriggers)
      t.reset();
    
    _currentGlobalCuePlugins = new LinkedList<>();
    _currentGlobalCuePlugins.addAll(_currentCue.plugins);
    _currentGlobalCuePlugins.addAll(getData().globalPlugins);
    PluginMonitor.getInstance().setPlugins(_currentGlobalCuePlugins);
  }

  @Override
  public synchronized void send(MidiMessage message) {
    if (!receiverReady() || _currentCue == null)
      return;
    
    if (!(message instanceof ShortMessage))
      return;
  
    send_perform((ShortMessage) message);
  }
  
  private void send_perform(ShortMessage sm) {
    final int channel = sm.getChannel() + 1; // libraries use 0-indexing, this application uses 1-indexing
    
    // find input keyboard:
    final Keyboard keyboard = _channelKeyboards.get(Integer.valueOf(channel));
    if (keyboard == null)
      return;
    
    for (final Trigger trigger : _currentTriggers) {
      trigger.receive(sm, this);
    }
    
    // send note info:
    noteorCC:
    if (MidiUtilities.isNoteOff(sm)) {
      final int midiNumber = sm.getData1();
      for (final PatchUsage pu : _currentCue.patches) {
        pu.noteReleased(midiNumber);
      }
      
      final Pair<Keyboard, Integer> key = Pair.make(keyboard, new Integer(midiNumber));
      final Set<Pair<Integer, Integer>> notes = _currentNotes.get(key);
      if (notes != null) {
        for (final Pair<Integer, Integer> entry : notes) {
          final int outChannel = entry._1().intValue();
          final int outNumber = entry._2().intValue();

          sendNoteOff(outNumber, outChannel);
          _currentNotes.remove(key);
        }
      }
    } else if (MidiUtilities.isControlChange(sm)) {
      final int control = sm.getData1();
      final int value = sm.getData2();
      
      if (control == 64) {
        // CC64 (damper) is speshul.  It needs to always be sent to all allocated channels.
        for (final Integer outChannel : _currentAssignments.values()) {
          sendCC(64, value, outChannel.intValue());
        }
        break noteorCC;
      }
      
      boolean foundOverride = false;
      
      // check cue overrides:
      for (final ControlMapEntry entry : _currentCue.getControlMap()) {
        if (entry.sourceCC == control) {
          foundOverride = true;
          for (final PatchUsage outPU : entry.destPatches) {
            for (final Integer outControl : entry.destCCs) {
              sendCC(outControl.intValue(), value, outPU);
            }
          }
          
          break;
        }
      }
      
      // check global overrides if no cue override found:
      if (!foundOverride && !_currentCue.disableGlobalControlMap) {
        for (final ControlMapEntry entry : getData().globalControlMap) {
          if (entry.sourceCC == control) {
            foundOverride = true;
            for (final Integer outControl : entry.destCCs) {
              for (final Integer outChannel : _currentAssignments.values()) {
                sendCC(outControl.intValue(), value, outChannel.intValue());
              }
            }
            break;
          }
        }
      }
      
      // if STILL nothing hit, then send it to all channels:
      if (!foundOverride) {
        for (final Integer outChannel : _currentAssignments.values()) {
          sendCC(control, value, outChannel.intValue());
        }
      }
    } else if (MidiUtilities.isNoteOn(sm)) {
      final int inputMidiNumber = sm.getData1();
      final int inputVelocity = sm.getData2();
      final Set<Pair<Integer, Integer>> noteEntry = new HashSet<>();
      
      for (final PatchUsage patchUsage : _currentCue.patches) {
        if (patchUsage.location.getKeyboard() == keyboard && patchUsage.location.contains(inputMidiNumber)) {
          final Integer outputChannel = _currentAssignments.get(patchUsage);
          
          final List<Plugin> puPlugins = new LinkedList<>();
          puPlugins.addAll(patchUsage.plugins);
          puPlugins.addAll(_currentGlobalCuePlugins);
          if (patchUsage.respondsTo(inputMidiNumber, inputVelocity)) {
            for (final int[] note : patchUsage.getNotes(inputMidiNumber, inputVelocity)) {
              int midiNumber = note[0];
              int velocity = note[1];
              
              for (final Plugin plugin : puPlugins) {
                velocity = MidiUtilities.clamp(plugin.process(midiNumber, velocity));
              }
              
              sendNoteOn(midiNumber, velocity, outputChannel.intValue());
              noteEntry.add(Pair.make(outputChannel, Integer.valueOf(midiNumber)));
            }
          }
        }
      }
      
      _currentNotes.put(Pair.make(keyboard, Integer.valueOf(inputMidiNumber)), noteEntry);
    } else {
      Debug.println("Unknown MIDI message: " + MidiUtilities.toString(sm));
    }
  }
  
  private void updatePerformanceLocation() {
    _cadenzaFrame.notifyPerformLocationChanged(_position, true);
  }

  public synchronized void restart() {
    allNotesOff();
    _position = 0;
    try {
      updatePosition(-1, 0);
    } catch (InvalidMidiDataException e) {
      e.printStackTrace();
    }
    updatePerformanceLocation();
  }
  
  public synchronized void allNotesOff() {
    for (int ch = 0; ch < 16; ++ch) {
      allNotesOff(ch);
    }
  }
  
  public synchronized void allNotesOff(int channel) {
    sendCC(120, 0, channel);
  }
}
