package cadenza.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.ciscavate.cjwizard.WizardContainer;
import org.ciscavate.cjwizard.WizardListener;
import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;

import cadenza.core.CadenzaData;
import cadenza.gui.wizard.CadenzaWizardPageFactory;
import cadenza.gui.wizard.OverviewPageTemplate;
import cadenza.preferences.Preferences;

import common.io.IOUtils;
import common.swing.BlockingTask;
import common.swing.SwingUtils;
import common.swing.dialog.Dialog;

@SuppressWarnings("serial")
public class Cadenza extends JFrame {
  private static File _lastPath = null;
  private static final File _RECENTS = new File("resources" + File.separator + "recentfiles.txt");
  private static final int NUM_RECENTS = 10;
  
  private static final String DELEGATE_PATH = "resources" + File.separator + "delegates" + File.separator;
  private static final String SYNTH_CONFIG_PATH = "resources" + File.separator + "synthconfigs" + File.separator;
  private static final String EXPANSION_CONFIG_PATH = "resources" + File.separator + "expansionconfigs" + File.separator;
  
  private static JComboBox<File> _recents;
  private static CadenzaDelegate _delegate;
  
  private static Cadenza INSTANCE = new Cadenza();
  
  private Cadenza() {
    super();
    _recents = buildRecents();
    init();
  }
  
  static void setDelegate(CadenzaDelegate delegate) {
    _delegate = delegate;
  }
  
  static void showHome() {
    INSTANCE.setVisible(true);
    if (_delegate != null)
      _delegate.doAfterShowHome();
  }
  
  private static void hideHome() {
    INSTANCE.setVisible(false);
  }
  
  private static JComboBox<File> buildRecents() {
    List<File> list;
    try {
      list = loadRecents();
    } catch (final IOException ioe) {
      ioe.printStackTrace();
      list = new ArrayList<>();
    }
    
    final JComboBox<File> result = new JComboBox<>(list.toArray(new File[list.size()]));
    result.setRenderer(new FileRenderer());
    result.setPreferredSize(new Dimension(200, 12));
    return result;
  }
  
  private static class FileRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value,
        int index, boolean isSelected, boolean cellHasFocus) {
      final JLabel label = (JLabel) super.getListCellRendererComponent(list,
          value, index, isSelected, cellHasFocus);
      if (value instanceof String) {
        label.setText((String) value);
      } else if (value instanceof File) {
        label.setText(((File) value).getName());
      }
      return label;
    }
  }
  
  private void init() {
    final JPanel panel = new JPanel();
    final GroupLayout layout = new GroupLayout(panel);
    panel.setLayout(layout);
    
    final JLabel createEditLabel = new JLabel("Create/Edit Cadenza Files");
    createEditLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
    final JButton newButton = SwingUtils.button("New File...", e -> showWizard(Cadenza.this));
    final JButton openButton = SwingUtils.button("Open File...", e -> showOpenFileDialog(Cadenza.this));
    final JButton openRecentButton = new JButton(new OpenRecentAction());
    
    final JLabel importLabel = new JLabel("Import Configurations");
    importLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
    final JButton importSynthButton = new JButton(new ImportSynthConfigAction());
    final JButton importExpButton = new JButton(new ImportExpansionConfigAction());
    final JButton importDelegateButton = new JButton(new ImportDelegateAction());
    
    final JLabel spacer = new JLabel("                 ");
    
    final int def = GroupLayout.DEFAULT_SIZE;
    final int max = Short.MAX_VALUE;
    
    layout.setHorizontalGroup(
      layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(Alignment.LEADING)
          .addComponent(createEditLabel, def, def, max)
          .addComponent(newButton, def, def, max)
          .addComponent(openButton,def, def, max)
          .addComponent(openRecentButton, def, def, max))
        .addComponent(_recents)
        .addComponent(spacer)
        .addGroup(layout.createParallelGroup(Alignment.LEADING)
          .addComponent(importLabel, def, def, max)
          .addComponent(importSynthButton, def, def, max)
          .addComponent(importExpButton, def, def, max)
          .addComponent(importDelegateButton, def, def, max))
    );
    layout.setVerticalGroup(
      layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(Alignment.BASELINE)
          .addComponent(createEditLabel)
          .addComponent(importLabel))
        .addGroup(layout.createParallelGroup(Alignment.BASELINE)
          .addComponent(newButton)
          .addComponent(importSynthButton))
        .addGroup(layout.createParallelGroup(Alignment.BASELINE)
          .addComponent(openButton)
          .addComponent(importExpButton))
        .addGroup(layout.createParallelGroup(Alignment.BASELINE)
          .addComponent(openRecentButton)
          .addComponent(_recents)
          .addComponent(spacer)
          .addComponent(importDelegateButton))
    );
    
    panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
    
    add(panel);
    pack();
    
    setTitle("Welcome to Cadenza!");
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    setLocationRelativeTo(null);
    setResizable(false);
  }
  
  private class OpenRecentAction extends AbstractAction {
    public OpenRecentAction() {
      super("Open Recent File:");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
      final File selected = _recents.getItemAt(_recents.getSelectedIndex());
      if (selected != null && selected.exists()) {
        try {
          final CadenzaFrame temp = new CadenzaFrame(loadFile(selected));
          temp._associatedSave = selected;
          temp.makeClean();
          if (_delegate != null)
            _delegate.setupFrame(temp);
          
          hideHome();
          temp.setVisible(true);
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    }
  }
  
  private static void showWizard(final JFrame parent) {
    new BlockingTask(parent, () -> {
      final Map<String, String> preferences = new LinkedHashMap<>();
      
      try {
        preferences.putAll(Preferences.readAllPreferences());
      } catch (Exception e) {
        System.err.println("Exception reading preferences file:");
        e.printStackTrace();
        // TODO: better error report
      }
      
      final CadenzaData newData = new CadenzaData();
      final CadenzaWizardPageFactory pageFactory = new CadenzaWizardPageFactory(newData, preferences);
      final WizardContainer wizard = new WizardContainer(pageFactory, new OverviewPageTemplate(pageFactory));
      
      SwingUtils.doInSwing(() -> {
        final JDialog dialog = new JDialog();
        dialog.getContentPane().add(wizard);
        
        wizard.addWizardListener(new WizardListener() {
          @Override
          public void onPageChanged(WizardPage newPage, List<WizardPage> path) {
            dialog.setTitle(newPage.getTitle());
          }
      
          @Override
          public void onFinished(List<WizardPage> path, WizardSettings settings) {
            for (final WizardPage page : path)
              page.updateSettings(settings);
            
            final String[] midiIO = Preferences.buildDefaultMIDIPorts(preferences);
            newData.savedInputDeviceName = midiIO[0];
            newData.savedOutputDeviceName = midiIO[1];
            
            final CadenzaFrame temp = new CadenzaFrame(newData);
            
            if (_delegate != null)
              _delegate.setupFrame(temp);
            
            dialog.dispose();
            temp.setVisible(true);
          }
      
          @Override
          public void onCanceled(List<WizardPage> path, WizardSettings settings) {
            showHome();
            dialog.dispose();
          }
        });
        
        dialog.pack();
        dialog.setSize(700, 500);
        dialog.setLocationRelativeTo(parent);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
        hideHome();
        dialog.setVisible(true);
      }, false);
    }).start();
  }
  
  private static void showOpenFileDialog(Frame parent) {
    final File[] selected = IOUtils.showOpenFileDialog(parent, _lastPath, ".cdza", "Cadenza Files");
    if (selected != null && selected.length > 0) {
      final File file = selected[0];
      _lastPath = file;
      notifyRecent(file);
      try {
        final CadenzaFrame temp = new CadenzaFrame(loadFile(file));
        temp._associatedSave = file;
        temp.makeClean();
        
        if (_delegate != null)
          _delegate.setupFrame(temp);
        
        hideHome();
        temp.setVisible(true);
        
      } catch (Exception e) {
        Dialog.error(parent, "Unable to read file " +
            "because it is from an earlier version of Cadenza " +
            "which is incompatible with this version.");
        e.printStackTrace();
      }
    }
  }
  
  static CadenzaData loadFile(File file) throws Exception {
    return CadenzaData.readFromFile(file);
  }
  
  private static File getRecents() throws IOException {
    if (!_RECENTS.exists())
      _RECENTS.createNewFile();
    return _RECENTS;
  }
  
  private static List<File> loadRecents() throws IOException {
    final List<String> paths = IOUtils.getLineList(getRecents());
    final List<File> result = new ArrayList<>();
    for (final String path : paths) {
      final File temp = new File(path);
      if (temp.exists()) {
        result.add(temp);
      }
    }
    saveRecents(result);
    
    return result;
  }
  
  private static void saveRecents(List<File> files) {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(getRecents()))) {
      for (final File file : files) {
        writer.write(file.getAbsolutePath());
        writer.newLine();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  private class ImportSynthConfigAction extends AbstractAction {
    public ImportSynthConfigAction() {
      super("Synthesizer Configuration...");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
      final File[] files = IOUtils.showOpenFileDialog(Cadenza.this, _lastPath, ".txt", "Text Files");
      if (files.length > 0) {
        final File file = files[0];
        _lastPath = file;
        
        final File newFile = new File(SYNTH_CONFIG_PATH + file.getName());
        try {
          IOUtils.copyFile(file, newFile);
          Dialog.info(Cadenza.this, "File imported successfully.", "Success!");
        } catch (IOException e1) {
          Dialog.error(Cadenza.this, "There was an I/O Exception copying the file.", "Error during import");
          e1.printStackTrace();
        }
      }
    }
  }
  
  private class ImportExpansionConfigAction extends AbstractAction {
    public ImportExpansionConfigAction() {
      super("Expansion Configuration...");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
      final File[] files = IOUtils.showOpenFileDialog(Cadenza.this, _lastPath, ".txt", "Text Files");
      if (files.length > 0) {
        final File file = files[0];
        _lastPath = file;
        
        String firstLine;
        try {
          firstLine = IOUtils.getLineArray(file, 1)[0];
        } catch (final IOException ioe) {
          ioe.printStackTrace();
          Dialog.error(Cadenza.this, "Exception trying to read the given file", "Error during import");
          return;
        }
        
        final String[] existingExpansions = getExpansionFolders();
        String foundExpansion = null;
        for (final String expansion : existingExpansions) {
          if (firstLine.startsWith(expansion)) {
            foundExpansion = expansion;
            break;
          }
        }
        if (foundExpansion == null) {
          foundExpansion = JOptionPane.showInputDialog(Cadenza.this, "Enter the name of the expansion card");
        }
        
        final String directory = EXPANSION_CONFIG_PATH + foundExpansion + File.separator;
        final File dir = new File(directory);
        if (!dir.exists())
          dir.mkdir();
        
        final File newFile = new File(directory + file.getName());
        try {
          IOUtils.copyFile(file, newFile);
          Dialog.info(Cadenza.this, "File imported successfully.", "Success!");
        } catch (IOException e1) {
          Dialog.error(Cadenza.this, "There was an I/O Exception copying the file.", "Error during import");
          e1.printStackTrace();
        }
      }
    }
  }
  
  private static String[] getExpansionFolders() {
    final File expansionsFolder = new File(EXPANSION_CONFIG_PATH);
    final File[] subdirectories = expansionsFolder.listFiles();
    final String[] names = new String[subdirectories.length];
    for (int i = 0; i < names.length; ++i)
      names[i] = subdirectories[i].getName();
    return names;
  }
  
  private class ImportDelegateAction extends AbstractAction {
    public ImportDelegateAction() {
      super("Synthesizer Delegate...");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
      final File[] files = IOUtils.showOpenFileDialog(Cadenza.this, _lastPath, ".txt", "Text Files");
      if (files.length > 0) {
        final File file = files[0];
        _lastPath = file;
        
        final File newFile = new File(DELEGATE_PATH + file.getName());
        try {
          IOUtils.copyFile(file, newFile);
          Dialog.info(Cadenza.this, "File imported successfully.", "Success!");
        } catch (IOException e1) {
          Dialog.error(Cadenza.this, "There was an I/O Exception copying the file.", "Error during import");
          e1.printStackTrace();
        }
      }
    }
  }
  
  static void notifyRecent(File file) {
    List<File> recents;
    try {
      recents = loadRecents();
    } catch (final IOException ioe) {
      recents = new ArrayList<>();
    }
    
    if (recents.contains(file)) {
      recents.remove(file);
      recents.add(0, file);
      saveRecents(recents);
    } else {
      recents.add(0, file);
      if (recents.size() > NUM_RECENTS) {
        recents.remove(recents.size()-1);
      }
      saveRecents(recents);
    }
    _recents.setModel(new DefaultComboBoxModel<>(recents.toArray(new File[recents.size()])));
  }
}
