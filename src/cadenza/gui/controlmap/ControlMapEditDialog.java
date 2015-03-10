package cadenza.gui.controlmap;

import java.awt.Component;
import java.util.List;
import java.util.stream.IntStream;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import cadenza.control.midiinput.AcceptsKeyboardInput;
import cadenza.control.midiinput.MIDIInputControlCenter;
import cadenza.core.ControlMapEntry;
import cadenza.core.patchusage.PatchUsage;
import cadenza.preferences.Preferences;
import common.swing.SimpleGrid;
import common.swing.SwingUtils;
import common.swing.VerificationException;
import common.swing.dialog.OKCancelDialog;

@SuppressWarnings("serial")
public class ControlMapEditDialog extends OKCancelDialog implements AcceptsKeyboardInput {
  private final List<PatchUsage> _patchUsages;
  private final ControlMapEntry _entry;
  
  private JList<Integer> _fromList;
  private JList<Integer> _toList;
  private JList<PatchUsage> _patchUsageList;
  
  public ControlMapEditDialog(Component parent, List<PatchUsage> patchUsages, ControlMapEntry entry) {
    super(parent);
    
    _patchUsages = patchUsages;
    _entry = entry;
    
    if (Preferences.allowMIDIInput())
      MIDIInputControlCenter.installWindowFocusGrabber(this);
  }
  
  @Override
  protected JComponent buildContent() {
    final Integer[] ints = IntStream.range(0, 128).boxed().toArray(Integer[]::new);
    
    _fromList = new JList<>(ints);
    _fromList.setCellRenderer(new ControlListRenderer());
    _fromList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    _toList = new JList<>(ints);
    _toList.setCellRenderer(new ControlListRenderer());
    _toList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    _patchUsageList = new JList<>(_patchUsages == null
        ? new PatchUsage[] {PatchUsage.ALL}
        : _patchUsages.toArray(new PatchUsage[_patchUsages.size()]));
    _patchUsageList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    
    if (_entry != null) {
      _fromList.setSelectedIndex(_entry.sourceCC);
      
      final int[] ta = new int[_entry.destCCs.size()];
      for (int i = 0; i < ta.length; ++i)
        ta[i] = _entry.destCCs.get(i).intValue();
      _toList.setSelectedIndices(ta);
      
      if (_patchUsages != null) {
        final int[] pa = new int[_entry.destPatches.size()];
        for (int i = 0; i < pa.length; ++i)
          pa[i] = _patchUsages.indexOf(_entry.destPatches.get(i));
        _patchUsageList.setSelectedIndices(pa);
      }
    }
    
    if (_patchUsages == null)
      _patchUsageList.setSelectedIndex(0);
    
    final JScrollPane fromPane = new JScrollPane(_fromList);
    final JScrollPane toPane = new JScrollPane(_toList);
    final JScrollPane patchPane = new JScrollPane(_patchUsageList);
    SwingUtils.freezeWidth(fromPane, 300);
    SwingUtils.freezeWidth(toPane, 300);
    SwingUtils.freezeWidth(patchPane, 300);
    
    return new SimpleGrid(new JComponent[][]
    {
      { new JLabel("From:"), new JLabel("To:"), new JLabel("On Patches:") },
      { fromPane,            toPane,            patchPane                 }
    });
  }
  
  @Override
  protected void initialize() {
    setSize(900, 400);
  }
  
  @Override
  public void controlReceived(int channel, int ccNumber, int value) {
    _fromList.setSelectedIndex(ccNumber);
  }
  
  @Override
  protected String declareTitle() {
    return _entry == null ? "Create Control Map Entry" : "Edit Control Map Entry";
  }
  
  @Override
  protected void verify() throws VerificationException {
    if (_fromList.getSelectedIndex() == -1)
      throw new VerificationException("Please select an input control", _fromList);
    if (_toList.getSelectedIndex() == -1)
      throw new VerificationException("Please select one or more output controls", _toList);
    if (_patchUsageList.getSelectedIndex() == -1)
      throw new VerificationException("Please select one or more patches", _patchUsageList);
  }
  
  public ControlMapEntry getEntry() {
    final int from = _fromList.getSelectedValue().intValue();
    final List<Integer> to = _toList.getSelectedValuesList();
    final List<PatchUsage> patches = _patchUsageList.getSelectedValuesList();
    
    return new ControlMapEntry(from, to, patches);
  }
}
