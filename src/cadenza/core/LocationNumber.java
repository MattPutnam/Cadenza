package cadenza.core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Represents a measure or song number.  LocationNumbers may contain a
 * number part and/or a letter part (at least one of the two) in that order,
 * such as "3A", "17", or "b".  The number must be >= 1, and the letter part
 * must contain only letters (upper or lower case).</p>
 * 
 * <p>LocationNumbers have a total ordering, first by the number (if present,
 * letter-only locations come first), then by the length of the letter part,
 * then finally by the letter part itself (non case sensitive).  That is, the
 * following examples are in order:</p>
 * 
 * <pre>a, B, c, z, aa, AB, ZZ, aBc, 1, 1a, 1z, 1bb, 2</pre>
 * 
 * <p>LocationNumbers are immutable.</p>
 * 
 * @author Matt Putnam
 */
public class LocationNumber implements Comparable<LocationNumber>, Serializable {
  private static final long serialVersionUID = 2L;
  
  /**
   * A utility LocationNumber with value "1", suitable for pre-populating
   * a field or UI control.
   */
  public static final LocationNumber TEMP =
      new LocationNumber(Integer.valueOf(1), null);
  
  private final Integer _numberPart;
  private final String  _letterPart;
  
  private LocationNumber(Integer numberPart, String letterPart) {
    _numberPart = numberPart;
    _letterPart = letterPart;
  }
  
  @Override
  public String toString() {
    return (_numberPart == null ? "" : _numberPart.toString())
         + (_letterPart == null ? "" : _letterPart);
  }
  
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof LocationNumber)) return false;
    final LocationNumber o = (LocationNumber) obj;
    return Objects.equals(_numberPart, o._numberPart)
        && Objects.equals(_letterPart, o._letterPart);
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(_numberPart, _letterPart);
  }
  
  @Override
  public int compareTo(LocationNumber o) {
    final int numCompare = (_numberPart == null ? Integer.valueOf(0) : _numberPart)
              .compareTo(o._numberPart == null ? Integer.valueOf(0) : o._numberPart);
    if (numCompare != 0)
      return numCompare;
    
    final String a = _letterPart == null ? "" : _letterPart;
    final String b = o._letterPart == null ? "" : o._letterPart;
    
    final int lengthCompare = a.length() - b.length();
    if (lengthCompare != 0)
      return lengthCompare;
    
    return a.compareToIgnoreCase(b);
  }
  
  private static final Map<String, LocationNumber> LNCACHE = new HashMap<>();
  private static final Pattern PATTERN = Pattern.compile("(\\d*)([a-zA-Z]*)");
  
  /**
   * Tries to parse the given String as a LocationNumber, returning either the
   * successfully parsed object or an Optional.empty().
   * @param string the input String that may represent a LocationNumber
   * @return <tt>Optional.of([a LocationNumber])</tt> if parsing is successful,
   *         <tt>Optional.emtpy()</tt> otherwise.
   */
  public static Optional<LocationNumber> tryParse(String string) {
    final LocationNumber tryget = LNCACHE.get(string);
    if (tryget != null)
      return Optional.of(tryget);
    
    if (string.isEmpty())
      return Optional.empty();
    
    final Matcher matcher = PATTERN.matcher(string);
    if (!matcher.matches())
      return Optional.empty();
    
    final String np = matcher.group(1);
    final String lp = matcher.group(2);
    
    final Integer numberPart = np.isEmpty() ? null : Integer.valueOf(np);
    if (numberPart != null && numberPart.intValue() < 1)
      return Optional.empty();
    
    final String letterPart = lp.isEmpty() ? null : lp;
    
    final LocationNumber ln = new LocationNumber(numberPart, letterPart);
    LNCACHE.put(string, ln);
    return Optional.of(ln);
  }
  
  /**
   * Returns whether or not the given String represents a valid LocationNumber
   * @param string the input String that may represent a LocationNumber
   * @return <tt>true</tt> iff the input is a valid LocationNumber
   */
  public static boolean isValid(String string) {
    return tryParse(string).isPresent();
  }
  
  /**
   * Parses the given String as a LocationNumber, or throws an exception if
   * parsing fails
   * @param string the input String that may represent a LocationNumber
   * @return the parsed LocationNumber
   * @throws IllegalArgumentException if parsing fails
   */
  public static LocationNumber parse(String string) {
    return tryParse(string).orElseThrow(IllegalArgumentException::new);
  }
}
