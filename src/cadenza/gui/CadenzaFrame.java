package cadenza.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSplitPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.lang3.SystemUtils;

import cadenza.control.MidiSolutionsMessageSender;
import cadenza.control.PerformanceController;
import cadenza.control.PreviewController;
import cadenza.control.midiinput.MIDIInputControlCenter;
import cadenza.core.CadenzaData;
import cadenza.core.ControlMapEntry;
import cadenza.core.Cue;
import cadenza.core.Keyboard;
import cadenza.core.Patch;
import cadenza.core.Song;
import cadenza.core.Synthesizer;
import cadenza.core.metronome.Metronome;
import cadenza.core.plugins.Plugin;
import cadenza.core.trigger.Trigger;
import cadenza.gui.control.ControlWindow;
import cadenza.gui.controlmap.ControlMapPanel;
import cadenza.gui.keyboard.KeyboardListEditor;
import cadenza.gui.plugins.edit.PluginChainViewerEditor;
import cadenza.gui.preferences.PreferencesDialog;
import cadenza.gui.synthesizer.SynthesizerListEditor;
import cadenza.gui.trigger.TriggerPanel;

import common.collection.ListAdapter;
import common.collection.ListEvent;
import common.io.IOUtils;
import common.midi.MidiPortFinder;
import common.swing.SwingUtils;
import common.swing.dialog.Dialog;
import common.swing.dialog.OKCancelDialog;

@SuppressWarnings("serial")
public class CadenzaFrame extends JFrame implements Receiver {
  private static final String MAC_OSX_MIDI_BUG_INFO =
      "There is a bug in the Mac OS X Java implementation that prevents the system\n" +
      "from refreshing the list of available MIDI devices once that list has been\n" +
      "retrieved once during the application's run.  If the device you're looking\n" +
      "for is not in the list, quit Cadenza, ensure that it is plugged in and any\n" +
      "drivers are installed, and start Cadenza again.";
  
  public static enum Mode { PERFORM, PREVIEW }
  
  private Mode _mode;
  
  private final CadenzaData _data;
  private final PerformanceController _performanceController;
  private final PreviewController _previewController;
  private final MIDIInputControlCenter _inputControlCenter;
  
  private PatchEditor _patchEditor;
  private CueListEditor _cueListEditor;
  private PreviewMixer _previewMixer;
  
  private ControlWindow _controlWindow;
  
  private JMenu _inputMenu;
  private JMenu _outputMenu;
  private List<Info> _inputs;
  private List<Info> _outputs;
  
  private MidiDevice _inDevice;
  private MidiDevice _outDevice;
  
  private MidiSolutionsMessageSender _msmSender;
  
  File _associatedSave = null;
  private static File _lastPath = null;
  private boolean _dirty;
  
  public CadenzaFrame(CadenzaData data) {
    super();
    
    _data = data;
    _performanceController = new PerformanceController(_data, this);
    _previewController = new PreviewController(_data);
    _inputControlCenter = new MIDIInputControlCenter();
    
    _data.synthesizers.addListener(new Dirtyer<Synthesizer>());
    _data.globalTriggers.addListener(new Dirtyer<Trigger>());
    _data.globalControlMap.addListener(new Dirtyer<ControlMapEntry>());
    _data.globalPlugins.addListener(new Dirtyer<Plugin>());
    _data.patches.addListener(new Dirtyer<Patch>());
    _data.cues.addListener(new Dirtyer<Cue>());
    _data.keyboards.addListener(new Dirtyer<Keyboard>());
    _data.songs.addListener(new Dirtyer<Song>());
    
    _data.keyboards.addListener(new ListAdapter<Keyboard>() {
      @Override
      public void anyChange(ListEvent<Keyboard> event) {
        _performanceController.updateKeyboardChannelMap();
      }
    });
    
    init();
  }
  
  public void addPatch(Patch patch) {
    _data.patches.add(patch);
  }
  
  public void notifyPerformLocationChanged(int cueIndex, boolean notifyCueListEditor) {
    _patchEditor.clearSelection();
    _previewMixer.goPerformMode();
    if (_controlWindow != null)
      _controlWindow.updatePerformanceLocation(cueIndex);
    if (notifyCueListEditor)
      _cueListEditor.setSelectedCue(cueIndex);
    
    _mode = Mode.PERFORM;
  }
  
  public void notifyPreviewPatchesChanged(List<Patch> previewPatches) {
    // this can only be triggered from the PatchSelectionEditor, no need to notify it
    _previewMixer.updatePreviewPatches(previewPatches);
    if (_controlWindow != null)
      _controlWindow.updatePreviewPatches(previewPatches);
    _cueListEditor.clearSelection();
    
    _mode = Mode.PREVIEW;
  }
  
  @Override
  public void send(MidiMessage message, long timestamp) {
    if (_mode == Mode.PERFORM)
      _performanceController.send(message);
    else if (_mode == Mode.PREVIEW)
      _previewController.send(message);
    
    _inputControlCenter.send(message);
    InputMonitor.getInstance().send(message);
  }
  
  @Override
  public void close() {
    if (_inDevice != null)
      _inDevice.close();
    if (_outDevice != null)
      _outDevice.close();
  }
  
  private void init() {
    _cueListEditor = new CueListEditor(this, _data, _performanceController);
    
    _patchEditor = new PatchEditor(this, _data, _previewController);
    _previewMixer = new PreviewMixer(_previewController, _data);
    
    final JSplitPane splitEast = new JSplitPane(JSplitPane.VERTICAL_SPLIT, _patchEditor, _previewMixer);
    splitEast.setDividerLocation(600);
    splitEast.setBorder(null);
    
    final JSplitPane splitMain = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, _cueListEditor, splitEast);
    splitMain.setDividerLocation(900);
    splitMain.setBorder(null);
    
    setLayout(new BorderLayout());
    add(splitMain, BorderLayout.CENTER);
    
    createMenuBar();
    
    makeClean();
    setSize(1600, 1000);
    setLocationRelativeTo(null);
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        closeWindow();
      }
    });
  }
  
  private void createMenuBar() {
    ///////////////////////////////////////////////////////////////////////
    // File menu:
    final JMenu fileMenu = SwingUtils.menu("File", 'F');
    fileMenu.add(SwingUtils.menuItem("Close", 'W', 'C', new CloseFileAction()));
    fileMenu.add(SwingUtils.menuItem("Save", 'S', 'S', new SaveFileAction()));
    fileMenu.add(SwingUtils.menuItem("Save As...", 'S', InputEvent.SHIFT_MASK, 'A', new SaveAsFileAction()));
    if (!SystemUtils.IS_OS_MAC_OSX) {
      fileMenu.addSeparator();
      fileMenu.add(SwingUtils.menuItem("Preferences", 'E', 'E', new PreferencesAction()));
      fileMenu.addSeparator();
      fileMenu.add(SwingUtils.menuItem("Quit", 'Q', 'Q', new QuitAction()));
    }
    
    ///////////////////////////////////////////////////////////////////////
    // Setup menu:
    final JMenu setupMenu = SwingUtils.menu("Setup", 'E');
    
    _inputMenu = new JMenu("Input:");
    setupMenu.add(_inputMenu);
    
    _outputMenu = new JMenu("Output:");
    setupMenu.add(_outputMenu);
    
    setupMenu.add(SwingUtils.menuItem("Rescan...", 'C', 'R', new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (SystemUtils.IS_OS_MAC_OSX)
          Dialog.info(CadenzaFrame.this, MAC_OSX_MIDI_BUG_INFO);
        
        new RescanTask();
      }
    }));
    new RescanTask();
    
    setupMenu.addSeparator();
    setupMenu.add(SwingUtils.menuItem("Configure Synthesizers", 'Y', 'Y', new ConfigureSynthesizersAction()));
    setupMenu.add(SwingUtils.menuItem("Configure Keyboards", 'K', 'K', new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        final KeyboardListEditor panel = new KeyboardListEditor(_data);
        if (OKCancelDialog.showInDialog(CadenzaFrame.this, "Edit Keyboards", panel)) {
          panel.doRemap();
        }
      }
    }));
    setupMenu.add(SwingUtils.menuItem("Configure Global Triggers", 'T', 'T', new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        final TriggerPanel panel = new TriggerPanel(_data, _data);
        if (OKCancelDialog.showInDialog(CadenzaFrame.this, "Edit Global Triggers", panel)) {
          _data.globalTriggers.clear();
          _data.globalTriggers.addAll(panel.getTriggers());
        }
      }
    }));
    setupMenu.add(SwingUtils.menuItem("Configure Global Control Overrides", 'L', 'L', new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        final ControlMapPanel panel = new ControlMapPanel(_data);
        if (OKCancelDialog.showInDialog(CadenzaFrame.this, "Edit Global Control Overrides", panel)) {
          final List<ControlMapEntry> controls = panel.getMapping();
          if (!controls.equals(_data.globalControlMap)) {
            _data.globalControlMap.clear();
            _data.globalControlMap.addAll(controls);
          }
        }
      }
    }));
    setupMenu.add(SwingUtils.menuItem("Configure Global Plugins", 'U', 'U', new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        final PluginChainViewerEditor panel = new PluginChainViewerEditor(_data.globalPlugins, true);
        if (OKCancelDialog.showInDialog(CadenzaFrame.this, "Edit Global Plugins", panel)) {
          final List<Plugin> plugins = panel.getPlugins();
          if (!plugins.equals(_data.globalPlugins)) {
            _data.globalPlugins.clear();
            _data.globalPlugins.addAll(panel.getPlugins());
          }
        }
      }
    }));
    
    setupMenu.addSeparator();
    setupMenu.add(SwingUtils.menuItem("Program MIDI Solutions device...", 'G', 'G', new ProgramMidiSolutionsAction()));
    
    ///////////////////////////////////////////////////////////////////////
    // Control menu:
    final JMenu controlMenu = SwingUtils.menu("Control", 'O');
    controlMenu.add(SwingUtils.menuItem("Show Control Window", 'O', 'O', new ShowControlWindowAction()));
    controlMenu.add(SwingUtils.menuItem("Show Metronome", 'M', 'M', new ShowMetronomeAction()));
    controlMenu.addSeparator();
    controlMenu.add(SwingUtils.menuItem("Show Plugin Monitor", 'P', 'P', new ShowPluginMonitorAction()));
    controlMenu.add(SwingUtils.menuItem("Show Input Monitor", 'I', 'I', new ShowInputMonitorAction()));
    
    final JMenuBar menuBar = new JMenuBar();
    menuBar.add(fileMenu);
    menuBar.add(setupMenu);
    menuBar.add(controlMenu);
    setJMenuBar(menuBar);
  }
  
  private class RescanTask extends Thread {
    public RescanTask() {
      super("CadenzaFrame.RescanTask");
      start();
    }
    
    @Override
    public void run() {
      _inputs = MidiPortFinder.getInputMidiDeviceInfos();
      _outputs = MidiPortFinder.getOutputMidiDeviceInfos();
      
      _inputMenu.removeAll();
      _outputMenu.removeAll();
      
      final ButtonGroup inGroup = new ButtonGroup();
      for (final Info in : _inputs) {
        final JRadioButtonMenuItem item = new JRadioButtonMenuItem(new MidiPortAction(in, true));
        inGroup.add(item);
        _inputMenu.add(item);
        
        if (in.getName().equals(_data.savedInputDeviceName)) {
          item.setSelected(true);
          setInput(in);
        }
      }
      
      final ButtonGroup outGroup = new ButtonGroup();
      for (final Info out : _outputs) {
        final JRadioButtonMenuItem item = new JRadioButtonMenuItem(new MidiPortAction(out, false));
        outGroup.add(item);
        _outputMenu.add(item);
        
        if (out.getName().equals(_data.savedOutputDeviceName)) {
          item.setSelected(true);
          setOutput(out);
        }
      }
    }
  }
  
  private class ConfigureSynthesizersAction extends AbstractAction {
    @Override
    public void actionPerformed(ActionEvent e) {
      final SynthesizerListEditor editor = new SynthesizerListEditor(_data.synthesizers);
      if (OKCancelDialog.showInDialog(CadenzaFrame.this, "Reconfigure Synthesizers", editor)) {
        final List<Synthesizer> newSynths = editor.getSynthesizers();
        final List<Patch> orphaned = new ArrayList<>();
        
        final Map<Patch, Synthesizer> auto = new HashMap<>();
        
        patch:
        for (final Patch patch : _data.patches) {
          final Synthesizer patchSynth = patch.getSynthesizer();
          synthesizer:
          for (final Synthesizer newSynth : newSynths) {
            if (!patchSynth.getName().equals(newSynth.getName()))
              continue synthesizer;
            
            if (newSynth.getBanks().contains(patch.bank) ||
              newSynth.getExpansions().keySet().contains(patch.bank)) {
              auto.put(patch, newSynth);
              patch.setSynthesizer(newSynth);
              continue patch;
            }
          }
          
          orphaned.add(patch);
        }
        
        // repaint cue list to update warnings
        _cueListEditor.revalidate();
        _cueListEditor.repaint();
        
        if (orphaned.isEmpty()) {
          _data.synthesizers.clear();
          _data.synthesizers.addAll(newSynths);
        } else {
          final OrphanedPatchRemapper remapper = new OrphanedPatchRemapper(CadenzaFrame.this,
              _data, orphaned, newSynths);
          remapper.showDialog();
          // dialog handles its own logic on ok
          if (remapper.okPressed()) {
            for (final Map.Entry<Patch, Synthesizer> entry : auto.entrySet()) {
              entry.getKey().setSynthesizer(entry.getValue());
            }
          }
        }
      }
    }
  }
  
  private class ProgramMidiSolutionsAction extends AbstractAction {
    @Override
    public void actionPerformed(ActionEvent _) {
      if (_msmSender == null || !_msmSender.isValid()) {
        Dialog.error(CadenzaFrame.this, "Output connection is not set");
        return;
      }
      
      new ProgramMidiSolutionsFrame(_msmSender);
    }
  }
  
  private class ShowControlWindowAction extends AbstractAction {
    @Override
    public void actionPerformed(ActionEvent _) {
      if (_inDevice == null || _outDevice == null) {
        Dialog.error(CadenzaFrame.this, "MIDI I/O is not set.  " +
            "Go to the Setup menu and select the MIDI Input/Output devices.");
        return;
      }
      
      if (_controlWindow == null) {
        _controlWindow = new ControlWindow(_performanceController, _data);
        Metronome.getInstance().addMetronomeListener(_controlWindow);
        _controlWindow.setSize(1600, 1000);
        SwingUtils.goInvisibleOnClose(_controlWindow);
        
        if (_mode == Mode.PERFORM && _data.cues.size() > 0)
          _controlWindow.updatePerformanceLocation(_performanceController.getCurrentCueIndex());
        else if (_mode == Mode.PREVIEW && _data.patches.size() > 0)
          _controlWindow.updatePreviewPatches(_previewController.getCurrentPreviewPatches());
      }
      
      _controlWindow.setVisible(true);
    }
  }
  
  private class ShowMetronomeAction extends AbstractAction {
    @Override
    public void actionPerformed(ActionEvent e) {
      MetronomeView.getInstance().setVisible(true);
    }
  }
  
  private class ShowPluginMonitorAction extends AbstractAction {
    @Override
    public void actionPerformed(ActionEvent _) {
      PluginMonitor.getInstance().setVisible(true);
    }
  }
  
  private class ShowInputMonitorAction extends AbstractAction {
    @Override
    public void actionPerformed(ActionEvent e) {
      InputMonitor.getInstance().setVisible(true);
    }
  }
  
  private class MidiPortAction extends AbstractAction {
    private final Info _info;
    private final boolean _isInput;
    
    public MidiPortAction(Info info, boolean isInput) {
      super(info.getName());
      _info = info;
      _isInput = isInput;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
      if (_isInput) {
        _data.savedInputDeviceName = _info.getName();
        setInput(_info);
      } else {
        _data.savedOutputDeviceName = _info.getName();
        setOutput(_info);
      }
      
      makeDirty();
    }
  }
  
  private void setInput(Info info) {
    if (_inDevice != null)
      _inDevice.close();
    
    _inputMenu.setText("Input: " + info.getName());
    
    try {
      _inDevice = MidiSystem.getMidiDevice(info);
      _inDevice.open();
      _inDevice.getTransmitter().setReceiver(this);
    } catch (MidiUnavailableException e) {
      // shouldn't happen...
      Dialog.error(CadenzaFrame.this, "An error occurred while opening the midi device: " + e.getMessage());
    }
  }
  
  private void setOutput(Info info) {
    if (_outDevice != null)
      _outDevice.close();
    
    _outputMenu.setText("Output: " + info.getName());
    
    try {
      _outDevice = MidiSystem.getMidiDevice(info);
      _outDevice.open();
      final Receiver receiver = _outDevice.getReceiver();
      _performanceController.setReceiver(receiver);
      _previewController.setReceiver(receiver);
      if (_msmSender == null)
        _msmSender = new MidiSolutionsMessageSender(receiver);
      else
        _msmSender.setReceiver(receiver);
    } catch (MidiUnavailableException e) {
      // shouldn't happen...
      Dialog.error(CadenzaFrame.this, "An error occurred while opening the midi device:" + e.getMessage());
    }
  }
  
  private void makeDirty() {
    _dirty = true;
    getRootPane().putClientProperty("Window.documentModified", Boolean.TRUE);
    setTitle("Cadenza - " + (_associatedSave == null ? "[unsaved]" : _associatedSave.getName() + " - modified"));
  }
  
  void makeClean() {
    _dirty = false;
    setTitle("Cadenza - " + (_associatedSave == null ? "[unsaved]" : _associatedSave.getName()));
    getRootPane().putClientProperty("Window.documentModified", Boolean.FALSE);
    getRootPane().putClientProperty("Window.documentFile", _associatedSave);
  }
  
  boolean isDirty() {
    return _dirty;
  }
  
  private boolean safeClose(boolean allowCancel) {
    if (_dirty) {
      int result = JOptionPane.showConfirmDialog(this, "Warning: This file has unsaved changes.  Save before closing?",
          "Unsaved Changes", allowCancel ? JOptionPane.YES_NO_CANCEL_OPTION : JOptionPane.YES_NO_OPTION);
      if (result == JOptionPane.CANCEL_OPTION) {
        return false;
      } else if (result == JOptionPane.YES_OPTION) {
        if (_associatedSave == null) {
          if (saveAs()) {
            closeWindow();
            return true;
          } else {
            return false;
          }
        } else {
          save();
          closeWindow();
          return true;
        }
      }
    }
    
    closeWindow();
    return true;
  }
  
  private class CloseFileAction extends AbstractAction {
    @Override
    public void actionPerformed(ActionEvent e) {
      safeClose(true);
    }
  }
  
  private class SaveFileAction extends AbstractAction {
    @Override
    public void actionPerformed(ActionEvent ae) {
      if (_associatedSave == null) {
        saveAs();
      } else {
        save();
      }
    }
  }
  
  private class SaveAsFileAction extends AbstractAction {
    @Override
    public void actionPerformed(ActionEvent ae) {
      saveAs();
    }
  }
  
  private void save() {
    CadenzaData.writeToFile(_associatedSave.getAbsolutePath(), _data);
    makeClean();
    Cadenza.notifyRecent(_associatedSave);
  }
  
  private boolean saveAs() {
    File selected = IOUtils.showSaveFileDialog(this, _lastPath, ".cdza", "Cadenza Files");
    if (selected != null) {
      if (!selected.getName().toLowerCase().endsWith(".cdza")) {
        selected = new File(selected.getAbsolutePath() + ".cdza");
      }
      _lastPath = selected;
      
      CadenzaData.writeToFile(selected.getAbsolutePath(), _data);
      _associatedSave = selected;
      makeClean();
      Cadenza.notifyRecent(selected);
      return true;
    }
    return false;
  }
  
  private void closeWindow() {
    Cadenza.showHome();
    dispose();
    close();
  }
  
  private class PreferencesAction extends AbstractAction {
    @Override
    public void actionPerformed(ActionEvent e) {
      final PreferencesDialog dialog = new PreferencesDialog(CadenzaFrame.this);
      dialog.showDialog();
      if (dialog.okPressed())
        dialog.commitPreferences();
    }
  }
  
  private class QuitAction extends AbstractAction {
    @Override
    public void actionPerformed(ActionEvent e) {
      final int result = JOptionPane.showConfirmDialog(CadenzaFrame.this,
          "Are you sure you want to quit?", "Quit", JOptionPane.YES_NO_OPTION);
      if (result == JOptionPane.YES_OPTION) {
        safeClose(false);
      }
    }
  }
  
  private class Dirtyer<T> extends ListAdapter<T> implements DocumentListener, ChangeListener {
    @Override
    public void anyChange(ListEvent<T> e) {
      makeDirty();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
      makeDirty();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
      makeDirty();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
      makeDirty();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
      makeDirty();
    }
    
  }
}
