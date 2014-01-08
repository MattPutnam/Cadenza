package cadenza.core.patchusage;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cadenza.core.Location;
import cadenza.core.Patch;

import common.tuple.Pair;

/**
 * A PatchUsage which causes a set of notes to be played when a single note is
 * played.  Also called "slave notes".
 * 
 * @author Matt Putnam
 */
public class GhostNotePatchUsage extends PatchUsage {
	/** The map of notes to play when a note is pressed */
	public Map<Integer, List<Integer>> ghosts;

	public GhostNotePatchUsage(Patch patch, Location location,
			int volume, Map<Integer, List<Integer>> ghosts) {
		super(patch, location, volume);
		this.ghosts = Collections.unmodifiableMap(new LinkedHashMap<>(ghosts));
	}

	@Override
	public List<Pair<Integer, Integer>> getNotes(int midiNumber, int velocity) {
		final List<Integer> ghostEntry = ghosts.get(midiNumber);
		final List<Pair<Integer, Integer>> result = new LinkedList<>();
		
		if (ghostEntry != null) {
			for (final Integer note : ghostEntry) {
				result.add(Pair.make(note, velocity));
			}
		}
		
		return result;
	}
	
	@Override
	boolean respondsTo_additional(int midiNumber, int velocity) {
		return ghosts.containsKey(midiNumber);
	}
	
	@Override
	String toString_additional() {
		return " with " + ghosts.size() + " ghost notes";
	}

}
