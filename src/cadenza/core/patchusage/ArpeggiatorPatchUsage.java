package cadenza.core.patchusage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cadenza.control.PerformanceController;
import cadenza.core.NoteRange;
import cadenza.core.Patch;
import cadenza.core.metronome.Metronome;
import cadenza.core.metronome.Metronome.Subdivision;
import cadenza.core.metronome.MetronomeListener;

/**
 * A PatchUsage type that arpeggiates the currently played chord in a variety
 * of different ways.  Uses a minimum number of notes to avoid the race
 * condition when the notes of the chord are played (nearly) simultaneously.
 * 
 * @author Matt Putnam
 */
public class ArpeggiatorPatchUsage extends PatchUsage implements MetronomeListener {
  private static final long serialVersionUID = 2L;
  
  /**
   * Enumerates the possible patterns for the arpeggiator.
   * 
   * @author Matt Putnam
   */
  public static enum Pattern {
    /** Plays the notes in ascending order */
    ASCENDING("Ascending"),
    /** Plays the notes in descending order */
    DESCENDING("Descending"),
    /** Sweeps up and down, starting at the bottom */
    ALTERNATING_BOTTOM("Alternating ascending/descending, starting at bottom"),
    /** Sweeps up and down, starting at the top */
    ALTERNATING_TOP("Alternating ascending/descending, starting at top"),
    /** Picks notes completely at random */
    RANDOM("Random"),
    /** Picks notes randomly, but won't repeat the same note twice */
    RANDOM_NONREPEATING("Random (won't repeat)");
    
    private final String _displayName;
    
    private Pattern(String displayName) {
      _displayName = displayName;
    }
    
    @Override
    public String toString() {
      return _displayName;
    }
  }
  
  /** The arpeggiator pattern to use */
  public final Pattern pattern;
  /** The metronome subdivision to use */
  public final Subdivision subdivision;
  /** The minimum chord size, must be at least 2 */
  public final int minSize;
  
  private transient volatile List<Integer> _currentNotes;
  private transient PerformanceController _controller;
  private transient volatile int _index = -1;
  private transient volatile int _currentlyPlayingNote = -1;
  private transient volatile boolean _up;
  private transient Random _random;
  
  private transient boolean _turnOffMetronomeOnExit;

  /**
   * Creates a new ArpeggiatorPatchUsage with the given PatchUsage basics,
   * the pattern, the subdivision, and the minimum size
   * @param patch the patch
   * @param noteRange the note range
   * @param volume the volume
   * @param pattern the arpeggiator pattern
   * @param subdivision the metronome subdivision
   * @param minSize the minimum size, must be at least 2
   */
  public ArpeggiatorPatchUsage(Patch patch, NoteRange noteRange, int volume,
      Pattern pattern, Subdivision subdivision, int minSize) {
    super(patch, noteRange, volume);
    if (minSize < 2)
      throw new IllegalArgumentException("minSize must be at least 2");
    this.pattern = pattern;
    this.subdivision = subdivision;
    this.minSize = minSize;
  }

  @Override
  public int[][] getNotes(int midiNumber, int velocity) {
    // abuse this just to get key pressed info
    _currentNotes.add(Integer.valueOf(midiNumber));
    _currentNotes.sort(null);
    Metronome.getInstance().start();
    return new int[][] {};
  }
  
  @Override
  public void prepare(PerformanceController controller) {
    _currentNotes = new ArrayList<>();
    Metronome.getInstance().addMetronomeListener(this);
    _controller = controller;
    _random = new Random();
    _turnOffMetronomeOnExit = !Metronome.getInstance().isRunning();
  }
  
  @Override
  public void cleanup(PerformanceController controller) {
    Metronome.getInstance().removeMetronomeListener(this);
    _currentNotes.clear();
    if (_currentlyPlayingNote != -1)
      _controller.sendNoteOff(_currentlyPlayingNote, this);
    if (_turnOffMetronomeOnExit)
      Metronome.getInstance().stop();
  }
  
  @Override
  public void noteReleased(int midiNumber) {
    _currentNotes.remove(Integer.valueOf(midiNumber));
  }

  @Override
  String toString_additional() {
    return " using arpeggiator: " + subdivision.toString() + " " + pattern.toString();
  }

  @Override
  public void metronomeClicked(int clickSubdivision) {
    if (subdivision.matches(clickSubdivision)) {
      if (_currentlyPlayingNote != -1)
        _controller.sendNoteOff(_currentlyPlayingNote, this);
      
      if (_currentNotes.size() < minSize) {
        _index = -1;
        _currentlyPlayingNote = -1;
        return;
      }

      _index = nextIndex();
      _currentlyPlayingNote = _currentNotes.get(_index).intValue();
      _controller.sendNoteOn(_currentlyPlayingNote, volume, this);
    }
  }
  
  private int nextIndex() {
    final int size = _currentNotes.size();
    switch (pattern) {
      case ASCENDING:
        return (_index+1) % size;
      case DESCENDING:
        return (_index == -1) ? (size-1) : ((_index-1+size) % size);
      case ALTERNATING_BOTTOM:
        if (_index == -1) {
          _up = true;
          return 0;
        } else if (_up) {
          if (_index == size-2) _up = false;
          return _index+1;
        } else {
          if (_index == 1) _up = true;
          return _index-1;
        }
      case ALTERNATING_TOP:
        if (_index == -1) {
          _up = false;
          return size-1;
        } else if (_up) {
          if (_index == size-2) _up = false;
          return _index+1;
        } else {
          if (_index == 1) _up = true;
          return _index-1;
        }
      case RANDOM:
        return _random.nextInt(size);
      case RANDOM_NONREPEATING:
        int chosen;
        do {
          chosen = _random.nextInt(size);
        } while (chosen == _index);
        return chosen;
      default: throw new IllegalStateException("Unknown pattern type");
    }
  }
}
