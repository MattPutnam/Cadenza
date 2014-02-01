package cadenza.gui.trigger;

import java.awt.Component;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import cadenza.core.CadenzaData;
import cadenza.core.trigger.actions.AdvanceAction;
import cadenza.core.trigger.actions.GotoAction;
import cadenza.core.trigger.actions.MetronomeAction;
import cadenza.core.trigger.actions.PanicAction;
import cadenza.core.trigger.actions.ReverseAction;
import cadenza.core.trigger.actions.TriggerAction;
import cadenza.core.trigger.actions.WaitAction;
import cadenza.gui.song.SongPanel;

import common.swing.NonNegativeIntField;
import common.swing.SimpleGrid;
import common.swing.SwingUtils;
import common.swing.VerificationException;
import common.swing.dialog.OKCancelDialog;

@SuppressWarnings("serial")
public class TriggerActionEditDialog extends OKCancelDialog {
	private final CadenzaData _data;
	private final TriggerAction _initial;
	
	private JTabbedPane _tabbedPane;
	private AdvancePane _advancePane;
	private ReversePane _reversePane;
	private GotoPane _gotoPane;
	private WaitPane _waitPane;
	private PanicPane _panicPane;
	private MetronomePane _metronomePane;

	public TriggerActionEditDialog(Component parent, CadenzaData data, TriggerAction initial) {
		super(parent);
		_data = data;
		_initial = initial;
	}

	@Override
	protected JComponent buildContent() {
		_tabbedPane = new JTabbedPane();
		_advancePane = new AdvancePane();
		_reversePane = new ReversePane();
		_gotoPane = new GotoPane();
		_waitPane = new WaitPane();
		_panicPane = new PanicPane();
		_metronomePane = new MetronomePane();
		
		_tabbedPane.addTab("Advance", _advancePane);
		_tabbedPane.addTab("Reverse", _reversePane);
		_tabbedPane.addTab("Go To", _gotoPane);
		_tabbedPane.addTab("Wait", _waitPane);
		_tabbedPane.addTab("Panic", _panicPane);
		_tabbedPane.addTab("Metronome", _metronomePane);
		
		return _tabbedPane;
	}
	
	@Override
	protected void initialize() {
		if (_initial == null)
			return;
		
		if (_initial instanceof AdvanceAction) {
			_tabbedPane.setSelectedComponent(_advancePane);
			_advancePane.initialize((AdvanceAction) _initial);
		} else if (_initial instanceof ReverseAction) {
			_tabbedPane.setSelectedComponent(_reversePane);
			_reversePane.initialize((ReverseAction) _initial);
		} else if (_initial instanceof GotoAction) {
			_tabbedPane.setSelectedComponent(_gotoPane);
			_gotoPane.initialize((GotoAction) _initial);
		} else if (_initial instanceof WaitAction) {
			_tabbedPane.setSelectedComponent(_waitPane);
			_waitPane.initialize((WaitAction) _initial);
		} else if (_initial instanceof PanicAction) {
			_tabbedPane.setSelectedComponent(_panicPane);
			_panicPane.initialize((PanicAction) _initial);
		} else if (_initial instanceof MetronomeAction) {
			_tabbedPane.setSelectedComponent(_metronomePane);
			_metronomePane.initialize((MetronomeAction) _initial);
		}
	}
	
	public TriggerAction getAction() {
		return ((ActionPane<?>) _tabbedPane.getSelectedComponent()).createAction();
	}

	@Override
	protected String declareTitle() {
		return "Create Action";
	}

	@Override
	protected void verify() throws VerificationException {
		((ActionPane<?>) _tabbedPane.getSelectedComponent()).verify();
	}
	
	private abstract class ActionPane<T extends TriggerAction> extends JPanel {
		/**
		 * Verifies the action UI
		 * @throws VerificationException if a UI element has a bad value
		 */
		public void verify() throws VerificationException {}
		
		/**
		 * Initialize the tab with the given action
		 * @param initial the action to imitate
		 */
		public void initialize(T initial) {}
		
		public abstract T createAction();
	}
	
	private class AdvancePane extends ActionPane<AdvanceAction> {
		public AdvancePane() {
			add(new JLabel("Advance to the next cue"));
		}
		
		@Override
		public AdvanceAction createAction() {
			return new AdvanceAction();
		}
	}
	
	private class ReversePane extends ActionPane<ReverseAction> {
		public ReversePane() {
			add(new JLabel("Reverse to the previous cue"));
		}
		
		@Override
		public ReverseAction createAction() {
			return new ReverseAction();
		}
	}
	
	private class GotoPane extends ActionPane<GotoAction> {
		private SongPanel _songPanel;
		private JTextField _measureField;
		
		public GotoPane() {
			_songPanel = new SongPanel(_data.songs, true);
			_measureField = new JTextField(16);
			
			add(new JLabel("Go to "));
			add(_songPanel);
			add(new JLabel("  Measure #"));
			add(_measureField);
		}
		
		@Override
		public void initialize(GotoAction initial) {
			_songPanel.setSelectedSong(initial.getSong());
			_measureField.setText(initial.getMeasure());
		}
		
		@Override
		public void verify() throws VerificationException {
			_songPanel.verify();
			if (_measureField.getText().trim().isEmpty())
				throw new VerificationException("Please enter a measure", _measureField);
		}
		
		@Override
		public GotoAction createAction() {
			return new GotoAction(_songPanel.getSelectedSong(), _measureField.getText().trim());
		}
	}
	
	private class WaitPane extends ActionPane<WaitAction> {
		private NonNegativeIntField _field;
		
		private JRadioButton _millisButton;
		private JRadioButton _beatsButton;
		
		public WaitPane() {
			_field = new NonNegativeIntField();
			_field.setColumns(6);
			
			_millisButton = new JRadioButton("milliseconds");
			_beatsButton = new JRadioButton("beats");
			SwingUtils.groupAndSelectFirst(_millisButton, _beatsButton);
			
			final Box box = Box.createVerticalBox();
			box.add(_millisButton);
			box.add(_beatsButton);
			
			add(new JLabel("Wait "));
			add(_field);
			add(box);
		}
		
		@Override
		public void initialize(WaitAction initial) {
			_field.setInt(initial.getNum());
			if (initial.isMillis())
				_millisButton.setSelected(true);
			else
				_beatsButton.setSelected(true);
		}
		
		@Override
		public void verify() throws VerificationException {
			_field.verify();
		}
		
		@Override
		public WaitAction createAction() {
			return new WaitAction(_field.getInt(), _millisButton.isSelected());
		}
	}
	
	private class PanicPane extends ActionPane<PanicAction> {
		public PanicPane() {
			add(new JLabel("Panic (all notes off)"));
		}
		
		@Override
		public PanicAction createAction() {
			return new PanicAction();
		}
	}
	
	private class MetronomePane extends ActionPane<MetronomeAction> {
		private JRadioButton _startButton;
		private JRadioButton _stopButton;
		private JRadioButton _setBPMButton;
		private JRadioButton _tapButton;
		
		private NonNegativeIntField _bpmField;
		
		public MetronomePane() {
			_startButton = new JRadioButton("Start");
			_stopButton = new JRadioButton("Stop");
			_setBPMButton = new JRadioButton("Set BPM = ");
			_tapButton = new JRadioButton("Tempo Tap");
			SwingUtils.groupAndSelectFirst(_startButton, _stopButton, _setBPMButton, _tapButton);
			
			_bpmField = new NonNegativeIntField();
			_bpmField.setColumns(6);
			
			add(new SimpleGrid(new JComponent[][] {
					{ _startButton,  null      },
					{ _stopButton,   null      },
					{ _setBPMButton, _bpmField },
					{ _tapButton,    null      }
			}));
		}
		
		@Override
		public void initialize(MetronomeAction initial) {
			switch (initial.getType()) {
				case START:   _startButton.setSelected(true);  break;
				case STOP:    _stopButton.setSelected(true);   break;
				case SET_BPM: _setBPMButton.setSelected(true); _bpmField.setInt(initial.getBPM()); break;
				case TAP:     _tapButton.setSelected(true);
			}
		}
		
		@Override
		public void verify() throws VerificationException {
			if (_setBPMButton.isSelected())
				_bpmField.verify();
		}
		
		@Override
		public MetronomeAction createAction() {
			if (_startButton.isSelected())
				return new MetronomeAction(MetronomeAction.Type.START);
			else if (_stopButton.isSelected())
				return new MetronomeAction(MetronomeAction.Type.STOP);
			else if (_setBPMButton.isSelected())
				return new MetronomeAction(_bpmField.getInt());
			else
				return new MetronomeAction(MetronomeAction.Type.TAP);
		}
	}

}
