package cadenza.delegate;

import java.util.ArrayList;
import java.util.List;

import cadenza.delegate.DelegateEntry.MessageType;

import common.tuple.Triple;

public class DelegateEntryParser {
  private static final String START = "TO SELECT BANK";
  private static final String PATCH = "PATCH";
  private static final String SEND = "SEND";
  private static final String CC = "CC#";
  private static final String PC = "PC";
  private static final String SYSEX = "SYSEX";
  private static final String VALUE = "VALUE=";
  private static final String THEN = "THEN";
  
  private DelegateEntryParser() {}
  
  public static DelegateEntry parse(String line) throws ParseException {
    // Split into bank/patch section and send section around the SEND token
    final int sendPosition = line.indexOf(SEND);
    if (sendPosition == -1)
      throw new ParseException(line, "No '" + SEND + "' token found");
    
    // Parse the bank/patch section
    final String bankAndPatch = line.substring(0, sendPosition).trim();
    if (!bankAndPatch.startsWith(START))
      throw new ParseException(line, "Line does not start with '" + START + "'");
    final int patchPosition = bankAndPatch.indexOf(PATCH);
    if (patchPosition == -1)
      throw new ParseException(line, "No '" + PATCH + "' token found");
    final String bank = bankAndPatch.substring(START.length(), patchPosition).trim();
    final String patchRange = bankAndPatch.substring(patchPosition + PATCH.length()).trim();
    final int patchDash = patchRange.indexOf("-");
    final int minNum, maxNum;
    try {
      if (patchDash == -1) {
        minNum = maxNum = Integer.parseInt(patchRange);
      } else {
        minNum = Integer.parseInt(patchRange.substring(0, patchDash).trim());
        maxNum = Integer.parseInt(patchRange.substring(patchDash+1).trim());
      }
    } catch (NumberFormatException e) {
      throw new ParseException(line, "Patch range '" + patchRange +
          "' malformed, must be a single number or number range separated by a hyphen");
    }
    
    // Parse the list of sends
    final String sends = line.substring(sendPosition + SEND.length()).trim();
    final String[] tokens = sends.split(THEN);
    if (tokens.length == 0)
      throw new ParseException(line, "No CC/PC sends found");
    final List<Triple<MessageType, Integer, ? extends Object>> commands = new ArrayList<>(tokens.length);
    for (final String token : tokens) {
      final String[] parts = token.trim().split("\\s");
      if (parts.length != 2)
        throw new ParseException(line, "Exactly 2 parts to each command expected, problem with '" + token +
            "'.  Make sure there are no extra spaces.");
      
      // part 1 is "CC#=[num]", "PC", or "SYSEX"
      final MessageType mt;
      final int cc;
      final String part0 = parts[0].trim();
      if (part0.equals(PC)) {
        mt = MessageType.PROGRAM_CHANGE;
        cc = 0;
      } else if (part0.startsWith(CC)) {
        mt = MessageType.CONTROL_CHANGE;
        try {
          cc = Integer.parseInt(part0.substring(3));
        } catch (NumberFormatException e) {
          throw new ParseException(line, "Malformed CC value '" + part0 + "', expected format is 'CC#[num]'");
        }
      } else if (part0.equals(SYSEX)) {
        mt = MessageType.SYSEX;
        cc = 0;
      } else {
        throw new ParseException(line, "Could not parse send command '" + token);
      }
      
      final String part1 = parts[1].trim();
      if (mt == MessageType.SYSEX) {
        // For Sysex, part 2 is "VALUE=[byte,byte...]" with commas delimiting (no spaces)
        if (!part1.startsWith(VALUE))
          throw new ParseException(line, "Malformed Sysex value '" + part1 + "', expected format is 'VALUE=[byte,byte,...]'");
        
        final String[] byteStrings = part1.substring(VALUE.length()+1, part1.length()-1).split("[\\s,]+");
        if (byteStrings.length == 0)
          throw new ParseException(line, "Malformed Sysex value '" + part1 + "', expected at least one byte");
        final byte[] bytes = new byte[byteStrings.length];
        for (int i = 0; i < byteStrings.length; ++i) {
          bytes[i] = Integer.decode(byteStrings[i].trim()).byteValue();
        }
        
        commands.add(Triple.make(mt, Integer.valueOf(0), bytes));
      } else {
        // For CC/PC, part 2 is "VALUE=[value]" or "VALUE=[min-max]".  For range just use the bottom number, top is cosmetic.
        if (!part1.startsWith(VALUE))
          throw new ParseException(line, "Malformed CC/PC value '" + part1 +
              "', expected format is 'VALUE=[num]' or 'VALUE=[min-max]''");
        final int val;
        try {
          final int i = part1.indexOf("-");
          if (i == -1)
            val = Integer.parseInt(part1.substring(6));
          else
            val = Integer.parseInt(part1.substring(6, i));
        } catch (NumberFormatException e) {
          throw new ParseException(line, "Malformed CC/PC value '" + part1 +
              ", expected format is 'VALUE=[num]' or 'VALUE=[min-max]''");
        }
        
        commands.add(Triple.make(mt, Integer.valueOf(cc), Integer.valueOf(val)));
      }
    }
    
    return new DelegateEntry(bank, minNum, maxNum, commands);
  }
}
