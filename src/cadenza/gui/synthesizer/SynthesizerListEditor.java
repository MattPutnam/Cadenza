package cadenza.gui.synthesizer;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.apache.commons.lang3.text.WordUtils;
import org.ciscavate.cjwizard.CustomWizardComponent;

import cadenza.core.Synthesizer;
import cadenza.gui.common.CadenzaTable;
import cadenza.gui.common.HelpButton;
import cadenza.gui.common.SynthConfigPanel;

import common.Utils;
import common.swing.VerificationException;
import common.swing.dialog.OKCancelDialog;
import common.swing.table.ListTableModel;

@SuppressWarnings("serial")
public class SynthesizerListEditor extends JPanel implements CustomWizardComponent {
  private static final String HELP_TEXT = "<html>Define the synthesizers used.<br><br>" +
      WordUtils.wrap("The <b>Default</b> synthesizer contains only the General " +
      "MIDI (level 1) patches and should be usable by any synthesizer " +
      "implementing the basic MIDI specification.", 60, "<br>", false) +
      "<br><br>" +
      WordUtils.wrap("Synthesizers with patch banks must be configured using " +
      "control files placed in the resources directory.  See the manual for " +
      "adding additional synthesizers if yours is not present.", 60, "<br>", false) +
      "<br><br>" +
      WordUtils.wrap("Each synthesizer must be assigned a list of output channels " +
      "to use, and these cannot overlap across synthesizers.  Specify the list as " +
      "integers, or ranges of integers, separated by commas.  Example: 1-4, 6, 8", 60, "<br>", false);
  
  private List<Synthesizer> _synthesizers;
  private SynthesizerTable _table;
  
  public SynthesizerListEditor(List<Synthesizer> synths) {
    _synthesizers = new ArrayList<>(synths);
    _table = new SynthesizerTable();
    
    setLayout(new BorderLayout());
    add(_table, BorderLayout.CENTER);
  }
  
  private class SynthesizerTable extends CadenzaTable<Synthesizer> {
    public SynthesizerTable() {
      super(_synthesizers, true, true, null, Box.createHorizontalStrut(16), new HelpButton(HELP_TEXT));
    }
    
    @Override
    protected ListTableModel<Synthesizer> createTableModel() {
      return new ListTableModel<Synthesizer>() {
        @Override
        public String[] declareColumns() {
          return new String[] {"Name", "Expansion Cards", "Output Channels"};
        }
        
        @Override
        public Object resolveValue(Synthesizer row, int column) {
          switch (column) {
            case 0: return row.getName();
            case 1: return row.getExpansionString();
            case 2: return Utils.makeRangeString(row.getChannels());
            default: throw new IllegalStateException("Unknown Column!");
          }
        }
      };
    }
    
    @Override
    protected String declareTypeName() {
      return "synthesizer";
    }
    
    @Override
    protected void takeActionOnAdd() {
      final SynthEditDialog dialog = new SynthEditDialog(null);
      dialog.showDialog();
      if (dialog.okPressed()) {
        _synthesizers.add(dialog.getSynthesizer());
        _table.accessTableModel().fireTableDataChanged();
      }
    }
    
    @Override
    protected void takeActionOnEdit(Synthesizer item) {
      final SynthEditDialog dialog = new SynthEditDialog(item);
      dialog.showDialog();
      if (dialog.okPressed()) {
        _synthesizers.set(_synthesizers.indexOf(item), dialog.getSynthesizer());
        _table.accessTableModel().fireTableDataChanged();
      }
    }
  }
  
  private class SynthEditDialog extends OKCancelDialog {
    private final Synthesizer _initial;
    
    private SynthConfigPanel _configPanel;
    
    public SynthEditDialog(Synthesizer initial) {
      super(SynthesizerListEditor.this);
      _initial = initial;
    }
    
    @Override
    protected JComponent buildContent() {
      _configPanel = new SynthConfigPanel(_synthesizers, _initial);
      return _configPanel;
    }
    
    @Override
    protected void initialize() {
      setSize(492, 540);
    }
    
    @Override
    protected String declareTitle() {
      return _initial == null ? "Create Synthesizer" : "Edit Synthesizer";
    }
    
    @Override
    protected void verify() throws VerificationException {
      _configPanel.verify();
    }
    
    public Synthesizer getSynthesizer() {
      return _configPanel.getSynthesizer();
    }
  }

  public void verify() throws VerificationException {
    _table.verify();
  }

  @Override
  public Object getValue() {
    return _synthesizers;
  }
  
  public List<Synthesizer> getSynthesizers() {
    return _synthesizers;
  }
}
