package cadenza.gui.wizard;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;

import cadenza.core.CadenzaData;
import cadenza.core.Synthesizer;
import cadenza.gui.synthesizer.SynthesizerListEditor;

@SuppressWarnings("serial")
public class SynthesizerWizardPage extends WizardPage {
  public static final String SYNTHESIZERS_KEY = "Synthesizers";
  
  private final CadenzaData _data;
  private final SynthesizerListEditor _editor;
  
  public SynthesizerWizardPage(CadenzaData data) {
    super("Synthesizers", "Set up the synthesizers used in this performance");
    _data = data;
    _editor = new SynthesizerListEditor(new ArrayList<Synthesizer>());
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
