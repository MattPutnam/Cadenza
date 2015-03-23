package cadenza.delegate;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cadenza.core.Bank;
import cadenza.core.Patch;
import cadenza.delegate.DelegateEntry.MessageType;
import cadenza.synths.GeneralMIDI;
import common.io.IOUtils;
import common.tuple.Pair;
import common.tuple.Triple;

public class PatchChangeDelegate {
  private static final Logger LOG = LogManager.getLogger(PatchChangeDelegate.class);
  
  private static final String DELEGATE_DIR = "resources/delegates/";
  
  private static final Map<String, PatchChangeDelegate> MAP;
  static {
    MAP = new HashMap<>();
    
    final File dir = new File(DELEGATE_DIR);
    final File[] delegateFiles = dir.listFiles();
    for (final File delegateFile : delegateFiles) {
      String[] lines = null;
      try {
        lines = IOUtils.getLineArray(delegateFile);
      } catch (IOException e) {
        LOG.fatal("IOException while handling delegate file " + delegateFile.getName(), e);
        continue;
      }
        
      final String name = lines[0].trim();
      
      final List<DelegateEntry> entries = new ArrayList<>();
      for (int i = 1; i < lines.length; ++i) {
        final String trimmed = lines[i].trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#"))
          continue;
        
        DelegateEntry entry = null;
        try {
          entry = DelegateEntryParser.parse(trimmed);
        } catch (ParseException e) {
          LOG.warn("Unable to parse delegate entry", e.getMessage());
          continue;
        }
        
        entries.add(entry);
      }
      
      MAP.put(name, new PatchChangeDelegate(entries));
    }
  }
  
  public static PatchChangeDelegate getDelegate(String name) {
    return MAP.get(name);
  }
  
  public static void performPatchChange(Receiver receiver, Patch patch, int channel)
      throws InvalidMidiDataException {
    getDelegate(patch.getSynthesizer().getName()).sendPatchChange(receiver, patch, channel);
  }

  private final List<DelegateEntry> _entries;
  private PatchChangeDelegate(List<DelegateEntry> entries) {
    _entries = entries;
  }
  
  private void sendPatchChange(Receiver receiver, Patch patch, int channel) throws InvalidMidiDataException {
    final int patchNum = patch.number;
    final ShortMessage msg = new ShortMessage();
    
    if (patch.bank.equals(Bank.GM1_BANK)) {
      msg.setMessage(ShortMessage.CONTROL_CHANGE, channel, 0, 121);
      receiver.send(msg, -1);
      
      msg.setMessage(ShortMessage.CONTROL_CHANGE, channel, 32, 0);
      receiver.send(msg, -1);
      
      msg.setMessage(ShortMessage.PROGRAM_CHANGE, channel, patchNum-1, 0);
      receiver.send(msg, -1);
    } else if (patch.bank.equals(Bank.GM2_BANK)) {
      final Pair<Integer, Integer> GM2_PCNum_LSB = GeneralMIDI.getGM2_PCNum_LSB(patchNum);
      
      msg.setMessage(ShortMessage.CONTROL_CHANGE, channel, 0, 121);
      receiver.send(msg, -1);
      
      msg.setMessage(ShortMessage.CONTROL_CHANGE, channel, 32, GM2_PCNum_LSB._2().intValue());
      receiver.send(msg, -1);
      
      msg.setMessage(ShortMessage.PROGRAM_CHANGE, channel, GM2_PCNum_LSB._1().intValue()-1, 0);
      receiver.send(msg, -1);
    } else {
      final String selector = patch.bank.getSelector();
      DelegateEntry entry = null;
      for (final DelegateEntry e : _entries) {
        if (e.bankName.equals(selector) && e.minNum <= patchNum && patchNum <= e.maxNum) {
          entry = e;
          break;
        }
      }
      
      if (entry == null)
        throw new InvalidMidiDataException("Could not find delegate entry for patch " + patch.toString());
      
      for (final Triple<MessageType, Integer, ?> command : entry.commands) {
        if (command._1() == MessageType.CONTROL_CHANGE) {
          msg.setMessage(ShortMessage.CONTROL_CHANGE, channel, command._2().intValue(), ((Integer) command._3()).intValue());
          receiver.send(msg, -1);
        } else if (command._1() == MessageType.PROGRAM_CHANGE) {
          msg.setMessage(ShortMessage.PROGRAM_CHANGE, channel, patchNum - entry.minNum + ((Integer) command._3()).intValue(), 0);
          receiver.send(msg, -1);
        } else if (command._1() == MessageType.SYSEX) {
          final byte[] bytes = (byte[]) command._3();
          final SysexMessage sm = new SysexMessage(bytes, bytes.length);
          receiver.send(sm, -1);
        } else {
          throw new InvalidMidiDataException("Unknown message type: " + command._1());
        }
      }
    }
  }
}
