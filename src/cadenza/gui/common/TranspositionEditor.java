package cadenza.gui.common;

import java.text.NumberFormat;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import common.swing.SwingUtils;

public class TranspositionEditor extends JPanel {
	private static final String[] DIRECTIONS = new String[] {
		"Up",
		"Down"
	};
	
	private static final String[] INTERVALS = new String[] {
		"0  (Unison)",
		"1  (m2)",
		"2  (M2)",
		"3  (m3)",
		"4  (M3)",
		"5  (P4)",
		"6  (TT)",
		"7  (P5)",
		"8  (m6)",
		"9  (M6)",
		"10 (m7)",
		"11 (M7)"
	};
	
	private final JComboBox<String> _direction;
	private final JComboBox<String> _interval;
	private final JTextField _octaveField;

	public TranspositionEditor(int initialValue) {
		_direction = new JComboBox<>(DIRECTIONS);
		_interval = new JComboBox<>(INTERVALS);
		_octaveField = new JFormattedTextField(NumberFormat.getIntegerInstance());
		_octaveField.setColumns(4);
		SwingUtils.freezeSize(_octaveField);
		
		setTransposition(initialValue);
		
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		add(Box.createHorizontalGlue());
		add(new JLabel("Transpose"));
		add(_direction);
		add(_interval);
		add(new JLabel(" plus "));
		add(_octaveField);
		add(new JLabel(" octaves"));
		add(Box.createHorizontalGlue());
	}
	
	public void setTransposition(int value) {
		_direction.setSelectedIndex(value < 0 ? 1 : 0);
		value = Math.abs(value);
		_interval.setSelectedIndex(value % 12);
		_octaveField.setText(String.valueOf(value / 12));
	}
	
	public int getTransposition() {
		final String octave = _octaveField.getText();
		int transposition = (octave.isEmpty() ? 0 : 12*Integer.parseInt(octave));
		transposition += _interval.getSelectedIndex();
		if (_direction.getSelectedIndex() == 1)
			transposition *= -1;
		
		return transposition;
	}

}
