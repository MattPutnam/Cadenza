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
import javax.sound.midi.Receiver;
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
import cadenza.gui.PluginMonitor;
import cadenza.gui.control.CadenzaListener;

import common.Debug;
import common.Utils;
import common.midi.MidiUtilities;
import common.tuple.Pair;

public final class CadenzaController {
	public static enum Mode { PERFORM, PREVIEW }
	
	private final CadenzaData _data;
	private final List<CadenzaListener> _listeners;
	
	/** Receiver for sending processed MIDI outputs to */
	private Receiver _midiOut;

	/** Whether or not the MIDI output connection is valid */
	private boolean _valid;
	
	/** The mode we're in */
	private Mode _mode;
	
	///////////////////////////////////////////////////////////////////////////
	// Perform mode fields:
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
	// end Perform mode fields
	///////////////////////////////////////////////////////////////////////////
	// Preview mode fields
	/** The currently assigned preview patch */
	private List<Patch> _previewPatches = null;
	
	public CadenzaController(CadenzaData data) {
		_data = data;
		_listeners = new LinkedList<>();
		_valid = false;
		
		_currentAssignments = new HashMap<>();
		_currentNotes = new HashMap<>();
		
		updateKeyboardChannelMap();
	}
	
	public synchronized void setOutput(Receiver midiOut) {
		_midiOut = midiOut;
		
		final boolean oldValid = _valid;
		_valid = _midiOut != null;
		
		Debug.println("Output set to '" + midiOut.toString());
		
		if (_valid && !oldValid) {
			Debug.println("Output is valid, re-sending info");
			if (_mode == Mode.PERFORM && _currentCue != null) {
				Debug.println("In perform mode with a valid cue, sending program changes");
				try {
					updatePosition(-1, _data.cues.indexOf(_currentCue));
				} catch (InvalidMidiDataException e) {
					notifyListeners(e);
				}
			} else if (_mode == Mode.PREVIEW && _previewPatches != null) {
				Debug.println("In preview mode with valid patches, sending program change");
				try {
				  int i = 0;
				  for (Patch patch : _previewPatches)
				    PatchChangeDelegate.performPatchChange(_midiOut, patch, i++);
				} catch (InvalidMidiDataException e) {
					notifyListeners(e);
				}
			}
		}
	}
	
	public synchronized void setMode(Mode mode) {
		if (_mode != mode) {
			_mode = mode;
			Debug.println("Mode set to " + mode.name());
			for (final CadenzaListener listener: _listeners) {
				listener.updateMode(_mode);
			}
			
			PluginMonitor.getInstance().setPlugins(_mode == Mode.PERFORM ? _currentGlobalCuePlugins : null);
		}
	}
	
	public synchronized boolean isValid() {
		return _valid;
	}
	
	private void ensureMode(Mode target) {
		if (_mode != target) {
			throw new IllegalStateException("Expected mode " + target.name());
		}
	}
	
	public synchronized void updateKeyboardChannelMap() {
		_channelKeyboards = new HashMap<>();
		for (final Keyboard keyboard : _data.keyboards) {
			_channelKeyboards.put(Integer.valueOf(keyboard.channel), keyboard);
		}
		
		Debug.println("Keyboard->Channel map updated to " + _channelKeyboards);
	}
	
	public synchronized int getCurrentlyAssignedChannel(PatchUsage patch) {
		final Integer i = _currentAssignments.get(patch);
		if (i == null)
			return -1;
		else
			return i.intValue();
	}
	
	public synchronized void goTo(Song song, String measure) {
		ensureMode(Mode.PERFORM);
		
		final int oldIndex = _position;
		_position = Cue.findCueIndex(_data.cues, song, measure);
		
		if (_valid) {
			try {
				updatePosition(oldIndex, _position);
			} catch (InvalidMidiDataException e) {
				notifyListeners(e);
			}
		}
		updatePerformanceLocation();
	}
	
	public synchronized void goTo(Cue cue) {
		ensureMode(Mode.PERFORM);
		
		final int oldIndex = _position;
		_position = _data.cues.indexOf(cue);
		
		if (_valid) {
			try {
				updatePosition(oldIndex, _position);
			} catch (InvalidMidiDataException e) {
				notifyListeners(e);
			}
		}
		updatePerformanceLocation();
		
	}
	
	public synchronized void advance() {
		ensureMode(Mode.PERFORM);
		
		advance(true);
	}
	
	public synchronized void reverse() {
		ensureMode(Mode.PERFORM);
		
		advance(false);
	}
	
	private void advance(boolean forward) {
		final int oldPosition = _position;
		int newPosition = oldPosition + (forward ? 1 : -1);
		
		if (newPosition < 0)
			newPosition = 0;
		else if (newPosition > _data.cues.size()-1)
			newPosition = _data.cues.size()-1;
		
		_position = newPosition;
		
		if (_valid) {
			try {
				updatePosition(oldPosition, newPosition);
			} catch (InvalidMidiDataException e) {
				notifyListeners(e);
			}
		}
		
		updatePerformanceLocation();
	}
	
	public synchronized void sendCC(int cc, int value, int channel) {
		ensureMode(Mode.PERFORM);
		
		if (!_valid)
			return;
		
		final ShortMessage sm = new ShortMessage();
		try {
			sm.setMessage(ShortMessage.CONTROL_CHANGE, channel, cc, value);
			_midiOut.send(sm, -1);
		} catch (InvalidMidiDataException e) {
			notifyListeners(e);
		}
	}
	
	public synchronized void sendCC(int cc, int value, PatchUsage patch) {
		ensureMode(Mode.PERFORM);
		
		sendCC(cc, value, _currentAssignments.get(patch).intValue());
	}
	
	public synchronized void sendNoteOn(int midiNumber, int velocity, int channel) {
		ensureMode(Mode.PERFORM);
		
		if (!_valid)
			return;
		
		final ShortMessage sm = new ShortMessage();
		try {
			sm.setMessage(ShortMessage.NOTE_ON, channel, midiNumber, velocity);
			_midiOut.send(sm, -1);
		} catch (InvalidMidiDataException e) {
			notifyListeners(e);
		}
	}
	
	public synchronized void sendNoteOn(int midiNumber, int velocity, PatchUsage patch) {
		sendNoteOn(midiNumber, velocity, _currentAssignments.get(patch).intValue());
	}
	
	public synchronized void sendNoteOff(int midiNumber, int channel) {
		ensureMode(Mode.PERFORM);
		
		if (!_valid)
			return;
		
		final ShortMessage sm = new ShortMessage();
		try {
			sm.setMessage(ShortMessage.NOTE_OFF, channel, midiNumber, 0);
			_midiOut.send(sm, -1);
		} catch (InvalidMidiDataException e) {
			notifyListeners(e);
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
			Debug.println("Same position, not sending program changes");
			return;
		}
		
		for (final PatchUsage pu : _currentAssignments.keySet()) {
			pu.cleanup(this);
		}
		
		Debug.println("Sending program changes");
		
		final Cue oldCue = oldPosition == -1 ? null : _data.cues.get(oldPosition);
		final Cue newCue = _data.cues.get(newPosition);
		
		final Map<PatchUsage, Integer> oldAssignments = _currentAssignments;
		final Map<PatchUsage, Integer> newAssignments = new HashMap<>();
		
		final List<PatchUsage> oldPatchUsages = oldCue == null ? Collections.<PatchUsage>emptyList()
															   : oldCue.getPatchUsages();
		final List<PatchUsage> newPatchUsages = newCue.getPatchUsages();
		
		final Map<Synthesizer, List<Integer>> availableChannels = new HashMap<>();
		for (final Synthesizer synth : _data.synthesizers) {
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
				notifyListeners(new RuntimeException("Not enough channels assigned to "
						+ pu.patch.getSynthesizer().getName() + ", patch '" + pu.patch.name + "' not assigned."));
				continue;
			}
			
			final Integer channel = available.remove(0);
			Debug.println("Patch '" + pu.patch.name + "' assigned to channel " + channel);
			newAssignments.put(pu, channel);
			PatchChangeDelegate.performPatchChange(_midiOut, pu.patch, channel.intValue());
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
			_currentTriggers.addAll(_data.globalTriggers);
		_currentTriggers.addAll(newCue.getTriggers());
		for (final Trigger t : _currentTriggers)
			t.reset();
		
		_currentGlobalCuePlugins = new LinkedList<>();
		_currentGlobalCuePlugins.addAll(_currentCue.plugins);
		_currentGlobalCuePlugins.addAll(_data.globalPlugins);
		PluginMonitor.getInstance().setPlugins(_currentGlobalCuePlugins);
	}

	public synchronized void send(MidiMessage message) {
		if (!_valid || (_mode == Mode.PERFORM && _currentCue == null))
			return;
		
		if (!(message instanceof ShortMessage))
			return;
	
		if (_mode == Mode.PERFORM)
			send_perform((ShortMessage) message);
		else
			send_preview((ShortMessage) message);
	}
	
	private void send_perform(ShortMessage sm) {
		final int channel = sm.getChannel() + 1; // libraries use 0-indexing, this application uses 1-indexing
		
		Debug.println("ShortMessage received: " + MidiUtilities.toString(sm));
		
		// find input keyboard:
		final Keyboard keyboard = _channelKeyboards.get(Integer.valueOf(channel));
		if (keyboard == null)
			return;
		
		Debug.println("\tMessage came from keyboard: " + keyboard.name);
		
		for (final Trigger trigger : _currentTriggers) {
			trigger.receive(sm, this);
		}
		
		// send note info:
		noteorCC:
		if (MidiUtilities.isNoteOff(sm)) {
			final int midiNumber = sm.getData1();
			Debug.println("\tMessage is a note off, being sent for:");
			
			for (final PatchUsage pu : _currentCue.patches) {
				pu.noteReleased(midiNumber);
			}
			
			final Pair<Keyboard, Integer> key = Pair.make(keyboard, new Integer(midiNumber));
			final Set<Pair<Integer, Integer>> notes = _currentNotes.get(key);
			if (notes != null) {
				for (final Pair<Integer, Integer> entry : notes) {
					final int outChannel = entry._1().intValue();
					final int outNumber = entry._2().intValue();

					Debug.println("\t\tchannel=" + outChannel + " note=" + outNumber);

					sendNoteOff(outNumber, outChannel);
					_currentNotes.remove(key);
				}
			} else {
				Debug.println("\tNo current notes for note off");
			}
		} else if (MidiUtilities.isControlChange(sm)) {
			final int control = sm.getData1();
			final int value = sm.getData2();
			Debug.println("\tMessage is control #" + control + " value=" + value);
			
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
					Debug.println("\tCue set to map this to: " + entry.destCCs + " on " + entry.destPatches);
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
				for (final ControlMapEntry entry : _data.globalControlMap) {
					if (entry.sourceCC == control) {
						foundOverride = true;
						Debug.println("\tGlobally to map this to: " + Utils.mkString(entry.destCCs) + " on all channels");
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
				Debug.println("\tNo override, sending to all currently-assigned channels");
				for (final Integer outChannel : _currentAssignments.values()) {
					Debug.println("\tSending to channel " + outChannel);
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
					Debug.println("\tMessage will be sent for patch '" + patchUsage.patch.name + "' on channel " + outputChannel);
					
					final List<Plugin> puPlugins = new LinkedList<>();
					puPlugins.addAll(patchUsage.plugins);
					puPlugins.addAll(_currentGlobalCuePlugins);
					if (patchUsage.respondsTo(inputMidiNumber, inputVelocity)) {
						for (final int[] note : patchUsage.getNotes(inputMidiNumber, inputVelocity)) {
							int midiNumber = note[0];
							int velocity = note[1];
							
							Debug.println("\tPatch responds by playing midi#" + midiNumber + " velocity=" + velocity);
							
							for (final Plugin plugin : puPlugins) {
								velocity = MidiUtilities.clamp(plugin.process(midiNumber, velocity));
								Debug.println("\t\tPlugin " + plugin.toString() + " results in velocity of " + velocity);
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
	
	public synchronized void setPatches(List<Patch> patches) {
		ensureMode(Mode.PREVIEW);
		
		_previewPatches = patches;
		if (_valid) {
			try {
			  int i = 0;
			  for (final Patch patch : _previewPatches)
			    PatchChangeDelegate.performPatchChange(_midiOut, patch, i++);
				Debug.println("Preview Patch set to " + patches.toString());
				// don't notify, this method is called from a notify so we get an infinite loop
			} catch (InvalidMidiDataException e) {
				notifyListeners(e);
			}
		}
	}
	
	private void send_preview(ShortMessage sm) {
		try {
			final ShortMessage newSM = new ShortMessage();
			for (int i = 0; i < _previewPatches.size(); ++i) {
  			newSM.setMessage(sm.getCommand(), i, sm.getData1(), sm.getData2());
  			_midiOut.send(newSM, -1);
			}
			
			Debug.println("Preview MIDI message sent: " + MidiUtilities.toString(newSM));
		} catch (InvalidMidiDataException e) {
			notifyListeners(e);
		}
	}
	
	public synchronized void addCadenzaListener(CadenzaListener listener) {
		_listeners.add(listener);
		listener.updateMode(_mode);
		if (_mode == Mode.PERFORM) {
			listener.updatePerformanceLocation(_position);
		} else if (_mode == Mode.PREVIEW) {
			listener.updatePreviewPatches(_previewPatches);
		}
	}
	
	public synchronized void removeCadenzaListener(CadenzaListener listener) {
		_listeners.remove(listener);
	}
	
	private void updatePerformanceLocation() {
		for (final CadenzaListener listener : _listeners) {
			listener.updatePerformanceLocation(_position);
		}
	}
	
	private void notifyListeners(Exception e) {
		for (final CadenzaListener listener : _listeners) {
			listener.handleException(e);
		}
	}

	public synchronized void restart() {
		ensureMode(Mode.PERFORM);
		
		allNotesOff();
		_position = 0;
		try {
			updatePosition(-1, 0);
		} catch (InvalidMidiDataException e) {
			notifyListeners(e);
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
