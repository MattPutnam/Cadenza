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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
import cadenza.core.Patch;
import cadenza.core.Song;
import cadenza.core.Synthesizer;
import cadenza.core.patchusage.PatchUsage;
import cadenza.core.trigger.Trigger;
import cadenza.gui.common.CadenzaTable;
import cadenza.gui.cue.CueEditDialog;
import cadenza.gui.song.SongEditDialog;

import common.Comparators;
import common.Utils;
import common.collection.ListAdapter;
import common.collection.ListEvent;
import common.swing.SimpleTableCellRenderer;
import common.swing.SwingUtils;
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
  
  private volatile boolean _disableListSelectionListener = false;
  
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
    
    _data.patches.addListener(new ListAdapter<Patch>() {
      @Override
      public void anyChange(ListEvent<Patch> event) {
        revalidate();
        repaint();
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
          _controller.notifyReceiver();
          return;
        } else {
          index--;
        }
      }
    }
  }
  
  public void clearSelection() {
    _disableListSelectionListener = true;
    _table.accessTable().getSelectionModel().clearSelection();
    _disableListSelectionListener = false;
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
      for (final PatchUsage pu : selected.patches) {
        cloned.patches.add(pu);
      }
      for (final Trigger trigger : selected.triggers) {
        cloned.triggers.add(trigger);
      }
      final CueEditDialog dialog = new CueEditDialog(_cadenzaFrame, cloned, _data);
      dialog.showDialog();
      if (dialog.okPressed()) {
        _data.cues.add(cloned);
        rebuildEntries();
      }
    }
  }
  
  private void rebuildEntries() {
    _entries.clear();
    for (final Cue cue : _data.cues)
      _entries.add(new CueTableEntry(cue));
    for (final Song song : _data.songs)
      _entries.add(new CueTableEntry(song));
    _entries.sort(null);
    _table.accessTableModel().setList(_entries);
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
    
    private String getSongNumber() {
      if (song != null)
        return song.number;
      else
        return cue.song.number;
    }
    
    @Override
    public int compareTo(CueTableEntry other) {
      final int val = Comparators.NUMERO_ALPHA.compare(getSongNumber(), other.getSongNumber());
      if (val != 0)
        return val;
      
      // same song, but will never have 2 identical Song entries.
      // Song entries come first:
      if (isSong())
        return -1;
      else if (other.isSong())
        return 1;
      else
        return Comparators.NUMERO_ALPHA.compare(cue.measureNumber, other.cue.measureNumber);
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
        if (oneCue && !_disableListSelectionListener) {
          _controller.goTo(entry.cue);
          _cadenzaFrame.notifyPerformLocationChanged(_data.cues.indexOf(entry.cue), false);
        }
      });
    }
    
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
            final StringBuilder sb = new StringBuilder("<html>");
            
            final Cue cue = ((CueTableEntry) value).cue;
            final Map<Keyboard, List<PatchUsage>> map = cue.getPatchUsagesByKeyboard(_data.keyboards);
            for (final Iterator<Map.Entry<Keyboard, List<PatchUsage>>> i = map.entrySet().iterator(); i.hasNext();) {
              final Map.Entry<Keyboard, List<PatchUsage>> entry = i.next();
              for (final Iterator<PatchUsage> iter = entry.getValue().iterator(); iter.hasNext();) {
                sb.append(iter.next().toString(false, true));
                if (iter.hasNext()) sb.append(", ");
              }
              if (_data.keyboards.size() > 1)
                sb.append(" on ").append(entry.getKey().name);
              if (i.hasNext()) sb.append(", ");
            }
            sb.append("</html>");
            label.setText(sb.toString());
            
            final Pair<Boolean, String> warning = getWarning(cue);
            label.setIcon(warning == null ? null : warning._1().booleanValue() ? ImageStore.ERROR : ImageStore.WARNING);
            label.setToolTipText(warning == null ? sb.toString() : warning._2());
          } else {
            label.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            label.setToolTipText(null);
            label.setIcon(null);
          }
        }
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
              case Col.TRIGGERS:    return Utils.countItems(row.cue.triggers, "trigger");
              case Col.CONTROL_MAP: return Utils.countItems(row.cue.getControlMap(), "mapped control");
              case Col.EFFECTS:     return Utils.countItems(row.cue.effects, "effects");
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
      final Cue newCue = new Cue(null, "");
      final CueEditDialog dialog = new CueEditDialog(_cadenzaFrame, newCue, _data);
      dialog.showDialog();
      if (dialog.okPressed()) {
        _data.cues.add(newCue);
        _data.cues.sort(null);
        
        if (!_data.songs.contains(newCue.song)) {
          _data.songs.add(newCue.song);
        }
        
        rebuildEntries();
      }
    }
    
    @Override
    protected void takeActionOnEdit(CueTableEntry cueTableEntry) {
      if (cueTableEntry.isCue()) {
        final Cue cue = cueTableEntry.cue;
        final CueEditDialog dialog = new CueEditDialog(_cadenzaFrame, cue, _data);
        dialog.showDialog();
        if (dialog.okPressed()) {
          _data.cues.sort(null);
          
          if (_data.songs.contains(cue.song)) {
            rebuildEntries();
          } else {
            _data.songs.add(cue.song);
            rebuildEntries();
          }
          _data.cues.notifyChange(cue);
        }
      } else {
        final Song song = cueTableEntry.song;
        final SongEditDialog dialog = new SongEditDialog(_cadenzaFrame, song);
        dialog.showDialog();
        if (dialog.okPressed()) {
          rebuildEntries();
          _data.songs.notifyChange(song);
        }
      }
    }
    
    @Override
    protected boolean allowDelete(List<CueTableEntry> toDelete) {
      // only allow delete of Song rows when singly selected
      if (toDelete.size() == 1)
        return true;
      else
        for (final CueTableEntry entry : toDelete)
          if (entry.isSong())
            return false;
      return true;
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
        // remove all cues using the song
        for (final Iterator<Cue> iter = _data.cues.iterator(); iter.hasNext();) {
          final Cue cue = iter.next();
          if (cue.song.equals(song))
            iter.remove();
        }
        
        rebuildEntries();
      } else {
        for (final CueTableEntry entry : removed) {
          _data.cues.remove(entry.cue);
        }
        
        rebuildEntries();
      }
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
