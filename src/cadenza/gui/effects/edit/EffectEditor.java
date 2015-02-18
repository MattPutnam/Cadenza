package cadenza.gui.effects.edit;

import javax.swing.JPanel;

import cadenza.core.effects.Effect;

@SuppressWarnings("serial")
public abstract class EffectEditor extends JPanel {
  public abstract Effect getEffect();
}
