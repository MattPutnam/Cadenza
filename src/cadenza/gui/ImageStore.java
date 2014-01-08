package cadenza.gui;

import java.io.File;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public class ImageStore {
	private static final String IMG_PATH = "resources" + File.separator + "icons" + File.separator;
	
	public static final ImageIcon APPLICATION_ICON;
	
	public static final ImageIcon ADD;
	public static final ImageIcon EDIT;
	public static final ImageIcon DELETE;
	public static final ImageIcon CLONE;
	public static final ImageIcon REPLACE;
	
	public static final ImageIcon UP_ARROW;
	public static final ImageIcon DOWN_ARROW;
	
	public static final ImageIcon HELP;
	public static final Icon HELP_LARGE;
	public static final ImageIcon SEARCH;

	static {
		APPLICATION_ICON = getImageIcon("add.png", "app icon"); // TODO fix app icon
		
		ADD = getImageIcon("add.png", "add");
		EDIT = getImageIcon("edit.png", "edit");
		DELETE = getImageIcon("delete.png", "delete");
		CLONE = getImageIcon("clone.png", "clone");
		REPLACE = getImageIcon("replace.png", "replace");
		
		UP_ARROW = getImageIcon("uparrow.png", "up arrow");
		DOWN_ARROW = getImageIcon("downarrow.png", "down arrow");
		
		HELP = getImageIcon("help.png", "help");
		HELP_LARGE = getImageIcon("help_large.png", "help");
		SEARCH = getImageIcon("search.png", "search");
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
