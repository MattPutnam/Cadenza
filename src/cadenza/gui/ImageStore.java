package cadenza.gui;

import java.io.File;

import javax.swing.ImageIcon;

public class ImageStore {
  private static final String IMG_PATH = "resources" + File.separator + "icons" + File.separator;
  
  public static final ImageIcon APPLICATION_ICON;
  
  public static final ImageIcon ADD;
  public static final ImageIcon EDIT;
  public static final ImageIcon DELETE;
  public static final ImageIcon CLONE;
  public static final ImageIcon REPLACE;
  public static final ImageIcon SELECT;
  
  public static final ImageIcon UP_ARROW;
  public static final ImageIcon DOWN_ARROW;
  public static final ImageIcon LEFT_ARROW;
  public static final ImageIcon RIGHT_ARROW;
  
  public static final ImageIcon BULLET_SELECTED;
  public static final ImageIcon BULLET_UNSELECTED;
  
  public static final ImageIcon HELP;
  public static final ImageIcon WARNING;
  public static final ImageIcon ERROR;
  public static final ImageIcon SEARCH;
  public static final ImageIcon USB;

  static {
    APPLICATION_ICON = getImageIcon("logo.png", "app icon");
    
    ADD = getImageIcon("cdisc_add.png", "add");
    EDIT = getImageIcon("cdisc_edit.png", "edit");
    DELETE = getImageIcon("cdisc_remove.png", "delete");
    CLONE = getImageIcon("cdisc_clone.png", "clone");
    REPLACE = getImageIcon("cdisc_replace.png", "replace");
    SELECT = getImageIcon("cdisc_select.png", "select");
    
    UP_ARROW = getImageIcon("cdisc_arrow_up.png", "up arrow");
    DOWN_ARROW = getImageIcon("cdisc_arrow_down.png", "down arrow");
    LEFT_ARROW = getImageIcon("cdisc_arrow_left.png", "down arrow");
    RIGHT_ARROW = getImageIcon("cdisc_arrow_right.png", "down arrow");
    
    BULLET_SELECTED = getImageIcon("cdisc_bullet_prime.png", "bullet selected");
    BULLET_UNSELECTED = getImageIcon("cdisc_bullet_sub.png", "bullet unselected");
    
    HELP = getImageIcon("cdisc_help.png", "help");
    WARNING = getImageIcon("cdisc_warning.png", "warning");
    ERROR = getImageIcon("cdisc_error.png", "error");
    SEARCH = getImageIcon("cdisc_search.png", "search");
    USB = getImageIcon("cdisc_iousb.png", "USB I/O");
  }
  
  private static ImageIcon getImageIcon(String filename, String description) {
    final String fullPath = IMG_PATH + filename;
    final File file = new File(fullPath);
    if (file.exists())
      return new ImageIcon(fullPath, description);
    else {
      System.err.println("No image found: " + fullPath);
      return null;
    }
  }
}
