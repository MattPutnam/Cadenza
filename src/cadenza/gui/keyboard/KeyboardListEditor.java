package cadenza.gui.keyboard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.apache.commons.lang3.text.WordUtils;
import org.ciscavate.cjwizard.CustomWizardComponent;

import cadenza.core.CadenzaData;
import cadenza.core.Cue;
import cadenza.core.Keyboard;
import cadenza.core.Location;
import cadenza.core.Note;
import cadenza.core.patchusage.PatchUsage;
import cadenza.gui.common.CadenzaTable;
import cadenza.gui.common.HelpButton;

import common.swing.IntField;
import common.swing.SwingUtils;
import common.swing.VerificationException;
import common.swing.dialog.OKCancelDialog;
import common.swing.table.ListTableModel;

@SuppressWarnings("serial")
public class KeyboardListEditor extends JPanel implements CustomWizardComponent {
	private static final String HELP_TEXT =
			"<html>Define the keyboards used.<br><br>" +
			WordUtils.wrap("The <b>range</b> specifies the range of keys that will be shown " +
			"in all editing windows.  This should be the full range of the physical keyboard, " +
			"or even more if you will be spoofing notes beyond that range.", 60, "<br>", false) +
			"<br><br>" +
			WordUtils.wrap("The <b>sounding range</b> specifies the range of notes that will " +
			"play sound.  You can set this to less than the keyboard's actual range to leave " +
			"out unused keys at the extreme ranges and use them to trigger various actions.", 60, "<br>", false) +
			"<br><br>" +
			WordUtils.wrap("The <b>main</b> keyboard is the primary one in your performance. " +
			"It is selected by default in dialogs and cannot be deleted. If another keyboard " +
			"is deleted, all patches assigned to it are moved to the main keyboard.", 60, "<br>", false) +
			"</html>";
	
	private final CadenzaData _data;
	
	private final List<Keyboard> _keyboards;
	private final Keyboard _main;
	private final Map<Keyboard, Keyboard> _remap;
	
	private final KeyboardTable _table;
	
	public KeyboardListEditor(CadenzaData data) {
		_data = data;
		
		_keyboards = new ArrayList<>(_data.keyboards);
		_main = Keyboard.findMain(_keyboards);
		_remap = new HashMap<>();
		
		_table = new KeyboardTable();
		
		setLayout(new BorderLayout());
		add(_table, BorderLayout.CENTER);
	}
	
	public List<Keyboard> getKeyboards() {
		return _keyboards;
	}
	
	public void doRemap() {
		for (final Cue cue : _data.cues) {
			for (final PatchUsage patchUsage : cue.patches) {
				final Keyboard newKeyboard = _remap.get(patchUsage.location.getKeyboard());
				if (newKeyboard != null) {
					patchUsage.location = new Location(patchUsage.location, newKeyboard);
				}
			}
		}
		
		_data.keyboards.clear();
		_data.keyboards.addAll(_keyboards);
	}
	
	private class KeyboardTable extends CadenzaTable<Keyboard> {
		public KeyboardTable() {
			super(_keyboards, true, true, null, Box.createHorizontalStrut(16), new HelpButton(HELP_TEXT));
			
			final TableColumn mainColumn = accessTable().getColumnModel().getColumn(Col.MAIN);
			mainColumn.setCellEditor(new MainCellEditorRenderer());
			mainColumn.setCellRenderer(new MainCellEditorRenderer());
			mainColumn.setPreferredWidth(20);
			
			accessTable().getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			
			accessTable().setRowHeight(24);
		}
		
		@Override
		protected ListTableModel<Keyboard> createTableModel() {
			return new ListTableModel<Keyboard>() {
				@Override
				public String[] declareColumns() {
					return Col.COLUMNS;
				}
				
				@Override
				public Object resolveValue(Keyboard row, int column) {
					switch (column) {
						case Col.NAME:				return row.name;
						case Col.RANGE:				return row.low.toString() + "-" + row.high.toString();
						case Col.SOUNDING_RANGE:	return row.soundingLow.toString() + "-" + row.soundingHigh.toString();
						case Col.MAIN:				return Boolean.valueOf(row.isMain);
						case Col.CHANNEL:			return Integer.valueOf(row.channel);
						default: throw new IllegalStateException("Unknown Column!");
					}
				}
				
				@Override
				public boolean isCellEditable(int row, int column) {
					return column == Col.MAIN;
				}
				
				@Override
				public void setValueAt(Object value, int row, int column) {
					_keyboards.get(row).isMain = ((Boolean) value).booleanValue();
				}
				
				@Override
				public Class<?> getColumnClass(int column) {
					return column == Col.MAIN ? Boolean.class : String.class;
				}
			};
		}
		
		@Override
		protected String declareTypeName() {
			return "keyboard";
		}
		
		@Override
		protected void takeActionOnAdd() {
			final Keyboard newKeyboard = new Keyboard(Keyboard.findFirstAvailableChannel(_keyboards));
			final KeyboardEditDialog dialog = new KeyboardEditDialog(newKeyboard);
			dialog.showDialog();
			if (dialog.okPressed()) {
				_keyboards.add(dialog.getKeyboard());
				if (newKeyboard.isMain)
					for (final Keyboard keyboard : _keyboards)
						if (keyboard != newKeyboard)
							keyboard.isMain = false;
			}
		}
		
		@Override
		protected void takeActionOnEdit(Keyboard keyboard) {
			final KeyboardEditDialog dialog = new KeyboardEditDialog(keyboard);
			dialog.showDialog();
			if (dialog.okPressed()) {
				final Keyboard newKeyboard = dialog.getKeyboard();
				_remap.put(keyboard, newKeyboard);
				
				_keyboards.set(_keyboards.indexOf(keyboard), newKeyboard);
			}
		}
		
		@Override
		protected boolean allowDelete(List<Keyboard> toDelete) {
			// single selection--list is always a single item
			return !toDelete.get(0).isMain;
		}
		
		@Override
		protected void takeActionAfterDelete(List<Keyboard> removed) {
			for (final Keyboard k : removed)
				_remap.put(k, _main);
		}
		
		private class MainCellEditorRenderer extends AbstractCellEditor implements TableCellEditor,
				TableCellRenderer, ActionListener {
			final JPanel _panel;
			final JRadioButton _button;
			
			public MainCellEditorRenderer() {
				_panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
				_button = new JRadioButton();
				_button.addActionListener(this);
				_button.setOpaque(false);
				
				_panel.add(_button);
			}
			
			@Override
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus,
					int row, int column) {
				_button.setSelected(Boolean.TRUE.equals(value));
				_panel.setBackground(isSelected ? accessTable().getSelectionBackground() : accessTable().getBackground());
				return _panel;
			}
			
			@Override
			public Component getTableCellEditorComponent(JTable table,
					Object value, boolean isSelected, int row, int column) {
				_button.setSelected(Boolean.TRUE.equals(value));
				_panel.setBackground(isSelected ? accessTable().getSelectionBackground() : accessTable().getBackground());
				return _panel;
			}
			
			@Override
			public void actionPerformed(ActionEvent e) {
				final Keyboard kbd = _keyboards.get(KeyboardTable.this.accessTable().getSelectedRow());
				for (final Keyboard k : _keyboards)
					k.isMain = (k == kbd);
				stopCellEditing();
			}
			
			@Override
			public Object getCellEditorValue() {
				return Boolean.valueOf(_button.isSelected());
			}
		}
		
		private class KeyboardEditDialog extends OKCancelDialog {
			private final Keyboard _keyboard;
			
			private final JTextField _nameField;
			private final IntField _channelField;
			private final SingleKeyboardPanel _keyboardPanel;
			
			private Location _selectedFullRange;
			private Location _selectedSoundingRange;
			
			private JPanel _rangeArea;
			
			public KeyboardEditDialog(Keyboard keyboard) {
				super(KeyboardListEditor.this);
				
				_keyboard = keyboard;
				_keyboardPanel = new SingleKeyboardPanel(new Note(0), new Note(127));
				
				_nameField = new JTextField(keyboard.name);
				_channelField = new IntField(keyboard.channel, 1, Integer.MAX_VALUE);
				SwingUtils.freezeWidth(_channelField, 50);
				_selectedFullRange = Location.range(keyboard, keyboard.low, keyboard.high);
				_selectedSoundingRange = Location.range(keyboard, keyboard.soundingLow, keyboard.soundingHigh);
			}
			
			@Override
			protected void initialize() {
				setResizable(false);
			}
			
			@Override
			protected JComponent buildContent() {
				final JComboBox<String> combo = new JComboBox<>(new String[] { "Specify Range", "Specify Sounding Range" });
				
				_rangeArea = new JPanel();
				_rangeArea.setLayout(null);
				SwingUtils.freezeSize(_rangeArea, _keyboardPanel.getSize().width, 50);
				
				_keyboardPanel.addKeyboardListener(new KeyboardAdapter() {
					@Override
					public void keyDragged(Note startNote, Note endNote) {
						if (combo.getSelectedIndex() == 0) {
							_selectedFullRange = Location.range(_keyboard, startNote, endNote);
							combo.setSelectedIndex(1);
							rebuildLabels();
						} else {
							_selectedSoundingRange = Location.range(_keyboard, startNote, endNote);
							combo.setSelectedIndex(0);
							rebuildLabels();
						}
					}
				});
				rebuildLabels();
				
				final Box content = Box.createVerticalBox();
				content.add(SwingUtils.buildRow(new JLabel("Name: "), _nameField, new JLabel("  Input channel: "), _channelField));
				content.add(combo);
				content.add(_keyboardPanel);
				content.add(_rangeArea);
				
				return content;
			}
			
			private void rebuildLabels() {
				_rangeArea.removeAll();
				Rectangle r1 = _keyboardPanel.accessKeyboardPanel().getKeyPosition(_selectedFullRange.getLowerOfRange());
				Rectangle r2 = _keyboardPanel.accessKeyboardPanel().getKeyPosition(_selectedFullRange.getUpperOfRange());
				int width = r2.x+r2.width-r1.x;
				final RangePanel fullPanel = new RangePanel("Physical Range", width);
				fullPanel.setBounds(r1.x, 1, width, 24);
				r1 = _keyboardPanel.accessKeyboardPanel().getKeyPosition(_selectedSoundingRange.getLowerOfRange());
				r2 = _keyboardPanel.accessKeyboardPanel().getKeyPosition(_selectedSoundingRange.getUpperOfRange());
				width = r2.x+r2.width-r1.x;
				final RangePanel soundingPanel = new RangePanel("Sounding Range", width);
				soundingPanel.setBounds(r1.x, 26, width, 24);
				
				_rangeArea.add(fullPanel);
				_rangeArea.add(soundingPanel);
				
				final KeyboardPanel kp = _keyboardPanel.accessKeyboardPanel();
				kp.unlabelAll();
				kp.labelNote(Note.A0);
				kp.labelNote(Note.C4);
				kp.labelNote(Note.C8);
				kp.labelNote(_selectedFullRange.getLowerOfRange());
				kp.labelNote(_selectedFullRange.getUpperOfRange());
				kp.labelNote(_selectedSoundingRange.getLowerOfRange());
				kp.labelNote(_selectedSoundingRange.getUpperOfRange());
				
				revalidate();
				repaint();
			}
			
			@Override
			protected String declareTitle() {
				return "Edit Keyboard";
			}
			
			@Override
			protected void verify() throws VerificationException {
				final String name = _nameField.getText().trim();
				if (name.isEmpty())
					throw new VerificationException("Please specify a name", _nameField);
				for (final Keyboard keyboard : _keyboards) {
					if (keyboard != _keyboard && name.equals(keyboard.name))
						throw new VerificationException("A keyboard with this name already exists", _nameField);
				}
				
				if (_channelField.getText().isEmpty())
					throw new VerificationException("Please enter a channel", _channelField);
			}
			
			public Keyboard getKeyboard() {
				// swap channel if conflicting
				final int ch = _channelField.getInt();
				for (final Keyboard keyboard : _keyboards) {
					if (keyboard != _keyboard && keyboard.channel == ch) {
						keyboard.channel = _keyboard.channel;
						break;
					}
				}
				
				return new Keyboard(_selectedFullRange.getLowerOfRange(), _selectedFullRange.getUpperOfRange(),
						_selectedSoundingRange.getLowerOfRange(), _selectedSoundingRange.getUpperOfRange(),
						_nameField.getText().trim(), _keyboard.isMain, ch);
			}
			
			private class RangePanel extends JPanel {
				public RangePanel(String text, int width) {
					super(null);
					setBackground(Color.WHITE);
					setBorder(BorderFactory.createLineBorder(Color.BLACK));
					final JLabel label = new JLabel(text, JLabel.CENTER);
					label.setBounds(0, 0, width, 24);
					add(label);
				}
			}
		}
	}
	
	private static class Col {
		public static final String[] COLUMNS = new String[] {
			"Name", "Range", "Sounding Range", "Main", "Channel"
		};
		
		public static final int NAME = 0;
		public static final int RANGE = 1;
		public static final int SOUNDING_RANGE = 2;
		public static final int MAIN = 3;
		public static final int CHANNEL = 4;
	}

	@Override
	public Object getValue() {
		return _keyboards;
	}
}
