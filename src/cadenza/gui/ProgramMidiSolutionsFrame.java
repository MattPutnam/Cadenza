package cadenza.gui;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import cadenza.control.MidiSolutionsMessageSender;

import common.midi.MidiUtilities;
import common.swing.CardPanel;
import common.swing.IntField;
import common.swing.SimpleGrid;
import common.swing.SwingUtils;
import common.swing.dialog.Dialog;

@SuppressWarnings("serial")
public class ProgramMidiSolutionsFrame extends JFrame {
  private final MidiSolutionsMessageSender _sender;
  
  public ProgramMidiSolutionsFrame(MidiSolutionsMessageSender sender) {
    _sender = sender;
    
    getContentPane().add(new CardPanel(buildMainContents(), buildMainNames()));
    pack();
    
    setTitle("Program MIDI Solutions device");
    setLocationRelativeTo(null);
    setVisible(true);
  }
  
  //////////////////////////////// MAIN //////////////////////////////////////
  
  private List<Component> buildMainContents() {
    final List<Component> result = new ArrayList<>(2);
    result.add(new CardPanel(buildFootswitchContents(), buildFootswitchNames()));
    result.add(new CardPanel(buildPedalContents(), buildPedalNames()));
    return result;
  }

  private static List<String> buildMainNames() {
    final List<String> result = new ArrayList<>(2);
    result.add("Footswitch Controller");
    result.add("Relay");
    return result;
  }
  
  //////////////////////////////// FOOTSWITCH ////////////////////////////////
  
  private List<Component> buildFootswitchContents() {
    final List<Component> result = new ArrayList<>();
    
    result.add(buildPanel(new String[] {"Note", "Channel"},
        (inputFields, msms) -> msms.sendFootswitchNoteOnMessage(
            extractNoteNumber(inputFields[0]),
            extractChannel(inputFields[1]))
    ));
    
    result.add(buildPanel(new String[] {"CC#", "Value", "Channel"},
        (inputFields, msms) -> msms.sendFootswitchControlChangeMessage(
            extractInt(inputFields[0]),
            extractInt(inputFields[1]),
            extractChannel(inputFields[2]))
    ));
    
    result.add(buildPanel(new String[] {}, (fields, msms) ->
        msms.sendFootswitchMidiStartMessage()));
    
    result.add(buildPanel(new String[] {}, (fields, msms) ->
        msms.sendFootswitchMidiStopMessage()));
    
    result.add(buildPanel(new String[] {}, (fields, msms) ->
        msms.sendFootswitchMidiStartStopMessage()));
    
    result.add(buildPanel(new String[] {"LSB", "MSB", "Channel"},
        (inputFields, msms) -> msms.sendFootswitchPitchBendMessage(
            extractInt(inputFields[0]),
            extractInt(inputFields[1]),
            extractChannel(inputFields[2]))
    ));
    
    result.add(buildPanel(new String[] {"Program #", "Channel"},
        (inputFields, msms) -> msms.sendFootswitchProgramChangeMessage(
            extractInt(inputFields[0]),
            extractChannel(inputFields[1]))
    ));
    
    result.add(buildPanel(new String[] {"Bank MSB", "Bank LSB", "Program #", "Channel"},
        (inputFields, msms) -> msms.sendFootswitchProgramChangeMessage(
            extractInt(inputFields[0]),
            extractInt(inputFields[1]),
            extractInt(inputFields[2]),
            extractChannel(inputFields[3]))
    ));
    
    result.add(buildPanel(new String[] {}, (inputFields, msms) ->
        msms.sendFootswitchProgramChangeCaptureMessage()));
    
    result.add(buildPanel(new String[] {"Channel"}, (inputFields, msms) ->
        msms.sendFootswitchINCMessage(extractChannel(inputFields[0]))));
    
    result.add(buildPanel(new String[] {"Channel"}, (inputFields, msms) ->
        msms.sendFootswitchDECMessage(extractChannel(inputFields[0]))));
    
    result.add(buildPanel(new String[] {}, (inputFields, msms) ->
        msms.sendFootswitchPanicMessage()));
    
    return result;
  }
  
  private static List<String> buildFootswitchNames() {
    return Arrays.asList("Note On", "Control Change", "Midi Start", "MIDI Stop",
        "MIDI Start on press, MIDI Stop on release", "Pitch Bend", "Program Change",
        "Bank/Program Change", "Program Change Capture", "Program Change INC",
        "Program Change DEC", "Panic");
  }
  
  ////////////////////////////////////// RELAY //////////////////////////////////////
  
  private List<Component> buildPedalContents() {
    final List<Component> result = new ArrayList<>();
    
    {
      final JCheckBox echoCheckBox = new JCheckBox("Echo MIDI in", true);
      final IntField curveAmountField = buildIntField();
      final JCheckBox curveUpCheckBox = new JCheckBox("Curve upwards", true);
      final IntField nrBottomField = buildIntField();
      final IntField nrTopField = buildIntField();
      
      final JButton sendButton = SwingUtils.button("Send", e -> {
        try {
          _sender.sendPedalDeviceParametersMessage(
              extractBoolean(echoCheckBox),
              curveAmountField.getInt(),
              extractBoolean(curveUpCheckBox),
              nrBottomField.getInt(),
              nrTopField.getInt());
        } catch (Throwable t) {
          Dialog.error(this, t.getLocalizedMessage());
        }
      });
      result.add(new SimpleGrid(new JComponent[][] {
          { echoCheckBox,                        null             },
          { new JLabel("Curvature amount:"),     curveAmountField },
          { curveUpCheckBox,                     null             },
          { new JLabel("Neutral range bottom:"), nrBottomField    },
          { new JLabel("Neutral range top:"),    nrTopField       },
          { sendButton,                          null             }
      }));
    }
    
    return result;
  }
  
  private static List<String> buildPedalNames() {
    return Arrays.asList("Device Settings");
  }
  
  ////////////////////////////////////// UTILS //////////////////////////////////////
  
  private Component buildPanel(String[] fieldLabels, final ButtonAction action) {
    final JTextField[] inputFields = new JTextField[fieldLabels.length];
    
    final Box box = Box.createHorizontalBox();
    for (int i = 0; i < fieldLabels.length; ++i) {
      final JTextField field = new JTextField(4);
      SwingUtils.freezeSize(field);
      inputFields[i] = field;
      
      box.add(Box.createHorizontalStrut(8));
      box.add(new JLabel(fieldLabels[i] + ": "));
      box.add(field);
    }
    final JButton sendButton = SwingUtils.button("Send", e -> {
      try {
        action.onClick(inputFields, _sender);
      } catch (Throwable t) {
        Dialog.error(box, t.getLocalizedMessage());
      }
    });
    box.add(sendButton);
    box.add(Box.createHorizontalGlue());
    
    return box;
  }
  
  private static IntField buildIntField() {
    final IntField result = new IntField(0, 0, 127);
    result.setColumns(4);
    SwingUtils.freezeSize(result);
    return result;
  }
  
  private static int extractNoteNumber(JTextField textField) {
    return MidiUtilities.noteNameToNumber(textField.getText().trim());
  }
  
  private static int extractChannel(JTextField textField) {
    final String text = textField.getText().trim();
    if (text.equalsIgnoreCase("all"))
      return 0x7F;
    else
      return Integer.parseInt(text);
  }
  
  private static int extractInt(JTextField textField) {
    return Integer.parseInt(textField.getText().trim());
  }
  
  private static boolean extractBoolean(JCheckBox checkBox) {
    return checkBox.isSelected();
  }
  
  @SuppressWarnings("unused")
  private static int extractInt(JRadioButton[] buttons) {
    for (int i = 0; i < buttons.length; ++i)
      if (buttons[i].isSelected())
        return i;
    return -1;
  }
  
  @FunctionalInterface
  private static interface ButtonAction {
    public void onClick(JTextField[] inputFields, MidiSolutionsMessageSender msms);
  }
}
