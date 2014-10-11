package cadenza.gui.wizard;

import java.util.List;
import java.util.Map;

import org.ciscavate.cjwizard.PageFactory;
import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;

import cadenza.core.CadenzaData;

public class CadenzaWizardPageFactory implements PageFactory {
  private final CadenzaData _data;
  private final WizardPage[] _pages;
  
  public CadenzaWizardPageFactory(CadenzaData data, Map<String, String> preferences) {
    _data = data;
    _pages = new WizardPage[] {
      new KeyboardWizardPage(_data, preferences),
      new SynthesizerWizardPage(_data),
      new GlobalTriggerWizardPage(_data),
      new GlobalControlMapWizardPage(_data)
    };
  }
  
  @Override
  public WizardPage createPage(List<WizardPage> path, WizardSettings settings) {
    return _pages[path.size()];
  }
  
  public WizardPage[] getPages() {
    return _pages;
  }
  
  public int getPageIndex(WizardPage page) {
    for (int i = 0; i < _pages.length; ++i)
      if (_pages[i] == page)
        return i;
    return -1;
  }
}
