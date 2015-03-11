package cadenza;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Version {
  private static final Logger LOG = LogManager.getLogger(Version.class);
  
  private Version() {}
  
  private static final File versionFile = new File("resources/version.txt");
  
  private static String version = "[unknown]";
  static {
    if (versionFile.isFile()) {
      try (BufferedReader reader = new BufferedReader(new FileReader(versionFile))) {
        version = reader.readLine();
      } catch (IOException e) {
        LOG.fatal("Error reading version file", e);
      } 
    } else {
      LOG.fatal("Version file not found");
    }
  }
  
  public static String getVersion() {
    return version;
  }
}
