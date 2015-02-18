package cadenza.gui;

import java.awt.BorderLayout;
import java.util.Collections;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import cadenza.core.effects.Effect;
import cadenza.gui.effects.edit.EffectChainViewerEditor;
import common.swing.SwingUtils;

@SuppressWarnings("serial")
public class EffectMonitor extends JFrame {
  private static final EffectMonitor INSTANCE = new EffectMonitor();
  public static EffectMonitor getInstance() {
    return INSTANCE;
  }
  
  private List<Effect> _effects;
  private EffectChainViewerEditor _viewer;
  
  private EffectMonitor() {
    _effects = Collections.emptyList();
    _viewer = new EffectChainViewerEditor(_effects, false);
    
    setLayout(new BorderLayout());
    add(_viewer, BorderLayout.CENTER);
    
    pack();
    
    setTitle("Current Effects");
    setAlwaysOnTop(true);
    SwingUtils.goInvisibleOnClose(this);
  }
  
  public synchronized void setEffects(List<Effect> effects) {
    _effects = (effects == null) ? Collections.<Effect>emptyList() : effects;
    SwingUtilities.invokeLater(() -> {
      _viewer.setEffects(_effects);
      _viewer.clearInputValues();
    });
  }
  
  public synchronized void notePlayed(final int midiNum, final int velocity) {
    SwingUtilities.invokeLater(() -> {
      int v = velocity;
      for (int i = 0; i < _effects.size(); ++i) {
        _viewer.showInputValue(i, midiNum, v);
        v = _effects.get(i).process(midiNum, v);
      }
    });
  }
}
