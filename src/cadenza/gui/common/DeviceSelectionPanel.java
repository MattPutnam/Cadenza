package cadenza.gui.common;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;

import common.swing.DocumentAdapter;

@SuppressWarnings("serial")
public class DeviceSelectionPanel extends JPanel {
	private final DeviceCombo _combo;
	private final JTextField _textField;
	private final List<ChangeListener> _listeners;
	private boolean _isOther;
	
	public DeviceSelectionPanel(String initial) {
		super();
		_combo = new DeviceCombo(initial);
		_textField = new JTextField(24);
		_listeners = new LinkedList<>();
		
		final String OTHER = "Other...";
		_combo.addItem(OTHER);
		_combo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (_combo.getSelectedItem() == OTHER) {
					_textField.setVisible(true);
					_textField.requestFocus();
					revalidate();
					_isOther = true;
				} else {
					_textField.setVisible(false);
					revalidate();
					_isOther = false;
				}
				
				notifyListeners(new ChangeEvent(_combo));
			}
		});
		
		_textField.getDocument().addDocumentListener(new DocumentAdapter() {
			@Override
			public void documentChanged(DocumentEvent e) {
				notifyListeners(new ChangeEvent(_textField));
			}
		});
		
		_textField.setVisible(false);
		_isOther = false;
		
		add(_combo);
		add(_textField);
	}
	
	public String getDevice() {
		if (_isOther) {
			return _textField.getText().trim();
		} else {
			return _combo.getItemAt(_combo.getSelectedIndex());
		}
	}
	
	public void addChangeListener(ChangeListener listener) {
		_listeners.add(listener);
	}
	
	private void notifyListeners(ChangeEvent event) {
		for (final ChangeListener listener : _listeners) {
			listener.stateChanged(event);
		}
	}
}
