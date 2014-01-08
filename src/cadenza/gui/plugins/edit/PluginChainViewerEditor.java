package cadenza.gui.plugins.edit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import cadenza.core.plugins.Compressor;
import cadenza.core.plugins.GraphicEQ;
import cadenza.core.plugins.ParametricEQ;
import cadenza.core.plugins.ParametricEQ.Band;
import cadenza.core.plugins.Plugin;
import cadenza.gui.ImageStore;
import cadenza.gui.plugins.view.PluginView;

import common.swing.SwingUtils;
import common.swing.VerificationException;
import common.swing.dialog.Dialog;
import common.swing.dialog.OKCancelDialog;

public class PluginChainViewerEditor extends JPanel {
	private static final int INSERT_SIZE = 40;
	private static final int[] INSERT_xPoints = new int[] {0,  32, 26, 29, 40, 29, 26, 32, 0};
	private static final int[] INSERT_yPoints = new int[] {18, 18, 12, 9,  20, 31, 28, 22, 22};
	private static final Font INSERT_FONT = Font.decode("Arial 9");
	private static final String INSERT_TOP_TEXT = "Click to";
	private static final String INSERT_BOTTOM_TEXT = "add new";
	
	private List<Plugin> _plugins;
	private final boolean _allowEdit;
	
	private List<PluginView> _pluginViews;
	private final JPanel _pluginViewPanel;
	
	public PluginChainViewerEditor(List<Plugin> initial, boolean allowEdit) {
		_plugins = new ArrayList<>(initial.size());
		for (final Plugin plugin : initial)
			_plugins.add(plugin.copy());
		_allowEdit = allowEdit;
		
		_pluginViewPanel = new JPanel();
		_pluginViewPanel.setLayout(new BoxLayout(_pluginViewPanel, BoxLayout.X_AXIS));
		final JScrollPane scrollPane = new JScrollPane(_pluginViewPanel,
				JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setPreferredSize(new Dimension(600, 180));
		
		setLayout(new BorderLayout());
		add(scrollPane, BorderLayout.CENTER);
		refreshPluginViews();
	}
	
	public List<Plugin> getPlugins() {
		return _plugins;
	}
	
	public void setPlugins(List<Plugin> plugins) {
		_plugins = plugins;
		refreshPluginViews();
	}
	
	public void showInputValue(int index, int midiNum, int velocity) {
		_pluginViews.get(index).showInputValue(midiNum, velocity);
	}
	
	public void clearInputValues() {
		for (final PluginView pv : _pluginViews)
			pv.clearInputValue();
	}
	
	private void refreshPluginViews() {
		_pluginViews = new ArrayList<>(_plugins.size());
		for (final Plugin plugin : _plugins) {
			_pluginViews.add(plugin.createView());
		}
		
		_pluginViewPanel.removeAll();
		_pluginViewPanel.add(Box.createHorizontalGlue());
		for (int i = 0; i < _plugins.size(); ++i) {
			_pluginViewPanel.add(new InsertPluginPanel(i));
			
			final PluginView pluginView = _pluginViews.get(i);
			_pluginViewPanel.add(pluginView);
			
			if (_allowEdit) {
				final int finali = i;
				pluginView.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						if (e.getModifiersEx() == InputEvent.CTRL_DOWN_MASK ||
							e.getButton() == MouseEvent.BUTTON2 ||
							e.getButton() == MouseEvent.BUTTON3) {
							final JPopupMenu menu = new JPopupMenu();
							menu.add(new JMenuItem(new EditAction(pluginView, finali)));
							menu.addSeparator();
							menu.add(new JMenuItem(new DeleteAction(finali)));
							menu.show(pluginView, e.getX(), e.getY());
						}
						
						if (e.getClickCount() == 2) {
							final PluginEditor editor = pluginView.createEditor();
							if (OKCancelDialog.showInDialog(PluginChainViewerEditor.this, "", editor)) {
								_plugins.set(finali, editor.getPlugin());
								refreshPluginViews();
							}
						}
					}
				});
			}
		}
		_pluginViewPanel.add(new InsertPluginPanel(_plugins.size()));
		_pluginViewPanel.add(Box.createHorizontalGlue());
		revalidate();
		repaint();
	}
	
	private class InsertPluginPanel extends JPanel {
		public InsertPluginPanel(final int index) {
			super();
			
			SwingUtils.freezeSize(this, INSERT_SIZE, INSERT_SIZE);
			setLayout(null);
			
			if (_allowEdit) {
				addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent _) {
						final AddPluginDialog dialog = new AddPluginDialog();
						dialog.showDialog();
						if (dialog.okPressed()) {
							_plugins.add(index, dialog.getPlugin());
							refreshPluginViews();
						}
					}
				});
			}
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			g.setColor(Color.DARK_GRAY);
			g.fillPolygon(INSERT_xPoints, INSERT_yPoints, INSERT_xPoints.length);
			
			if (_allowEdit) {
				g.setFont(INSERT_FONT);
				g.drawString(INSERT_TOP_TEXT, 4, 8);
				g.drawString(INSERT_BOTTOM_TEXT, 2, 38);
			}
		}
	}
	
	private class AddPluginDialog extends OKCancelDialog {
		private JTabbedPane _tabbed;
		
		public AddPluginDialog() {
			super(PluginChainViewerEditor.this);
		}
		
		@Override
		protected JComponent buildContent() {
			final Band band = new Band(80, 3.0, 0.5);
			
			_tabbed = new JTabbedPane();
			_tabbed.addTab("Compressor/Limiter", new CompressorEditor(new Compressor(80, 2.5)));
			_tabbed.addTab("Parametric EQ", new ParametricEQEditor(new ParametricEQ(Collections.singletonList(band))));
			_tabbed.addTab("128-Band Graphic EQ", new GraphicEQEditor(new GraphicEQ(new int[128])));
			
			return _tabbed;
		}
		
		@Override
		protected void initialize() {
			setResizable(false);
		}
		
		@Override
		protected String declareTitle() {
			return "Add new plugin";
		}
		
		public Plugin getPlugin() {
			return ((PluginEditor) _tabbed.getSelectedComponent()).getPlugin();
		}
		
		@Override
		protected void verify() throws VerificationException { /* no-op */ }
	}
	
	private class EditAction extends AbstractAction {
		private final PluginView _pluginView;
		private final int _index;
		
		public EditAction(PluginView pluginView, int index) {
			super("Edit");
			putValue(SMALL_ICON, ImageStore.EDIT);
			
			_pluginView = pluginView;
			_index = index;
		}
		
		@Override
		public void actionPerformed(ActionEvent _) {
			final PluginEditor editor = _pluginView.createEditor();
			if (OKCancelDialog.showInDialog(PluginChainViewerEditor.this, "", editor)) {
				_plugins.set(_index, editor.getPlugin());
				refreshPluginViews();
			}
		}
	}
	
	private class DeleteAction extends AbstractAction {
		private final int _index;
		
		public DeleteAction(int index) {
			super("Delete");
			putValue(SMALL_ICON, ImageStore.DELETE);
			
			_index = index;
		}
		
		@Override
		public void actionPerformed(ActionEvent _) {
			if (Dialog.confirm(PluginChainViewerEditor.this, "Are you sure you want to delete this plugin?")) {
				_plugins.remove(_index);
				refreshPluginViews();
			}
		}
	}
}
