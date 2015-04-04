package cadenza.gui.controlmap;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.Box;
import javax.swing.JPanel;

import org.apache.commons.lang3.text.WordUtils;
import org.ciscavate.cjwizard.CustomWizardComponent;

import cadenza.core.ControlMapEntry;
import cadenza.core.ControlMapProvider;
import cadenza.core.ControlNames;
import cadenza.core.patchusage.PatchUsage;
import cadenza.gui.common.CadenzaTable;
import cadenza.gui.common.HelpButton;
import common.swing.dialog.OKCancelDialog;
import common.swing.table.ListTableModel;

@SuppressWarnings("serial")
public class ControlMapPanel extends JPanel implements CustomWizardComponent {
  private static final String HELP_TEXT = "<html>Define control map overrides.<br><br>" +
      WordUtils.wrap("By default, all control signals are sent to all active patches.  " +
      "Add a control map entry to override a particular control signal to behave as " +
      "another, or to only send certain controls to certain patches.  For example, if " +
      "your MIDI controller only has a volume fader, but you need modulation control for " +
      "a particular patch, map volume to modulation.", 60, "<br>", false) + "<br><br>" +
      WordUtils.wrap("When a control change is received by Cadenza, first control map " +
      "for the cue is checked, and if an override is found then it is used.  If it is " +
      "not found, the global control map is checked.  If no matching items are found, " +
      "the signal is sent for all current patches.", 60, "<br>", false) + "<br><br>" +
      WordUtils.wrap("It is not recommended to override the sustain pedal (CC#64) as this " +
      "can cause notes to get stuck if the cue is changed while the pedal is depressed.", 60, "<br>", false);
  
  private final List<PatchUsage> _patchUsages;
  private final List<ControlMapEntry> _selectedMapping;
  
  private final ControlMapTable _table;
  
  public ControlMapPanel(ControlMapProvider provider) {
    _patchUsages = provider.getPatchUsages();
    _selectedMapping = new ArrayList<>(provider.getControlMap());
    
    _table = new ControlMapTable();
    
    setLayout(new BorderLayout());
    add(_table, BorderLayout.CENTER);
  }
  
  private class ControlMapTable extends CadenzaTable<ControlMapEntry> {
    public ControlMapTable() {
      super(_selectedMapping, true, false, null, Box.createHorizontalStrut(16), new HelpButton(HELP_TEXT));
    }
    
    @Override
    protected ListTableModel<ControlMapEntry> createTableModel() {
      return new ListTableModel<ControlMapEntry>() {
        @Override
        public String[] declareColumns() {
          return new String[] {"Input Control Change", "Output Control Change(s)", "Affected Patches"};
        }
        
        @Override
        public Object resolveValue(ControlMapEntry row, int column) {
          switch (column) {
            case 0: return row.sourceCC + ": " + ControlNames.getName(row.sourceCC);
            case 1: return row.getDestCCString();
            case 2: return row.getDestPatchesString();
            default: throw new IllegalStateException("Unknown Column!");
          }
        }
      };
    }
    
    @Override
    protected String declareTypeName() {
      return "control map entry";
    }
    
    @Override
    protected void takeActionOnAdd() {
      final ControlMapEditDialog editor = new ControlMapEditDialog(ControlMapPanel.this, _patchUsages, null);
      editor.showDialog();
      if (editor.okPressed()) {
        _selectedMapping.add(editor.getEntry());
        _selectedMapping.sort(COMPARATOR);
      }
    }
    
    @Override
    protected void takeActionOnEdit(ControlMapEntry item) {
      final int index = _selectedMapping.indexOf(item);
      OKCancelDialog.showDialog(new ControlMapEditDialog(ControlMapPanel.this, _patchUsages, item), dialog -> {
        _selectedMapping.set(index, dialog.getEntry());
        _selectedMapping.sort(COMPARATOR);
      });
    }
  }

  public List<ControlMapEntry> getMapping() {
    return _selectedMapping;
  }
  
  private static final Comparator<ControlMapEntry> COMPARATOR = (cme1, cme2) -> cme1.sourceCC - cme2.sourceCC;

  @Override
  public Object getValue() {
    return _selectedMapping;
  }
}
