package cadenza.gui.sequencer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import cadenza.core.metronome.Metronome.Subdivision;
import cadenza.core.sequencer.Sequencer;
import cadenza.core.sequencer.Sequencer.NoteChangeBehavior;
import cadenza.gui.common.ScaleSelector;

import common.swing.IntField;
import common.swing.SwingUtils;
import common.swing.VerificationException;
import common.swing.dialog.OKCancelDialog;

@SuppressWarnings("serial")
public class SequencerEditDialog extends OKCancelDialog {
	private final Sequencer _initial;
	
	private JTextField _nameField;
	private JComboBox<Subdivision> _subdivisionCombo;
	private JComboBox<NoteChangeBehavior> _noteChangeCombo;
	
	private ScaleSelector _scaleSelector;
	
	private SequencerGridEditPanel _gridPanel;
	
	private IntField _columnsToAddField;
	
	public SequencerEditDialog(Component parent, Sequencer sequencer) {
		super(parent);
		_initial = sequencer;
	}
	
	@Override
	protected JComponent buildContent() {
		_nameField = new JTextField();
		_subdivisionCombo = new JComboBox<>(Subdivision.values());
		_noteChangeCombo = new JComboBox<>(NoteChangeBehavior.values());
		
		_scaleSelector = new ScaleSelector(_initial == null ? null : _initial.getScale());
		
		if (_initial == null) {
			_gridPanel = new SequencerGridEditPanel(Sequencer.DEFAULT);
		} else {
			_gridPanel = new SequencerGridEditPanel(_initial);
			
			_nameField.setText(_initial.getName());
			_subdivisionCombo.setSelectedItem(_initial.getSubdivision());
			_noteChangeCombo.setSelectedItem(_initial.getNoteChangeBehavior());
		}
		
		_columnsToAddField = new IntField(1, 1, Integer.MAX_VALUE);
		_columnsToAddField.setColumns(3);
		SwingUtils.freezeSize(_columnsToAddField);
		
		final Box north = SwingUtils.buildLeftAlignedRow(
				new JLabel("Name: "), _nameField,
				new JLabel(" Subdivision: "), _subdivisionCombo,
				new JLabel(" On note change: "), _noteChangeCombo);
		
		final Box east = Box.createVerticalBox();
		east.add(SwingUtils.buildLeftAlignedRow(new JButton(new AddColumnsAction()), _columnsToAddField));
		east.add(SwingUtils.hugWest(new JButton(new TrimColumnsAction())));
		east.add(Box.createVerticalStrut(24));
		east.add(SwingUtils.hugWest(new JButton(new AddTopAction())));
		east.add(SwingUtils.hugWest(new JButton(new AddBottomAction())));
		east.add(SwingUtils.hugWest(new JButton(new TrimRowsAction())));
		
		final JPanel content = new JPanel(new BorderLayout());
		content.add(SwingUtils.hugNorth(_scaleSelector), BorderLayout.WEST);
		content.add(new JScrollPane(_gridPanel), BorderLayout.CENTER);
		content.add(SwingUtils.hugNorth(east), BorderLayout.EAST);
		content.add(north, BorderLayout.NORTH);
		return content;
	}
	
	@Override
	protected String declareTitle() {
		return _initial == null ? "Create Sequencer" : "Edit Sequencer";
	}
	
	public Sequencer getSequencer() {
		return new Sequencer(_nameField.getText().trim(), _gridPanel.getGrid(),
				_gridPanel.getIntervals(), _scaleSelector.getSelectedScale(),
				(Subdivision) _subdivisionCombo.getSelectedItem(),
				(NoteChangeBehavior) _noteChangeCombo.getSelectedItem());
	}
	
	@Override
	protected void verify() throws VerificationException {
		if (_nameField.getText().trim().isEmpty())
			throw new VerificationException("Please enter a name", _nameField);
		if (_subdivisionCombo.getSelectedIndex() == -1)
			throw new VerificationException("Please select a subdivision", _subdivisionCombo);
		
		final boolean[][] grid = _gridPanel.getGrid();
		boolean found = false;
		outer:
		for (int row = 0; row < grid.length; ++row) {
			for (int col = 0; col < grid[row].length; ++col) {
				if (grid[row][col]) {
					found = true;
					break outer;
				}
			}
		}
		
		if (!found)
			throw new VerificationException("Please select at least one note to play");
	}
	
	private class AddColumnsAction extends AbstractAction {
		public AddColumnsAction() {
			super("Add columns: ");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			final int col = _columnsToAddField.getInt();
			if (col > 0)
				_gridPanel.addColumns(col);
		}
	}
	
	private class TrimColumnsAction extends AbstractAction {
		public TrimColumnsAction() {
			super("Trim unused columns");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			_gridPanel.trimColumns();
		}
	}
	
	private class AddTopAction extends AbstractAction {
		public AddTopAction() {
			super("Add row to top");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			_gridPanel.addRowToTop();
		}
	}
	
	private class AddBottomAction extends AbstractAction {
		public AddBottomAction() {
			super("Add row to bottom");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			_gridPanel.addRowToBottom();
		}
	}
	
	private class TrimRowsAction extends AbstractAction {
		public TrimRowsAction() {
			super("Trim unused rows");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			_gridPanel.trimUnusedRows();
		}
	}
}
