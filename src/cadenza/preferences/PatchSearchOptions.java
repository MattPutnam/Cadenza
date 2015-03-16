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
  private boolean _regexWrap;
  private boolean _caseSensitive;
  private boolean _excludeUser;
  
  public PatchSearchOptions(PatchSearchMode searchMode,
      boolean regexWrap, boolean caseSensitive, boolean excludeUser) {
    _searchMode = searchMode;
    _regexWrap = regexWrap;
    _caseSensitive = caseSensitive;
    _excludeUser = excludeUser;
  }
  
  public PatchSearchMode getSearchMode() { return _searchMode; }
  public boolean isRegexWrap() { return _regexWrap; }
  public boolean isCaseSensitive() { return _caseSensitive; }
  public boolean isExcludeUser() { return _excludeUser; }
}
