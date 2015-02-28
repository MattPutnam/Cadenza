package cadenza.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Vector;

import javax.sound.midi.MidiMessage;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import cadenza.core.CadenzaData;

import common.midi.MidiUtilities;
import common.swing.SwingUtils;



@SuppressWarnings("serial")
public class InputMonitor extends JFrame {  
  private final KeyboardInputViewer _viewer;
  private final JList<MidiEvent> _jList;
  private final Vector<MidiEvent> _listData;
  private final JScrollPane _sPane;
  
  private final Set<MidiEvent> _savedEvents;
  
  public InputMonitor(CadenzaData data) {
    _viewer = new KeyboardInputViewer(data.keyboards);
    _jList = new JList<>();
    _listData = new Vector<>();
    
    _savedEvents = new LinkedHashSet<>();
    
    final JButton saveButton = SwingUtils.button("Save selected", e ->
        _savedEvents.addAll(_jList.getSelectedValuesList()));
    
    final JPanel subBottom = new JPanel(new BorderLayout());
    subBottom.add(SwingUtils.button("Clear", e -> {
      _listData.clear();
      _jList.setListData(_listData);
    }), BorderLayout.WEST);
    subBottom.add(saveButton, BorderLayout.EAST);
    
    _jList.setCellRenderer(new MIDIEventRenderer());
    _jList.addListSelectionListener(e -> {
      if (!e.getValueIsAdjusting())
        saveButton.setEnabled(_jList.getSelectedIndex() != -1);
    });
    saveButton.setEnabled(false);
    
    setLayout(new BorderLayout());
    _sPane = new JScrollPane(_jList);
    add(_sPane, BorderLayout.CENTER);
    add(subBottom, BorderLayout.SOUTH);
    add(_viewer, BorderLayout.NORTH);
    
    pack();
    
    setTitle("Input Monitor");
    setLocationRelativeTo(null);
    setAlwaysOnTop(true);
    SwingUtils.goInvisibleOnClose(this);
  }
  
  public void send(final MidiMessage mm) {
    SwingUtilities.invokeLater(() -> {
      _listData.add(new MidiEvent(mm));
      _jList.setListData(_listData);
      final JScrollBar bar = _sPane.getVerticalScrollBar();
      if (bar != null)
        bar.setValue(bar.getMaximum());
    });
    
    _viewer.receive(mm);
  }
  
  public Set<MidiEvent> accessSavedEvents() {
    return _savedEvents;
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
    
    private static void copyProperties(JLabel source, JLabel target) {
      target.setBackground(source.getBackground());
      target.setForeground(source.getForeground());
      target.setBorder(source.getBorder());
    }
  }
}
