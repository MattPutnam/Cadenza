package cadenza.gui.sequencer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.MouseInputAdapter;

import cadenza.core.sequencer.Sequencer;

import common.swing.SimpleGrid;
import common.swing.SwingUtils;

@SuppressWarnings("serial")
public class SequencerGridEditPanel extends JPanel {
  private static final Color ON = Color.GREEN;
  private static final Color OFF = Color.RED;
  private static final int PREF_SIZE = new JSpinner().getPreferredSize().height;
  
  private JPanel _content;
  private JComponent[][] _grid;
  
  public SequencerGridEditPanel(Sequencer sequencer) {
    _grid = new JComponent[sequencer.getNotes().length][sequencer.getLength()+1];
    for (int note = 0; note < sequencer.getNotes().length; ++note) {
      final JSpinner spinner = new JSpinner(new SpinnerNumberModel(sequencer.getNotes()[note], -64, 64, 1));
      SwingUtils.freezeSize(spinner);
      _grid[note][0] = spinner;
      
      for (int index = 0; index < sequencer.getLength(); ++index)
        _grid[note][index+1] = new NotePanel(sequencer.isOn(note, index));
    }
    _content = new SimpleGrid(_grid);
    
    setLayout(new BorderLayout());
    add(_content, BorderLayout.CENTER);
  }
  
  public void addColumns(int numToAdd) {
    final JComponent[][] newGrid = new JComponent[_grid.length][_grid[0].length + numToAdd];
    for (int note = 0; note < _grid.length; ++note) {
      int index;
      for (index = 0; index < _grid[0].length; ++index) {
        newGrid[note][index] = _grid[note][index];
      }
      for (; index < _grid[0].length + numToAdd; ++index) {
        newGrid[note][index] = new NotePanel(false);
      }
    }
    
    resetGrid(newGrid);
  }
  
  public void trimColumns() {
    int col = _grid[0].length-1;
    outer:
    while (col > 1) {
      for (int note = 0; note < _grid.length; ++note) {
        if (((NotePanel) _grid[note][col]).isOn()) {
          break outer;
        }
      }
      --col;
    }
    
    ++col;
    final JComponent[][] newGrid = new JComponent[_grid.length][col];
    for (int note = 0; note < _grid.length; ++note) {
      for (int index = 0; index < col; ++index) {
        newGrid[note][index] = _grid[note][index];
      }
    }
    
    resetGrid(newGrid);
  }
  
  public void addRowToTop() {
    int topVal = ((Integer) ((JSpinner) _grid[0][0]).getValue()).intValue() + 1;
    if (topVal > 64) topVal = 64;
    else if (topVal < -64) topVal = -64;
    final JSpinner spinner = new JSpinner(new SpinnerNumberModel(topVal, -64, 64, 1));
    SwingUtils.freezeSize(spinner);
    
    final JComponent[][] newGrid = new JComponent[_grid.length+1][_grid[0].length];
    
    // new row:
    newGrid[0][0] = spinner;
    for (int index = 1; index < _grid[0].length; ++index)
      newGrid[0][index] = new NotePanel(false);
    
    // existing rows:
    for (int note = 0; note < _grid.length; ++note)
      for (int index = 0; index < _grid[0].length; ++index)
        newGrid[note+1][index] = _grid[note][index];
    
    resetGrid(newGrid);
  }
  
  public void addRowToBottom() {
    int bottomVal = ((Integer) ((JSpinner) _grid[_grid.length-1][0]).getValue()).intValue() - 1;
    if (bottomVal < -64) bottomVal = -64;
    else if (bottomVal > 64) bottomVal = 64;
    final JSpinner spinner = new JSpinner(new SpinnerNumberModel(bottomVal, -64, 64, 1));
    SwingUtils.freezeSize(spinner);
    
    final JComponent[][] newGrid = new JComponent[_grid.length+1][_grid[0].length];
    
    // new row:
    newGrid[_grid.length][0] = spinner;
    for (int index = 1; index < _grid[0].length; ++index)
      newGrid[_grid.length][index] = new NotePanel(false);
    
    for (int note = 0; note < _grid.length; ++note)
      for (int index = 0; index < _grid[0].length; ++index)
        newGrid[note][index] = _grid[note][index];
    
    resetGrid(newGrid);
  }
  
  public void trimUnusedRows() {
    final List<Integer> rowsToRemove = new ArrayList<>();
    note:
    for (int note = 0; note < _grid.length; ++note) {
      for (int index = 1; index < _grid[note].length; ++index) {
        if (((NotePanel) _grid[note][index]).isOn())
          continue note;
      }
      rowsToRemove.add(Integer.valueOf(note));
    }
    
    if (!rowsToRemove.isEmpty()) {
      final int newLength = _grid.length - rowsToRemove.size();
      final JComponent[][] newGrid = new JComponent[newLength][_grid[0].length];
      int oldIndex = -1;
      for (int note = 0; note < newLength; ++note) {
        while (rowsToRemove.contains(Integer.valueOf(++oldIndex)));
        for (int index = 0; index < _grid[0].length; ++index) {
          newGrid[note][index] = _grid[oldIndex][index];
        }
      }
      
      resetGrid(newGrid);
    }
  }
  
  private void resetGrid(JComponent[][] newGrid) {
    remove(_content);
    
    _grid = newGrid;
    _content = new SimpleGrid(_grid);
    add(_content, BorderLayout.CENTER);
    
    revalidate();
    repaint();
  }
  
  public void clear() {
    for (int note = 0; note < _grid.length; ++note) {
      for (int index = 1; index < _grid[note].length; ++index) {
        ((NotePanel) _grid[note][index]).setOn(false);
      }
    }
  }
  
  public int[] getIntervals() {
    final int[] result = new int[_grid.length];
    for (int i = 0; i < _grid.length; ++i) {
      result[i] = ((Integer) ((JSpinner) _grid[i][0]).getValue()).intValue();
    }
    return result;
  }
  
  public boolean[][] getGrid() {
    final boolean[][] result = new boolean[_grid.length][_grid[0].length-1];
    for (int note = 0; note < _grid.length; ++note) {
      for (int index = 1; index < _grid[0].length;  ++index) {
        result[note][index-1] = ((NotePanel) _grid[note][index]).isOn();
      }
    }
    return result;
  }
  
  private static class NotePanel extends JPanel {
    private boolean _isOn;
    
    public NotePanel(boolean isOn) {
      _isOn = isOn;
      
      SwingUtils.freezeSize(this, PREF_SIZE, PREF_SIZE);
      
      addMouseListener(new MouseInputAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
          _isOn = !_isOn;
          setColor();
        }
        
        @Override
        public void mouseEntered(MouseEvent e) {
          if (e.getModifiersEx() == InputEvent.BUTTON1_DOWN_MASK) {
            _isOn = !_isOn;
            setColor();
          }
        }
      });
      setColor();
      
      setBorder(BorderFactory.createLineBorder(Color.BLACK));
    }
    
    public boolean isOn() {
      return _isOn;
    }
    
    public void setOn(boolean on) {
      _isOn = on;
      setColor();
    }
    
    private void setColor() {
      setBackground(_isOn ? ON : OFF);
    }
  }
}
