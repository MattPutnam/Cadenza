package cadenza.preferences;

final class Keys {
  private Keys() {}
  
  static class Keyboard {
    static String NAME           = "keyboard.name";
    static String CHANNEL        = "keyboard.channel";
    static String RANGE          = "keyboard.range";
    static String SOUNDING_RANGE = "keyboard.soundingrange";
  }
  
  static class Synthesizer {
    static String SYNTH      = "synthesizer.synth";
    static String CHANNELS   = "synthesizer.channels";
    static String EXPANSIONS = "synthesizer.expansions";
  }
  
  static class Midiport {
    static String INPUT  = "midiport.input";
    static String OUTPUT = "midiport.output";
  }
  
  static class Input {
    static String ALLOW_MIDI_INPUT  = "input.allow";
    
    static String ALLOW_VOLUME      = "input.volume.allow";
    static String VOLUME_STRICT     = "input.volume.strict";
    
    static String ALLOW_PATCHUSAGE  = "input.patchusage.allow";
    static String PATCHUSAGE_SINGLE = "input.patchusage.single";
    static String PATCHUSAGE_RANGE  = "input.patchusage.range";
    static String PATCHUSAGE_WHOLE  = "input.patchusage.whole";
  }
  
  static class PatchSearch {
    static String MODE           = "patchsearch.mode";
    static String REGEX_WRAP = "patchsearch.regexwrap";
    static String CASE_SENSITIVE = "patchsearch.casesensitive";
    static String EXCLUDE_USER   = "patchsearch.excludeuser";
  }
}
