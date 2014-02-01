package cadenza.gui.trigger;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JPanel;

import org.apache.commons.lang3.text.WordUtils;
import org.ciscavate.cjwizard.CustomWizardComponent;

import cadenza.core.CadenzaData;
import cadenza.core.trigger.Trigger;
import cadenza.gui.common.CadenzaTable;
import cadenza.gui.common.HelpButton;

import common.Utils;
import common.swing.table.ListTableModel;

@SuppressWarnings("serial")
public class TriggerPanel extends JPanel implements CustomWizardComponent {
	private static final String HELP_TEXT = "<html>Define the triggers used.<br><br>" +
			WordUtils.wrap("Triggers listen for various inputs and fire actions in response.  " +
			"Globally, set a pair of keys to go forward or backward in the performance, or use " +
			"a pedal for this purpose by listening for control change messages.  Automatically " +
			"advance to the next cue by listening for a particular note to be pressed or " +
			"released.", 60, "<br>", false);
	
	private final CadenzaData _data;
	private final List<Trigger> _triggers;
	private final TriggerTable _table;
	
	public TriggerPanel(HasTriggers hasTriggers, CadenzaData data) {
		super();
		_data = data;
		
		_triggers = new ArrayList<>(hasTriggers.getTriggers());
		_table = new TriggerTable();
		
		setLayout(new BorderLayout());
		add(_table, BorderLayout.CENTER);
	}
	
	private class TriggerTable extends CadenzaTable<Trigger> {
		public TriggerTable() {
			super(_triggers, true, true, null, Box.createHorizontalStrut(16), new HelpButton(HELP_TEXT));
		}
		
		@Override
		protected ListTableModel<Trigger> createTableModel() {
			return new ListTableModel<Trigger>() {
				@Override
				public String[] declareColumns() {
					return new String[]{"Conditions", "Operator", "Actions"};
				}
				
				@Override
				public Object resolveValue(Trigger row, int column) {
					switch (column) {
						case 0: return Utils.mkString(row.predicates);
						case 1: return row.predicates.size() == 1 ? "" :
											row.AND ? (row.inorder ? "AND (in order)" : "AND") : "OR";
						case 2: return Utils.mkString(row.actions);
						default: throw new IllegalStateException();
					}
				}
			};
		}
		
		@Override
		protected String declareTypeName() {
			return "trigger";
		}
		
		@Override
		protected void takeActionOnAdd() {
			final Trigger toAdd = new Trigger();
			final TriggerEditDialog editor = new TriggerEditDialog(TriggerPanel.this, toAdd, _data);
			editor.showDialog();
			if (editor.okPressed()) {
				_triggers.add(toAdd);
			}
		}
		
		@Override
		protected void takeActionOnEdit(Trigger item) {
			final TriggerEditDialog editor = new TriggerEditDialog(TriggerPanel.this, item, _data);
			editor.showDialog();
		}
	}
	
	public List<Trigger> getTriggers() {
		return _triggers;
	}

	@Override
	public Object getValue() {
		return _triggers;
	}

}
