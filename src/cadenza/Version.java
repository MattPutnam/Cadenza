package cadenza;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Version {
  private Version() {}
  
  private static final File versionFile = new File("resources/version.txt");
  
  private static String version = "[unknown]";
  static {
    if (versionFile.isFile()) {
      try (BufferedReader reader = new BufferedReader(new FileReader(versionFile))) {
        version = reader.readLine();
      } catch (IOException e) {
        System.err.println("Error reading version file:");
        e.printStackTrace();
      } 
    } else {
      System.err.println("Version file not found");
    }
  }
  
  public static String getVersion() {
    return version;
  }
}
