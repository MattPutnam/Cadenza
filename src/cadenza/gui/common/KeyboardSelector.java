package cadenza.gui.common;

import java.util.List;

import javax.swing.JComboBox;

import cadenza.core.Keyboard;

@SuppressWarnings("serial")
public class KeyboardSelector extends JComboBox<Keyboard> {
  public KeyboardSelector(List<Keyboard> keyboards) {
    this(keyboards, null);
  }
  
  public KeyboardSelector(List<Keyboard> keyboards, Keyboard initial) {
    super(keyboards.toArray(new Keyboard[keyboards.size()]));
    
    if (initial == null) {
      final Keyboard main = Keyboard.findMain(keyboards);
      if (main != null)
        setSelectedItem(main);
    } else {
      setSelectedItem(initial);
    }
  }
  
  public Keyboard getSelectedKeyboard() {
    return getItemAt(getSelectedIndex());
  }
}
