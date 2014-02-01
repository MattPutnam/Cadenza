package cadenza.gui.sequencer;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import cadenza.core.sequencer.Sequencer;

import common.swing.SimpleGrid;
import common.swing.SwingUtils;

@SuppressWarnings("serial")
public class SequencerGridPreviewPanel extends JPanel {
	private static final int GRID_SIZE = 8;
	private static final Color ON = Color.GREEN;
	private static final Color OFF = Color.RED;
	
	public SequencerGridPreviewPanel(Sequencer sequencer) {
		final JPanel[][] grid = new JPanel[sequencer.getNotes().length][sequencer.getLength()];
		for (int note = 0; note < sequencer.getNotes().length; ++note) {
			for (int index = 0; index < sequencer.getLength(); ++index) {
				final JPanel panel = new JPanel();
				SwingUtils.freezeSize(panel, GRID_SIZE, GRID_SIZE);
				panel.setBackground(sequencer.isOn(note, index) ? ON : OFF);
				panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
				grid[note][index] = panel;
			}
		}
		
		setLayout(new BorderLayout());
		add(new SimpleGrid(grid));
		SwingUtils.freezeSize(this, grid[0].length * GRID_SIZE, grid.length * GRID_SIZE);
		validate();
	}
}
