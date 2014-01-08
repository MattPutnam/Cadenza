package cadenza.gui.plugins.view;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JPanel;

import cadenza.gui.plugins.edit.PluginEditor;

public abstract class PluginView extends JPanel {
	public static final int MARGIN = 14;
	
	public static final Color BACKGROUND = Color.WHITE;
	public static final Color AXES = Color.BLACK;
	public static final Color DATA = Color.BLUE;
	public static final Color INPUT = Color.BLUE.brighter();
	public static final Color INPUT_GR = Color.RED;
	
	public static final Font AXIS_FONT = Font.decode("Arial 10");
	
	public static final String AXIS_LABEL_INPUT_VELOCITY = "Input Velocity";
	public static final String AXIS_LABEL_INPUT_MIDINUM = "Input MIDI Number";
	
	public static final String AXIS_LABEL_OUTPUT_VELOCITY = "Output Velocity";
	
	protected int _midiNum = -1;
	protected int _velocity = -1;
	
	public void showInputValue(int midiNum, int velocity) {
		_midiNum = midiNum;
		_velocity = velocity;
		repaint();
	}
	
	public void clearInputValue() {
		_midiNum = -1;
		_velocity = -1;
		repaint();
	}
	
	public abstract PluginEditor createEditor();
}
