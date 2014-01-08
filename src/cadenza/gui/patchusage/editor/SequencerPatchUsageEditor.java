package cadenza.gui.patchusage.editor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import cadenza.core.CadenzaData;
import cadenza.core.Scale;
import cadenza.core.patchusage.SequencerPatchUsage;
import cadenza.core.sequencer.Sequencer;
import cadenza.gui.common.CadenzaTable;
import cadenza.gui.sequencer.SequencerEditDialog;

import common.swing.VerificationException;
import common.swing.table.ListTableModel;

public class SequencerPatchUsageEditor extends JPanel {
	private final CadenzaData _data;
	private final SequencerTable _table;
	
	public SequencerPatchUsageEditor(CadenzaData data) {
		super();
		_data = data;
		_table = new SequencerTable();
		
		setLayout(new BorderLayout());
		add(_table, BorderLayout.CENTER);
	}
	
	public void populate(SequencerPatchUsage initialPatchUsage) {
		final int index = _data.sequencers.indexOf(initialPatchUsage.sequencer);
		if (index != -1)
			_table.accessTable().setRowSelectionInterval(index, index);
	}
	
	public void verify() throws VerificationException {
		if (_table.accessTable().getSelectedRow() == -1)
			throw new VerificationException("Please select a sequencer");
	}
	
	public Sequencer getSequencer() {
		return _data.sequencers.get(_table.accessTable().getSelectedRow());
	}
	
	private class SequencerTable extends CadenzaTable<Sequencer> {
		public SequencerTable() {
			// have to set the data after setting the renderer to initially render properly
			super(new ArrayList<Sequencer>(), true, true);
			
			accessTable().getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			
			final TableColumnModel tcm = accessTable().getColumnModel();
			tcm.getColumn(0).setPreferredWidth(100);
			tcm.getColumn(1).setPreferredWidth(500);
			tcm.getColumn(2).setPreferredWidth(100);
			
			tcm.getColumn(1).setCellRenderer(new PreviewColumnRenderer());
			
			accessTableModel().setList(_data.sequencers);
		}

		@Override
		protected ListTableModel<Sequencer> createTableModel() {
			return new ListTableModel<Sequencer>() {

				@Override
				public String[] declareColumns() {
					return new String[] { "Name", "Pattern", "Subdivision", "Scale" };
				}

				@Override
				public Object resolveValue(Sequencer row, int column) {
					switch (column) {
						case 0: return row.getName();
						case 1: return row; // handled by renderer
						case 2: return row.getSubdivision();
						case 3: final Scale scale = row.getScale();
								return scale == null ? "None-chromatic" : scale.getName();
						default: throw new IllegalStateException("Unknown Column!");
					}
				}
				
			};
		}

		@Override
		protected void takeActionOnAdd() {
			final SequencerEditDialog dialog = new SequencerEditDialog(SequencerPatchUsageEditor.this, Sequencer.DEFAULT);
			dialog.showDialog();
			if (dialog.okPressed()) {
				_data.sequencers.add(dialog.getSequencer());
			}
		}
		
		@Override
		protected void takeActionOnEdit(Sequencer item) {
			final SequencerEditDialog dialog = new SequencerEditDialog(SequencerPatchUsageEditor.this, item);
			dialog.showDialog();
			if (dialog.okPressed()) {
				_data.sequencers.set(_data.sequencers.indexOf(item), dialog.getSequencer());
			}
		}

		@Override
		protected String declareTypeName() {
			return "sequencer";
		}
		
		private class PreviewColumnRenderer implements TableCellRenderer {
			@Override
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus,
					int row, int column) {
				return ((Sequencer) value).getPreviewPanel();
			}
		}
	}
}
