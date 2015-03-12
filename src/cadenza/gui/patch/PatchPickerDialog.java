package cadenza.gui.patch;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;

import cadenza.core.Patch;
import cadenza.core.Synthesizer;
import cadenza.preferences.PatchSearchOptions.PatchSearchMode;
import cadenza.preferences.Preferences;
import cadenza.synths.Synthesizers;

import common.swing.DocumentAdapter;
import common.swing.VerificationException;
import common.swing.dialog.OKCancelDialog;

@SuppressWarnings("serial")
public class PatchPickerDialog extends OKCancelDialog {
  private final List<Synthesizer> _synthesizers;
  private final List<Patch> _suggestions;
  private final boolean _hasSuggestions;
  
  private JList<Patch> _resultList;
  private JList<Patch> _suggestionList;
  private JTextField _nameField;
  
  public PatchPickerDialog(Component parent, List<Synthesizer> synthesizers) {
    this(parent, synthesizers, null);
  }
  
  public PatchPickerDialog(Component parent, List<Synthesizer> synthesizers, List<Patch> suggestions) {
    super(parent);
    _synthesizers = synthesizers;
    _suggestions = suggestions;
    _hasSuggestions = _suggestions != null && _suggestions.size() > 0;
  }
  
  @Override
  protected JComponent buildContent() {
    final List<Patch> patches = new ArrayList<>();
    for (Synthesizer synthesizer : _synthesizers)
      patches.addAll(Synthesizers.loadPatches(synthesizer));
    
    _resultList = new JList<>();
    _resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    _resultList.setCellRenderer(new PatchRenderer());
    _resultList.setListData(patches.toArray(new Patch[patches.size()]));
    
    if (_hasSuggestions) {
      _suggestionList = new JList<>(_suggestions.toArray(new Patch[_suggestions.size()]));
      _suggestionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      _suggestionList.setCellRenderer(new PatchRenderer());
      
      _resultList.addListSelectionListener(e -> _suggestionList.clearSelection());
      
      _suggestionList.addListSelectionListener(e -> _resultList.clearSelection());
    }
    
    _nameField = new JTextField(16);
    
    final JTextField searchField = new JTextField(16);
    searchField.getDocument().addDocumentListener(new FilterListener(patches, searchField));
    searchField.putClientProperty("JTextField.variant", "search");
    
    final Box search = Box.createHorizontalBox();
    search.add(new JLabel("Filter text:"));
    search.add(searchField);
    final JPanel top = new JPanel(new BorderLayout());
    top.add(search, BorderLayout.SOUTH);
    if (_hasSuggestions) {
      top.add(new JLabel("Suggestions:"), BorderLayout.NORTH);
      top.add(new JScrollPane(_suggestionList), BorderLayout.CENTER);
    }
    
    final Box bottom = Box.createHorizontalBox();
    bottom.add(new JLabel("Rename:"));
    bottom.add(_nameField);
    
    final JPanel panel = new JPanel(new BorderLayout());
    panel.add(top, BorderLayout.NORTH);
    panel.add(new JScrollPane(_resultList), BorderLayout.CENTER);
    panel.add(bottom, BorderLayout.SOUTH);
    
    return panel;
  }
  
  @Override
  protected void initialize() {
    setSize(500, 400);
  }
  
  @Override
  protected String declareTitle() {
    return "Select Patch";
  }
  
  @Override
  protected void verify() throws VerificationException {
    if (_hasSuggestions) {
      if (_resultList.getSelectedIndex() == -1 && _suggestionList.getSelectedIndex() == -1)
        throw new VerificationException("Please select a patch", _suggestionList);
    } else if (_resultList.getSelectedIndex() == -1) {
      throw new VerificationException("Please select a patch", _resultList);
    }
  }
  
  public Patch getSelectedPatch() {
    final Patch selected;
    if (_hasSuggestions) {
      final Patch suggested = _suggestionList.getSelectedValue();
      selected = suggested == null ? _resultList.getSelectedValue() : suggested;
    } else {
      selected = _resultList.getSelectedValue();
    }
    
    final String text = _nameField.getText();
    if (!text.isEmpty())
      selected.name = text;
    
    return selected;
  }
  
  private class FilterListener extends DocumentAdapter {
    private final List<Patch> _patches;
    private final JTextField _searchField;
    
    public FilterListener(List<Patch> patches, JTextField searchField) {
      _patches = patches;
      _searchField = searchField;
    }
    
    @Override
    public void documentChanged(DocumentEvent e) {
      final String searchText = _searchField.getText();
      final PatchSearchMode mode = Preferences.getPatchSearchOptions().getSearchMode();
      final boolean caseSensitive = Preferences.getPatchSearchOptions().isCaseSensitive();
      
      if (mode == PatchSearchMode.SIMPLE) {
        if (caseSensitive) {
          _resultList.setListData(_patches.stream()
                                          .filter(patch -> patch.name.contains(searchText))
                                          .toArray(Patch[]::new));
        } else {
          final String lower = searchText.toLowerCase();
          _resultList.setListData(_patches.stream()
                                          .filter(patch -> patch.name.toLowerCase().contains(lower))
                                          .toArray(Patch[]::new));
        }
      } else if (mode == PatchSearchMode.PIPES) {
        final String[] tokens = searchText.split("\\|");
        if (caseSensitive) {
          final String[] searchTerms = Arrays.stream(tokens)
                                             .map(String::trim)
                                             .toArray(String[]::new);
          
          _resultList.setListData(_patches.stream()
                                          .filter(patch -> Arrays.stream(searchTerms)
                                                                 .anyMatch(patch.name::contains))
                                          .toArray(Patch[]::new));
        } else {
          final String[] searchTerms = Arrays.stream(tokens)
                                             .map(s -> s.trim().toLowerCase())
                                             .toArray(String[]::new);
          
          _resultList.setListData(_patches.stream()
                                          .filter(patch -> Arrays.stream(searchTerms)
                                                                 .anyMatch(patch.name.toLowerCase()::contains))
                                          .toArray(Patch[]::new));
        }
      } else { // mode == Preferences.REGEX
        final Pattern pattern;
        try {
          pattern = Pattern.compile(searchText);
        } catch (PatternSyntaxException ex) {
          return;
        }
        _resultList.setListData(_patches.stream()
                                        .filter(patch -> pattern.matcher(patch.name).matches())
                                        .toArray(Patch[]::new));
      }
      
      
    }
  }
}
