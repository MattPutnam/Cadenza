package cadenza.core;

import static cadenza.core.Note.PitchClass.A;
import static cadenza.core.Note.PitchClass.Ab;
import static cadenza.core.Note.PitchClass.As;
import static cadenza.core.Note.PitchClass.B;
import static cadenza.core.Note.PitchClass.Bb;
import static cadenza.core.Note.PitchClass.Bs;
import static cadenza.core.Note.PitchClass.C;
import static cadenza.core.Note.PitchClass.Cb;
import static cadenza.core.Note.PitchClass.Cs;
import static cadenza.core.Note.PitchClass.D;
import static cadenza.core.Note.PitchClass.Db;
import static cadenza.core.Note.PitchClass.Ds;
import static cadenza.core.Note.PitchClass.E;
import static cadenza.core.Note.PitchClass.Eb;
import static cadenza.core.Note.PitchClass.Es;
import static cadenza.core.Note.PitchClass.F;
import static cadenza.core.Note.PitchClass.Fb;
import static cadenza.core.Note.PitchClass.Fs;
import static cadenza.core.Note.PitchClass.G;
import static cadenza.core.Note.PitchClass.Gb;
import static cadenza.core.Note.PitchClass.Gs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import cadenza.core.Note.PitchClass;

/**
 * A Scale is a collection of {@link PitchClass}es with a name.
 * 
 * @author Matt Putnam
 */
public final class Scale implements Serializable {
  private static final long serialVersionUID = 1L;
  
  public static final class Diatonic {
    public static final Scale C_Major  = new Scale("C Major", C, D, E, F, G, A, B);
    
    public static final Scale F_Major  = new Scale( "F Major", F, G, A, Bb, C, D, E);
    public static final Scale Bb_Major = new Scale("Bb Major", Bb, C, D, Eb, F, G, A);
    public static final Scale Eb_Major = new Scale("Eb Major", Eb, F, G, Ab, Bb, C, D);
    public static final Scale Ab_Major = new Scale("Ab Major", Ab, Bb, C, Db, Eb, F, G);
    public static final Scale Db_Major = new Scale("Db Major", Db, Eb, F, Gb, Ab, Bb, C);
    public static final Scale Gb_Major = new Scale("Gb Major", Gb, Ab, Bb, Cb, Db, Eb, F);
    public static final Scale Cb_Major = new Scale("Cb Major", Cb, Db, Eb, Fb, Gb, Ab, Bb);
    
    public static final Scale G_Major  = new Scale( "G Major", G, A, B, C, D, E, Fs);
    public static final Scale D_Major  = new Scale( "D Major", D, E, Fs, G, A, B, Cs);
    public static final Scale A_Major  = new Scale( "A Major", A, B, Cs, D, E, Fs, Gs);
    public static final Scale E_Major  = new Scale( "E Major", E, Fs, Gs, A, B, Cs, Ds);
    public static final Scale B_Major  = new Scale( "B Major", B, Cs, Ds, E, Fs, Gs, As);
    public static final Scale Fs_Major = new Scale("F# Major", Fs, Gs, As, B, Cs, Ds, Es);
    public static final Scale Cs_Major = new Scale("C# Major", Cs, Ds, Es, Fs, Gs, As, Bs);
    
    public static final Scale  C_Minor = new Scale( "C Minor", Eb_Major);
    public static final Scale Cs_Minor = new Scale("C# Minor",  E_Major);
    public static final Scale  D_Minor = new Scale( "D minor",  F_Major);
    public static final Scale Ds_Minor = new Scale("D# Minor", Fs_Major);
    public static final Scale Eb_Minor = new Scale("Eb Minor", Gb_Major);
    public static final Scale  E_Minor = new Scale( "E Minor",  G_Major);
    public static final Scale  F_Minor = new Scale( "F Minor", Ab_Major);
    public static final Scale Fs_Minor = new Scale("F# Minor",  A_Major);
    public static final Scale  G_Minor = new Scale( "G Minor", Bb_Major);
    public static final Scale Gs_Minor = new Scale("G# Minor",  B_Major);
    public static final Scale Ab_Minor = new Scale("Ab Minor", Cb_Major);
    public static final Scale  A_Minor = new Scale( "A Minor",  C_Major);
    public static final Scale As_Minor = new Scale("A# Minor", Cs_Major);
    public static final Scale Bb_Minor = new Scale("Bb Minor", Db_Major);
    public static final Scale  B_Minor = new Scale( "B Minor",  D_Major);
    
    public static final Scale  C_Harmonic = new Scale( "C Harmonic Minor", C,  D,  Eb, F,  G,  Ab, B );
    public static final Scale Cs_Harmonic = new Scale("C# Harmonic Minor", Cs, Ds, E,  Fs, Gs, A,  Bs);
    public static final Scale  D_Harmonic = new Scale( "D Harmonic Minor", D,  E,  F,  G,  A,  Bb, Cs);
    public static final Scale Eb_Harmonic = new Scale("Eb Harmonic Minor", Eb, F,  Gb, Ab, Bb, Cb, D );
    public static final Scale  E_Harmonic = new Scale( "E Harmonic Minor", E,  Fs, G,  A,  B,  C,  Ds);
    public static final Scale  F_Harmonic = new Scale( "F Harmonic Minor", F,  G,  Ab, Bb, C,  Db, E );
    public static final Scale Fs_Harmonic = new Scale("F# Harmonic Minor", Fs, Gs, A,  B,  Cs, D,  Es);
    public static final Scale  G_Harmonic = new Scale( "G Harmonic Minor", G,  A,  Bb, C,  D,  Eb, Fs);
    public static final Scale Ab_Harmonic = new Scale("Ab Harmonic Minor", Ab, Bb, Cb, Db, Eb, Fb, G );
    public static final Scale  A_Harmonic = new Scale( "A Harmonic Minor", A,  B,  C,  D,  E,  F,  Gs);
    public static final Scale Bb_Harmonic = new Scale("Bb Harmonic Minor", Bb, C,  Db, Eb, F,  Gb, A );
    public static final Scale  B_Harmonic = new Scale(" B Harmonic Minor", B,  Cs, D,  E,  Fs, G,  As);
    
    public static final List<Scale> ALL_MAJOR = Collections.unmodifiableList(Arrays.asList(
        C_Major, Cs_Major, Db_Major, D_Major, Eb_Major, E_Major, F_Major, Fs_Major,
        Gb_Major, G_Major, Ab_Major, A_Major, Bb_Major, B_Major, Cb_Major));
    
    public static final List<Scale> ALL_MINOR = Collections.unmodifiableList(Arrays.asList(
        C_Minor, Cs_Minor, D_Minor, Ds_Minor, Eb_Minor, E_Minor, F_Minor, Fs_Minor,
        G_Minor, Gs_Minor, Ab_Minor, A_Minor, As_Minor, Bb_Minor, B_Minor));
    
    public static final List<Scale> ALL_HARMONIC = Collections.unmodifiableList(Arrays.asList(
        C_Harmonic, Cs_Harmonic, D_Harmonic, Eb_Harmonic, E_Harmonic, F_Harmonic,
        Fs_Harmonic, G_Harmonic, Ab_Harmonic, A_Harmonic, Bb_Harmonic, B_Harmonic));
    
    public static final List<Scale> ALL;
    static {
      final List<Scale> temp = new ArrayList<>(30);
      temp.addAll(ALL_MAJOR);
      temp.addAll(ALL_MINOR);
      temp.addAll(ALL_HARMONIC);
      ALL = Collections.unmodifiableList(temp);
    }
  }
  
  public static final class Modal {
    public static final Scale  C_Ionian = new Scale( "C Ionian", Diatonic. C_Major);
    public static final Scale Cs_Ionian = new Scale("C# Ionian", Diatonic.Cs_Major);
    public static final Scale Db_Ionian = new Scale("Db Ionian", Diatonic.Db_Major);
    public static final Scale  D_Ionian = new Scale("D Ionian",  Diatonic. D_Major);
    public static final Scale Eb_Ionian = new Scale("Eb Ionian", Diatonic.Eb_Major);
    public static final Scale  E_Ionian = new Scale( "E Ionian", Diatonic. E_Major);
    public static final Scale  F_Ionian = new Scale( "F Ionian", Diatonic. F_Major);
    public static final Scale Fs_Ionian = new Scale("F# Ionian", Diatonic.Fs_Major);
    public static final Scale Gb_Ionian = new Scale("Gb Ionian", Diatonic.Gb_Major);
    public static final Scale  G_Ionian = new Scale( "G Ionian", Diatonic. G_Major);
    public static final Scale Ab_Ionian = new Scale("Ab Ionian", Diatonic.Ab_Major);
    public static final Scale  A_Ionian = new Scale( "A Ionian", Diatonic. A_Major);
    public static final Scale Bb_Ionian = new Scale("Bb Ionian", Diatonic.Bb_Major);
    public static final Scale  B_Ionian = new Scale( "B Ionian", Diatonic. B_Major);
    public static final Scale Cb_Ionian = new Scale("Cb Ionian", Diatonic.Cb_Major);
    
    public static final List<Scale> ALL_IONIAN = Collections.unmodifiableList(Arrays.asList(
        C_Ionian, Cs_Ionian, Db_Ionian, D_Ionian, Eb_Ionian, E_Ionian, F_Ionian, Fs_Ionian,
        Gb_Ionian, G_Ionian, Ab_Ionian, A_Ionian, Bb_Ionian, B_Ionian, Cb_Ionian));
    
    public static final Scale  C_Dorian = new Scale( "C Dorian", Diatonic.Bb_Major);
    public static final Scale Cs_Dorian = new Scale("C# Dorian", Diatonic. B_Major);
    public static final Scale Db_Dorian = new Scale("Db Dorian", Diatonic.Cb_Major);
    public static final Scale  D_Dorian = new Scale( "D Dorian", Diatonic. C_Major);
    public static final Scale Ds_Dorian = new Scale("D# Dorian", Diatonic.Cs_Major);
    public static final Scale Eb_Dorian = new Scale("Eb Dorian", Diatonic.Db_Major);
    public static final Scale  E_Dorian = new Scale( "E Dorian", Diatonic. D_Major);
    public static final Scale  F_Dorian = new Scale( "F Dorian", Diatonic.Eb_Major);
    public static final Scale Fs_Dorian = new Scale("F# Dorian", Diatonic. E_Major);
    public static final Scale  G_Dorian = new Scale( "G Dorian", Diatonic. F_Major);
    public static final Scale Gs_Dorian = new Scale("G# Dorian", Diatonic.Fs_Major);
    public static final Scale Ab_Dorian = new Scale("Ab Dorian", Diatonic.Gb_Major);
    public static final Scale  A_Dorian = new Scale( "A Dorian", Diatonic. G_Major);
    public static final Scale Bb_Dorian = new Scale("Bb Dorian", Diatonic.Ab_Major);
    public static final Scale  B_Dorian = new Scale(" B Dorian", Diatonic. A_Major);
    
    public static final List<Scale> ALL_DORIAN = Collections.unmodifiableList(Arrays.asList(
        C_Dorian, Cs_Dorian, Db_Dorian, D_Dorian, Ds_Dorian, Eb_Dorian, E_Dorian, F_Dorian,
        Fs_Dorian, G_Dorian, Gs_Dorian, Ab_Dorian, A_Dorian, Bb_Dorian, B_Dorian));
    
    public static final Scale  C_Phrygian = new Scale( "C Phrygian", Diatonic.Ab_Major);
    public static final Scale Cs_Phrygian = new Scale("C# Phrygian", Diatonic. A_Major);
    public static final Scale  D_Phrygian = new Scale( "D Phrygian", Diatonic.Bb_Major);
    public static final Scale Ds_Phrygian = new Scale("D# Phrygian", Diatonic. B_Major);
    public static final Scale Eb_Phrygian = new Scale("Eb Phrygian", Diatonic.Cb_Major);
    public static final Scale  E_Phrygian = new Scale( "E Phrygian", Diatonic. C_Major);
    public static final Scale Es_Phrygian = new Scale("E# Phrygian", Diatonic.Cs_Major);
    public static final Scale  F_Phrygian = new Scale( "F Phrygian", Diatonic.Db_Major);
    public static final Scale Fs_Phrygian = new Scale("F# Phrygian", Diatonic. D_Major);
    public static final Scale  G_Phrygian = new Scale( "G Phrygian", Diatonic.Eb_Major);
    public static final Scale Gs_Phrygian = new Scale("G# Phrygian", Diatonic. E_Major);
    public static final Scale  A_Phrygian = new Scale( "A Phrygian", Diatonic. F_Major);
    public static final Scale As_Phrygian = new Scale("A# Phrygian", Diatonic.Fs_Major);
    public static final Scale Bb_Phrygian = new Scale("Bb Phrygian", Diatonic.Gb_Major);
    public static final Scale  B_Phrygian = new Scale( "B Phrygian", Diatonic. G_Major);
    
    public static final List<Scale> ALL_PHRYGIAN = Collections.unmodifiableList(Arrays.asList(
        C_Phrygian, Cs_Phrygian, D_Phrygian, Ds_Phrygian, Eb_Phrygian, E_Phrygian, Es_Phrygian, F_Phrygian,
        Fs_Phrygian, G_Phrygian, Gs_Phrygian, A_Phrygian, As_Phrygian, Bb_Phrygian, B_Phrygian));
    
    public static final Scale  C_Lydian = new Scale( "C Lydian", Diatonic. G_Major);
    public static final Scale Db_Lydian = new Scale("Db Lydian", Diatonic.Ab_Major);
    public static final Scale  D_Lydian = new Scale( "D Lydian", Diatonic. A_Major);
    public static final Scale Eb_Lydian = new Scale("Eb Lydian", Diatonic.Bb_Major);
    public static final Scale  E_Lydian = new Scale( "E Lydian", Diatonic. B_Major);
    public static final Scale Fb_Lydian = new Scale("Fb Lydian", Diatonic.Cb_Major);
    public static final Scale  F_Lydian = new Scale( "F Lydian", Diatonic. C_Major);
    public static final Scale Fs_Lydian = new Scale("F# Lydian", Diatonic.Cs_Major);
    public static final Scale Gb_Lydian = new Scale("Gb Lydian", Diatonic.Db_Major);
    public static final Scale  G_Lydian = new Scale( "G Lydian", Diatonic. D_Major);
    public static final Scale Ab_Lydian = new Scale("Ab Lydian", Diatonic.Eb_Major);
    public static final Scale  A_Lydian = new Scale( "A Lydian", Diatonic. E_Major);
    public static final Scale Bb_Lydian = new Scale("Bb Lydian", Diatonic. F_Major);
    public static final Scale  B_Lydian = new Scale( "B Lydian", Diatonic.Fs_Major);
    public static final Scale Cb_Lydian = new Scale("Cb Lydian", Diatonic.Gb_Major);
    
    public static final List<Scale> ALL_LYDIAN = Collections.unmodifiableList(Arrays.asList(
        C_Lydian, Db_Lydian, D_Lydian, Eb_Lydian, E_Lydian, Fb_Lydian, F_Lydian, Fs_Lydian,
        Gb_Lydian, G_Lydian, Ab_Lydian, A_Lydian, Bb_Lydian, B_Lydian, Cb_Lydian));
    
    public static final Scale  C_Mixolydian = new Scale( "C Mixolydian", Diatonic. F_Major);
    public static final Scale Cs_Mixolydian = new Scale("C# Mixolydian", Diatonic.Fs_Major);
    public static final Scale Db_Mixolydian = new Scale("Db Mixolydian", Diatonic.Gb_Major);
    public static final Scale  D_Mixolydian = new Scale( "D Mixolydian", Diatonic. G_Major);
    public static final Scale Eb_Mixolydian = new Scale("Eb Mixolydian", Diatonic.Ab_Major);
    public static final Scale  E_Mixolydian = new Scale( "E Mixolydian", Diatonic. A_Major);
    public static final Scale  F_Mixolydian = new Scale( "F Mixolydian", Diatonic.Bb_Major);
    public static final Scale Fs_Mixolydian = new Scale("F# Mixolydian", Diatonic. B_Major);
    public static final Scale Gb_Mixolydian = new Scale("Gb Mixolydian", Diatonic.Cb_Major);
    public static final Scale  G_Mixolydian = new Scale( "G Mixolydian", Diatonic. C_Major);
    public static final Scale Gs_Mixolydian = new Scale("G# Mixolydian", Diatonic.Cs_Major);
    public static final Scale Ab_Mixolydian = new Scale("Ab Mixolydian", Diatonic.Db_Major);
    public static final Scale  A_Mixolydian = new Scale( "A Mixolydian", Diatonic. D_Major);
    public static final Scale Bb_Mixolydian = new Scale("Bb Mixolydian", Diatonic.Eb_Major);
    public static final Scale  B_Mixolydian = new Scale( "B Mixolydian", Diatonic. E_Major);
    
    public static final List<Scale> ALL_MIXOLYDIAN = Collections.unmodifiableList(Arrays.asList(
        C_Mixolydian, Cs_Mixolydian, Db_Mixolydian, D_Mixolydian, Eb_Mixolydian, E_Mixolydian,
        F_Mixolydian, Fs_Mixolydian, Gb_Mixolydian, G_Mixolydian, Gs_Mixolydian, Ab_Mixolydian,
        A_Mixolydian, Bb_Mixolydian, B_Mixolydian));
    
    public static final Scale  C_Aeolian = new Scale( "C Aeolian", Diatonic.Eb_Major);
    public static final Scale Cs_Aeolian = new Scale("C# Aeolian", Diatonic. E_Major);
    public static final Scale  D_Aeolian = new Scale( "D Aeolian", Diatonic. F_Major);
    public static final Scale Ds_Aeolian = new Scale("D# Aeolian", Diatonic.Fs_Major);
    public static final Scale Eb_Aeolian = new Scale("Eb Aeolian", Diatonic.Gb_Major);
    public static final Scale  E_Aeolian = new Scale( "E Aeolian", Diatonic. G_Major);
    public static final Scale  F_Aeolian = new Scale( "F Aeolian", Diatonic.Ab_Major);
    public static final Scale Fs_Aeolian = new Scale("F# Aeolian", Diatonic. A_Major);
    public static final Scale  G_Aeolian = new Scale( "G Aeolian", Diatonic.Bb_Major);
    public static final Scale Gs_Aeolian = new Scale("G# Aeolian", Diatonic. B_Major);
    public static final Scale Ab_Aeolian = new Scale("Ab Aeolian", Diatonic.Cb_Major);
    public static final Scale  A_Aeolian = new Scale( "A Aeolian", Diatonic. C_Major);
    public static final Scale As_Aeolian = new Scale("A# Aeolian", Diatonic.Cs_Major);
    public static final Scale Bb_Aeolian = new Scale("Bb Aeolian", Diatonic.Db_Major);
    public static final Scale  B_Aeolian = new Scale( "B Aeolian", Diatonic. D_Major);
    
    public static final List<Scale> ALL_AEOLIAN = Collections.unmodifiableList(Arrays.asList(
        C_Aeolian, Cs_Aeolian, D_Aeolian, Ds_Aeolian, Eb_Aeolian, E_Aeolian, F_Aeolian, Fs_Aeolian,
        G_Aeolian, Gs_Aeolian, Ab_Aeolian, A_Aeolian, As_Aeolian, Bb_Aeolian, B_Aeolian));
    
    public static final Scale  C_Locrian = new Scale ("C Locrian", Diatonic.Db_Major);
    public static final Scale Cs_Locrian = new Scale("C# Locrian", Diatonic. D_Major);
    public static final Scale  D_Locrian = new Scale( "D Locrian", Diatonic.Eb_Major);
    public static final Scale Ds_Locrian = new Scale("D# Locrian", Diatonic. E_Major);
    public static final Scale  E_Locrian = new Scale( "E Locrian", Diatonic. F_Major);
    public static final Scale Es_Locrian = new Scale("E# Locrian", Diatonic.Fs_Major);
    public static final Scale  F_Locrian = new Scale( "F Locrian", Diatonic.Gb_Major);
    public static final Scale Fs_Locrian = new Scale("Fs Locrian", Diatonic. G_Major);
    public static final Scale  G_Locrian = new Scale( "G Locrian", Diatonic.Ab_Major);
    public static final Scale Gs_Locrian = new Scale("G# Locrian", Diatonic. A_Major);
    public static final Scale  A_Locrian = new Scale( "A Locrian", Diatonic.Bb_Major);
    public static final Scale As_Locrian = new Scale("A# Locrian", Diatonic. B_Major);
    public static final Scale Bb_Locrian = new Scale("Bb Locrian", Diatonic.Cb_Major);
    public static final Scale  B_Locrian = new Scale( "B Locrian", Diatonic. C_Major);
    public static final Scale Bs_Locrian = new Scale("B# Locrian", Diatonic.Cs_Major);
    
    public static final List<Scale> ALL_LOCRIAN = Collections.unmodifiableList(Arrays.asList(
        C_Locrian, Cs_Locrian, D_Locrian, Ds_Locrian, E_Locrian, Es_Locrian, F_Locrian, Fs_Locrian,
        G_Locrian, Gs_Locrian, A_Locrian, As_Locrian, Bb_Locrian, B_Locrian, Bs_Locrian));
    
    public static final List<Scale> ALL;
    static {
      final List<Scale> temp = new ArrayList<>(7*15);
      temp.addAll(ALL_IONIAN);
      temp.addAll(ALL_DORIAN);
      temp.addAll(ALL_PHRYGIAN);
      temp.addAll(ALL_LYDIAN);
      temp.addAll(ALL_MIXOLYDIAN);
      temp.addAll(ALL_AEOLIAN);
      temp.addAll(ALL_LOCRIAN);
      ALL = Collections.unmodifiableList(temp);
    }
  }
  
  public static final class Blues {
    public static final Scale  C_Blues = new Scale( "C Blues", C,  Eb, F,  Fs, G,  Bb);
    public static final Scale Cs_Blues = new Scale("C# Blues", Cs, E,  Fs, G,  Gs, B );
    public static final Scale  D_Blues = new Scale( "D Blues", D,  F,  G,  Gs, A,  C );
    public static final Scale Eb_Blues = new Scale("Eb Blues", Eb, Gb, Ab, A,  Bb, Db);
    public static final Scale  E_Blues = new Scale( "E Blues", E,  G,  A,  As, B,  D );
    public static final Scale  F_Blues = new Scale( "F Blues", F,  Ab, Bb, B,  C,  Eb);
    public static final Scale Fs_Blues = new Scale("F# Blues", Fs, A,  B,  Bs, Cs, E );
    public static final Scale  G_Blues = new Scale( "G Blues", G,  Bb, C,  Cs, D,  F );
    public static final Scale Gs_Blues = new Scale("G# Blues", Gs, B,  Cs, D,  Ds, Fs);
    public static final Scale Ab_Blues = new Scale("Ab Blues", Ab, Cb, Db, D,  Eb, Gb);
    public static final Scale  A_Blues = new Scale( "A Blues", A,  C,  D,  Ds, E,  G );
    public static final Scale As_Blues = new Scale("A# Blues", As, Cs, Ds, E,  Es, Gs);
    public static final Scale Bb_Blues = new Scale("Bb Blues", Bb, Db, Eb, E,  F,  Ab);
    public static final Scale  B_Blues = new Scale( "B Blues", B,  D,  E,  Es, Fs, A );
    
    public static final List<Scale> ALL = Collections.unmodifiableList(Arrays.asList(
        C_Blues, Cs_Blues, D_Blues, Eb_Blues, E_Blues, F_Blues, Fs_Blues, G_Blues,
        Gs_Blues, Ab_Blues, A_Blues, As_Blues, Bb_Blues, B_Blues));
    
    public static final List<Scale> ALL_UNIQUE = Collections.unmodifiableList(Arrays.asList(
        C_Blues, Cs_Blues, D_Blues, Eb_Blues, E_Blues, F_Blues, Fs_Blues, G_Blues,
        Ab_Blues, A_Blues, Bb_Blues, B_Blues));
  }
  
  public static final class Pentatonic {
    public static final Scale  C_Major_Pentatonic = new Scale( "C Major Pentatonic", C,  D,  E,  G,  A );
    public static final Scale Cs_Major_Pentatonic = new Scale("C# Major Pentatonic", Cs, Ds, Es, Gs, As);
    public static final Scale Db_Major_Pentatonic = new Scale("Db Major Pentatonic", Db, Eb, F,  Ab, Bb);
    public static final Scale  D_Major_Pentatonic = new Scale( "D Major Pentatonic", D,  E,  Fs, A,  B );
    public static final Scale Eb_Major_Pentatonic = new Scale("Eb Major Pentatonic", Eb, F,  G,  Bb, C );
    public static final Scale  E_Major_Pentatonic = new Scale( "E Major Pentatonic", E,  Fs, Gs, B,  Cs);
    public static final Scale  F_Major_Pentatonic = new Scale( "F Major Pentatonic", F,  G,  A,  C,  D );
    public static final Scale Fs_Major_Pentatonic = new Scale("F# Major Pentatonic", Fs, Gs, As, Cs, Ds);
    public static final Scale Gb_Major_Pentatonic = new Scale("Gb Major Pentatonic", Gb, Ab, Bb, Db, Eb);
    public static final Scale  G_Major_Pentatonic = new Scale( "G Major Pentatonic", G,  A,  B,  D,  E );
    public static final Scale Ab_Major_Pentatonic = new Scale("Ab Major Pentatonic", Ab, Bb, Cb, Eb, Fb);
    public static final Scale  A_Major_Pentatonic = new Scale( "A Major Pentatonic", A,  B,  Cs, E,  Fs);
    public static final Scale Bb_Major_Pentatonic = new Scale("Bb Major Pentatonic", Bb, C,  D,  F,  G );
    public static final Scale  B_Major_Pentatonic = new Scale( "B Major Pentatonic", B,  Cs, Ds, Fs, Gs);
    public static final Scale Cb_Major_Pentatonic = new Scale("Cb Major Pentatonic", Cb, Db, Eb, Gb, Ab);
    
    public static final Scale  C_Minor_Pentatonic = new Scale( "C Minor Pentatonic", Eb_Major_Pentatonic);
    public static final Scale Cs_Minor_Pentatonic = new Scale("C# Minor Pentatonic",  E_Major_Pentatonic);
    public static final Scale  D_Minor_Pentatonic = new Scale( "D Minor Pentatonic",  F_Major_Pentatonic);
    public static final Scale Ds_Minor_Pentatonic = new Scale("D# Minor Pentatonic", Fs_Major_Pentatonic);
    public static final Scale Eb_Minor_Pentatonic = new Scale("Eb Minor Pentatonic", Gb_Major_Pentatonic);
    public static final Scale  E_Minor_Pentatonic = new Scale( "E Minor Pentatonic",  G_Major_Pentatonic);
    public static final Scale  F_Minor_Pentatonic = new Scale( "F Minor Pentatonic", Ab_Major_Pentatonic);
    public static final Scale Fs_Minor_Pentatonic = new Scale("F# Minor Pentatonic",  A_Major_Pentatonic);
    public static final Scale  G_Minor_Pentatonic = new Scale( "G Minor Pentatonic", Bb_Major_Pentatonic);
    public static final Scale Gs_Minor_Pentatonic = new Scale("G# Minor Pentatonic",  B_Major_Pentatonic);
    public static final Scale Ab_Minor_Pentatonic = new Scale("Ab Minor Pentatonic", Cb_Major_Pentatonic);
    public static final Scale  A_Minor_Pentatonic = new Scale( "A Minor Pentatonic",  C_Major_Pentatonic);
    public static final Scale As_Minor_Pentatonic = new Scale("A# Minor Pentatonic", Cs_Major_Pentatonic);
    public static final Scale Bb_Minor_Pentatonic = new Scale("Bb Minor Pentatonic", Db_Major_Pentatonic);
    public static final Scale  B_Minor_Pentatonic = new Scale( "B Minor Pentatonic",  D_Major_Pentatonic);
    
    public static final List<Scale> ALL_MAJOR = Collections.unmodifiableList(Arrays.asList(
         C_Major_Pentatonic, Cs_Major_Pentatonic, Db_Major_Pentatonic,  D_Major_Pentatonic, Eb_Major_Pentatonic,
         E_Major_Pentatonic,  F_Major_Pentatonic, Fs_Major_Pentatonic, Gb_Major_Pentatonic,  G_Major_Pentatonic,
        Ab_Major_Pentatonic,  A_Major_Pentatonic, Bb_Major_Pentatonic,  B_Major_Pentatonic, Cb_Major_Pentatonic));
    
    public static final List<Scale> ALL_MINOR = Collections.unmodifiableList(Arrays.asList(
         C_Minor_Pentatonic, Cs_Minor_Pentatonic,  D_Minor_Pentatonic, Ds_Minor_Pentatonic, Eb_Minor_Pentatonic,
         E_Minor_Pentatonic,  F_Minor_Pentatonic, Fs_Minor_Pentatonic,  G_Minor_Pentatonic, Gs_Minor_Pentatonic,
        Ab_Minor_Pentatonic,  A_Minor_Pentatonic, As_Minor_Pentatonic, Bb_Minor_Pentatonic,  B_Minor_Pentatonic));
    
    public static final List<Scale> ALL;
    static {
      final List<Scale> temp = new ArrayList<>(30);
      temp.addAll(ALL_MAJOR);
      temp.addAll(ALL_MINOR);
      ALL = Collections.unmodifiableList(temp);
    }
  }
  
  public static final class WholeTone {
    public static final Scale Cb_Whole = new Scale("Cb Whole Tone", Cb, Db, Eb, F,  G,  A );
    public static final Scale  C_Whole = new Scale( "C Whole Tone", C,  D,  E,  Fs, Gs, As);
    public static final Scale Cs_Whole = new Scale("C# Whole Tone", Cs, Ds, F,  G,  A,  B );
    public static final Scale Db_Whole = new Scale("Db Whole Tone", Db, Eb, F,  G,  A,  B );
    public static final Scale  D_Whole = new Scale( "D Whole Tone",  C_Whole);
    public static final Scale Ds_Whole = new Scale("D# Whole Tone", Cs_Whole);
    public static final Scale Eb_Whole = new Scale("Eb Whole Tone", Db_Whole);
    public static final Scale  E_Whole = new Scale( "E Whole Tone",  C_Whole);
    public static final Scale  F_Whole = new Scale( "F Whole Tone", Cs_Whole);
    public static final Scale Fs_Whole = new Scale("F# Whole Tone",  C_Whole);
    public static final Scale Gb_Whole = new Scale("Gb Whole Tone", Gb, Ab, Bb, C,  D,  E );
    public static final Scale  G_Whole = new Scale( "G Whole Tone", Cs_Whole);
    public static final Scale Gs_Whole = new Scale("G# Whole Tone",  C_Whole);
    public static final Scale Ab_Whole = new Scale("Ab Whole Tone", Gb_Whole);
    public static final Scale  A_Whole = new Scale( "A Whole Tone", Cs_Whole);
    public static final Scale As_Whole = new Scale("A# Whole Tone",  C_Whole);
    public static final Scale Bb_Whole = new Scale("Bb Whole Tone", Gb_Whole);
    public static final Scale  B_Whole = new Scale( "B Whole Tone", Cs_Whole);
    public static final Scale Bs_Whole = new Scale("B# Whole Tone", Bs, D,  E,  Fs, Gs, As);
    
    public static final List<Scale> ALL_UNIQUE = Collections.unmodifiableList(Arrays.asList(C_Whole, Cs_Whole));
    
    public static final List<Scale> ALL = Collections.unmodifiableList(Arrays.asList(Cb_Whole, C_Whole, Cs_Whole,
        Db_Whole, D_Whole, Ds_Whole, Eb_Whole, E_Whole, F_Whole, Fs_Whole, Gb_Whole, G_Whole,
        Gs_Whole, Ab_Whole, A_Whole, As_Whole, Bb_Whole, B_Whole, Bs_Whole));
  }
  
  public static final class Diminished {
    public static final Scale  C_Diminished = new Scale( "C Diminished", C,  D,  Ds, F,  Fs, Gs, A,  B );
    public static final Scale Cs_Diminished = new Scale("C# Diminished", Cs, Ds, E,  Fs, G,  A,  As, C );
    public static final Scale Db_Diminished = new Scale("Db Diminished", Cs_Diminished);
    public static final Scale  D_Diminished = new Scale( "D Diminished", D,  E,  F,  G,  Gs, As, B,  Cs);
    public static final Scale Ds_Diminished = new Scale("D# Diminished",  C_Diminished);
    public static final Scale Eb_Diminished = new Scale("Eb Diminished",  C_Diminished);
    public static final Scale  E_Diminished = new Scale( "E Diminished", Cs_Diminished);
    public static final Scale  F_Diminished = new Scale( "F Diminished",  D_Diminished);
    public static final Scale Fs_Diminished = new Scale("F# Diminished",  C_Diminished);
    public static final Scale Gb_Diminished = new Scale("Gb Diminished",  C_Diminished);
    public static final Scale  G_Diminished = new Scale( "G Diminished", Cs_Diminished);
    public static final Scale Gs_Diminished = new Scale("G# Diminished",  D_Diminished);
    public static final Scale Ab_Diminished = new Scale("Ab Diminished",  D_Diminished);
    public static final Scale  A_Diminished = new Scale( "A Diminished",  C_Diminished);
    public static final Scale As_Diminished = new Scale("A# Diminished", Cs_Diminished);
    public static final Scale Bb_Diminished = new Scale("Bb Diminished", Cs_Diminished);
    public static final Scale  B_Diminished = new Scale( "B Diminished",  D_Diminished);
    
    public static final List<Scale> ALL_UNIQUE = Collections.unmodifiableList(Arrays.asList(
        C_Diminished, Cs_Diminished, D_Diminished));
    
    public static final List<Scale> ALL = Collections.unmodifiableList(Arrays.asList(C_Diminished,
        Cs_Diminished, Db_Diminished, D_Diminished, Ds_Diminished, Eb_Diminished, E_Diminished,
        F_Diminished, Fs_Diminished, Gb_Diminished, G_Diminished, Gs_Diminished, Ab_Diminished,
        A_Diminished, As_Diminished, Bb_Diminished, B_Diminished));
  }
  
  public static final class Chromatic {
    public static final Scale Chromatic_sharps = new Scale("Chromatic (sharps)",
        C, Cs, D, Ds, E, F, Fs, G, Gs, A, As, B);
    public static final Scale Chromatic_flats = new Scale("Chromatic (flats)",
        C, Db, D, Eb, E, F, Gb, G, Ab, A, Bb, B);
  }
  
  public static List<Scale> getAll() {
    final List<Scale> result = new ArrayList<>();
    result.addAll(Diatonic.ALL);
    result.addAll(Modal.ALL);
    result.addAll(Blues.ALL);
    result.addAll(Pentatonic.ALL);
    result.addAll(WholeTone.ALL);
    result.addAll(Diminished.ALL);
    result.add(Chromatic.Chromatic_sharps);
    result.add(Chromatic.Chromatic_flats);
    return result;
  }
  
  public static List<Scale> getAllUnique() {
    final List<Scale> result = new ArrayList<>();
    result.addAll(Diatonic.ALL_MAJOR);
    result.addAll(Diatonic.ALL_HARMONIC);
    result.addAll(Blues.ALL_UNIQUE);
    result.addAll(Pentatonic.ALL_MAJOR);
    result.addAll(WholeTone.ALL_UNIQUE);
    result.addAll(Diminished.ALL_UNIQUE);
    result.add(Chromatic.Chromatic_sharps);
    return result;
  }
  
  private final String _name;
  private final List<PitchClass> _pitches;
  private final int _size;
  
  /**
   * Creates a new Scale with the given name and array of PitchClass
   * @param name the name
   * @param pitches the set of pitches in the scale
   */
  public Scale(String name, PitchClass... pitches) {
    this(name, Arrays.asList(pitches));
  }
  
  /**
   * Creates a new Scale with the given name and list of PitchClass
   * @param name the name
   * @param pitches the set of pitches in the scale
   */
  public Scale(String name, List<PitchClass> pitches) {
    _name = name;
    _pitches = new ArrayList<>(pitches);
    _size = _pitches.size();
    _pitches.sort(null);
  }
  
  /**
   * Creates a new Scale with the given name and all of the pitches from the
   * given scale.  Useful for scales sharing the same notes, for example
   * C major and A minor.
   * @param name the name
   * @param scale the scale to copy pitches from
   */
  public Scale(String name, Scale scale) {
    _name = name;
    _pitches = new ArrayList<>(scale._pitches);
    _size = scale._size;
  }
  
  /**
   * @return the name of this scale
   */
  public String getName() {
    return _name;
  }
  
  @Override
  public String toString() {
    return _name;
  }
  
  /**
   * Returns whether or not this Scale contains the given PitchClass
   * @param pitch the pitch class
   * @return whether or not this Scale contains <tt>pitch</tt>
   */
  public boolean contains(PitchClass pitch) {
    return _pitches.contains(pitch);
  }
  
  /**
   * Returns whether or not this Scale includes the given Note
   * @param note the note
   * @return whether or not this Scale contains <tt>note</tt>
   */
  public boolean contains(Note note) {
    return _pitches.contains(note.getPitchClass());
  }
  
  /**
   * Returns the next PitchClass in the Scale above the given one
   * @param pitch the pitch
   * @return the next PitchClass in the Scale above <tt>pitch</tt>
   */
  public PitchClass next(PitchClass pitch) {
    while (true) {
      pitch = pitch.next();
      if (contains(pitch))
        return pitch;
    }
  }
  
  /**
   * Returns the next Note in the Scale above the given one
   * @param note the note
   * @return the next Note in the Scale above <tt>note>
   */
  public Note next(Note note) {
    while (true) {
      note = note.next();
      if (contains(note))
        return note;
    }
  }
  
  /**
   * Returns the next PitchClass in the scale below the given one
   * @param pitch the pitch
   * @return the next PitchClass in the scale below <tt>pitch</tt>
   */
  public PitchClass prev(PitchClass pitch) {
    while (true) {
      pitch = pitch.prev();
      if (contains(pitch))
        return pitch;
    }
  }
  
  /**
   * Returns the next Note in the scale below the given one
   * @param note the note
   * @return the next Note in the scale below <tt>note</tt>
   */
  public Note prev(Note note) {
    while (true) {
      note = note.prev();
      if (contains(note))
        return note;
    }
  }
  
  /**
   * Finds the Note in the Scale which is the given interval above the given
   * note.  The interval is in terms of the notes of the scale, so 2 above C
   * in C blues is F.
   * @param note the base note
   * @param interval the interval
   * @return the Note in the Scale which is <tt>interval</tt> steps above
   * <tt>note</tt>
   */
  public Note upDegrees(Note note, int interval) {
    if (interval < 0)
      return downDegrees(note, interval);
    
    while (interval > 0) {
      note = note.next();
      if (contains(note))
        --interval;
    }
    return note;
  }
  
  /**
   * Finds the Note in the Scale which is the given interval below the given
   * note.  The interval is in terms of the notes of the scale, so 2 below C
   * in C blues is G.
   * @param note the base note
   * @param interval the interval
   * @return the Note in the Scale which is <tt>interval</tt> steps below
   * <tt>note</tt>
   */
  public Note downDegrees(Note note, int interval) {
    if (interval < 0)
      return upDegrees(note, interval);
    
    while (interval > 0) {
      note = note.prev();
      if (contains(note))
        --interval;
    }
    return note;
  }
  
  /**
   * Builds a chord based on the given note, and the intervals from it.
   * Example:
   * <pre>Scale.Diatonic.C_Major.buildChord(new Note("D4"), 2, 4) => {D4, F4, A4}</pre>
   * @param root the base note
   * @param intervals the intervals
   * @return a list of the notes that are the given intervals above/below the root
   */
  public List<Note> buildChord(Note root, int... intervals) {
    final List<Note> result = new ArrayList<>(intervals.length+1);
    result.add(root);
    for (final int interval : intervals)
      result.add(upDegrees(root, interval));
    return result;
  }

  /**
   * Builds a map from the natural of each pitch in the scale to the number
   * of half steps needed to take that natural to the original pitch.
   * Example:
   * <pre>Scale.Diatonic.G_Major.buildMapFromNaturals() =>
   * {
   *   C => 0
   *   D => 0
   *   E => 0
   *   F => 1
   *   G => 0
   *   A => 0
   *   B => 0
   * }</pre>
   * 
   * <b>Note:</b> This method will not work properly on any scale which has
   * multiple uses of any pitch class, which includes all octatonic (and
   * above) scales and many of the blues scales.
   * @return a map from the natural of each pitch class to the number of
   * half steps needed to take that natural back to the original pitch
   */
  public Map<PitchClass, Integer> buildMapFromNaturals() {
    final Map<PitchClass, Integer> result = new IdentityHashMap<>();
    
    for (final PitchClass pc : _pitches) {
      result.put(pc.getNatural(), Integer.valueOf(pc.ordinal()-pc.getNatural().ordinal()));
    }
    
    return result;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    final Scale scale = (Scale) obj;
    return _name.equals(scale._name) && _pitches.equals(scale._pitches);
  }
  
  @Override
  public int hashCode() {
    int hashCode = _name.hashCode();
    hashCode = 63*hashCode + _pitches.hashCode();
    
    return hashCode;
  }
}
