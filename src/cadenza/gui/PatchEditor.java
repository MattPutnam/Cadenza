package cadenza.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

import cadenza.control.PreviewController;
import cadenza.core.CadenzaData;
import cadenza.core.Cue;
import cadenza.core.Patch;
import cadenza.core.patchusage.PatchUsage;
import cadenza.gui.common.CadenzaTable;
import cadenza.gui.common.SinglePatchSelectionDialog;
import cadenza.gui.patch.PatchEditDialog;
import cadenza.gui.patch.PatchPickerDialog;

import common.collection.ListAdapter;
import common.collection.ListEvent;
import common.swing.SwingUtils;
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
        // save selection...
        final List<Patch> selected = _table.getSelectedRows();
        
        // because this blasts it...
        _table.accessTableModel().setList(_data.patches);
        
        // and reselect:
        final ListSelectionModel selectionModel = _table.accessTable().getSelectionModel();
        for (final Patch p : selected) {
          final int i = _data.patches.indexOf(p);
          selectionModel.addSelectionInterval(i, i);
        }
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
      final PatchEditDialog dialog = new PatchEditDialog(_cadenzaFrame, _data.synthesizers, null, _data.patches);
      dialog.showDialog();
      if (dialog.okPressed()) {
        _data.patches.add(dialog.getPatch());
        Collections.sort(_data.patches);
      }
    }
    
    @Override
    protected void takeActionOnEdit(Patch patch) {
      final PatchEditDialog dialog = new PatchEditDialog(_cadenzaFrame, _data.synthesizers, patch, _data.patches);
      dialog.showDialog();
      if (dialog.okPressed()) {
        final Patch edit = dialog.getPatch();
        if (patch.equals(edit)) {
          return;
        }
        
        if (patch.defaultVolume != edit.defaultVolume) {
          int result = JOptionPane.showConfirmDialog(_cadenzaFrame,
              "Persist change to default volume?",
              "Choose", JOptionPane.YES_NO_OPTION);
          if (result == JOptionPane.OK_OPTION) {
            for (Cue cue : _data.cues) {
              for (PatchUsage patchUsage : cue.patches) {
                if (patchUsage.patch == patch && patchUsage.volume == patch.defaultVolume) {
                  patchUsage.volume = edit.defaultVolume;
                }
              }
            }
          }
        }
        
        patch.copyFrom(edit, true);
        Collections.sort(_data.patches);
      }
    }
    
    @Override
    protected String declareAdditionalDeleteWarning(List<Patch> toDelete) {
      return "Patches will be removed from any cue that uses them.";
    }
    
    @Override
    protected void takeActionAfterDelete(List<Patch> removed) {
      for (final Patch patch : removed) {
        for (final Cue cue : _data.cues) {
          final List<PatchUsage> remove = new LinkedList<>();
          for (final PatchUsage usage : cue.patches) {
            if (usage.patch == patch) {
              remove.add(usage);
            }
          }
          for (final PatchUsage usage : remove) {
            cue.patches.remove(usage);
          }
        }
      }
    }
    
    private class PatchTableRenderer extends DefaultTableCellRenderer {
      @Override
      public Component getTableCellRendererComponent(JTable table,
          Object value, boolean isSelected, boolean hasFocus,
          int row, int column) {
        final JLabel label = (JLabel) super.getTableCellRendererComponent(
            table, value, isSelected, hasFocus, row, column);
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
        
        return label;
      }
    }
  }
  
  private class SelectPatchAction extends AbstractAction {
    public SelectPatchAction() {
      putValue(SHORT_DESCRIPTION, "Select a patch from a searchable list");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
      final PatchPickerDialog dialog = new PatchPickerDialog(_cadenzaFrame, _data.synthesizers);
      dialog.showDialog();
      if (dialog.okPressed()) {
        _data.patches.add(dialog.getSelectedPatch());
        Collections.sort(_data.patches);
      }
    }
  }
  
  private class ReplacePatchAction extends AbstractAction {
    public ReplacePatchAction() {
      putValue(SHORT_DESCRIPTION, "Replace all occurrences with another patch");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
      final Patch patch = _data.patches.get(_table.accessTable().getSelectedRow());
      final SinglePatchSelectionDialog dialog = new SinglePatchSelectionDialog(_cadenzaFrame, patch, _data.patches, _data.synthesizers);
      dialog.showDialog();
      if (dialog.okPressed()) {
        final Patch replacement = dialog.getSelectedPatch();
        if (replacement.equals(patch)) {
          return;
        }
        
        _data.patches.remove(patch);
        
        if (!_data.patches.contains(replacement)) {
          _data.patches.add(replacement);
        }
        
        Collections.sort(_data.patches);
        
        for (final Cue cue : _data.cues) {
          for (final PatchUsage patchUsage : cue.patches) {
            if (patchUsage.patch.equals(patch)) {
              patchUsage.patch = replacement;
            }
          }
        }
      }
    }
  }
}
