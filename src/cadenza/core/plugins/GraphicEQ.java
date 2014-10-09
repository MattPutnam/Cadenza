package cadenza.core.plugins;

import java.util.Arrays;

import cadenza.gui.plugins.view.GraphicEQView;
import cadenza.gui.plugins.view.PluginView;

public class GraphicEQ implements Plugin {
  private static final long serialVersionUID = 1L;
  
  private int[] _levels;
  
  public GraphicEQ(int[] levels) {
    setLevels(levels);
  }
  
  public void setLevel(int num, int level) {
    _levels[num] = level;
  }
  
  public int[] getLevels() {
    return Arrays.copyOf(_levels, 128);
  }
  
  public void setLevels(int[] levels) {
    _levels = Arrays.copyOf(levels, 128);
  }
  
  @Override
  public int process(int midiNumber, int velocity) {
    return velocity + _levels[midiNumber];
  }
  
  @Override
  public Plugin copy() {
    return new GraphicEQ(getLevels());
  }
  
  @Override
  public PluginView createView() {
    return new GraphicEQView(this);
  }
  
  @Override
  public String toString() {
    return "128bandGEQ";
  }

}
