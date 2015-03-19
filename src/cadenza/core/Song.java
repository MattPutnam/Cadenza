package cadenza.core;

import java.io.Serializable;

public class Song implements Comparable<Song>, Serializable {
  private static final long serialVersionUID = 2L;
  
  public LocationNumber number;
  public String name;
  
  public Song(LocationNumber number, String name) {
    this.number = number;
    this.name = name;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Song))
      return false;
    return ((Song) obj).number.equals(number);
  }
  
  @Override
  public int hashCode() {
    return number.hashCode();
  }
  
  @Override
  public String toString() {
    return number + ": " + name;
  }
  
  @Override
  public int compareTo(Song other) {
    return number.compareTo(other.number);
  }
}
