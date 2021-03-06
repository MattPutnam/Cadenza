package cadenza.synths;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cadenza.core.Bank;
import cadenza.core.Patch;
import cadenza.core.Synthesizer;
import common.io.IOUtils;
import common.tuple.Pair;

public class Synthesizers {
  private static final Logger LOG = LogManager.getLogger(Synthesizers.class);
  
  private Synthesizers() {}
  
  private static final String _SYNTH_PATH = "resources" + File.separator + "synthconfigs";
  private static final String _EXP_PATH = "resources" + File.separator + "expansionconfigs";
  
  private static final String CARD = "CARD";
  
  /** List of all synthesizer names */
  public static List<String> SYNTH_NAMES = new ArrayList<>();
  
  /** List of all expansion card names */
  public static List<String> EXPANSION_NAMES = new ArrayList<>();
  
  /** Synth name -> (File, (Slot name -> Card type)) */
  private static final Map<String, Pair<File, Map<String, String>>> _SYNTH_FILES = new LinkedHashMap<>();
  
  /** Synth name -> list of banks */
  private static final Map<String, List<Bank>> _SYNTH_BANKS = new LinkedHashMap<>();
  
  /** Card name -> (Card type, File) */
  private static final Map<String, Pair<String, File>> _EXPANSION_FILES = new TreeMap<>();
  
  static {
    // build synthesizer names and info
    final File synthRoot = new File(_SYNTH_PATH);
    if (!synthRoot.isDirectory()) {
      LOG.fatal("Root directory '" + synthRoot.getAbsolutePath() + "' is not a directory.");
    }
    final File[] files = synthRoot.listFiles();
    for (final File file : files) {
      String[] info;
      try {
        info = IOUtils.getLineArray(file, 2);
      } catch (IOException ioe) {
        LOG.error("Error getting synth file info", ioe);
        info = new String[] {"", ""};
      }
      
      final String name = info[0];
      final String exps = info[1];
      
      final String[] expAssignments = exps.split(",");
      final Map<String, String> map = new TreeMap<>();
      for (final String assignment : expAssignments) {
        final String trimmed = assignment.trim();
        if (trimmed.isEmpty()) continue;
        
        final int equalsIndex = trimmed.indexOf('=');
        if (equalsIndex == -1)
          map.put(trimmed, CARD);
        else {
          map.put(trimmed.substring(0, equalsIndex).trim(),
                  trimmed.substring(equalsIndex+1).trim());
        }
      }
      
      _SYNTH_FILES.put(name, Pair.make(file, map));
      SYNTH_NAMES.add(name);
    }
    
    // load banks for all synth files
    for (final Entry<String, Pair<File, Map<String, String>>> entry : _SYNTH_FILES.entrySet()) {
      final List<Bank> banks = new ArrayList<>();
      final File file = entry.getValue()._1();
      
      try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
        String str;
        while ((str = reader.readLine()) != null) {
          if (str.startsWith("#"))
            banks.add(new Bank(str.substring(1)));
        }
      } catch (IOException e) {
        LOG.error("Error loading banks for synth file", e);
      }
      
      _SYNTH_BANKS.put(entry.getKey(), banks);
    }
    
    // build expansion info
    final File expRoot = new File(_EXP_PATH);
    if (!expRoot.isDirectory()) {
      LOG.fatal("Root directory '" + expRoot.getAbsolutePath() + "' is not a directory.");
    }
    final File[] subdirectories = expRoot.listFiles();
    for (final File dir : subdirectories) {
      final String dirname = dir.getName();
      final File[] expoFiles = dir.listFiles();
      for (final File expoFile : expoFiles) {
        String name;
        try {
          name = IOUtils.getLineArray(expoFile, 1)[0];
        } catch (final IOException ioe) {
          LOG.error("Error trying to read expansion file", ioe);
          name = "";
        }
        
        _EXPANSION_FILES.put(name, Pair.make(dirname, expoFile));
        EXPANSION_NAMES.add(name);
      }
    }
  }
  
  /**
   * Gets all of the expansions for a given synthesizer
   * @param synthname the synthesizer to look up the expansions of
   * @return Map[Slot name -> Card type]
   */
  public static Map<String, String> getExpansionsForSynth(String synthname) {
    return new LinkedHashMap<>(_SYNTH_FILES.get(synthname)._2());
  }
  
  public static List<Bank> getBanksForSynth(String synthname) {
    return new ArrayList<>(_SYNTH_BANKS.get(synthname));
  }
  
  /**
   * Gets a list of all expansion cards of a given type
   * @param type the type of expansion card to look up
   * @return a list of all expansion cards of the given type
   */
  public static List<String> getExpansionsOfType(String type) {
    final List<String> result = new ArrayList<>();
    
    for (final Map.Entry<String, Pair<String, File>> entry : _EXPANSION_FILES.entrySet()) {
      if (entry.getValue()._1().equals(type)) {
        result.add(entry.getKey());
      }
    }
    
    return result;
  }
  
  private static final Map<Synthesizer, List<Patch>> SM_CACHE = new HashMap<>();
  
  /**
   * Returns a list of patches for the given synthesizer and listed expansions.  This method caches
   * its result, so calling it is very cheap except for the first time.
   * @param synthesizer the synthesizer.  Patches will be drawn from presets on the device, and all
   * defined expansion cards
   * @return a list of all available patches for the synth given
   */
  public static List<Patch> loadPatches(Synthesizer synthesizer) {
    if (SM_CACHE.containsKey(synthesizer))
      return new ArrayList<>(SM_CACHE.get(synthesizer));
    
    final File synthFile = _SYNTH_FILES.get(synthesizer.getName())._1();
    
    final List<Patch> result = new ArrayList<>(4*128);
    
    // get built-in patches
    String[] mainLines;
    try {
      mainLines = IOUtils.getLineArray(synthFile);
    } catch (final IOException ioe) {
      LOG.error("Error while loading synth patches", ioe);
      mainLines = new String[0];
    }
    
    Bank bank = null;
    for (int i = 2; i < mainLines.length; ++i) {
      final String line = mainLines[i];
      if (line.trim().isEmpty())
        continue;
      
      if (line.startsWith("#")) {
        bank = new Bank(line.substring(1).trim());
        
        if (bank.equals(Bank.GM1_BANK))
          result.addAll(GeneralMIDI.getGM1Patches(synthesizer));
        else if (bank.equals(Bank.GM2_BANK))
          result.addAll(GeneralMIDI.getGM2Patches(synthesizer));
      } else {
        final int spaceIndex = StringUtils.indexOfAny(line, ' ', '\t');
        final String num = line.substring(0, spaceIndex).trim();
        final String name = line.substring(spaceIndex+1).trim();
        result.add(new Patch(synthesizer, name, bank, Integer.parseInt(num)));
      }
    }
    
    // get expansion patches
    for (final Map.Entry<String, String> entry : synthesizer.getExpansions().entrySet()) {
      final String slot = entry.getKey();
      final String card = entry.getValue();
      
      final File expFile = _EXPANSION_FILES.get(card)._2();
      
      String[] expLines;
      try {
        expLines = IOUtils.getLineArray(expFile);
      } catch (final IOException ioe) {
        LOG.error("Error while loading expansion patches", ioe);
        expLines = new String[0];
      }
      
      for (int i = 1; i < expLines.length; ++i) {
        final String line = expLines[i];
        final int spaceIndex = line.indexOf(' ');
        final String num = line.substring(0, spaceIndex).trim();
        final String name = line.substring(spaceIndex + 1).trim();
        result.add(new Patch(synthesizer, name, new Bank(slot, getSelector(synthesizer.getName(), slot, synthesizer.getExpansions())), Integer.parseInt(num)));
      }
    }
    
    SM_CACHE.put(synthesizer, result);
    return result;
  }
  
  public static Stream<Patch> streamPatches(Synthesizer synthesizer) {
    return loadPatches(synthesizer).stream();
  }
  
  public static Stream<Bank> getAllBanks(Synthesizer synthesizer) {
    return streamPatches(synthesizer).map(p -> p.bank).distinct();
  }
  
  private static String getSelector(String synthname, String slotName, Map<String, String> expansions) {
    if (synthname.equals("Roland XV-5080") || synthname.equals("Roland XV-3080")) {
      // The XV-5080 and 3080 banks depend on the card; Cadenza uses the card number
      // example: "SR-JV80-01", "SRX-03"
      String fullCard = expansions.get(slotName);
      return fullCard.substring(0, fullCard.indexOf(" "));
    } else {
      return slotName;
    }
  }
}
