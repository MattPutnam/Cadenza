package cadenza.delegate;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import cadenza.core.Patch;
import cadenza.delegate.DelegateEntry.MessageType;
import cadenza.synths.GeneralMIDI;

import common.io.IOUtils;
import common.tuple.Pair;
import common.tuple.Triple;

public class PatchChangeDelegate {
	private static final String DELEGATE_DIR = "resources/delegates/";
	
	private static final Map<String, PatchChangeDelegate> MAP;
	static {
		MAP = new HashMap<>();
		
		final File dir = new File(DELEGATE_DIR);
		final File[] delegateFiles = dir.listFiles();
		for (final File delegateFile : delegateFiles) {
			String[] lines = null;
			try {
				lines = IOUtils.getLineArray(delegateFile);
			} catch (IOException e) {
				System.err.println("IOException while handling delegate file " +
						delegateFile.getName() + "\n" + e.getMessage());
				continue;
			}
				
			final String name = lines[0].trim();
			
			final List<DelegateEntry> entries = new ArrayList<>();
			for (int i = 1; i < lines.length; ++i) {
				final String trimmed = lines[i].trim();
				if (trimmed.isEmpty() || trimmed.startsWith("#"))
					continue;
				
				DelegateEntry entry = null;
				try {
					entry = DelegateEntryParser.parse(trimmed);
				} catch (ParseException e) {
					System.err.println(e.getMessage());
					continue;
				}
				
				entries.add(entry);
			}
			
			MAP.put(name, new PatchChangeDelegate(entries));
		}
	}
	
	public static PatchChangeDelegate getDelegate(String name) {
		return MAP.get(name);
	}
	
	public static void performPatchChange(Receiver receiver, Patch patch, int channel)
			throws InvalidMidiDataException {
		getDelegate(patch.getSynthesizer().getName()).sendPatchChange(receiver, patch, channel);
	}

	private final List<DelegateEntry> _entries;
	private PatchChangeDelegate(List<DelegateEntry> entries) {
		_entries = entries;
	}
	
	private void sendPatchChange(Receiver receiver, Patch patch, int channel) throws InvalidMidiDataException {
		final String bank = patch.bank;
		final int patchNum = patch.number;
		final ShortMessage msg = new ShortMessage();
		
		if (bank.equals("GM")) {
			msg.setMessage(ShortMessage.CONTROL_CHANGE, channel, 0, 121);
			receiver.send(msg, -1);
			
			msg.setMessage(ShortMessage.CONTROL_CHANGE, channel, 32, 0);
			receiver.send(msg, -1);
			
			msg.setMessage(ShortMessage.PROGRAM_CHANGE, channel, patchNum-1, 0);
			receiver.send(msg, -1);
		} else if (bank.equals("GM2")) {
			final Pair<Integer, Integer> GM2_PCNum_LSB = GeneralMIDI.getGM2_PCNum_LSB(patchNum);
			
			msg.setMessage(ShortMessage.CONTROL_CHANGE, channel, 0, 121);
			receiver.send(msg, -1);
			
			msg.setMessage(ShortMessage.CONTROL_CHANGE, channel, 32, GM2_PCNum_LSB._2());
			receiver.send(msg, -1);
			
			msg.setMessage(ShortMessage.PROGRAM_CHANGE, channel, GM2_PCNum_LSB._1()-1, 0);
			receiver.send(msg, -1);
		} else {
			DelegateEntry entry = null;
			for (final DelegateEntry e : _entries) {
				if (e.bankName.equals(bank) && e.minNum <= patchNum && patchNum <= e.maxNum) {
					entry = e;
					break;
				}
			}
			
			if (entry == null)
				throw new InvalidMidiDataException("Could not find delegate entry for patch " + patch.toString());
			
			for (final Triple<MessageType, Integer, Integer> command : entry.commands) {
				if (command._1() == MessageType.CONTROL_CHANGE) {
					msg.setMessage(ShortMessage.CONTROL_CHANGE, channel, command._2(), command._3());
					receiver.send(msg, -1);
				} else { // PC
					msg.setMessage(ShortMessage.PROGRAM_CHANGE, channel, patchNum - entry.minNum + command._3(), 0);
					receiver.send(msg, -1);
				}
			}
		}
	}
}
