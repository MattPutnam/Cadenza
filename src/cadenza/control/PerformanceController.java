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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cadenza.core.CadenzaData;
import cadenza.core.ControlMapEntry;
import cadenza.core.Cue;
import cadenza.core.Keyboard;
import cadenza.core.LocationNumber;
import cadenza.core.NoteRange;
import cadenza.core.Patch;
import cadenza.core.PatchAssignment;
import cadenza.core.PatchAssignment.Response;
import cadenza.core.Song;
import cadenza.core.Synthesizer;
import cadenza.core.effects.Effect;
import cadenza.core.patchusage.PatchUsage;
import cadenza.core.trigger.Trigger;
import cadenza.delegate.PatchChangeDelegate;
import cadenza.gui.CadenzaFrame;
import cadenza.gui.EffectMonitor;

import common.midi.MidiUtilities;
import common.tuple.Pair;

public final class PerformanceController extends CadenzaController {
  private static final Logger LOG = LogManager.getLogger(PerformanceController.class);
  
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
  
  /** The current list of global and cue-level effects */
  private List<Effect> _currentGlobalCueEffects;
  
  /** The current cue */
  private Cue _currentCue;
  
  /** The cue number */
  private int _position = -1;
  
  private boolean _shouldIgnoreOldPosition = false;
  
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
  public void notifyReceiver() {
    if (_currentCue != null) {
      try {
        updatePosition(-1, getData().cues.indexOf(_currentCue));
      } catch (InvalidMidiDataException e) {
        LOG.error("Error updating performance position", e);
      }
    }
  }
  
  public synchronized void updateKeyboardChannelMap() {
    _channelKeyboards = new HashMap<>();
    for (final Keyboard keyboard : getData().keyboards) {
      _channelKeyboards.put(Integer.valueOf(keyboard.channel), keyboard);
    }
  }
  
  public synchronized void goTo(Song song, LocationNumber measure) {
    final int oldIndex = _position;
    _position = Cue.findCueIndex(getData().cues, song, measure);
    
    if (receiverReady()) {
      try {
        updatePosition(oldIndex, _position);
      } catch (InvalidMidiDataException e) {
        LOG.error("Error updating performance position", e);
      }
    }
    updatePerformanceLocation();
  }
  
  public synchronized void goTo(Cue cue) {
    final int oldIndex = _position;
    _position = getData().cues.indexOf(cue);
    
    if (_position != oldIndex && receiverReady()) {
      try {
        updatePosition(oldIndex, _position);
        updatePerformanceLocation();
      } catch (InvalidMidiDataException e) {
        LOG.error("Error updating performance position", e);
      }
    }
  }
  
  public synchronized void goTo(int cueIndex) {
    if (cueIndex != _position) {
      final int oldIndex = _position;
      _position = cueIndex;
      
      if (receiverReady()) {
        try {
          updatePosition(oldIndex, _position);
        } catch (InvalidMidiDataException e) {
          LOG.error("Error updating performance position", e);
        }
      }
      updatePerformanceLocation();
    }
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
        LOG.error("Error updating performance position", e);
      }
    }
    
    updatePerformanceLocation();
  }
  
  private synchronized void sendCC(int cc, int value, int channel) {
    if (!receiverReady())
      return;
    
    final ShortMessage sm = new ShortMessage();
    try {
      sm.setMessage(ShortMessage.CONTROL_CHANGE, channel, cc, value);
      getReceiver().send(sm, -1);
    } catch (InvalidMidiDataException e) {
      LOG.error("Error sending CC value", e);
    }
  }
  
  private synchronized void sendCC(int cc, int value, PatchUsage patch) {
    sendCC(cc, value, _currentAssignments.get(patch).intValue());
  }
  
  private synchronized void sendNoteOn(int midiNumber, int velocity, int channel) {
    if (!receiverReady())
      return;
    
    final ShortMessage sm = new ShortMessage();
    try {
      sm.setMessage(ShortMessage.NOTE_ON, channel, midiNumber, velocity);
      getReceiver().send(sm, -1);
    } catch (InvalidMidiDataException e) {
      LOG.error("Error sending note on", e);
    }
  }
  
  public synchronized void sendNoteOn(int midiNumber, int velocity, PatchUsage patch) {
    sendNoteOn(midiNumber, velocity, _currentAssignments.get(patch).intValue());
  }
  
  private synchronized void sendNoteOff(int midiNumber, int channel) {
    if (!receiverReady())
      return;
    
    final ShortMessage sm = new ShortMessage();
    try {
      sm.setMessage(ShortMessage.NOTE_OFF, channel, midiNumber, 0);
      getReceiver().send(sm, -1);
    } catch (InvalidMidiDataException e) {
      LOG.error("Error sending note off", e);
    }
  }
  
  public synchronized void sendNoteOff(int midiNumber, PatchUsage patch) {
    // There is currently a bug where SequencerPatchUsage can get de-synced
    // and try to send a note off when the cue has been left.  This is due
    // to the metronome still triggering stuff somehow.  Catch this case and
    // just turn all notes off as a safety.
    final Integer channel = _currentAssignments.get(patch);
    if (channel == null)
      allNotesOff();
    else
      sendNoteOff(midiNumber, channel.intValue());
  }
  
  /**
   * Notifies the controller that next time it goes to update the location,
   * it should ignore the old cue and reload everything from scratch.  Call
   * this after mucking with the cue list.
   */
  public void clearOldCue() {
    _shouldIgnoreOldPosition = true;
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
  private synchronized void updatePosition(int oldPosition, int newPosition) throws InvalidMidiDataException {
    if (oldPosition == newPosition && !_shouldIgnoreOldPosition) {
      return;
    }
    
    final Cue oldCue = (_shouldIgnoreOldPosition || oldPosition == -1) ? null : getData().cues.get(oldPosition);
    _shouldIgnoreOldPosition = false;
    final Cue newCue = getData().cues.get(newPosition);
    
    if (oldCue != null) {
      oldCue.getAllAssignments().forEach(pa -> pa.cleanup(this));
    }
    
    final Map<PatchUsage, Integer> oldAssignments = _currentAssignments;
    final Map<PatchUsage, Integer> newAssignments = new HashMap<>();
    
    final List<PatchUsage> oldPatchUsages = oldCue == null ? Collections.emptyList()
                                                           : oldCue.getPatchUsages();
    final List<PatchUsage> newPatchUsages = newCue.getPatchUsages();
    
    final Map<Synthesizer, List<Integer>> availableChannels = new HashMap<>();
    for (final Synthesizer synth : getData().synthesizers) {
      // sort available channels by synth, and move currently assigned ones to
      // the back of the list, so they get used last:
      final List<Integer> synthChannels = new ArrayList<>(synth.getChannels());
      for (final Integer i : oldAssignments.values())
        if (synthChannels.remove(i))
          synthChannels.add(i);
      
      availableChannels.put(synth, synthChannels);
    }
    
    final List<PatchUsage> unassigned = new LinkedList<>();
    
    matchPatches:
    for (final PatchUsage newUsage : newPatchUsages) {
      final Patch patch = newUsage.patch;
      for (final PatchUsage oldUsage : oldPatchUsages) {
        if (patch == oldUsage.patch && oldAssignments.containsKey(oldUsage)) {
          final Integer channel = oldAssignments.get(oldUsage);
          LOG.info("Patch '" + patch.name + "' was already assigned, keeping on channel " + channel);
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
        LOG.warn("Not enough channels assigned to "
            + pu.patch.getSynthesizer().getName() + ", patch '" + pu.patch.name + "' not assigned.");
        continue;
      }
      
      final Integer channel = available.remove(0);
      LOG.info("Patch '" + pu.patch.name + "' assigned to channel " + channel);
      newAssignments.put(pu, channel);
      PatchChangeDelegate.performPatchChange(getReceiver(), pu.patch, channel.intValue());
    }
    
    newPatchUsages.forEach(pu -> sendCC(7, pu.volume, newAssignments.get(pu).intValue()));
    
    _currentAssignments = newAssignments;
    _currentCue = newCue;
    
    _currentCue.getAllAssignments().forEach(pa -> pa.prepare(this));
    
    _currentTriggers = new ArrayList<>();
    if (!newCue.disableGlobalTriggers)
      _currentTriggers.addAll(getData().globalTriggers);
    _currentTriggers.addAll(newCue.getTriggers());
    for (final Trigger t : _currentTriggers)
      t.reset();
    
    _currentGlobalCueEffects = new LinkedList<>();
    _currentGlobalCueEffects.addAll(_currentCue.effects);
    if (!_currentCue.disableGlobalEffects)
      _currentGlobalCueEffects.addAll(getData().globalEffects);
    EffectMonitor.getInstance().setEffects(_currentGlobalCueEffects);
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
    final int channel = sm.getChannel();
    
    // find input keyboard:
    final Keyboard keyboard = _channelKeyboards.get(Integer.valueOf(channel));
    if (keyboard == null)
      return;
    
    _currentTriggers.forEach(t -> t.receive(sm,  this));
    
    // send note info:
    noteorCC:
    if (MidiUtilities.isNoteOff(sm)) {
      final int midiNumber = sm.getData1();
      _currentCue.getAllAssignments().forEach(pu -> pu.noteReleased(midiNumber));
      
      final Pair<Keyboard, Integer> key = Pair.make(keyboard, Integer.valueOf(midiNumber));
      final Set<Pair<Integer, Integer>> notes = _currentNotes.get(key);
      if (notes != null) {
        for (final Pair<Integer, Integer> entry : notes) {
          final int outChannel = entry._1().intValue();
          final int outNumber = entry._2().intValue();

          sendNoteOff(outNumber, outChannel);
        }
        _currentNotes.remove(key);
      }
    } else if (MidiUtilities.isControlChange(sm)) {
      final int control = sm.getData1();
      final int value = sm.getData2();
      
      _currentCue.getAssignmentsByKeyboard(_channelKeyboards.get(Integer.valueOf(channel)))
                 .forEach(pa -> pa.controlChanged(control, value));
      
      if (control == 64) {
        // CC64 (damper) is speshul.  It needs to always be sent to all allocated channels.
        // HOTFIX: send this to ALL channels for now.
//        for (final Integer outChannel : _currentAssignments.values()) {
//          sendCC(64, value, outChannel.intValue());
//        }
        for (int i = 0; i < 16; ++i) {
          sendCC(64, value, i);
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
      
      for (final PatchAssignment assignment : _currentCue.patchAssignments) {
        final NoteRange noteRange = assignment.getNoteRange();
        if (noteRange.getKeyboard() == keyboard && noteRange.contains(inputMidiNumber)) {
          final Response response = assignment.receive(inputMidiNumber, inputVelocity);
          final PatchUsage pu = response.getPatchUsage();
          final Integer outputChannel = _currentAssignments.get(pu);
          if (outputChannel == null)
            System.err.println("Output channel not found for patch usage " + pu.toString(false, false, false) + " on cue " + _currentCue.toString());
          
          final List<Effect> effects = new LinkedList<>();
          effects.addAll(pu.effects);
          effects.addAll(_currentGlobalCueEffects);
          
          for (final int[] note : response.getNotes()) {
            final int midiNumber = note[0];
            int velocity = note[1];
            
            for (final Effect effect : effects)
              velocity = MidiUtilities.clamp(effect.process(midiNumber, velocity));
            
            sendNoteOn(midiNumber, velocity, outputChannel.intValue());
            noteEntry.add(Pair.make(outputChannel, Integer.valueOf(midiNumber)));
          }
        }
      }
      
      _currentNotes.put(Pair.make(keyboard, Integer.valueOf(inputMidiNumber)), noteEntry);
    } else {
      LOG.warn("Unknown MIDI message: " + MidiUtilities.toString(sm));
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
      LOG.warn("Error trying to restart");
    }
    updatePerformanceLocation();
  }
  
  public synchronized void allNotesOff() {
    for (int ch = 0; ch < 16; ++ch) {
      allNotesOff(ch);
    }
  }
  
  private synchronized void allNotesOff(int channel) {
    sendCC(120, 0, channel);
  }
}
