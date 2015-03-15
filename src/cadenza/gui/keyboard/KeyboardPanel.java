package cadenza.gui.keyboard;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;

import cadenza.core.Note;
import cadenza.core.Note.PitchClass;

import common.swing.GraphicsUtils;


/**
 * A component that displays a piano keyboard over a specified range of notes.
 * The dimensions of this keyboard are fixed.  This class can be queried for
 * the physical location of keys within the keyboard, or told to highlight
 * certain notes, but does not provide any other utilities.  For clicking /
 * dragging within one keyboard, see SingleKeyboardPanel; for clicking /
 * dragging between multiple keyboards, see MultipleKeyboardPanel.
 * 
 * @author Matt Putnam
 */
@SuppressWarnings("serial")
public final class KeyboardPanel extends JPanel {
  public static final Color HIGHLIGHT_COLOR = new Color(22, 136, 220);
  public static final Color LIGHT_HIGHLIGHT_COLOR = new Color(176, 217, 247);
  
  /** Length of white key */
  static final int WHITE_HEIGHT = 80;
  /** Width of white key */
  static final int WHITE_WIDTH = 14;
  /** Length of black key */
  static final int BLACK_HEIGHT = 50;
  /** Width of black key */
  static final int BLACK_WIDTH = 10;
  
  // Cut-in of C# into C, half the width of a white key
  private static final int CUT_HALF = WHITE_WIDTH / 2;
  // Cut-in of C# into D, the remaining part of the black key
  private static final int CUT_NEGHALF = BLACK_WIDTH - CUT_HALF;
  // Cut-in of G# into G or A, half the width of a black key
  private static final int CUT_MIDHALF = BLACK_WIDTH / 2;
  
  private static final Map<PitchClass, Integer> ADVANCE = new HashMap<>();
  static {
    ADVANCE.put(PitchClass.C, Integer.valueOf(CUT_HALF));
      ADVANCE.put(PitchClass.Cs, Integer.valueOf(CUT_HALF));
    ADVANCE.put(PitchClass.D, Integer.valueOf(WHITE_WIDTH - CUT_NEGHALF));
      ADVANCE.put(PitchClass.Ds, Integer.valueOf(CUT_NEGHALF));
    ADVANCE.put(PitchClass.E, Integer.valueOf(WHITE_WIDTH));
    ADVANCE.put(PitchClass.F, Integer.valueOf(CUT_HALF));
      ADVANCE.put(PitchClass.Fs, Integer.valueOf(CUT_HALF));
    ADVANCE.put(PitchClass.G, Integer.valueOf(WHITE_WIDTH - CUT_MIDHALF));
      ADVANCE.put(PitchClass.Gs, Integer.valueOf(CUT_MIDHALF));
    ADVANCE.put(PitchClass.A, Integer.valueOf(WHITE_WIDTH - CUT_NEGHALF));
      ADVANCE.put(PitchClass.As, Integer.valueOf(CUT_NEGHALF));
    ADVANCE.put(PitchClass.B, Integer.valueOf(WHITE_WIDTH));
  }
  
  private static final Map<PitchClass, Integer> WHITE_ORDINAL = new HashMap<>();
  static {
    WHITE_ORDINAL.put(PitchClass.C, Integer.valueOf(1));
      WHITE_ORDINAL.put(PitchClass.Cs, Integer.valueOf(1));
    WHITE_ORDINAL.put(PitchClass.D, Integer.valueOf(2));
      WHITE_ORDINAL.put(PitchClass.Ds, Integer.valueOf(2));
    WHITE_ORDINAL.put(PitchClass.E, Integer.valueOf(3));
    WHITE_ORDINAL.put(PitchClass.F, Integer.valueOf(4));
      WHITE_ORDINAL.put(PitchClass.Fs, Integer.valueOf(4));
    WHITE_ORDINAL.put(PitchClass.G, Integer.valueOf(5));
      WHITE_ORDINAL.put(PitchClass.Gs, Integer.valueOf(5));
    WHITE_ORDINAL.put(PitchClass.A, Integer.valueOf(6));
      WHITE_ORDINAL.put(PitchClass.As, Integer.valueOf(6));
    WHITE_ORDINAL.put(PitchClass.B, Integer.valueOf(7));
  }
  
  private static final Font LABEL_FONT_WHITE = Font.decode("Arial 12");
  private static final Font LABEL_FONT_BLACK = Font.decode("Arial 11");
  
  private final Note _lowNote;
  private final Note _highNote;
  
  private final Dimension _size;
  
  private Map<Note, Rectangle> _whiteMap;
  private Map<Note, Rectangle> _blackMap;
  
  private Map<Note, Color> _highlightedNotes = new HashMap<>();
  
  private Set<Note> _labeledNotes = new HashSet<>();
  
  /**
   * Creates a new KeyboardPanel with the specified range.  For example, a
   * standard 88-key keyboard runs A0 to C8.  The low note must not be above
   * the high note.
   * @param lowNote the lowest note of the range
   * @param highNote the highest note of the range
   */
  public KeyboardPanel(Note lowNote, Note highNote) {
    super();
    
    _lowNote = lowNote;
    _highNote = highNote;
    
    if (_lowNote == null)
      throw new IllegalArgumentException("'" + lowNote + "' is not a valid note");
    if (_highNote == null)
      throw new IllegalArgumentException("'" + highNote + "' is not a valid note");
    if (_lowNote.compareTo(_highNote) >= 0)
      throw new IllegalArgumentException("Range is not valid");
    
    _size = calcSize();
    initMaps();
  }
  
  private Dimension calcSize() {
    final int numWhiteKeys = WHITE_ORDINAL.get(_highNote.getPitchClass()).intValue() -
                 WHITE_ORDINAL.get(_lowNote.getPitchClass()).intValue() +
                 1 + 7 * (_highNote.getOctave() - _lowNote.getOctave());
    
    return new Dimension(numWhiteKeys * WHITE_WIDTH + 1, WHITE_HEIGHT + 1);
  }
  
  private void initMaps() {
    _whiteMap = new HashMap<>();
    _blackMap = new HashMap<>();
    
    PitchClass currentPitchClass = _lowNote.getPitchClass();
    int currentOctave = _lowNote.getOctave();
    final int highOctave = _highNote.getOctave();
    int xpos = 0;
    while (currentOctave < highOctave ||
        (currentOctave == highOctave && currentPitchClass.ordinal() <= _highNote.getPitchClass().ordinal())) {
      final Note note = new Note(currentPitchClass, currentOctave);
      if (currentPitchClass.isWhite()) {
        final Rectangle rect = new Rectangle(xpos, 0, WHITE_WIDTH, WHITE_HEIGHT);
        _whiteMap.put(note, rect);
      } else {
        final Rectangle rect = new Rectangle(xpos, 0, BLACK_WIDTH, BLACK_HEIGHT);
        _blackMap.put(note, rect);
      }
      
      xpos += ADVANCE.get(currentPitchClass).intValue();
      
      currentPitchClass = currentPitchClass.next();
      if (currentPitchClass == PitchClass.C)
        ++currentOctave;
    }
  }
  
  /**
   * @return the lowest note on this keyboard
   */
  public Note getLowNote() {
    return _lowNote;
  }
  
  /**
   * @return the highest note on this keyboard
   */
  public Note getHighNote() {
    return _highNote;
  }
  
  /**
   * Gets the location/size of a given key.  Useful for external components to point
   * to specific keys.
   * @param note the note
   * @return a Rectangle representing the size and location of the drawn key
   */
  public Rectangle getKeyPosition(Note note) {
    final Rectangle white = _whiteMap.get(note);
    if (white != null)
      return white;
    else
      return _blackMap.get(note);
  }
  
  /**
   * Gets the key which is at a given point.
   * @param x - the x coordinate of the point
   * @param y - the y coordinate of the point
   * @return the key at point (x, y)
   */
  public Note getNoteAt(int x, int y) {
    for (final Map.Entry<Note, Rectangle> entry : _blackMap.entrySet())
      if (entry.getValue().contains(x, y))
        return entry.getKey();
    for (final Map.Entry<Note, Rectangle> entry : _whiteMap.entrySet())
      if (entry.getValue().contains(x, y))
        return entry.getKey();
    return null;
  }
  
  /**
   * Gets the key which is at a given point.
   * @param p - the point
   * @return the key at Point p
   */
  public Note getNoteAt(Point p) {
    return getNoteAt(p.x, p.y);
  }
  /**
   * Gets the key which is at a given horizontal position
   * @param x - the x coordinate of the point.
   * @return the key at horizontal point x
   */
  
  public Note getNoteAt(int x) {
    return getNoteAt(x, 0);
  }
  
  /**
   * Tells this component to start highlighting the given note.
   * @param note - the note to highlight
   * @param color - the color to use
   */
  public void highlightNote(Note note, Color color) {
    _highlightedNotes.put(note, color);
    revalidate();
    repaint();
  }
  
  /**
   * Tells this component to start highlighting the given note.
   * Uses the default color
   * @param note - the note to highlight
   */
  public void highlightNote(Note note) {
    highlightNote(note, HIGHLIGHT_COLOR);
  }
  
  /**
   * Tells this component to start highlighting the note at the given point.
   * @param p - the point of the note to highlight
   */
  public void highlightNoteAt(Point p, Color color) {
    highlightNote(getNoteAt(p), color);
  }
  
  /**
   * Tells this component to stop highlighting the given note.
   * @param note - the note to stop highlighting
   */
  public void unhighlightNote(Note note) {
    _highlightedNotes.remove(note);
    revalidate();
    repaint();
  }
  
  /**
   * Tells this component to stop highlighting the note at the given point.
   * @param p - the point of the note to stop highlighting
   */
  public void unhighlightNoteAt(Point p) {
    unhighlightNote(getNoteAt(p));
  }
  
  /**
   * Tells this component to stop highlighting all notes
   */
  public void unhighlightAll() {
    _highlightedNotes.clear();
    revalidate();
    repaint();
  }
  
  /**
   * Tells this component to start labeling the ngiven note.
   * @param note - the note to label
   */
  public void labelNote(Note note) {
    _labeledNotes.add(note);
    revalidate();
    repaint();
  }
  
  /**
   * Tells this component to start labeling the note at the given point.
   * @param p - the point of the note to start labeling
   */
  public void labelNoteAt(Point p) {
    labelNote(getNoteAt(p));
  }
  
  /**
   * Tells this component to stop labeling the given note.
   * @param note - the note to stop labeling
   */
  public void unlabelNote(Note note) {
    _labeledNotes.remove(note);
    revalidate();
    repaint();
  }
  
  /**
   * Tells this component to stop labeling the note at the given point
   * @param p - the point of the note to stop labeling
   */
  public void unlabelNoteAt(Point p) {
    unlabelNote(getNoteAt(p));
  }
  
  public void unlabelAll() {
    _labeledNotes.clear();
    revalidate();
    repaint();
  }
  
  @Override
  protected void paintComponent(Graphics g) {
    g.setColor(Color.WHITE);
    g.fillRect(0, 0, _size.width, _size.height);
    g.setColor(Color.BLACK);
    
    // draw all white keys
    for (final Map.Entry<Note, Rectangle> entry : _whiteMap.entrySet()) {
      final Note val = entry.getKey();
      final Rectangle rect = entry.getValue();
      if (_highlightedNotes.containsKey(val)) {
        g.setColor(_highlightedNotes.get(val));
        g.fillRect(rect.x, rect.y, rect.width, rect.height);
      }
      g.setColor(Color.BLACK);
      g.drawRect(rect.x, rect.y, rect.width, rect.height);
    }
    
    // draw all black keys
    for (final Map.Entry<Note, Rectangle> entry : _blackMap.entrySet()) {
      final Note val = entry.getKey();
      final Rectangle rect = entry.getValue();
      if (_highlightedNotes.containsKey(val)) {
        g.setColor(_highlightedNotes.get(val));
        g.fillRect(rect.x, rect.y, rect.width, rect.height);
        g.setColor(Color.BLACK);
        g.drawRect(rect.x, rect.y, rect.width-1, rect.height-1);
      }
      else {
        g.setColor(Color.BLACK);
        g.fillRect(rect.x, rect.y, rect.width, rect.height);
      }
    }
    
    // label notes
    if (!_labeledNotes.isEmpty()) {
      final Graphics2D g2d = (Graphics2D) g;
      final int height = getHeight();
      
      final AffineTransform saved = g2d.getTransform();
      g2d.rotate(-Math.PI/2, 0, height);
      
      for (final Note note : _labeledNotes) {
        final Rectangle pos = getKeyPosition(note);
        if (_whiteMap.containsKey(note)) {
          g2d.setColor(Color.BLACK);
          g2d.setFont(LABEL_FONT_WHITE);
          GraphicsUtils.drawString(g2d, note.toString(), 4, height+pos.x+WHITE_WIDTH-2);
        } else {
          g2d.setColor(Color.WHITE);
          g2d.setFont(LABEL_FONT_BLACK);
          GraphicsUtils.drawString(g2d, note.toString(), 4+height-BLACK_HEIGHT, height+pos.x+BLACK_WIDTH-1);
        }
      }
      
      g2d.setTransform(saved);
    }
  }
  
  @Override
  public Dimension getPreferredSize() {
    return _size;
  }
  
  @Override
  public Dimension getMinimumSize() {
    return _size;
  }
  
  @Override
  public Dimension getMaximumSize() {
    return _size;
  }
  
  @Override
  public Dimension getSize() {
    return _size;
  }
  
  @Override
  public int getWidth() {
    return _size.width;
  }
  
  @Override
  public int getHeight() {
    return _size.height;
  }
}
