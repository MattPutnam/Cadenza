package cadenza.gui.common;

import javax.swing.JComboBox;

import cadenza.synths.Synthesizers;


@SuppressWarnings("serial")
public class SynthCombo extends JComboBox<String> {
  public SynthCombo(String initialSelection) {
    super(Synthesizers.SYNTH_NAMES.toArray(new String[Synthesizers.SYNTH_NAMES.size()]));
    if (initialSelection != null) {
      setSelectedItem(initialSelection);
    }
  }
  
  public String getSynth() {
    return getItemAt(getSelectedIndex());
  }
}
