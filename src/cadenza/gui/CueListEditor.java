package cadenza.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicTableUI;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import cadenza.control.PerformanceController;
import cadenza.core.CadenzaData;
import cadenza.core.Cue;
import cadenza.core.Keyboard;
import cadenza.core.LocationNumber;
import cadenza.core.Patch;
import cadenza.core.PatchAssignmentEntity;
import cadenza.core.Song;
import cadenza.core.Synthesizer;
import cadenza.core.patchusage.PatchUsage;
import cadenza.gui.common.CadenzaTable;
import cadenza.gui.cue.CueEditDialog;
import cadenza.gui.song.SongEditDialog;
import common.Utils;
import common.collection.ListAdapter;
import common.collection.ListEvent;
import common.swing.SimpleTableCellRenderer;
import common.swing.SwingUtils;
import common.swing.dialog.OKCancelDialog;
import common.swing.table.ListTableModel;
import common.tuple.Pair;

@SuppressWarnings("serial")
public class CueListEditor extends JPanel {
  private static final Color SONG_BACKGROUND = new Color(200, 210, 255);
  
  private final CadenzaFrame _cadenzaFrame;
  private final CadenzaData _data;
  private final PerformanceController _controller;
  
  private final CueTable _table;
  private final List<CueTableEntry> _entries;
  
  public CueListEditor(CadenzaFrame cadenzaFrame, CadenzaData data, PerformanceController controller) {
    super();
    _cadenzaFrame = cadenzaFrame;
    _data = data;
    _controller = controller;
    
    _entries = new ArrayList<>();
    _table = new CueTable(SwingUtils.iconButton(ImageStore.CLONE, new CloneCueAction()));
    rebuildEntries();
    
    setLayout(new BorderLayout());
    add(_table, BorderLayout.CENTER);
    
    _data.cues.addListener(new ListAdapter<Cue>() {
      @Override
      public void anyChange(ListEvent<Cue> event) {
        SwingUtils.doInSwing(() -> {
          revalidate();
          repaint();
        }, false);
      }
    });
    
    _data.patches.addListener(new ListAdapter<Patch>() {
      @Override
      public void anyChange(ListEvent<Patch> event) {
        SwingUtils.doInSwing(() -> {
          revalidate();
          repaint();
        }, false);
      }
    });
  }
  
  public void setSelectedCue(int index) {
    if (index == -1) {
      _table.accessTable().clearSelection();
      return;
    }
    
    for (int row = 0; row < _entries.size(); ++row) {
      final CueTableEntry entry = _entries.get(row);
      if (entry.isCue()) {
        if (index == 0) {
          _table.accessTable().setRowSelectionInterval(row, row);
          return;
        } else {
          index--;
        }
      }
    }
  }
  
  public void clearSelection() {
    _table.accessTable().getSelectionModel().clearSelection();
  }
  
  private Song suggestSong() {
    final List<CueTableEntry> selected = _table.getSelectedRows();
    if (selected.isEmpty()) {
      if (_data.cues.isEmpty())
        return null;
      else
        return _data.cues.get(_data.cues.size()-1).song;
    }
    else {
      final CueTableEntry c = selected.get(0);
      if (c.isSong())
        return c.song;
      else
        return c.cue.song;
    }
  }
  
  private class CloneCueAction extends AbstractAction {
    public CloneCueAction() {
      super();
      putValue(SHORT_DESCRIPTION, "Clone selected cue");
      setEnabled(false);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
      final Cue selected = _entries.get(_table.accessTable().getSelectedRow()).cue;
      final Cue cloned = new Cue(selected.song, selected.measureNumber);
      
      selected.patches.forEach(cloned.patches::add);
      selected.triggers.forEach(cloned.triggers::add);
      
      OKCancelDialog.showDialog(new CueEditDialog(_cadenzaFrame, cloned, _data), dialog -> {
        _data.cues.add(cloned);
        rebuildEntries();
      });
    }
  }
  
  private void rebuildEntries() {
    _entries.clear();
    
    _data.cues.forEach(cue -> _entries.add(new CueTableEntry(cue)));
    _data.songs.forEach(song -> _entries.add(new CueTableEntry(song)));
    
    _entries.sort(null);
    _table.accessTableModel().setList(_entries);
    
    _controller.clearOldCue();
  }
  
  private final static class Col {
    public static final int MEASURE = 0;
    public static final int PATCHES = 1;
    public static final int TRIGGERS = 2;
    public static final int CONTROL_MAP = 3;
    public static final int EFFECTS = 4;
    
    public static String[] COLUMNS = {
      "Measure", "Patches", "Triggers", "Control Map", "Effects"
    };
  }
  
  private class CueTableEntry implements Comparable<CueTableEntry> {
    public final Song song;
    public final Cue cue;
    
    public CueTableEntry(Song song) {
      this.song = song;
      cue = null;
    }
    
    public CueTableEntry(Cue cue) {
      song = null;
      this.cue = cue;
    }
    
    public boolean isSong() {
      return song != null;
    }
    
    public boolean isCue() {
      return cue != null;
    }
    
    private LocationNumber getSongNumber() {
      if (isSong())
        return song.number;
      else
        return cue.song.number;
    }
    
    @Override
    public int compareTo(CueTableEntry other) {
      final int val = getSongNumber().compareTo(other.getSongNumber());
      if (val != 0)
        return val;
      
      // same song, but will never have 2 identical Song entries.
      // Song entries come first:
      if (isSong())
        return -1;
      else if (other.isSong())
        return 1;
      else
        return cue.measureNumber.compareTo(other.cue.measureNumber);
    }
  }
  
  private class CueTable extends CadenzaTable<CueTableEntry> {
    public CueTable(final JButton cloneButton) {
      super(_entries, true, false, "Cues:", cloneButton);
      
      accessTable().setUI(new CueTableUI());
      
      final TableColumnModel tcm = accessTable().getColumnModel();
      tcm.getColumn(Col.MEASURE)    .setPreferredWidth(15);
      tcm.getColumn(Col.PATCHES)    .setPreferredWidth(400);
      tcm.getColumn(Col.TRIGGERS)   .setPreferredWidth(25);
      tcm.getColumn(Col.CONTROL_MAP).setPreferredWidth(50);
      tcm.getColumn(Col.EFFECTS)    .setPreferredWidth(25);
      
      accessTable().setDefaultRenderer(Object.class, new CueTableRenderer());
      
      accessTable().getSelectionModel().addListSelectionListener(e -> {
        if (e.getValueIsAdjusting()) return;
        
        CueTableEntry entry = null;
        final boolean oneCue = accessTable().getSelectedRowCount() == 1 &&
               (entry = _entries.get(accessTable().getSelectedRow())).isCue();
        cloneButton.setEnabled(oneCue);
        if (oneCue) {
          _controller.goTo(entry.cue);
          _cadenzaFrame.notifyPerformLocationChanged(_data.cues.indexOf(entry.cue), false);
        }
      });
    }
    
    /*
     * We have to be horribly clunky like this because the song column spans
     * the full width of the table.  If you try to give separate renderers
     * to the various column types, the types conflict with each other when
     * dealing with the song rows.
     */
    private class CueTableRenderer extends SimpleTableCellRenderer<Object> {
      @Override
      protected void processLabel(JLabel label, JTable table, Object value,
          boolean isSelected, boolean hasFocus, int row, int column) {
        if (_entries.get(row).isSong()) {
          label.setHorizontalAlignment(SwingConstants.CENTER);
          label.setBackground(isSelected ? table.getSelectionBackground() : SONG_BACKGROUND);
          label.setToolTipText(null);
          label.setIcon(null);
        } else {
          label.setHorizontalAlignment(SwingConstants.LEFT);
          if (column == Col.PATCHES) {
            final Cue cue = ((CueTableEntry) value).cue;
            final String initialText = getPatchDisplay(cue);
            final String text = initialText.isEmpty() ? null : "<html>" + initialText + "</html>";
            label.setText(text);
            
            final Pair<Boolean, String> warning = getWarning(cue);
            label.setIcon(warning == null ? null : warning._1().booleanValue() ? ImageStore.ERROR : ImageStore.WARNING);
            label.setToolTipText(warning == null ? text : warning._2());
          } else if (column == Col.TRIGGERS || column == Col.CONTROL_MAP || column == Col.EFFECTS) {
            @SuppressWarnings("unchecked")
            final Pair<List<?>, String> pair = (Pair<List<?>, String>) value;
            label.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            label.setText(Utils.countItems(pair._1(), pair._2()));
            label.setToolTipText(pair._1().isEmpty() ? null : Utils.mkString(pair._1(), "<html>", "<br>", "</html>"));
            label.setIcon(null);
          } else {
            label.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            label.setToolTipText(null);
            label.setIcon(null);
          }
        }
      }
      
      private String getPatchDisplay(Cue cue) {
        final List<String> keyboardStrings = new LinkedList<>();
        final boolean multiple = _data.keyboards.size() > 1;
        
        for (final Keyboard keyboard : _data.keyboards) {
          final List<PatchAssignmentEntity> list = cue.getAssignmentsByKeyboard(keyboard);
          
          if (list != null && !list.isEmpty()) {
            final String s = list.stream()
                                 .map(pae -> pae.toString(false, false, true))
                                 .collect(Collectors.joining(", "));
            keyboardStrings.add(s + (multiple ? " on " + keyboard.name : ""));
          }
        }
        
        return Utils.mkString(keyboardStrings);
      }
      
      private Pair<Boolean, String> getWarning(Cue cue) {
        final List<String> messages = new LinkedList<>();
        
        final Map<Synthesizer, Integer> counts = new IdentityHashMap<>();
        for (final PatchUsage pu : cue.patches) {
          final Synthesizer synth = pu.patch.getSynthesizer();
          final Integer integer = counts.get(synth);
          
          counts.put(synth, Integer.valueOf(integer == null ? 1 : integer.intValue()+1));
        }
        
        boolean isError = false;
        for (final Map.Entry<Synthesizer, Integer> entry : counts.entrySet()) {
          final Synthesizer synth = entry.getKey();
          final int count = entry.getValue().intValue();
          final int max = synth.getChannels().size();
          
          if (count > max) {
            isError = true;
            messages.add("This cue uses more patches (" + count + ") on synth " +
                synth.getName() + " than are allocated.");
          }
          else if (2*count > max) {
            messages.add("This cue uses more patches (" + count + ") on synth " +
                synth.getName() + " than can be swapped in free space.");
          }
        }
        
        if (messages.isEmpty())
          return null;
        else
          return Pair.make(Boolean.valueOf(isError), Utils.mkString(messages, "<html>", "<br>", "</html>"));
      }
    }
    
    @Override
    protected JTable createTable() {
      return new JTable() {
        @Override
        public Rectangle getCellRect(int row, int column, boolean includeSpacing) {
          final Rectangle rect = super.getCellRect(row, column, includeSpacing);
          if (_entries.get(row).isSong())
            rect.width = getSize().width;
          return rect;
        }
        
        @Override
        public int columnAtPoint(Point point) {
          final int c = super.columnAtPoint(point);
          if (c == -1)
            return -1;
          
          if (_entries.get(rowAtPoint(point)).isSong())
            return 0;
          else
            return c;
        }
      };
    }

    @Override
    protected ListTableModel<CueTableEntry> createTableModel() {
      return new ListTableModel<CueTableEntry>() {

        @Override
        public String[] declareColumns() {
          return Col.COLUMNS;
        }

        @Override
        public Object resolveValue(CueTableEntry row, int column) {
          if (row.isCue()) {
            switch (column) {
              case Col.MEASURE:     return "m. " + row.cue.measureNumber;
              case Col.PATCHES:     return row; // Renderer handles this
              case Col.TRIGGERS:    return Pair.make(row.cue.triggers, "trigger");
              case Col.CONTROL_MAP: return Pair.make(row.cue.getControlMap(), "mapped control");
              case Col.EFFECTS:     return Pair.make(row.cue.effects, "effect");
              default: throw new IllegalStateException("Unknown Column!");
            }
          } else {
            return (column == 0) ? row.song.toString() : "";
          }
        }
        
      };
    }

    @Override
    protected void takeActionOnAdd() {
      final Cue newCue = new Cue(null, LocationNumber.TEMP);
      newCue.song = suggestSong();
      
      OKCancelDialog.showDialog(new CueEditDialog(_cadenzaFrame, newCue, _data), dialog -> {
        _data.cues.add(newCue);
        _data.cues.sort(null);
        
        if (!_data.songs.contains(newCue.song)) {
          _data.songs.add(newCue.song);
        }
        
        rebuildEntries();
      });
    }
    
    @Override
    protected void takeActionOnEdit(CueTableEntry cueTableEntry) {
      if (cueTableEntry.isCue()) {
        final Cue cue = cueTableEntry.cue;
        
        OKCancelDialog.showDialog(new CueEditDialog(_cadenzaFrame, cue, _data), dialog -> {
          _data.cues.sort(null);
          if (!_data.songs.contains(cue.song))
            _data.songs.add(cue.song);
          rebuildEntries();
          _data.cues.notifyChange(cue);
        });
      } else {
        final Song song = cueTableEntry.song;
        OKCancelDialog.showDialog(new SongEditDialog(_cadenzaFrame, song), dialog -> {
          rebuildEntries();
          _data.songs.notifyChange(song);
        });
      }
    }
    
    @Override
    protected boolean allowDelete(List<CueTableEntry> toDelete) {
      // only allow delete of Song rows when singly selected
      if (toDelete.size() == 1)
        return true;
      else
        return !toDelete.stream().anyMatch(CueTableEntry::isSong);
    }
    
    @Override
    protected String declareAdditionalDeleteWarning(List<CueTableEntry> toDelete) {
      return toDelete.get(0).isSong() ? "All cues using this song will be deleted" : "";
    };
    
    @Override
    protected void takeActionAfterDelete(List<CueTableEntry> removed) {
      // 'removed' is either a single Song or 1+ Cues
      if (removed.get(0).isSong()) {
        final Song song = removed.get(0).song;
        _data.songs.remove(song);
        _data.cues.removeIf(cue -> cue.song.equals(song));
      } else {
        removed.forEach(entry -> _data.cues.remove(entry.cue));
      }
      
      rebuildEntries();
    }

    @Override
    protected String declareTypeName() {
      return "cue";
    }
  }
  
  private class CueTableUI extends BasicTableUI {
    @Override
    public void paint(Graphics g, JComponent c) {
      final Rectangle r = g.getClipBounds();
      final int firstRow = table.rowAtPoint(new Point(0, r.y));
      int lastRow = table.rowAtPoint(new Point(0, r.y + r.height));
      // -1 is a flag that the ending point is outside the table
      if (lastRow == -1)
        lastRow = table.getRowCount() - 1;
      for (int i = firstRow; i <= lastRow; ++i)
        paintRow(i, g, r);
    }
    
    private void paintRow(int row, Graphics g, Rectangle r) {
      if (_entries.get(row).isCue()) {
        for (int column = 0; column < table.getColumnCount(); ++column) {
          final Rectangle r1 = table.getCellRect(row, column, true);
          if (r1.intersects(r)) {
            paintCell(row, column, g, r1);
          }
        }
      } else {
        final Rectangle r1 = table.getCellRect(row, 0, true);
        paintCell(row, 0, g, r1);
      }
    }
    
    private void paintCell(int row, int column, Graphics g, Rectangle area) {
      final int verticalMargin = table.getRowMargin();
      final int horizontalMargin = table.getColumnModel().getColumnMargin();
      
      final Color c = g.getColor();
      g.setColor(table.getGridColor());
      g.drawRect(area.x, area.y, area.width-1, area.height-1);
      g.setColor(c);
      
      area.setBounds(area.x + horizontalMargin/2,
               area.y + verticalMargin/2,
               area.width - horizontalMargin,
               area.height - verticalMargin);
      
      final TableCellRenderer renderer = table.getCellRenderer(row, column);
      final Component component = table.prepareRenderer(renderer, row, column);
      if (component.getParent() == null)
        rendererPane.add(component);
      rendererPane.paintComponent(g, component, table, area.x, area.y, area.width, area.height, true);
    }
  }
}
