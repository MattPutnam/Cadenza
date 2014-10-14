package cadenza.gui.preferences;

import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.MidiDevice.Info;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import common.midi.MidiPortFinder;
import common.swing.SimpleGrid;

@SuppressWarnings("serial")
public class DefaultMIDIPortsPanel extends JPanel {
  private List<String> _inputNames;
  private List<String> _outputNames;
  
  private JComboBox<String> _inputCombo;
  private JComboBox<String> _outputCombo;
  
  public DefaultMIDIPortsPanel() {
    final List<Info> inputInfos = MidiPortFinder.getInputMidiDeviceInfos();
    final List<Info> outputInfos = MidiPortFinder.getOutputMidiDeviceInfos();
    
    final int inputSize = inputInfos.size();
    final int outputSize = outputInfos.size();
    
    _inputNames = new ArrayList<>(inputSize);
    _outputNames = new ArrayList<>(outputSize);
    
    for (final Info info : inputInfos)
      _inputNames.add(info.getName());
    for (final Info info : outputInfos)
      _outputNames.add(info.getName());
    
    _inputCombo = new JComboBox<>(_inputNames.toArray(new String[inputSize]));
    _outputCombo = new JComboBox<>(_outputNames.toArray(new String[outputSize]));
    
    add(new SimpleGrid(new JComponent[][]
    {
      { new JLabel("Input:  "), _inputCombo  },
      { new JLabel("Output: "), _outputCombo }
    }));
  }
  
  // TODO: add rescan ability
  
  public void match(String[] ports) {
    if (_inputNames.contains(ports[0])) {
      _inputCombo.setSelectedItem(ports[0]);
    } else {
      _inputCombo.insertItemAt(ports[0], 0);
      _inputCombo.setSelectedIndex(0);
    }
    
    if (_outputNames.contains(ports[1])) {
      _outputCombo.setSelectedItem(ports[1]);
    } else {
      _outputCombo.insertItemAt(ports[1], 0);
      _outputCombo.setSelectedIndex(0);
    }
  }
  
  public String[] getSelectedPorts() {
    return new String[] {(String) _inputCombo.getSelectedItem(), (String) _outputCombo.getSelectedItem()};
  }
}
