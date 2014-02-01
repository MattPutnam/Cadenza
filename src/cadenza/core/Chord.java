package cadenza.core;

import static cadenza.core.Chord.Interval.AUG_5;
import static cadenza.core.Chord.Interval.DIM_5;
import static cadenza.core.Chord.Interval.DIM_7;
import static cadenza.core.Chord.Interval.MAJ_2;
import static cadenza.core.Chord.Interval.MAJ_3;
import static cadenza.core.Chord.Interval.MAJ_7;
import static cadenza.core.Chord.Interval.MAJ_9;
import static cadenza.core.Chord.Interval.MIN_3;
import static cadenza.core.Chord.Interval.MIN_7;
import static cadenza.core.Chord.Interval.PFT_4;
import static cadenza.core.Chord.Interval.PFT_5;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Chord implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public static enum Interval {
		PFT_U("P1", 0),
		AUG_U("A1", 1),
		MIN_2("m2", 1),
		MAJ_2("M2", 2),
		AUG_2("A2", 3),
		DIM_3("d3", 2),
		MIN_3("m3", 3),
		MAJ_3("M3", 4),
		AUG_3("A3", 5),
		DIM_4("d4", 4),
		PFT_4("P4", 5),
		AUG_4("A4", 6),
		DIM_5("d5", 6),
		PFT_5("P5", 7),
		AUG_5("A5", 8),
		DIM_6("d6", 7),
		MIN_6("m6", 8),
		MAJ_6("M6", 9),
		AUG_6("A6", 10),
		DIM_7("d7", 9),
		MIN_7("m7", 10),
		MAJ_7("M7", 11),
		AUG_7("A7", 12),
		DIM_8("d8", 11),
		PFT_8("P8", 12),
		AUG_8("A8", 13),
		
		MIN_9("m9", 13),
		MAJ_9("M9", 14),
		AUG_9("A9", 15),
		
		MIN_11("m11", 16),
		MAJ_11("M11", 17),
		AUG_11("A11", 18),
		
		MIN_13("m13", 20),
		MAJ_13("M13", 21),
		AUG_13("A13", 22);
		
		private final String _name;
		private final int _halfSteps;
		
		private Interval(String name, int halfSteps) {
			_name = name;
			_halfSteps = halfSteps;
		}
		
		@Override
		public String toString() {
			return _name + " (" + _halfSteps + ")";
		}
	}
	
	public static final class Triad {
		public static final Chord Major      = new Chord("Major",      "",    MAJ_3, PFT_5);
		public static final Chord Minor      = new Chord("Minor",      "m",   MIN_3, PFT_5);
		public static final Chord Diminished = new Chord("Diminished", "dim", MIN_3, DIM_5);
		public static final Chord Augmented  = new Chord("Augmented",  "+",   MAJ_3, AUG_5);
		
		public static final Chord Sus4 = new Chord("Suspended 4", "sus4", PFT_4, PFT_5);
		public static final Chord Sus2 = new Chord("Suspended 2", "sus2", MAJ_2, PFT_5);
		
		public static final List<Chord> ALL = Collections.unmodifiableList(Arrays.asList(
				Major, Minor, Diminished, Augmented, Sus4, Sus2));
	}
	
	public static final class Seventh {
		public static final Chord Maj7  = new Chord("Major 7",           "M7", MAJ_3, PFT_5, MAJ_7);
		public static final Chord Min7  = new Chord("Minor 7",           "m7", MIN_3, PFT_5, MIN_7);
		public static final Chord Dom7  = new Chord("Dominant 7",         "7", MAJ_3, PFT_5, MIN_7);
		public static final Chord HDim7 = new Chord("Half-diminished 7", "o7", MIN_3, DIM_5, MIN_7);
		public static final Chord Dim7  = new Chord("Diminished 7",    "dim7", MIN_3, DIM_5, DIM_7);
		public static final Chord mM7   = new Chord("Minor-Major 7",    "mM7", MIN_3, PFT_5, MAJ_7);
		
		public static final List<Chord> ALL = Collections.unmodifiableList(Arrays.asList(
				Maj7, Min7, Dom7, HDim7, Dim7, mM7));
	}
	
	public static final class Ninth {
		public static final Chord Maj9 = new Chord("Major 9",   "M9", MAJ_3, PFT_5, MAJ_7, MAJ_9);
		public static final Chord Min9 = new Chord("Minor 9",   "m9", MIN_3, PFT_5, MIN_7, MAJ_9);
		public static final Chord Dom9 = new Chord("Dominant 9", "9", MAJ_3, PFT_5, MIN_7, MAJ_9);
		
		public static final Chord add9 = new Chord("Add 9", "add9", MAJ_3, PFT_5, MAJ_9);
		
		public static final List<Chord> ALL = Collections.unmodifiableList(Arrays.asList(
				Maj9, Min9, Dom9, add9));
	}
	
	public static final List<Chord> getAll() {
		final List<Chord> temp = new ArrayList<>();
		temp.addAll(Triad.ALL);
		temp.addAll(Seventh.ALL);
		temp.addAll(Ninth.ALL);
		return Collections.unmodifiableList(temp);
	}
	
	private final String _name;
	private final String _abbreviation;
	private final List<Interval> _intervals;
	
	public Chord(String name, String abbreviation, Interval... intervals) {
		this(name, abbreviation, Arrays.asList(intervals));
	}
	
	public Chord(String name, String abbreviation, List<Interval> intervals) {
		_name = name;
		_abbreviation = abbreviation;
		_intervals = new ArrayList<>(intervals);
	}
	
	public String getName() {
		return _name;
	}
	
	public String getAbbreviation() {
		return _abbreviation;
	}
	
	public List<Note> build(Note root) {
		final List<Note> result = new ArrayList<>(_intervals.size()+1);
		result.add(root);
		for (final Interval interval : _intervals)
			result.add(root.add(interval._halfSteps));
		return result;
	}
}
