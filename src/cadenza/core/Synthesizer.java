package cadenza.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import common.Utils;

public class Synthesizer implements Serializable {
  private static final long serialVersionUID = 1L;
  
  private static final String EXP_STRING_MAP_ARROW = " -> ";
  
  public static final Synthesizer TEMP = new Synthesizer("TEMP", new ArrayList<String>(),
      new HashMap<String, String>(), new ArrayList<Integer>());
  
  /** The name of the synthesizer */
  private final String _name;
  
  /** The list of banks */
  private final List<String> _banks;
  
  /** The expansion cards: Slot name -> card type */
  private final Map<String, String> _expansions;
  
  private final String _expansionString;
  
  private List<Integer> _channels;
  
  public Synthesizer(String name, List<String> banks, Map<String, String> expansions, List<Integer> channels) {
    if (name == null || name.trim().isEmpty())
      throw new IllegalArgumentException("Name cannot be null or blank");
    
    _name = name;
    _banks = banks;
    _expansions = new HashMap<>(expansions);
    _channels = new ArrayList<>(channels);
    
    final List<String> tokens = new LinkedList<>();
    for (final Map.Entry<String, String> expansion : _expansions.entrySet()) {
      if (expansion.getValue() != null)
        tokens.add(expansion.getKey() + EXP_STRING_MAP_ARROW + expansion.getValue());
    }
    _expansionString = Utils.mkString(tokens);
  }
  
  @Override
  public String toString() {
    return _name;
  }
  
  public String getName() {
    return _name;
  }
  
  public List<String> getBanks() {
    return new ArrayList<>(_banks);
  }
  
  public Map<String, String> getExpansions() {
    return new HashMap<>(_expansions);
  }
  
  public String getExpansionString() {
    return _expansionString;
  }
  
  public static Map<String, String> parseExpansions(String string) {
    final Map<String, String> result = new LinkedHashMap<>();
    
    for (final String token : string.split("\\s*,\\s*")) {
      if (token.isEmpty()) continue;
      final int index = token.indexOf(EXP_STRING_MAP_ARROW);
      result.put(token.substring(0, index).trim(), token.substring(index + EXP_STRING_MAP_ARROW.length()).trim());
    }
    
    return result;
  }
  
  public List<Integer> getChannels() {
    return new ArrayList<>(_channels);
  }
  
  public void setChannels(List<Integer> channels) {
    _channels = new ArrayList<>(channels);
  }
  
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    final Synthesizer sm = (Synthesizer) obj;
    return this._name.equals(sm._name) &&
         this._expansions.equals(sm._expansions) &&
         this._banks.equals(sm._banks);
  }
  
  @Override
  public int hashCode() {
    int hashCode = _name.hashCode();
    hashCode = 31*hashCode + _expansions.hashCode();
    hashCode = 31*hashCode + _banks.hashCode();
    return hashCode;
  }
}
