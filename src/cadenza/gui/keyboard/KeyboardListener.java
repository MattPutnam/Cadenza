package cadenza.gui.keyboard;

import cadenza.core.Note;

/**
 * Listener for key click/drag events on a KeyboardPanel.
 * 
 * 
 * @author Matt Putnam
 */
public interface KeyboardListener
{
  /**
   * Called when a key is depressed.
   * @param note the note pressed
   */
  public void keyPressed(Note note);
  
  /**
   * Called when a key is released
   * @param note the note released
   */
  public void keyReleased(Note note);
  
  /**
   * Called when a key is released, and the press was also
   * on the given keyboard.  This method is called after
   * keyReleased.
   * @param startNote the note starting the drag
   * @param endNote the note ending the drag
   */
  public void keyDragged(Note startNote, Note endNote);
  
  /**
   * Called when a key is clicked, that is, pressed and released.
   * This method is called after keyReleased.
   * @param note the note clicked
   */
  public void keyClicked(Note note);
}
