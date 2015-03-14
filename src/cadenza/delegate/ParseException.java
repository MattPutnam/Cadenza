package cadenza.delegate;

@SuppressWarnings("serial")
public class ParseException extends Exception {
  public ParseException(String line, String reason) {
    super("Unable to parse line '" + line + "', " + reason);
  }
}
