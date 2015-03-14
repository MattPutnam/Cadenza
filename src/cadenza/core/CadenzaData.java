 package cadenza.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cadenza.Version;
import cadenza.core.effects.Effect;
import cadenza.core.patchusage.PatchUsage;
import cadenza.core.sequencer.Sequencer;
import cadenza.core.trigger.Trigger;
import cadenza.gui.trigger.HasTriggers;

import common.collection.NotifyingList;
import common.swing.dialog.Dialog;

/**
 * All of the data that represents a Cadenza file.  Cadenza saves are
 * serializations of this class.
 * 
 * @author Matt Putnam
 */
public class CadenzaData implements Serializable, HasTriggers, ControlMapProvider {
  private static final Logger LOG = LogManager.getLogger(CadenzaData.class);
  
  private static final long serialVersionUID = 2L;
  
  /** The synthesizers to be used */
  public NotifyingList<Synthesizer> synthesizers;
  
  /** The global triggers */
  public NotifyingList<Trigger> globalTriggers;
  
  /** The global control overrides */
  public NotifyingList<ControlMapEntry> globalControlMap;
  
  /** The global effects */
  public NotifyingList<Effect> globalEffects;
  
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
    globalEffects = new NotifyingList<>();
    patches = new NotifyingList<>();
    cues = new NotifyingList<>();
    keyboards = new NotifyingList<>();
    songs = new NotifyingList<>();
    sequencers = new NotifyingList<>();
    savedInputDeviceName = null;
    savedOutputDeviceName = null;
  }
  
  public static void writeToFile(String filename, CadenzaData data) {
    FileOutputStream fout;
    try {
      fout = new FileOutputStream(new File(filename));
    } catch (FileNotFoundException e) {
      // shouldn't happen
      LOG.fatal("Error writing to file", e);
      return;
    }
    
    try (ObjectOutputStream oos = new ObjectOutputStream(fout)) {
      oos.writeUTF(Version.getVersion());
      oos.writeObject(data);
    } catch (Exception e) {
      LOG.fatal("Exception while writing to file:", e);
    }
    
    try {
      fout.close();
    } catch (IOException e) {
      LOG.fatal("Error writing to file", e);
    }
  }
  
  public static CadenzaData readFromFile(File file) throws Exception {
    FileInputStream fin;
    try {
      fin = new FileInputStream(file);
    } catch (FileNotFoundException e) {
      // shouldn't happen
      LOG.fatal("Error reading from file", e);
      return null;
    }
    
    try (ObjectInputStream ois = new ObjectInputStream(fin)) {
      final String thatVersion = ois.readUTF();
      final String thisVersion = Version.getVersion();
      if (!thisVersion.equals(thatVersion)) {
        Dialog.error(null, "This file was created in version " + thatVersion +
            ", which is incompatible with this version (" + thisVersion +
            ").  To open this file, go to http://www.cadenzasoftware.com and download version "
            + thatVersion + ".");
        throw new RuntimeException("Version mismatch: this is " + thisVersion + ", other is " + thatVersion);
      }
      
      final CadenzaData data = (CadenzaData) ois.readObject();
      return data;
    } catch (Exception e) {
      LOG.fatal("Exception while reading from file:", e);
      throw e;
    } finally {
      try {
        fin.close();
      } catch (IOException e) {
        LOG.fatal("Error reading from file", e);
      }
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
