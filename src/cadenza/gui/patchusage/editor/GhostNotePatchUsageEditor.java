package cadenza.gui.patchusage.editor;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import cadenza.core.CadenzaData;
import cadenza.core.Cue;
import cadenza.core.Note;
import cadenza.core.NoteRange;
import cadenza.core.patchusage.GhostNotePatchUsage;
import cadenza.core.patchusage.PatchUsage;
import cadenza.gui.keyboard.KeyboardPanel;

import common.swing.SimpleListCellRenderer;
import common.swing.SwingUtils;
import common.tuple.Pair;

@SuppressWarnings("serial")
public class GhostNotePatchUsageEditor extends JPanel {
  private final Map<Integer, List<Integer>> _map;
  
  private KeyboardPanel _sourcePanel;
  private KeyboardPanel _destPanel;
  
  private Note _currentlySelected;
  private Integer _currentlySelectedNum;
  
  private Box _sourceRow;

  public GhostNotePatchUsageEditor(PatchUsage initial, CadenzaData data) {
    super();
    _map = new HashMap<>();
    if (initial instanceof GhostNotePatchUsage)
      _map.putAll(((GhostNotePatchUsage) initial).ghosts);
    
    final List<Pair<Cue, GhostNotePatchUsage>> othersFound = new ArrayList<>();
    data.cues.forEach(cue -> {
      cue.streamPatchUsages()
         .filter(pu -> (pu instanceof GhostNotePatchUsage))
         .map(pu -> (GhostNotePatchUsage) pu)
         .forEach(pu -> othersFound.add(Pair.make(cue, pu)));
    });
    
    _destPanel = new KeyboardPanel(Note.MIN, Note.MAX);
    _sourceRow = Box.createHorizontalBox();
    
    _destPanel.labelNote(Note.A0);
    _destPanel.labelNote(Note.C4);
    _destPanel.labelNote(Note.C8);
    
    _destPanel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (_currentlySelected != null) {
          final Note clicked = _destPanel.getNoteAt(e.getPoint());
          final Integer clickedNum = Integer.valueOf(clicked.getMidiNumber());
          
          if (_map.containsKey(_currentlySelectedNum)) {
            final List<Integer> list = _map.get(_currentlySelectedNum);
            
            if (list.contains(clickedNum)) {
              _destPanel.unhighlightNote(clicked);
              list.remove(clickedNum);
            } else {
              _destPanel.highlightNote(clicked, KeyboardPanel.HIGHLIGHT_COLOR);
              list.add(clickedNum);
            }
          } else {
            final List<Integer> list = new ArrayList<>();
            list.add(clickedNum);
            _map.put(_currentlySelectedNum, list);
            _destPanel.highlightNote(clicked, KeyboardPanel.HIGHLIGHT_COLOR);
          }
        }
      }
    });
    
    rebuildSourceRow(initial.getNoteRange());
    
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    
    if (!othersFound.isEmpty()) {
      @SuppressWarnings("unchecked")
      final JComboBox<Pair<Cue, GhostNotePatchUsage>> combo = new JComboBox<>(othersFound.toArray(new Pair[0]));
      combo.addActionListener(e -> {
        _map.clear();
        @SuppressWarnings("unchecked")
        final Pair<Cue, GhostNotePatchUsage> pair = (Pair<Cue, GhostNotePatchUsage>) combo.getSelectedItem();
        _map.putAll(pair._2().ghosts);
        rehighlight();
      });
      combo.setRenderer(new ComboRenderer());
      
      add(SwingUtils.buildCenteredRow(new JLabel("Populate from: "), combo));
    }
    
    add(SwingUtils.buildCenteredRow(new JLabel("When the following note is pressed:")));
    add(_sourceRow);
    add(SwingUtils.buildCenteredRow(new JLabel("Sound the following notes:")));
    add(_destPanel);
  }
  
  public void setPatchUsage(GhostNotePatchUsage patchUsage) {
    _map.clear();
    _map.putAll(patchUsage.ghosts);
    
    rebuildSourceRow(patchUsage.getNoteRange());
    rehighlight();
  }
  
  public Map<Integer, List<Integer>> getMap() {
    purgeEmptyEntries();
    return _map;
  }
  
  public void setNoteRange(NoteRange noteRange) {
    rebuildSourceRow(noteRange);
  }
  
  private void rebuildSourceRow(NoteRange noteRange) {
    final Note low = noteRange.getLower();
    final Note high = noteRange.getUpper();
    _sourcePanel = new KeyboardPanel(low, high);
    
    final int sourceSpacer = _destPanel.getKeyPosition(low).x;
    _sourceRow.removeAll();
    _sourceRow.add(Box.createHorizontalStrut(sourceSpacer));
    _sourceRow.add(_sourcePanel);
    _sourceRow.add(Box.createHorizontalGlue());
    _sourceRow.revalidate();
    _sourceRow.repaint();
    
    final List<Integer> keysToRemove = _map.keySet().stream()
                                                    .filter(key -> !noteRange.contains(key.intValue()))
                                                    .collect(Collectors.toList());
    keysToRemove.forEach(_map::remove);
    
    _currentlySelected = null;
    _currentlySelectedNum = Integer.valueOf(-1);
    _sourcePanel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        _currentlySelected = _sourcePanel.getNoteAt(e.getPoint());
        _currentlySelectedNum = Integer.valueOf(_currentlySelected.getMidiNumber());
        
        rehighlight();
      }
    });
  }
  
  private void rehighlight() {
    _sourcePanel.unhighlightAll();
    _destPanel.unhighlightAll();
    
    _map.forEach((srcKey, destKeyList) -> {
      _sourcePanel.highlightNote(Note.valueOf(srcKey.intValue()), KeyboardPanel.LIGHT_HIGHLIGHT_COLOR);
      destKeyList.forEach(destKey -> _destPanel.highlightNote(Note.valueOf(destKey.intValue()), KeyboardPanel.LIGHT_HIGHLIGHT_COLOR));
    });
    
    if (_currentlySelected != null) {
      _sourcePanel.highlightNote(_currentlySelected);
      if (_map.containsKey(_currentlySelectedNum)) {
        _map.get(_currentlySelectedNum).forEach(key -> _destPanel.highlightNote(Note.valueOf(key.intValue())));
      }
    }
  }
  
  private void purgeEmptyEntries() {
    final List<Integer> toRemove = _map.keySet().stream()
                                                .filter(key -> _map.get(key).isEmpty())
                                                .collect(Collectors.toList());
    toRemove.forEach(_map::remove);
  }
  
  private static class ComboRenderer extends SimpleListCellRenderer<Pair<Cue, GhostNotePatchUsage>> {
    @Override
    protected void processLabel(JLabel label, JList<Pair<Cue, GhostNotePatchUsage>> list,
        Pair<Cue, GhostNotePatchUsage> value, int index, boolean isSelected, boolean cellHasFocus) {
      final Cue cue = value._1();
      final GhostNotePatchUsage gnpu = value._2();
      label.setText("Entry from cue at " + cue.song + " m. " + cue.measureNumber + " (" + gnpu.patch.name + ")");
    }
  }

}

