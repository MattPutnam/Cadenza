package cadenza.control;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Receiver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import common.midi.MidiMessageSender;

/**
 * Sends messages that can be used to program the devices made by MidiSolutions
 * (http://www.midisolutions.com).  This class does not provide all possible
 * programming options, but rather a collection thought useful for use in
 * Cadenza.  See the manuals on the MidiSolutions website for complete
 * specifications.
 * 
 * @author Matt Putnam
 */
public class MidiSolutionsMessageSender extends MidiMessageSender {
  private static final Logger LOG = LogManager.getLogger(MidiSolutionsMessageSender.class);
  
  public MidiSolutionsMessageSender(Receiver receiver) {
    super(receiver);
  }
  
  /**
   * Sends the command to program a Footswitch Controller to send a NOTE ON
   * signal.  No corresponding NOTE OFF is sent.  The message is sent with
   * velocity 0x80.  Use this to program a footswitch to send a note out of
   * range of the keyboard (for example, C#8 on a standard keyboard), which
   * can be used for patch advance triggers.
   * @param midiNumber - the MIDI number of the note
   * @param channel - the channel for the note, 0-indexed
   */
  public void sendFootswitchNoteOnMessage(int midiNumber, int channel) {
    if (!isValid())
      return;
    
    try {
      sendSysexMessage(-1, 0xF0, 0, 0, 0x50, 0x04, 0x11, midiNumber, 0x80, channel, 0xF7);
    } catch (InvalidMidiDataException e) {
      // shouldn't happen
      LOG.error("Error during sendFootswitchNoteOnMessage", e);
    }
  }
  
  /**
   * Sends the command to program a Footswitch Controller to send a control
   * change message.
   * @param number - the control change number to send
   * @param value - the control change value to send
   * @param channel - the channel for the message, 0-indexed
   */
  public void sendFootswitchControlChangeMessage(int number, int value, int channel) {
    if (!isValid())
      return;
    
    try {
      sendSysexMessage(-1, 0xF0, 0, 0, 0x50, 0x04, 0x12, number, value, channel, 0xF7);
    } catch (InvalidMidiDataException e) {
      // shouldn't happen
      LOG.error("Error during sendFootswitchControlChangeMessage", e);
    }
  }
  
  /**
   * Sends the command to program a Footswitch Controller to send a MIDI
   * Start message.  No stop message is sent.
   */
  public void sendFootswitchMidiStartMessage() {
    if (!isValid())
      return;
    
    try {
      sendSysexMessage(-1, 0xF0, 0, 0, 0x50, 0x04, 0x05, 0x01, 0xF7);
    } catch (InvalidMidiDataException e) {
      // shouldn't happen
      LOG.error("Error during sendFootswitchMidiStartMessage", e);
    }
  }
  
  /**
   * Sends the command to program a Footswitch Controller to send a MIDI
   * Stop message.
   */
  public void sendFootswitchMidiStopMessage() {
    if (!isValid())
      return;
    
    try {
      sendSysexMessage(-1, 0xF0, 0, 0, 0x50, 0x04, 0x05, 0x00, 0xF7);
    } catch (InvalidMidiDataException e) {
      // shouldn't happen
      LOG.error("Error during sendFootswitchMidiStopMessage", e);
    }
  }
  
  /**
   * Sends the command to program a Footswitch Controller to send a MIDI
   * Start message when pressed, and a Stop message when released.
   */
  public void sendFootswitchMidiStartStopMessage() {
    if (!isValid())
      return;
    
    try {
      sendSysexMessage(-1, 0xF0, 0, 0, 0x50, 0x04, 0x05, 0xF7);
    } catch (InvalidMidiDataException e) {
      // shouldn't happen
      LOG.error("Error during sendFootswitchMidiStartStopMessage", e);
    }
  }
  
  /**
   * Sends the command to program a Footswitch controller to send a Pitch
   * Bend message when pressed, and to reset to 0 when released.
   * @param lsb - the pitch bend LSB
   * @param msb - the pitch bend MSB
   * @param channel - the channel, 0-indexed
   */
  public void sendFootswitchPitchBendMessage(int lsb, int msb, int channel) {
    if (!isValid())
      return;
    
    try {
      sendSysexMessage(-1, 0xF0, 0, 0, 0x50, 0x04, 0x03, lsb, msb, channel, 0xF7);
    } catch (InvalidMidiDataException e) {
      // shouldn't happen
      LOG.error("Error during sendFootswitchPitchBendMessage", e);
    }
  }
  
  /**
   * Sends the command to program a Footswitch Controller to send a Program
   * Change message when pressed.  No Bank Select is sent.
   * @param programNumber - the program number to send
   * @param channel - the channel, 0-indexed
   */
  public void sendFootswitchProgramChangeMessage(int programNumber, int channel) {
    if (!isValid())
      return;
    
    try {
      sendSysexMessage(-1, 0xF0, 0, 0, 0x50, 0x04, 0x04, programNumber, channel, 0xF7);
    } catch (InvalidMidiDataException e) {
      // shouldn't happen
      LOG.error("Error during sendFootswitchProgramChangeMessage", e);
    }
  }
  
  /**
   * Sends the command to program a Footswitch Controller to send a Bank
   * Select and Program Change message when pressed.
   * @param bankMSB - the MSB of the bank select
   * @param bankLSB - the LSB of the bank select
   * @param programNumber - the program number to send
   * @param channel - the channel, 0-indexed
   */
  public void sendFootswitchProgramChangeMessage(int bankMSB, int bankLSB, int programNumber, int channel) {
    if (!isValid())
      return;
    
    try {
      sendSysexMessage(-1, 0xF0, 0, 0, 0x50, 0x04, 0x04, bankMSB, bankLSB, programNumber, channel, 0xF7);
    } catch (InvalidMidiDataException e) {
      // shouldn't happen
      LOG.error("Error during sendFootswitchProgramChangeMessage", e);
    }
  }
  
  /**
   * Sends the command to program a Footswitch Controller to perform the
   * Program Change Capture command.
   */
  public void sendFootswitchProgramChangeCaptureMessage() {
    if (!isValid())
      return;
    
    try {
      sendSysexMessage(-1, 0xF0, 0, 0, 0x50, 0x04, 0x0D, 0xF7);
    } catch (InvalidMidiDataException e) {
      // shouldn't happen
      LOG.error("Error during sendFootswitchProgramChangeCaptureMessage", e);
    }
  }
  
  /**
   * Sends the command to program a Footswitch Controller to send a Program
   * Change INC command when pressed.  No min/max values for the program
   * number are used.  If using one pedal as a PC INC and another as a PC
   * DEC, the MIDI OUT of the DEC unit must be connected to the MIDI IN of
   * the INC unit.
   * @param channel - the channel, 0-indexed.
   */
  public void sendFootswitchINCMessage(int channel) {
    if (!isValid())
      return;
    
    try {
      sendSysexMessage(-1, 0xF0, 0, 0, 0x50, 0x04, 0x07, 0x01, channel, 0xF7);
    } catch (InvalidMidiDataException e) {
      // shouldn't happen
      LOG.error("Error during sendFootswitchINCMessage", e);
    }
  }
  
  /**
   * Sends the command to program a Footswitch Controller to send a Program
   * Change DEC command when pressed.  No min/max values for the program
   * number are used.  If using one pedal as a PC INC and another as a PC
   * DEC, the MIDI OUT of the DEC unit must be connected to the MIDI IN of
   * the INC unit.
   * @param channel - the channel, 0-indexed.
   */
  public void sendFootswitchDECMessage(int channel) {
    if (!isValid())
      return;
    
    try {
      sendSysexMessage(-1, 0xF0, 0, 0, 0x50, 0x04, 0x07, 0x00, channel, 0xF7);
    } catch (InvalidMidiDataException e) {
      // shouldn't happen
      LOG.error("Error during sendFootswitchDECMessage", e);
    }
  }
  
  /**
   * Sends the command to program a Footswitch Controller to send a Program
   * Change INC command when pressed, with the given min and max values.
   * If using one pedal as a PC INC and another as a PC DEC, the MIDI OUT of
   * the DEC unit must be connected to the MIDI IN of the INC unit.
   * @param channel - the channel, 0-indexed.
   * @param min - the minimum program number
   * @param max - the maximum program number
   */
  public void sendFootswitchINCMessage(int channel, int min, int max) {
    if (!isValid())
      return;
    
    try {
      sendSysexMessage(-1, 0xF0, 0, 0, 0x50, 0x04, 0x07, 0x01, channel, min, max, 0xF7);
    } catch (InvalidMidiDataException e) {
      // shouldn't happen
      LOG.error("Error during sendFootswitchINCMessage", e);
    }
  }
  
  /**
   * Sends the command to program a Footswitch Controller to send a Program
   * Change DEC command when pressed, with the given min and max values.
   * If using one pedal as a PC INC and another as a PC DEC, the MIDI OUT of
   * the DEC unit must be connected to the MIDI IN of the INC unit.
   * @param channel - the channel, 0-indexed.
   * @param min - the minimum program number
   * @param max - the maximum program number
   */
  public void sendFootswitchDECMessage(int channel, int min, int max) {
    if (!isValid())
      return;
    
    try {
      sendSysexMessage(-1, 0xF0, 0, 0, 0x50, 0x04, 0x07, 0x00, channel, min, max, 0xF7);
    } catch (InvalidMidiDataException e) {
      // shouldn't happen
      LOG.error("Error during sendFootswitchDECMessage", e);
    }
  }
  
  /**
   * Sends the command to program a Footswitch Controller to send a Sysex
   * message when pressed.
   * @param onPress - <tt>true</tt> to send the message when the footswitch
   * is pressed, <tt>false</tt> to send when the footswitch is released.  A
   * Footswitch Controller can be programmed to do both by sending both
   * programming commands
   * @param message - the Sysex message to send
   */
  public void sendFootswitchSysexMessage(boolean onPress, int... message) {
    if (!isValid())
      return;
    
    try {
      sendSysexMessage(-1, 0xF0, 0, 0, 0x50, 0x04, 0x06, (onPress ? 0x01 : 0x00), 0xF7);
      sendSysexMessage(-1, message);
    } catch (InvalidMidiDataException e) {
      // shouldn't happen
      LOG.error("Error during sendFootswitchSysexMessage", e);
    }
  }
  
  /**
   * Sends the command to program a Footswitch Controller to act as a panic
   * switch, sending an ALL NOTES OFF message to all channels when depressed.
   */
  public void sendFootswitchPanicMessage() {
    if (!isValid())
      return;
    
    try {
      sendSysexMessage(-1, 0xF0, 0, 0, 0x50, 0x04, 0x0C, 0x7F, 0x01, 0xF7);
    } catch (InvalidMidiDataException e) {
      // shouldn't happen
      LOG.error("Error during sendFootswitchPanicMessage", e);
    }
  }
  
  /**
   * Sends the command to clear all settings on a Relay
   */
  public void sendRelayClearSettingsMessage() {
    if (!isValid())
      return;
    
    try {
      sendSysexMessage(-1, 0xF0, 0x00, 0x00, 0x50, 0x06, 0x00, 0xF7);
    } catch (InvalidMidiDataException e) {
      // shouldn't happen
      LOG.error("Error during sendRelayClearSettingsMessage", e);
    }
  }
  
  /**
   * Sends the command to dump settings on a Relay
   */
  public void sendRelayDumpSettingsMessage() {
    if (!isValid())
      return;
    
    try {
      sendSysexMessage(-1, 0xF0, 0x00, 0x00, 0x50, 0x06, 0x10, 0xF7);
    } catch (InvalidMidiDataException e) {
      // shouldn't happen
      LOG.error("Error during sendRelayDumpSettingsMessage", e);
    }
  }
  
  /**
   * Sends the command to program a Relay to have the given link settings
   * @param linkSettings the settings:
   * <ul>
   *   <li>0: Link OFF - each Note and Control Change setting operates independently</li>
   *   <li>1: CLOSED Link - relay closes only if conditions of all Note and Control
   *   Change settings are in the closed state</li>
   *   <li>2: OPEN Link - relay opens only if conditions of all Note and Control
   *   Change settings are in the open state</li>
   * </ul>
   */
  public void sendRelayLinkSettingsMessage(int linkSettings) {
    if (!isValid())
      return;
    
    try {
      sendSysexMessage(-1, 0xF0, 0x00, 0x00, 0x50, 0x06, 0x05, linkSettings, 0xF7);
    } catch (InvalidMidiDataException e) {
      // shouldn't happen
      LOG.error("Error during sendRelayDumpSettingsMessage", e);
    }
  }
  
  /**
   * Sends the command to program a Pedal Controller to have the given device settings
   * @param echo whether or not to echo MIDI in
   * @param curvatureAmount specifies how much the pedal movement differs from a linear response
   * @param curveUp whether or not the curvature is upwards
   * @param neutralRangeBottom the amount of range at the bottom for which no change occurs
   * @param neutralRangeTop the amount of range at the top for which no change occurs
   */
  public void sendPedalDeviceParametersMessage(boolean echo, int curvatureAmount,
      boolean curveUp, int neutralRangeBottom, int neutralRangeTop) {
    if (!isValid())
      return;
    
    try {
      sendSysexMessage(-1, 0xF0, 0x00, 0x00, 0x50, 0x16, 0x00,
          (echo ? 1 : 0), curvatureAmount, (curveUp ? 1 : 0),
          neutralRangeBottom, neutralRangeTop, 0xF7);
    } catch (InvalidMidiDataException e) {
      // shouldn't happen
      LOG.error("Error during sendRelayDumpSettingsMessage", e);
    }
  }
  
  /**
   * Sends the command to set a Pedal Controller to send the given mesage
   * @param messageType the type of message that the pedal should send:
   * <ul>
   *   <li>0 &mdash; Controller</li>
   *   <li>1 &mdash; Aftertouch</li>
   *   <li>2 &mdash; Pitch Bend</li>
   *   <li>3 &mdash; System Exclusive</li>
   * </ul>
   * @param ccNumOrSysexByte the control change (if messageType=0) or sysex byte # (if messageType=3)
   * @param channel the channel, ignored if messageType=3
   * @param min the minimum value transmitted
   * @param max the maximum value transmitted
   * @param extraChannels up to 3 additional MID channels to use
   */
  public void sendPedalMessageTypeMessage(int messageType, int ccNumOrSysexByte,
      int channel, int min, int max, int... extraChannels) {
    if (!isValid())
      return;
    
    final int[] message = new int[12+extraChannels.length];
    final int[] baselineMessage = new int[] {0xF0, 0x00, 0x00, 0x50, 0x16, 0x01,
        messageType, ccNumOrSysexByte, channel, min, max};
    System.arraycopy(baselineMessage, 0, message, 0, baselineMessage.length);
    System.arraycopy(extraChannels, 0, message, baselineMessage.length, extraChannels.length);
    message[message.length-1] = 0xF7;
    
    try {
      sendSysexMessage(-1, message);
    } catch (InvalidMidiDataException e) {
      // shouldn't happen
      LOG.error("Error during sendRelayDumpSettingsMessage", e);
    }
  }
  
  /**
   * Sends the command to have a Pedal Controller dump its settings
   */
  public void sendPedalDumpSettingsMessage() {
    if (!isValid())
      return;
    
    try {
      sendSysexMessage(-1, 0xF0, 0x00, 0x00, 0x50, 0x16, 0x10, 0xF7);
    } catch (InvalidMidiDataException e) {
      // shouldn't happen
      LOG.error("Error during sendRelayDumpSettingsMessage", e);
    }
  }
}
