package cadenza.preferences;

public class PatchSearchOptions {
  public static enum PatchSearchMode {
    SIMPLE("Simple search - search the given text verbatim"),
    PIPES("Multiple search - separate multiple terms with a pipe (|)"),
    REGEX("Regex search - use a Java formatted regex (advanced)");
    
    private final String _display;
    private PatchSearchMode(String display) {
      _display = display;
    }
    
    @Override
    public String toString() {
      return _display;
    }
  }
  
  private PatchSearchMode _searchMode;
  private boolean _caseSensitive;
  
  public PatchSearchOptions(PatchSearchMode searchMode, boolean caseSensitive) {
    _searchMode = searchMode;
    _caseSensitive = caseSensitive;
  }
  
  public PatchSearchMode getSearchMode() { return _searchMode; }
  public boolean isCaseSensitive() { return _caseSensitive; }
}
