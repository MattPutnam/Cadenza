package cadenza.core;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cadenza.core.effects.Effect;
import cadenza.core.patchusage.PatchUsage;
import cadenza.core.trigger.Trigger;
import cadenza.gui.trigger.HasTriggers;

import common.Utils;

/**
 * A cue, as represented in Cadenza.  Cues consist of the song/measure, and
 * the list of patch and trigger usages used.
 * 
 * @author Matt Putnam
 */
public class Cue implements Comparable<Cue>, Serializable, ControlMapProvider, HasTriggers {
  private static final long serialVersionUID = 2L;
  
  /** The song number */
  public Song song;
  
  /** The measure number */
  public LocationNumber measureNumber;
  
  /** The patches used */
  public List<PatchUsage> patches;
  
  /** The triggers used */
  public List<Trigger> triggers;
  
  /** Whether or not to disable global triggers for this cue */
  public boolean disableGlobalTriggers = false;
  
  /** Custom control message mapping */
  private List<ControlMapEntry> _controlMapping;
  
  /** Whether or not to disable the global control map for this cue */
  public boolean disableGlobalControlMap = false;
  
  /** The effects used */
  public List<Effect> effects;
  
  public boolean disableGlobalEffects = false;
  
  /**
   * Creates a new Cue for the given song and measure.  The list of patches and triggers is initially empty
   * @param songNumber - the song number
   * @param measureNumber - the measure number
   */
  public Cue(Song song, LocationNumber measureNumber) {
    this.song = song;
    this.measureNumber = measureNumber;
    patches = new ArrayList<>();
    triggers = new ArrayList<>();
    _controlMapping = new ArrayList<>();
    effects = new ArrayList<>();
  }
  
  /**
   * Set this Cue's values to be the same as the given cue.
   * @param other - the other Cue to copy
   */
  public void copyFrom(Cue other) {
    this.song = other.song;
    this.measureNumber = other.measureNumber;
    this.patches = other.patches;
    this.triggers = other.triggers;
    this.disableGlobalTriggers = other.disableGlobalTriggers;
    this._controlMapping = other._controlMapping;
    this.effects = other.effects;
  }
  
  public List<PatchUsage> getPatchUsagesByKeyboard(Keyboard keyboard) {
    final List<PatchUsage> result = new LinkedList<>();
    for (final PatchUsage patchUsage : patches) {
      if (patchUsage.location.getKeyboard() == keyboard)
        result.add(patchUsage);
    }
    return result;
  }
  
  public Map<Keyboard, List<PatchUsage>> getPatchUsagesByKeyboard(List<Keyboard> keyboards) {
    final Map<Keyboard, List<PatchUsage>> result = new LinkedHashMap<>();
    for (final Keyboard keyboard : keyboards) {
      final List<PatchUsage> patchUsages = getPatchUsagesByKeyboard(keyboard);
      if (!patchUsages.isEmpty())
        result.put(keyboard, patchUsages);
    }
    
    return result;
  }
  
  public String buildMappingDisplay() {
    if (_controlMapping.isEmpty())
      return "Default";
    
    return Utils.mkString(_controlMapping);
  }
  
  @Override
  public List<ControlMapEntry> getControlMap() {
    return new ArrayList<>(_controlMapping);
  }
  
  public void setControlMap(List<ControlMapEntry> controlMap) {
    _controlMapping.clear();
    _controlMapping.addAll(controlMap);
  }
  
  @Override
  public List<PatchUsage> getPatchUsages() {
    return patches;
  }

  // Sort by song, then measure for organization/display
  @Override
  public int compareTo(Cue cue) {
    final int temp = song.number.compareTo(cue.song.number);
    if (temp != 0)
      return temp;
    
    return measureNumber.compareTo(cue.measureNumber);
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (obj == this) return true;
    final Cue cue = (Cue) obj;
    return this.song.equals(cue.song) &&
         this.measureNumber.equals(cue.measureNumber) &&
         this.patches.equals(cue.patches) &&
         this.triggers.equals(cue.triggers) &&
         this.disableGlobalTriggers == cue.disableGlobalTriggers &&
         this._controlMapping.equals(cue._controlMapping) &&
         this.effects.equals(cue.effects) &&
         this.disableGlobalEffects == cue.disableGlobalEffects;
  }
  
  @Override
  public int hashCode() {
    int hashCode = song.hashCode();
    hashCode = 31*hashCode + measureNumber.hashCode();
    hashCode = 31*hashCode + patches.hashCode();
    hashCode = 31*hashCode + triggers.hashCode();
    hashCode =  2*hashCode + (disableGlobalTriggers ? 1 : 0);
    hashCode = 31*hashCode + _controlMapping.hashCode();
    hashCode = 31*hashCode + effects.hashCode();
    hashCode =  2*hashCode + (disableGlobalEffects ? 1 : 0);
    
    return hashCode;
  }
  
  public static int findCueIndex(List<Cue> cues, Song song, LocationNumber measure) {
    if (measure == null) {
      // no measure specified, go to first cue with the given song or later
      int i;
      for (i = 0; i < cues.size(); ++i) {
        if (cues.get(i).song.compareTo(song) >= 0)
          return i;
      }
      return i-1;
    }
    
    // go through until we find a cue after the given location, then back up one
    final Cue dummy = new Cue(song, measure);
    int i;
    for (i = 0; i < cues.size(); ++i) {
      final int result = cues.get(i).compareTo(dummy);
      if (result == 0)
        return i;
      if (result > 0)
        return i == 0 ? 0 : i-1;
    }
    
    // all cues are before the given location, return last cue
    return i-1;
  }
  
  public static Cue findCue(List<Cue> cues, Song song, LocationNumber measure) {
    return cues.get(findCueIndex(cues, song, measure));
  }

  @Override
  public List<Trigger> getTriggers() {
    return triggers;
  }
}