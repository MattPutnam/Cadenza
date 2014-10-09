package cadenza.gui.plugins.edit;

import javax.swing.JPanel;

import cadenza.core.plugins.Plugin;

@SuppressWarnings("serial")
public abstract class PluginEditor extends JPanel {
  public abstract Plugin getPlugin();
}
