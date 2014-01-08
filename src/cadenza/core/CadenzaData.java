 package cadenza.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;

import cadenza.core.patchusage.PatchUsage;
import cadenza.core.plugins.Plugin;
import cadenza.core.sequencer.Sequencer;
import cadenza.core.trigger.Trigger;
import cadenza.gui.trigger.HasTriggers;

import common.collection.NotifyingList;

/**
 * All of the data that represents a Cadenza file.  Cadenza saves are
 * serializations of this class.
 * 
 * @author Matt Putnam
 */
public class CadenzaData implements Serializable, HasTriggers, ControlMapProvider {
	/** The synthesizers to be used */
	public NotifyingList<Synthesizer> synthesizers;
	
	/** The global triggers */
	public NotifyingList<Trigger> globalTriggers;
	
	/** The global control overrides */
	public NotifyingList<ControlMapEntry> globalControlMap;
	
	/** The global plugins */
	public NotifyingList<Plugin> globalPlugins;
	
	/** All of the patches used in this Cadenza file */
	public NotifyingList<Patch> patches;
	
	/** All of the cues used in the performance */
	public NotifyingList<Cue> cues;
	
	/** All of the keyboards used in the performance */
	public NotifyingList<Keyboard> keyboards;
	
	/** All of the songs used in this Cadenza file */
	public NotifyingList<Song> songs;
	
	/** All of the sequencers defined from SequencerPatchUsage */
	public NotifyingList<Sequencer> sequencers;
	
	/** The name of the input port, or null if not saved */
	public String savedInputDeviceName;
	
	/** The name of the output port, or null if not saved */
	public String savedOutputDeviceName;
	
	public CadenzaData() {
		synthesizers = new NotifyingList<>();
		globalTriggers = new NotifyingList<>();
		globalControlMap = new NotifyingList<>();
		globalPlugins = new NotifyingList<>();
		patches = new NotifyingList<>();
		cues = new NotifyingList<>();
		keyboards = new NotifyingList<>();
		songs = new NotifyingList<>();
		sequencers = new NotifyingList<>();
		savedInputDeviceName = null;
		savedOutputDeviceName = null;
	}
	
	public static void writeToXML(String filename, CadenzaData data) {
		FileOutputStream fout;
		try {
			fout = new FileOutputStream(new File(filename));
		} catch (FileNotFoundException e) {
			// shouldn't happen
			e.printStackTrace();
			return;
		}
		
		try (ObjectOutputStream oos = new ObjectOutputStream(fout)) {
			oos.writeObject(data);
		} catch (Exception e) {
			System.err.println("Exception while writing to file:");
			e.printStackTrace();
		}
	}
	
	public static CadenzaData readFromXML(File file) {
		FileInputStream fin;
		try {
			fin = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			// shouldn't happen
			e.printStackTrace();
			return null;
		}
		
		try (ObjectInputStream ois = new ObjectInputStream(fin)) {
			final CadenzaData data = (CadenzaData) ois.readObject();
			return data;
		} catch (Exception e) {
			System.err.println("Exception while reading from file:");
			e.printStackTrace();
			return null;
		}
	}

	// Compatibility:
	
	@Override
	public List<Trigger> getTriggers() {
		return globalTriggers;
	}

	@Override
	public List<ControlMapEntry> getControlMap() {
		return globalControlMap;
	}

	@Override
	public List<PatchUsage> getPatchUsages() {
		return null;
	}
}
