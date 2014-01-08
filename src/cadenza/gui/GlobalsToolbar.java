package cadenza.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import cadenza.core.CadenzaData;
import cadenza.core.ControlMapEntry;
import cadenza.core.plugins.Plugin;
import cadenza.gui.controlmap.ControlMapPanel;
import cadenza.gui.keyboard.KeyboardListEditor;
import cadenza.gui.plugins.edit.PluginChainViewerEditor;
import cadenza.gui.trigger.TriggerPanel;

import common.swing.dialog.OKCancelDialog;

public class GlobalsToolbar extends JPanel {
	private static final long ERROR_TIMEOUT = 10000;
	
	private final Component _parent;
	private final CadenzaData _data;
	
	private final JLabel _errorLabel;
	private Thread _errorTimeoutThread;
	
	public GlobalsToolbar(Component parent, CadenzaData data) {
		_parent = parent;
		_data = data;
		
		_errorLabel = new JLabel();
		_errorLabel.setForeground(Color.RED);
		
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		add(new JLabel("Edit:"));
		add(new JButton(new KeyboardAction()));
		add(new JButton(new GlobalTriggerAction()));
		add(new JButton(new GlobalControlAction()));
		add(new JButton(new GlobalPluginsAction()));
		add(Box.createHorizontalGlue());
		
		setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
	}
	
	public void showError(Exception e) {
		_errorLabel.setText(e.getMessage());
		if (_errorTimeoutThread != null)
			_errorTimeoutThread.interrupt();
		
		_errorTimeoutThread = new Thread(new ErrorLabelClearer());
		_errorTimeoutThread.start();
	}
	
	private class KeyboardAction extends AbstractAction {
		public KeyboardAction() {
			super("Keyboards");
		}
		
		@Override
		public void actionPerformed(ActionEvent _) {
			final KeyboardListEditor panel = new KeyboardListEditor(_data);
			if (OKCancelDialog.showInDialog(_parent, "Edit Keyboards", panel)) {
				panel.doRemap();
			}
		}
	}
	
	private class GlobalTriggerAction extends AbstractAction {
		public GlobalTriggerAction() {
			super("Global Triggers");
		}
		
		@Override
		public void actionPerformed(ActionEvent _) {
			final TriggerPanel panel = new TriggerPanel(_data, _data);
			if (OKCancelDialog.showInDialog(_parent, "Edit Global Triggers", panel)) {
				_data.globalTriggers.clear();
				_data.globalTriggers.addAll(panel.getTriggers());
			}
		}
	}
	
	private class GlobalControlAction extends AbstractAction {
		public GlobalControlAction() {
			super("Global Control Overrides");
		}
		
		@Override
		public void actionPerformed(ActionEvent _) {
			final ControlMapPanel panel = new ControlMapPanel(_data);
			if (OKCancelDialog.showInDialog(_parent, "Edit Global Control Overrides", panel)) {
				final List<ControlMapEntry> controls = panel.getMapping();
				if (!controls.equals(_data.globalControlMap)) {
					_data.globalControlMap.clear();
					_data.globalControlMap.addAll(controls);
				}
			}
		}
	}
	
	private class GlobalPluginsAction extends AbstractAction {
		public GlobalPluginsAction() {
			super("Global Plugins");
		}
		
		@Override
		public void actionPerformed(ActionEvent _) {
			final PluginChainViewerEditor panel = new PluginChainViewerEditor(_data.globalPlugins, true);
			if (OKCancelDialog.showInDialog(_parent, "Edit Global Plugins", panel)) {
				final List<Plugin> plugins = panel.getPlugins();
				if (!plugins.equals(_data.globalPlugins)) {
					_data.globalPlugins.clear();
					_data.globalPlugins.addAll(panel.getPlugins());
				}
			}
		}
	}
	
	private class ErrorLabelClearer implements Runnable {
		@Override
		public void run() {
			try {
				Thread.sleep(ERROR_TIMEOUT);
			} catch (InterruptedException e) {
				return;
			}
			
			_errorLabel.setText(null);
		}
	}
}
