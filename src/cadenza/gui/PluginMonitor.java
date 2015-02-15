package cadenza.gui;

import java.awt.BorderLayout;
import java.util.Collections;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import cadenza.core.plugins.Plugin;
import cadenza.gui.plugins.edit.PluginChainViewerEditor;

import common.swing.SwingUtils;

@SuppressWarnings("serial")
public class PluginMonitor extends JFrame {
  private static final PluginMonitor INSTANCE = new PluginMonitor();
  public static PluginMonitor getInstance() {
    return INSTANCE;
  }
  
  private List<Plugin> _plugins;
  private PluginChainViewerEditor _viewer;
  
  private PluginMonitor() {
    _plugins = Collections.emptyList();
    _viewer = new PluginChainViewerEditor(_plugins, false);
    
    setLayout(new BorderLayout());
    add(_viewer, BorderLayout.CENTER);
    
    pack();
    
    setTitle("Current Plugins");
    setAlwaysOnTop(true);
    SwingUtils.goInvisibleOnClose(this);
  }
  
  public synchronized void setPlugins(List<Plugin> plugins) {
    _plugins = (plugins == null) ? Collections.<Plugin>emptyList() : plugins;
    SwingUtilities.invokeLater(() -> {
      _viewer.setPlugins(_plugins);
      _viewer.clearInputValues();
    });
  }
  
  public synchronized void notePlayed(final int midiNum, final int velocity) {
    SwingUtilities.invokeLater(() -> {
      int v = velocity;
      for (int i = 0; i < _plugins.size(); ++i) {
        _viewer.showInputValue(i, midiNum, v);
        v = _plugins.get(i).process(midiNum, v);
      }
    });
  }
}
