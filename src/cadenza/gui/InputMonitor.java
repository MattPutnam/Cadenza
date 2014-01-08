package cadenza.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.sound.midi.MidiMessage;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import common.midi.MidiUtilities;
import common.swing.SwingUtils;

public class InputMonitor extends JFrame {
	private static final InputMonitor INSTANCE = new InputMonitor();
	public static InputMonitor getInstance() {
		return INSTANCE;
	}
	
	private JTextArea _area;
	private JScrollPane _scrollPane;
	
	private InputMonitor() {
		_area = new JTextArea(20, 48);
		_scrollPane = new JScrollPane(_area);
		
		final JButton clearButton = new JButton(new ClearAction());
		
		setLayout(new BorderLayout());
		add(_scrollPane, BorderLayout.CENTER);
		add(clearButton, BorderLayout.SOUTH);
		
		pack();
		
		setTitle("Input Monitor");
		setAlwaysOnTop(true);
		SwingUtils.goInvisibleOnClose(this);
	}
	
	public synchronized void send(final MidiMessage mm) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				_area.append(MidiUtilities.toString(mm) + "\n");
			}
		});
	}
	
	private class ClearAction extends AbstractAction {
		public ClearAction() {
			super("Clear");
		}
		
		@Override
		public void actionPerformed(ActionEvent _) {
			_area.setText(null);
		}
	}
}
