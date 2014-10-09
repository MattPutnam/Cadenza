 package cadenza.core;

import java.io.Serializable;
import java.util.IdentityHashMap;
import java.util.Map;

import common.midi.MidiUtilities;

public class Note implements Comparable<Note>, Serializable {
  private static final long serialVersionUID = 1L;
  
  public static final String SHARP = "\u266F";
  public static final String FLAT = "\u266D";
  
  public static enum PitchClass {
      Cb("Cb", true, 3, 1),
    C("C", true, 2, 1),
      Cs("C#", false, 1, 2),
      Db("Db", false, 2, 1),
    D("D", true, 1, 1),
      Ds("D#", false, 1, 2),
      Eb("Eb", false, 2, 1),
    E("E", true, 1, 3),
      Es("E#", true, 1, 3),
      Fb("Fb", true, 3, 1),
    F("F", true, 3, 1),
      Fs("F#", false, 1, 2),
      Gb("Gb", false, 2, 1),
    G("G", true, 1, 1),
      Gs("G#", false, 1, 2),
      Ab("Ab", false, 2, 1),
    A("A", true, 1, 1),
      As("A#", false, 1, 2),
      Bb("Bb", false, 2, 1),
    B("B", true, 1, 3),
      Bs("B#", true, 1, 3);
    
    private static Map<PitchClass, PitchClass> _mapToNaturals = new IdentityHashMap<>();
    static {
      _mapToNaturals.put(Cb, C);
      _mapToNaturals.put(C, C);
      _mapToNaturals.put(Cs, C);
      
      _mapToNaturals.put(Db, D);
      _mapToNaturals.put(D, D);
      _mapToNaturals.put(Ds, D);
      
      _mapToNaturals.put(Eb, E);
      _mapToNaturals.put(E, E);
      _mapToNaturals.put(Es, E);
      
      _mapToNaturals.put(Fb, F);
      _mapToNaturals.put(F, F);
      _mapToNaturals.put(Fs, F);
      
      _mapToNaturals.put(Gb, G);
      _mapToNaturals.put(G, G);
      _mapToNaturals.put(Gs, G);
      
      _mapToNaturals.put(Ab, A);
      _mapToNaturals.put(A, A);
      _mapToNaturals.put(As, A);
      
      _mapToNaturals.put(Bb, B);
      _mapToNaturals.put(B, B);
      _mapToNaturals.put(Bs, B);
    }
    
    private final String _display;
    private final boolean _isWhite;
    
    private final int _distDownToNextUnique;
    private final int _distUpToNextUnique;
    
    private PitchClass(String display, boolean isWhite,
        int distDownToNextUnique, int distUpToNextUnique) {
      _display = display;
      _isWhite = isWhite;
      _distDownToNextUnique = distDownToNextUnique;
      _distUpToNextUnique = distUpToNextUnique;
    }
    
    public String getDisplay() {
      return _display;
    }
    
    public boolean isWhite() {
      return _isWhite;
    }
    
    @Override
    public String toString() {
      return _display;
    }
    
    public PitchClass next() {
      return PitchClass.values()[(ordinal()+_distUpToNextUnique) % PitchClass.values().length];
    }
    
    public PitchClass prev() {
      int i = ordinal()-_distDownToNextUnique;
      if (i < 0)
        i += PitchClass.values().length;
      
      return PitchClass.values()[i];
    }
    
    public PitchClass getNatural() {
      return _mapToNaturals.get(this);
    }
    
    public static PitchClass getNormal(int pc) {
      switch (pc) {
        case 0: return C;
        case 1: return Db;
        case 2: return D;
        case 3: return Eb;
        case 4: return E;
        case 5: return F;
        case 6: return Fs;
        case 7: return G;
        case 8: return Ab;
        case 9: return A;
        case 10: return Bb;
        case 11: return B;
        default: throw new IllegalArgumentException("param must be 0-11");
      }
    }
  }
  
  public static final Note A0 = new Note(PitchClass.A, 0);
  public static final Note C4 = new Note(PitchClass.C, 4);
  public static final Note C8 = new Note(PitchClass.C, 8);
  
  public static final Note MIN = new Note(0);
  public static final Note MAX = new Note(127);
  
  private final PitchClass _pitchClass;
  private final String _pitchClassDisplay;
  private final int _octave;
  private final int _midiNumber;
  
  public Note(PitchClass pitchClass, int octave) {
    _pitchClass = pitchClass;
    _pitchClassDisplay = pitchClass.getDisplay();
    _octave = octave;
    _midiNumber = MidiUtilities.noteNameToNumber(toString());
  }
  
  public Note(int midiNumber) {
    _midiNumber = midiNumber;
    _octave = (midiNumber / 12) - 1;
    _pitchClass = PitchClass.getNormal(midiNumber % 12);
    _pitchClassDisplay = _pitchClass.getDisplay();
  }
  
  public PitchClass getPitchClass() {
    return _pitchClass;
  }
  
  public int getOctave() {
    return _octave;
  }
  
  public int getMidiNumber() {
    return _midiNumber;
  }
  
  @Override
  public boolean equals(Object other) {
    final Note n2 = (Note) other;
    return _midiNumber == n2._midiNumber;
  }
  
  @Override
  public int hashCode() {
    return _midiNumber;
  }
  
  @Override
  public String toString() {
    return _pitchClassDisplay + _octave;
  }
  
  @Override
  public int compareTo(Note other) {
    return _midiNumber - other._midiNumber;
  }
  
  public boolean above(Note other) {
    return _midiNumber > other._midiNumber;
  }
  
  public boolean below(Note other) {
    return _midiNumber < other._midiNumber;
  }
  
  public Note next() {
    final PitchClass pc = _pitchClass.next();
    return new Note(pc, _octave + (pc == PitchClass.C ? 1 : 0));
  }
  
  public Note prev() {
    final PitchClass pc = _pitchClass.prev();
    return new Note(pc, _octave - (pc == PitchClass.B ? 1 : 0));
  }
  
  public Note add(int interval) {
    Note result = this;
    while (interval > 0) {
      result = result.next();
      --interval;
    }
    return result;
  }
}
