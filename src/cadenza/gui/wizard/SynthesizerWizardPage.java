package cadenza.gui.wizard;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;

import cadenza.core.CadenzaData;
import cadenza.core.Synthesizer;
import cadenza.gui.synthesizer.SynthesizerListEditor;
import cadenza.preferences.Preferences;

@SuppressWarnings("serial")
public class SynthesizerWizardPage extends WizardPage {
  public static final String SYNTHESIZERS_KEY = "Synthesizers";
  
  private final CadenzaData _data;
  private final SynthesizerListEditor _editor;
  
  public SynthesizerWizardPage(CadenzaData data, Map<String, String> preferences) {
    super("Synthesizers", "Set up the synthesizers used in this performance");
    _data = data;
    
    final Synthesizer synth = Preferences.buildDefaultSynthesizer(preferences);
    final List<Synthesizer> synths = new ArrayList<>(1);
    synths.add(synth);
    
    _editor = new SynthesizerListEditor(synths);
    _editor.setName("SynthesizerListEditor");
    
    setLayout(new BorderLayout());
    add(_editor, BorderLayout.CENTER);
  }
  
  @Override
  public void rendering(List<WizardPage> path, WizardSettings settings) {
    setNextEnabled(true);
    setFinishEnabled(true);
  }
  
  @Override
  public void updateSettings(WizardSettings settings) {
    super.updateSettings(settings);
    _data.synthesizers.clear();
    _data.synthesizers.addAll(_editor.getSynthesizers());
  }
}
