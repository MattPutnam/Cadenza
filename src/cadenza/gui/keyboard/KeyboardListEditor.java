package cadenza.gui.keyboard;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.swing.AbstractCellEditor;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.apache.commons.lang3.text.WordUtils;
import org.ciscavate.cjwizard.CustomWizardComponent;

import cadenza.control.midiinput.AcceptsKeyboardInput;
import cadenza.control.midiinput.NoteRangeEntryTracker;
import cadenza.control.midiinput.MIDIInputControlCenter;
import cadenza.core.CadenzaData;
import cadenza.core.Cue;
import cadenza.core.Keyboard;
import cadenza.core.NoteRange;
import cadenza.core.Note;
import cadenza.core.patchusage.PatchUsage;
import cadenza.core.trigger.Trigger;
import cadenza.core.trigger.predicates.HasNoteRange;
import cadenza.gui.common.CadenzaTable;
import cadenza.gui.common.HelpButton;
import cadenza.preferences.Preferences;
import common.swing.VerificationException;
import common.swing.dialog.OKCancelDialog;
import common.swing.table.ListTableModel;

@SuppressWarnings("serial")
public class KeyboardListEditor extends JPanel implements CustomWizardComponent {
  private static final String HELP_TEXT =
      "<html>Define the keyboards used.<br><br>" +
      WordUtils.wrap("The <b>range</b> specifies the range of keys that will be shown " +
      "in all editing windows.  This should be the full range of the physical keyboard, " +
      "or even more if you will be spoofing notes beyond that range.", 60, "<br>", false) +
      "<br><br>" +
      WordUtils.wrap("The <b>sounding range</b> specifies the range of notes that will " +
      "play sound.  You can set this to less than the keyboard's actual range to leave " +
      "out unused keys at the extreme ranges and use them to trigger various actions.", 60, "<br>", false) +
      "<br><br>" +
      WordUtils.wrap("The <b>main</b> keyboard is the primary one in your performance. " +
      "It is selected by default in dialogs and cannot be deleted. If another keyboard " +
      "is deleted, all patches assigned to it are moved to the main keyboard.", 60, "<br>", false) +
      "</html>";
  
  private final CadenzaData _data;
  
  private final List<Keyboard> _keyboards;
  private final Keyboard _main;
  private final Map<Keyboard, Keyboard> _remap;
  
  private final KeyboardTable _table;
  
  public KeyboardListEditor(CadenzaData data) {
    _data = data;
    
    _keyboards = new ArrayList<>(_data.keyboards);
    _main = Keyboard.findMain(_keyboards);
    _remap = new HashMap<>();
    
    _table = new KeyboardTable();
    
    setLayout(new BorderLayout());
    add(_table, BorderLayout.CENTER);
  }
  
  public List<Keyboard> getKeyboards() {
    return _keyboards;
  }
  
  public void doRemap() {
    for (final Cue cue : _data.cues) {
      boolean modified = false;
      
      /*
       * Copy all note ranges to new keyboards, if that works.  If the keyboard
       * size changed and ranges fall off the edited keyboards, remove the
       * corresponding items.
       */
      final Iterator<PatchUsage> puIterator = cue.patches.iterator();
      while (puIterator.hasNext()) {
        final PatchUsage patchUsage = puIterator.next();
        final Keyboard newKeyboard = _remap.get(patchUsage.noteRange.getKeyboard());
        if (newKeyboard != null) {
          final Optional<NoteRange> opt = patchUsage.noteRange.copyTo(newKeyboard, true);
          if (opt.isPresent())
            patchUsage.noteRange = opt.get();
          else
            puIterator.remove();
          
          modified = true;
        }
      }
      
      final Iterator<Trigger> triggerIterator = cue.triggers.iterator();
      while (triggerIterator.hasNext()) {
        final Trigger trigger = triggerIterator.next();
        final List<HasNoteRange> hls = trigger.predicates.stream()
                                                        .filter(p -> p instanceof HasNoteRange)
                                                        .map(p -> ((HasNoteRange) p))
                                                        .collect(Collectors.toList());
        for (final HasNoteRange hl : hls) {
          final NoteRange curLoc = hl.getNoteRange();
          final Keyboard newKeyboard = _remap.get(curLoc.getKeyboard());
          final Optional<NoteRange> opt = curLoc.copyTo(newKeyboard, false);
          if (opt.isPresent())
            hl.setNoteRange(opt.get());
          else
            trigger.predicates.remove(hl);
          
          modified = true;
        }
        
        if (trigger.predicates.isEmpty())
          triggerIterator.remove();
      }
      
      if (modified) {
        _data.cues.notifyChange(cue);
      }
    }
    
    _data.keyboards.clear();
    _data.keyboards.addAll(_keyboards);
  }
  
  private class KeyboardTable extends CadenzaTable<Keyboard> {
    public KeyboardTable() {
      super(_keyboards, true, true, null, Box.createHorizontalStrut(16), new HelpButton(HELP_TEXT));
      
      final TableColumn mainColumn = accessTable().getColumnModel().getColumn(Col.MAIN);
      mainColumn.setCellEditor(new MainCellEditorRenderer());
      mainColumn.setCellRenderer(new MainCellEditorRenderer());
      mainColumn.setPreferredWidth(20);
      
      accessTable().getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      
      accessTable().setRowHeight(24);
    }
    
    @Override
    protected ListTableModel<Keyboard> createTableModel() {
      return new ListTableModel<Keyboard>() {
        @Override
        public String[] declareColumns() {
          return Col.COLUMNS;
        }
        
        @Override
        public Object resolveValue(Keyboard row, int column) {
          switch (column) {
            case Col.NAME:           return row.name;
            case Col.RANGE:          return row.low.toString() + "-" + row.high.toString();
            case Col.SOUNDING_RANGE: return row.soundingLow.toString() + "-" + row.soundingHigh.toString();
            case Col.MAIN:           return Boolean.valueOf(row.isMain);
            case Col.CHANNEL:        return Integer.valueOf(row.channel);
            default: throw new IllegalStateException("Unknown Column!");
          }
        }
        
        @Override
        public boolean isCellEditable(int row, int column) {
          return column == Col.MAIN;
        }
        
        @Override
        public void setValueAt(Object value, int row, int column) {
          _keyboards.get(row).isMain = ((Boolean) value).booleanValue();
        }
        
        @Override
        public Class<?> getColumnClass(int column) {
          return column == Col.MAIN ? Boolean.class : String.class;
        }
      };
    }
    
    @Override
    protected String declareTypeName() {
      return "keyboard";
    }
    
    @Override
    protected void takeActionOnAdd() {
      final Keyboard newKeyboard = new Keyboard(Keyboard.findFirstAvailableChannel(_keyboards));
      OKCancelDialog.showDialog(new KeyboardEditDialog(newKeyboard), dialog -> {
        _keyboards.add(dialog.getKeyboard());
        if (newKeyboard.isMain)
          for (final Keyboard keyboard : _keyboards)
            if (keyboard != newKeyboard)
              keyboard.isMain = false;
      });
    }
    
    @Override
    protected void takeActionOnEdit(Keyboard keyboard) {
      OKCancelDialog.showDialog(new KeyboardEditDialog(keyboard), dialog -> {
        final Keyboard newKeyboard = dialog.getKeyboard();
        _remap.put(keyboard, newKeyboard);
        
        _keyboards.set(_keyboards.indexOf(keyboard), newKeyboard);
      });
    }
    
    @Override
    protected boolean allowDelete(List<Keyboard> toDelete) {
      // single selection--list is always a single item
      return !toDelete.get(0).isMain;
    }
    
    @Override
    protected void takeActionAfterDelete(List<Keyboard> removed) {
      for (final Keyboard k : removed)
        _remap.put(k, _main);
    }
    
    private class MainCellEditorRenderer extends AbstractCellEditor implements TableCellEditor,
        TableCellRenderer, ActionListener {
      final JPanel _panel;
      final JRadioButton _button;
      
      public MainCellEditorRenderer() {
        _panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        _button = new JRadioButton();
        _button.addActionListener(this);
        _button.setOpaque(false);
        
        _panel.add(_button);
      }
      
      @Override
      public Component getTableCellRendererComponent(JTable table,
          Object value, boolean isSelected, boolean hasFocus,
          int row, int column) {
        _button.setSelected(Boolean.TRUE.equals(value));
        _panel.setBackground(isSelected ? accessTable().getSelectionBackground() : accessTable().getBackground());
        return _panel;
      }
      
      @Override
      public Component getTableCellEditorComponent(JTable table,
          Object value, boolean isSelected, int row, int column) {
        _button.setSelected(Boolean.TRUE.equals(value));
        _panel.setBackground(isSelected ? accessTable().getSelectionBackground() : accessTable().getBackground());
        return _panel;
      }
      
      @Override
      public void actionPerformed(ActionEvent e) {
        final Keyboard kbd = _keyboards.get(KeyboardTable.this.accessTable().getSelectedRow());
        for (final Keyboard k : _keyboards)
          k.isMain = (k == kbd);
        stopCellEditing();
      }
      
      @Override
      public Object getCellEditorValue() {
        return Boolean.valueOf(_button.isSelected());
      }
    }
    
    private class KeyboardEditDialog extends OKCancelDialog implements AcceptsKeyboardInput {
      private final KeyboardEditPanel _panel;
      
      private NoteRangeTracker _noteRangeTracker;
      
      public KeyboardEditDialog(Keyboard keyboard) {
        super(KeyboardListEditor.this);
        
        _panel = new KeyboardEditPanel(keyboard);
        
        if (Preferences.getMIDIInputOptions().allowMIDIInput()) {
          MIDIInputControlCenter.installWindowFocusGrabber(this);
          _noteRangeTracker = new NoteRangeTracker(new Keyboard(
              Note.MIN, Note.MAX, Note.MIN, Note.MAX, "dummy", true, 0));
        }
      }
      
      @Override
      protected JComponent buildContent() {
        return _panel;
      }
      
      @Override
      protected void initialize() {
        setResizable(false);
      }
      
      @Override
      protected String declareTitle() {
        return "Edit Keyboard";
      }
      
      @Override
      protected void verify() throws VerificationException {
        _panel.verify(_keyboards);
      }
      
      public Keyboard getKeyboard() {
        return _panel.buildKeyboard();
      }
      
      @Override
      public void keyPressed(int channel, int midiNumber, int velocity) {
        SwingUtilities.invokeLater(() -> {
          _panel.accessKeyboardPanel().highlightNote(Note.valueOf(midiNumber));
          _noteRangeTracker.keyPressed(channel, midiNumber);
        });
      }
      
      @Override
      public void keyReleased(int channel, int midiNumber) {
        SwingUtilities.invokeLater(() -> {
          _panel.accessKeyboardPanel().unhighlightNote(Note.valueOf(midiNumber));
          _noteRangeTracker.keyReleased(channel, midiNumber);
        });
      }
      
      private class NoteRangeTracker extends NoteRangeEntryTracker {
        public NoteRangeTracker(Keyboard keyboard) {
          super(Collections.singletonList(keyboard));
        }
        
        @Override
        protected void rangePressed(Keyboard keyboard, int lowNumber, int highNumber) {
          _panel.applyNoteRange(new NoteRange(keyboard, Note.valueOf(lowNumber), Note.valueOf(highNumber)));
        }
      }
    }
  }
  
  private static class Col {
    public static final String[] COLUMNS = new String[] {
      "Name", "Range", "Sounding Range", "Main", "Channel"
    };
    
    public static final int NAME = 0;
    public static final int RANGE = 1;
    public static final int SOUNDING_RANGE = 2;
    public static final int MAIN = 3;
    public static final int CHANNEL = 4;
  }

  @Override
  public Object getValue() {
    return _keyboards;
  }
}
