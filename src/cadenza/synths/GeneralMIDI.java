package cadenza.synths;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cadenza.core.Patch;
import cadenza.core.Synthesizer;

import common.io.IOUtils;
import common.tuple.Pair;

public class GeneralMIDI {
  private static final String GM_PATH = "resources" + File.separator + "GM" + File.separator + "GM.txt";
  
  private static final List<Patch> GM1_PATCHES = new ArrayList<>(128);
  private static final List<Patch> GM2_PATCHES = new ArrayList<>(256);
  
  private static final List<Pair<Integer, Integer>> GM2_DATA = new ArrayList<>();
  
  private static boolean GM_VALID = false;
  
  static {
    final File GMFile = new File(GM_PATH);
    
    String[] lines = null;
    try {
      lines = IOUtils.getLineArray(GMFile);
      
      int num = 1;
      for (final String line : lines) {
        if (line.trim().isEmpty()) continue;
        
        final int PCnum = Integer.parseInt(line.substring(0, 3));
        final int GM2num = Integer.parseInt(line.substring(4, 5));
        final String name = line.substring(6);
        
        if (GM2num == 0)
          GM1_PATCHES.add(new Patch(Synthesizer.TEMP, name, "GM", PCnum));
        
        GM2_PATCHES.add(new Patch(Synthesizer.TEMP, name, "GM2", num));
        GM2_DATA.add(Pair.make(Integer.valueOf(PCnum), Integer.valueOf(GM2num)));
        num++;
      }
      
      GM_VALID = true;
    } catch (IOException e) {
      System.err.println("IOException trying to read GM file:");
      e.printStackTrace();
    } catch (Exception e) {
      System.err.println("Exception parsing GM file:");
      e.printStackTrace();
    }
  }
  
  /**
   * Builds a list of General MIDI level 1 patches for the given synthesizer.
   * Each Patch's synthesizer will be the given synth, and its bank will be "GM"
   * @param synth the synthesizer to use
   * @return a list of GM1 patches
   */
  public static List<Patch> getGM1Patches(Synthesizer synth) {
    if (!GM_VALID)
      return new ArrayList<>();
    
    final List<Patch> result = new ArrayList<>(GM1_PATCHES.size());
    for (final Patch patch : GM1_PATCHES)
      result.add(new Patch(synth, patch.name, patch.bank, patch.number));
    return result;
  }
  
  /**
   * Builds a list of General MIDI level 2 patches for the given synthesizer.
   * Each Patch's synthesizer will be the given synth, and its bank will be "GM2"
   * @param synth the synthesizer to sue
   * @return a list of GM2 patches
   */
  public static List<Patch> getGM2Patches(Synthesizer synth) {
    if (!GM_VALID)
      return new ArrayList<>();
    
    final List<Patch> result = new ArrayList<>(GM2_PATCHES.size());
    for (final Patch patch : GM2_PATCHES)
      result.add(new Patch(synth, patch.name, patch.bank, patch.number));
    return result;
  }
  
  /**
   * Determines the GM2 patch number and LSB value for the given GM2 patch number
   * @param GM2PatchNum the GM2 patch number to query
   * @return a pair of the patch number and LSB value to send to select the given
   * GM2 patch
   */
  public static Pair<Integer, Integer> getGM2_PCNum_LSB(int GM2PatchNum) {
    return GM2_DATA.get(GM2PatchNum);
  }
  
  private GeneralMIDI() {}
}
