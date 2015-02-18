package cadenza.gui.control;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import cadenza.control.PerformanceController;
import cadenza.core.CadenzaData;
import cadenza.core.Cue;
import cadenza.core.Keyboard;
import cadenza.core.Patch;
import cadenza.core.metronome.Metronome;
import cadenza.core.metronome.MetronomeListener;
import cadenza.core.patchusage.PatchUsage;
import cadenza.gui.song.SongPanel;

import common.Utils;
import common.swing.SwingUtils;

@SuppressWarnings("serial")
public class ControlWindow extends JFrame implements MetronomeListener {
  private static final Color BG = Color.BLACK;
  private static final Color FG = Color.WHITE;
  
  private JLabel _topLabel;
  private JLabel _mainLabel;
  
  private SongPanel _songPanel;
  private JTextField _measureField;
  private List<Component> _toolbarComponents;
  
  private JLabel _metronomeLabel;
  private JPanel _metronomeArea;
  
  private final PerformanceController _controller;
  private final CadenzaData _data;
  
  public ControlWindow(PerformanceController controller, CadenzaData data) {
    super();
    _controller = controller;
    _data = data;
    
    buildContent();
    
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent e) {
        Metronome.getInstance().removeMetronomeListener(ControlWindow.this);
      }
    });
  }
  
  private void buildContent() {  
    final ActionListener gotoAction = e -> _controller.goTo(_songPanel.getSelectedSong(), _measureField.getText());
    
    _topLabel = new JLabel("", JLabel.CENTER);
    _topLabel.setForeground(FG);
    
    _mainLabel = new JLabel("Initializing...", JLabel.CENTER);
    _mainLabel.setForeground(FG);
    
    final JPanel main = new JPanel(new BorderLayout());
    main.setBackground(BG);
    main.add(_topLabel, BorderLayout.NORTH);
    main.add(_mainLabel, BorderLayout.CENTER);
    
    final JLabel gotoLabel = new JLabel("Go to ");
    gotoLabel.setForeground(FG);
    final JLabel measureLabel = new JLabel(" measure: ");
    measureLabel.setForeground(FG);
    
    _songPanel = new SongPanel(_data.songs, false);
    _songPanel.setBackground(BG);
    _songPanel.setForeground(FG);
    SwingUtils.freezeWidth(_songPanel);
    _measureField = new JTextField(8);
    _measureField.addActionListener(gotoAction);
    SwingUtils.freezeWidth(_measureField);
    
    _metronomeLabel = new JLabel("Metronome: ");
    _metronomeLabel.setForeground(FG);
    _metronomeArea = new JPanel();
    _metronomeArea.setBackground(BG);
    SwingUtils.freezeWidth(_metronomeArea, 24);
    
    _toolbarComponents = new LinkedList<>();
    _toolbarComponents.add(SwingUtils.button("Advance", e -> _controller.advance()));
    _toolbarComponents.add(SwingUtils.button("Reverse", e -> _controller.reverse()));
    _toolbarComponents.add(gotoLabel);
    _toolbarComponents.add(_songPanel);
    _toolbarComponents.add(measureLabel);
    _toolbarComponents.add(_measureField);
    _toolbarComponents.add(SwingUtils.button("Go", gotoAction));
    _toolbarComponents.add(SwingUtils.button("Restart", e -> _controller.restart()));
    _toolbarComponents.add(Box.createHorizontalGlue());
    _toolbarComponents.add(SwingUtils.button("Panic", e -> _controller.allNotesOff()));
    _toolbarComponents.add(SwingUtils.button("Close", e -> ControlWindow.this.dispose()));
    _toolbarComponents.add(Box.createHorizontalGlue());
    _toolbarComponents.add(_metronomeLabel);
    _toolbarComponents.add(_metronomeArea);
    
    final JPanel toolbar = new JPanel();
    toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.X_AXIS));
    for (final Component c : _toolbarComponents)
      toolbar.add(c);
    toolbar.setBackground(BG);
    
    final JPanel south = new JPanel(new BorderLayout());
    south.add(toolbar, BorderLayout.NORTH);
    
    setLayout(new BorderLayout());
    add(main, BorderLayout.CENTER);
    add(south, BorderLayout.SOUTH);
  }
  
  public void updatePerformanceLocation(int position) {
    updateDisplay_perform(position);
  }
  
  public void updatePreviewPatches(List<Patch> patches) {
    updateDisplay_preview(patches);
  }
  
  private void updateDisplay_perform(int position) {
    final Cue cue = _data.cues.get(position);
    final Cue nextCue = position == _data.cues.size()-1 ? null : _data.cues.get(position+1);
    
    final String songDisplay = buildSongDisplay(cue, nextCue);
    final String patchDisplay = buildPatchDisplay(cue);
    SwingUtils.doInSwing(() -> {
      for (final Component c : _toolbarComponents)
        c.setEnabled(true);
      
      _topLabel.setText(songDisplay);
      _mainLabel.setText(patchDisplay);
    }, false);
  }
  
  private static String buildSongDisplay(Cue cue, Cue nextCue) {
    final StringBuilder sb = new StringBuilder();
    sb.append(cue.song.toString()).append("<br>");
    
    sb.append("m. ").append(cue.measureNumber);
    if (nextCue == null)
      sb.append(" to end of show");
    else if (cue.song == nextCue.song)
      sb.append("-").append(nextCue.measureNumber);
    else
      sb.append(" to end of song");
    
    return wrapHTML(sb.toString());
  }
  
  private String buildPatchDisplay(Cue cue) {
    final StringBuilder sb = new StringBuilder();
    
    final Map<Keyboard, List<PatchUsage>> map = cue.getPatchUsagesByKeyboard(_data.keyboards);
    if (map.size() == 0) {
      sb.append("No patches");
    } else if (_data.keyboards.size() == 1) {
      final List<PatchUsage> patchUsages = map.values().iterator().next();
      final List<String> tokens = new ArrayList<>(patchUsages.size());
      for (final PatchUsage pu : patchUsages)
        tokens.add(pu.toString(false));
      sb.append(Utils.mkString(tokens, ", "));
    } else {
      for (Map.Entry<Keyboard, List<PatchUsage>> entry : map.entrySet()) {
        sb.append(entry.getKey().name).append(":<br>");
        final List<PatchUsage> patchUsages = entry.getValue();
        final List<String> tokens = new ArrayList<>(patchUsages.size());
        for (final PatchUsage pu : patchUsages)
          tokens.add(pu.toString(false));
        sb.append(Utils.mkString(tokens, ", ")).append("<br>");
      }
    }
    
    return wrapHTML(sb.toString());
  }
  
  private void updateDisplay_preview(List<Patch> patches) {
    final String topText = wrapHTML("Preview Mode");
    final String mainText = wrapHTML("Previewing:<br>" + Utils.mkString(patches, "<br>"));
    
    SwingUtils.doInSwing(() -> {
      for (final Component c : _toolbarComponents)
        c.setEnabled(false);
      
      _topLabel.setText(topText);
      _mainLabel.setText(mainText);
    }, false);
  }
  
  private static String wrapHTML(String str) {
    return "<html><h1 style='font-size:200%;text-align:center'>" + str + "</h1></html>";
  }
  
  @Override
  public void bpmSet(int bpm) {
    _metronomeLabel.setText("Metronome: " + bpm + " bpm");
  }
  
  @Override
  public void metronomeClicked(int subdivision) {
    if (subdivision == 0) {
      _metronomeArea.setBackground(Color.GREEN);
      SwingUtils.doDelayedInSwing(() -> _metronomeArea.setBackground(BG), 100);
    }
  }
  
  @Override
  public void metronomeStarted() { /* no op */ }
  
  @Override
  public void metronomeStopped() { /* no op */ }
}
