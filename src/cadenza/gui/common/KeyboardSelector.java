package cadenza.gui.common;

import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import cadenza.core.Keyboard;

@SuppressWarnings("serial")
public class KeyboardSelector extends JPanel {
	private final JComboBox<Keyboard> _combo;
	
	public KeyboardSelector(List<Keyboard> keyboards) {
		this(keyboards, null);
	}
	
	public KeyboardSelector(List<Keyboard> keyboards, Keyboard initial) {
		super();
		
		_combo = new JComboBox<>(keyboards.toArray(new Keyboard[keyboards.size()]));
		if (initial == null) {
			final Keyboard main = Keyboard.findMain(keyboards);
			if (main != null)
				_combo.setSelectedItem(main);
		} else {
			_combo.setSelectedItem(initial);
		}
		
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		add(new JLabel("Keyboard:"));
		add(_combo);
	}
	
	public Keyboard getSelectedKeyboard() {
		return _combo.getItemAt(_combo.getSelectedIndex());
	}
	
	public void setSelectedKeyboard(Keyboard keyboard) {
		_combo.setSelectedItem(keyboard);
	}
	
	public int getSelectedIndex() {
		return _combo.getSelectedIndex();
	}
	
	public void addActionListener(ActionListener listener) {
		_combo.addActionListener(listener);
	}
}
