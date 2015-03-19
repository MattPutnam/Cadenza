package cadenza.core;

import java.io.Serializable;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jgoodies.common.base.Objects;

public class LocationNumber implements Comparable<LocationNumber>, Serializable {
  private static final long serialVersionUID = 2L;
  
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
    return _numberPart.hashCode() << 16 + _letterPart.hashCode();
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
  
  private static final Pattern PATTERN = Pattern.compile("(\\d*)([a-zA-Z]*)");
  
  public static Optional<LocationNumber> tryParse(String string) {
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
    
    return Optional.of(new LocationNumber(numberPart, letterPart));
  }
  
  public static boolean isValid(String string) {
    return tryParse(string).isPresent();
  }
  
  public static LocationNumber parse(String string) {
    return tryParse(string).orElseThrow(IllegalArgumentException::new);
  }
}
