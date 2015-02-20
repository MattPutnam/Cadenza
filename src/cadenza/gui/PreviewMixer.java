package cadenza.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import cadenza.control.PreviewController;
import cadenza.core.CadenzaData;
import cadenza.core.Patch;
import cadenza.gui.CadenzaFrame.Mode;

import common.swing.IntField;
import common.swing.SwingUtils;

@SuppressWarnings("serial")
public class PreviewMixer extends JPanel {
  private final PreviewController _controller;
  private final CadenzaData _data;
  
  private CardLayout _cardLayout;
  
  private JPanel _performModePane;
  private JPanel _previewModePane;
  
  private Box _mixerBox;
  
  public PreviewMixer(PreviewController controller, CadenzaData data) {
    _controller = controller;
    _data = data;
    
    init();
  }
  
  private void init() {
    _cardLayout = new CardLayout();
    
    _mixerBox = Box.createHorizontalBox();
    
    _performModePane = new JPanel(new BorderLayout());
    _performModePane.add(new JLabel("Perform Mode", SwingConstants.CENTER), BorderLayout.CENTER);
    
    _previewModePane = new JPanel(new BorderLayout());
    _previewModePane.add(new JLabel("Previewing Patches:", SwingConstants.CENTER), BorderLayout.NORTH);
    _previewModePane.add(_mixerBox, BorderLayout.CENTER);
    
    setLayout(_cardLayout);
    add(_performModePane, Mode.PERFORM.name());
    add(_previewModePane, Mode.PREVIEW.name());
  }

  public void updatePreviewPatches(final List<Patch> patches) {
    SwingUtilities.invokeLater(() -> {
      _cardLayout.show(PreviewMixer.this, Mode.PREVIEW.name());
      
      _mixerBox.removeAll();
      _mixerBox.add(Box.createHorizontalGlue());
      for (final Patch patch : patches) {
        _mixerBox.add(createChannel(patch));
      }
      _mixerBox.add(Box.createHorizontalGlue());
      _previewModePane.revalidate();
      _previewModePane.repaint();
    });
  }
  
  public void goPerformMode() {
    SwingUtilities.invokeLater(() -> _cardLayout.show(PreviewMixer.this, Mode.PERFORM.name()));
  }
  
  private JComponent createChannel(final Patch patch) {
    final JLabel label = new JLabel(patch.name, SwingConstants.CENTER);
    label.setOpaque(true);
    label.setBackground(patch.getDisplayColor());
    label.setForeground(patch.getTextColor());
    
    final JSlider slider = new JSlider(SwingConstants.VERTICAL, 0, 127, patch.defaultVolume);
    final IntField textField = new IntField(patch.defaultVolume, 0, 127);
    
    final boolean[] modifying = {false};
    slider.addChangeListener(e -> {
      if (modifying[0]) return;
      
      final int value = slider.getValue();
      modifying[0] = true;
      textField.setText(String.valueOf(value));
      modifying[0] = false;
      
      _controller.setVolume(value, patch);
    });
    textField.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void changedUpdate(DocumentEvent e) {
        changed();
      }

      @Override
      public void insertUpdate(DocumentEvent e) {
        changed();
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        changed();
      }
      
      private void changed() {
        if (modifying[0]) return;
        
        // if replacing all text with a new value, it will temporarily be empty
        if (textField.getText().isEmpty()) return;
        
        final int value = textField.getInt();
        modifying[0] = true;
        slider.setValue(value);
        modifying[0] = false;
        
        _controller.setVolume(value, patch);
      }
    });
    
    final Box subSouth = Box.createVerticalBox();
    subSouth.add(textField);
    subSouth.add(SwingUtils.button("Reset",  "Reset to the default volume", e -> textField.setInt(patch.defaultVolume)));
    subSouth.add(SwingUtils.button("Set", "Set the selected volume as the default", e -> {
      patch.defaultVolume = textField.getInt();
      _data.patches.notifyChange(patch);
    }));
    
    final JPanel panel = new JPanel(new BorderLayout());
    panel.add(label, BorderLayout.NORTH);
    panel.add(slider, BorderLayout.CENTER);
    panel.add(subSouth, BorderLayout.SOUTH);
    panel.setBackground(Color.WHITE);
    panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2),
                                                       BorderFactory.createLineBorder(Color.BLACK)));
    
    SwingUtils.freezeWidth(panel, 100);
    
    return panel;
  }
}
