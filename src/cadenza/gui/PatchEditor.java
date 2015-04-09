package cadenza.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableColumnModel;

import cadenza.control.PreviewController;
import cadenza.core.CadenzaData;
import cadenza.core.Patch;
import cadenza.gui.common.CadenzaTable;
import cadenza.gui.common.SinglePatchSelectionDialog;
import cadenza.gui.patch.PatchEditDialog;
import cadenza.gui.patch.PatchPickerDialog;

import common.collection.ListAdapter;
import common.collection.ListEvent;
import common.swing.SimpleTableCellRenderer;
import common.swing.SwingUtils;
import common.swing.dialog.OKCancelDialog;
import common.swing.table.ListTableModel;

@SuppressWarnings("serial")
public class PatchEditor extends JPanel {
  private static final Color ODD_BACKGROUND = new Color(200, 210, 255);
  private static final Color EVEN_BACKGROUND = Color.WHITE;
  
  private final CadenzaFrame _cadenzaFrame;
  private final CadenzaData _data;
  private final PreviewController _controller;
  
  private PatchTable _table;
  
  private volatile boolean _disableListSelectionListener = false;
  
  public PatchEditor(CadenzaFrame cadenzaFrame, CadenzaData data, PreviewController controller) {
    super();
    _cadenzaFrame = cadenzaFrame;
    _data = data;
    _controller = controller;
    init();
  }
  
  private void init() {
    final JButton selectButton = SwingUtils.iconButton(ImageStore.SELECT, new SelectPatchAction());
    final JButton replaceButton = SwingUtils.iconButton(ImageStore.REPLACE, new ReplacePatchAction());
    replaceButton.setEnabled(false);
    
    _table = new PatchTable(selectButton, replaceButton);
    
    final TableColumnModel tcm = _table.accessTable().getColumnModel();
    tcm.getColumn(0).setPreferredWidth(400);
    tcm.getColumn(1).setPreferredWidth(200);
    tcm.getColumn(2).setPreferredWidth(100);
    tcm.getColumn(3).setPreferredWidth(100);
    tcm.getColumn(4).setPreferredWidth(200);

    _table.accessTable().getSelectionModel().addListSelectionListener(e -> {
      if (_disableListSelectionListener || e.getValueIsAdjusting()) return;
      
      final boolean one = _table.accessTable().getSelectedRowCount() == 1;
      replaceButton.setEnabled(one);
      
      final List<Patch> selected = _table.getSelectedRows();
      _controller.setPatches(selected);
      _cadenzaFrame.notifyPreviewPatchesChanged(selected);
    });
    
    setLayout(new BorderLayout());
    add(_table, BorderLayout.CENTER);
    
    _data.patches.addListener(new ListAdapter<Patch>() {
      @Override
      public void anyChange(ListEvent<Patch> e) {
        _table.accessTableModel().setList(_data.patches);
      }
    });
  }
  
  public synchronized void clearSelection() {
    _disableListSelectionListener = true;
    _table.accessTable().getSelectionModel().clearSelection();
    _disableListSelectionListener = false;
  }
  
  private class PatchTable extends CadenzaTable<Patch> {
    public PatchTable(final JButton selectButton, final JButton replaceButton) {
      super(_data.patches, true, false, "Patches:", Box.createHorizontalStrut(16), selectButton, replaceButton);
      
      accessTable().setDefaultRenderer(Object.class, new PatchTableRenderer());
    }
    
    @Override
    protected ListTableModel<Patch> createTableModel() {
      return new ListTableModel<Patch>() {
        @Override
        public String[] declareColumns() {
          return new String[] {"Name", "Synth", "Bank", "Number", "Default Volume"};
        }
        
        @Override
        public Object resolveValue(Patch row, int column) {
          switch (column) {
            case 0: return row.name;
            case 1: return row.getSynthesizer().getName();
            case 2: return row.bank;
            case 3: return Integer.valueOf(row.number);
            case 4: return Integer.valueOf(row.defaultVolume);
            default: throw new IllegalStateException("Unknown Column!");
          }
        }
      };
    }
    
    @Override
    protected String declareTypeName() {
      return "patch";
    }
    
    @Override
    protected void takeActionOnAdd() {
      OKCancelDialog.showDialog(new PatchEditDialog(_cadenzaFrame, _data.synthesizers, null, _data.patches), dialog -> {
        _data.patches.add(dialog.getPatch());
        _data.patches.sort(null);
      });
    }
    
    @Override
    protected void takeActionOnEdit(Patch patch) {
      OKCancelDialog.showDialog(new PatchEditDialog(_cadenzaFrame, _data.synthesizers, patch, _data.patches), dialog -> {
        final Patch edit = dialog.getPatch();
        if (patch.equals(edit)) {
          return;
        }
        
        if (patch.defaultVolume != edit.defaultVolume) {
          int result = JOptionPane.showConfirmDialog(_cadenzaFrame,
              "Persist change to default volume?",
              "Choose", JOptionPane.YES_NO_OPTION);
          if (result == JOptionPane.OK_OPTION) {
            _data.cues.forEach(cue -> {
              cue.getPatchUsages().stream()
                                  .filter(pu -> pu.patch == patch && pu.volume == patch.defaultVolume)
                                  .forEach(pu -> pu.volume = edit.defaultVolume);
              _data.cues.notifyChange(cue);
            });
          } else {
            // notify all cues with the given patch so they will update in cue editor
            _data.cues.stream()
                      .filter(cue -> cue.getPatchUsages().stream().anyMatch(pu -> pu.patch == patch))
                      .forEach(_data.cues::notifyChange);
          }
        }
        
        patch.copyFrom(edit, true);
        _data.patches.sort(null);
        _data.patches.notifyChange(patch);
      });
    }
    
    @Override
    protected String declareAdditionalDeleteWarning(List<Patch> toDelete) {
      return "Patches will be removed from any cue that uses them.";
    }
    
    @Override
    protected void takeActionAfterDelete(List<Patch> removed) {
      removed.forEach(patch ->
        _data.cues.forEach(cue ->
          cue.patchAssignments.removeIf(pa -> pa.contains(patch))));
    }
    
    private class PatchTableRenderer extends SimpleTableCellRenderer<Object> {
      @Override
      protected void processLabel(JLabel label, JTable table, Object value,
          boolean isSelected, boolean hasFocus, int row, int column) {
        if (isSelected) {
          label.setForeground(table.getSelectionForeground());
          label.setBackground(table.getSelectionBackground());
        }
        else {
          if (column == 0) {
            final Patch patch = _data.patches.get(row);
            label.setBackground(patch.getDisplayColor());
            label.setForeground(patch.getTextColor());
          }
          else {
            label.setForeground(Color.BLACK);
            label.setBackground(row % 2 == 0 ? EVEN_BACKGROUND : ODD_BACKGROUND);
          }
        }
      }
    }
  }
  
  private class SelectPatchAction extends AbstractAction {
    public SelectPatchAction() {
      putValue(SHORT_DESCRIPTION, "Select a patch from a searchable list");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
      OKCancelDialog.showDialog(new PatchPickerDialog(_cadenzaFrame, _data.synthesizers), dialog -> {
        _data.patches.add(dialog.getSelectedPatch());
        _data.patches.sort(null);
      });
    }
  }
  
  private class ReplacePatchAction extends AbstractAction {
    public ReplacePatchAction() {
      putValue(SHORT_DESCRIPTION, "Replace all occurrences with another patch");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
      final Patch patch = _data.patches.get(_table.accessTable().getSelectedRow());
      OKCancelDialog.showDialog(new SinglePatchSelectionDialog(_cadenzaFrame, patch, _data.patches, _data.synthesizers), dialog -> {
        final Patch replacement = dialog.getSelectedPatch();
        if (replacement.equals(patch)) {
          return;
        }
        
        _data.patches.remove(patch);
        
        if (!_data.patches.contains(replacement)) {
          _data.patches.add(replacement);
        }
        
        _data.patches.sort(null);
        
        _data.cues.forEach(cue -> cue.patchAssignments.forEach(pa -> pa.replace(patch, replacement)));
      });
    }
  }
}
