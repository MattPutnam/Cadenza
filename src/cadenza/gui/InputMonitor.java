package cadenza.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Vector;

import javax.sound.midi.MidiMessage;
import javax.swing.AbstractAction;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import common.midi.MidiUtilities;
import common.swing.SwingUtils;

@SuppressWarnings("serial")
public class InputMonitor extends JFrame {
  private static final InputMonitor INSTANCE = new InputMonitor();
  public static InputMonitor getInstance() {
    return INSTANCE;
  }
  
  private final JList<MidiEvent> _jList;
  private final Vector<MidiEvent> _listData;
  
  private final Set<MidiEvent> _savedEvents;
  
  private InputMonitor() {
    _jList = new JList<>();
    _listData = new Vector<>();
    
    _savedEvents = new LinkedHashSet<>();
    
    final JButton saveButton = new JButton(new SaveAction());
    
    final JPanel subBottom = new JPanel(new BorderLayout());
    subBottom.add(new JButton(new ClearAction()), BorderLayout.WEST);
    subBottom.add(saveButton, BorderLayout.EAST);
    
    _jList.setCellRenderer(new MIDIEventRenderer());
    _jList.addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;
        
        saveButton.setEnabled(_jList.getSelectedIndex() != -1);
      }
    });
    saveButton.setEnabled(false);
    
    setLayout(new BorderLayout());
    add(new JScrollPane(_jList), BorderLayout.CENTER);
    add(subBottom, BorderLayout.SOUTH);
    
    pack();
    
    setTitle("Input Monitor");
    setAlwaysOnTop(true);
    SwingUtils.goInvisibleOnClose(this);
  }
  
  public synchronized void send(final MidiMessage mm) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        _listData.add(new MidiEvent(mm));
        _jList.setListData(_listData);
      }
    });
  }
  
  public Set<MidiEvent> accessSavedEvents() {
    return _savedEvents;
  }
  
  private class ClearAction extends AbstractAction {
    public ClearAction() {
      super("Clear");
    }
    
    @Override
    public void actionPerformed(ActionEvent _) {
      _listData.clear();
      _jList.setListData(_listData);
    }
  }
  
  private class SaveAction extends AbstractAction {
    public SaveAction() {
      super("Save selected");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
      _savedEvents.addAll(_jList.getSelectedValuesList());
    }
  }
  
  public static class MidiEvent {
    private static final DateFormat FORMAT = DateFormat.getTimeInstance(DateFormat.MEDIUM);
    
    private final MidiMessage _message;
    private final long _timeStamp;
    
    private String _timeStampString = null;
    
    public MidiEvent(MidiMessage message) {
      _message = message;
      _timeStamp = System.currentTimeMillis();
    }
    
    public MidiMessage getMidiMessage() {
      return _message;
    }
    
    public long getTimeStamp() {
      return _timeStamp;
    }
    
    public String getTimeStampString() {
      if (_timeStampString == null)
        _timeStampString = FORMAT.format(new Date(_timeStamp));
      return _timeStampString;
    }
    
    @Override
    public String toString() {
      return MidiUtilities.toString(_message) + " @ " + getTimeStampString();
    }
    
    @Override
    public boolean equals(Object obj) {
      final MidiEvent other = (MidiEvent) obj;
      return _message.equals(other._message) && _timeStamp == other._timeStamp;
    }
    
    @Override
    public int hashCode() {
      return (Long.valueOf(_timeStamp).hashCode() << 16) + _message.hashCode();
    }
  }
  
  public static class MIDIEventRenderer extends DefaultListCellRenderer {
    final JLabel _left;
    final JLabel _right;
    final JPanel _panel;
    
    public MIDIEventRenderer() {
      _left = new JLabel();
      _right = new JLabel();
      _panel = new JPanel(new BorderLayout());
      
      _left.setOpaque(true);
      _right.setOpaque(true);
      _panel.setOpaque(false);
      
      _panel.add(_left, BorderLayout.CENTER);
      _panel.add(_right, BorderLayout.EAST);
    }
    
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
        boolean cellHasFocus) {
      final JLabel dummy = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      
      final MidiEvent event = (MidiEvent) value;
      _left.setText(MidiUtilities.toString(event.getMidiMessage()));
      _right.setText(event.getTimeStampString());
      
      copyProperties(dummy, _left);
      copyProperties(dummy, _right);
      
      return _panel;
    }
    
    private void copyProperties(JLabel source, JLabel target) {
      target.setBackground(source.getBackground());
      target.setForeground(source.getForeground());
      target.setBorder(source.getBorder());
    }
  }
}
