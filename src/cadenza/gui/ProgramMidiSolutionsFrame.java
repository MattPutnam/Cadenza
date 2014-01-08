package cadenza.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import cadenza.control.MidiSolutionsMessageSender;

import common.midi.MidiUtilities;
import common.swing.CardPanel;
import common.swing.NonNegativeIntField;
import common.swing.SwingUtils;
import common.swing.dialog.Dialog;

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
	
	////////////////////////////////////// MAIN //////////////////////////////////////
	
	private List<Component> buildMainContents() {
		final List<Component> result = new ArrayList<>(2);
		result.add(new CardPanel(buildFootswitchContents(), buildFootswitchNames()));
		result.add(new CardPanel(buildPedalContents(), buildPedalNames()));
		return result;
	}

	private List<String> buildMainNames() {
		final List<String> result = new ArrayList<>(2);
		result.add("Footswitch Controller");
		result.add("Relay");
		return result;
	}
	
	////////////////////////////////////// FOOTSWITCH //////////////////////////////////////
	
	private List<Component> buildFootswitchContents() {
		final List<Component> result = new ArrayList<>();
		
		result.add(buildPanel(new String[] {"Note", "Channel"}, new ButtonAction() {
			@Override
			public void onClick(JTextField[] inputFields,
					MidiSolutionsMessageSender msms) {
				msms.sendFootswitchNoteOnMessage(
						extractNoteNumber(inputFields[0]),
						extractChannel(inputFields[1]));
			}
		}));
		
		result.add(buildPanel(new String[] {"CC#", "Value", "Channel"},
				new ButtonAction() {
			@Override
			public void onClick(JTextField[] inputFields,
					MidiSolutionsMessageSender msms) {
				msms.sendFootswitchControlChangeMessage(
						extractInt(inputFields[0]),
						extractInt(inputFields[1]),
						extractChannel(inputFields[2]));
			}
		}));
		
		result.add(buildPanel(new String[] {}, new ButtonAction() {
			@Override
			public void onClick(JTextField[] inputFields,
					MidiSolutionsMessageSender msms) {
				msms.sendFootswitchMidiStartMessage();
			}
		}));
		
		result.add(buildPanel(new String[] {}, new ButtonAction() {
			@Override
			public void onClick(JTextField[] inputFields,
					MidiSolutionsMessageSender msms) {
				msms.sendFootswitchMidiStopMessage();
			}
		}));
		
		result.add(buildPanel(new String[] {}, new ButtonAction() {
			@Override
			public void onClick(JTextField[] inputFields,
					MidiSolutionsMessageSender msms) {
				msms.sendFootswitchMidiStartStopMessage();
			}
		}));
		
		result.add(buildPanel(new String[] {"LSB", "MSB", "Channel"},
				new ButtonAction() {
			@Override
			public void onClick(JTextField[] inputFields,
					MidiSolutionsMessageSender msms) {
				msms.sendFootswitchPitchBendMessage(
						extractInt(inputFields[0]),
						extractInt(inputFields[1]),
						extractChannel(inputFields[2]));
			}
		}));
		
		result.add(buildPanel(new String[] {"Program #", "Channel"},
				new ButtonAction() {
			@Override
			public void onClick(JTextField[] inputFields,
					MidiSolutionsMessageSender msms) {
				msms.sendFootswitchProgramChangeMessage(
						extractInt(inputFields[0]),
						extractChannel(inputFields[1]));
			}
		}));
		
		result.add(buildPanel(new String[] {"Bank MSB", "Bank LSB", "Program #", "Channel"},
				new ButtonAction() {
			@Override
			public void onClick(JTextField[] inputFields,
					MidiSolutionsMessageSender msms) {
				msms.sendFootswitchProgramChangeMessage(
						extractInt(inputFields[0]),
						extractInt(inputFields[1]),
						extractInt(inputFields[2]),
						extractChannel(inputFields[3]));
			}
		}));
		
		result.add(buildPanel(new String[] {}, new ButtonAction() {
			@Override
			public void onClick(JTextField[] inputFields,
					MidiSolutionsMessageSender msms) {
				msms.sendFootswitchProgramChangeCaptureMessage();
			}
		}));
		
		result.add(buildPanel(new String[] {"Channel"}, new ButtonAction() {
			@Override
			public void onClick(JTextField[] inputFields,
					MidiSolutionsMessageSender msms) {
				msms.sendFootswitchINCMessage(extractChannel(inputFields[0]));
			}
		}));
		
		result.add(buildPanel(new String[] {"Channel"}, new ButtonAction() {
			@Override
			public void onClick(JTextField[] inputFields,
					MidiSolutionsMessageSender msms) {
				msms.sendFootswitchDECMessage(extractChannel(inputFields[0]));
			}
		}));
		
		result.add(buildPanel(new String[] {}, new ButtonAction() {
			@Override
			public void onClick(JTextField[] inputFields,
					MidiSolutionsMessageSender msms) {
				msms.sendFootswitchPanicMessage();
			}
		}));
		
		return result;
	}
	
	private List<String> buildFootswitchNames() {
		return Arrays.asList("Note On", "Control Change", "Midi Start", "MIDI Stop",
				"MIDI Start on press, MIDI Stop on release", "Pitch Bend", "Program Change",
				"Bank/Program Change", "Program Change Capture", "Program Change INC",
				"Program Change DEC", "Panic");
	}
	
	////////////////////////////////////// RELAY //////////////////////////////////////
	
	private List<Component> buildPedalContents() {
		final List<Component> result = new ArrayList<>();
		
		final Box deviceParametersPanel = Box.createHorizontalBox();
		{
			final JCheckBox echoCheckBox = new JCheckBox("Echo MIDI in", true);
			final NonNegativeIntField curveAmountField = buildIntField();
			final JCheckBox curveUpCheckBox = new JCheckBox("Curve upwards", true);
			final NonNegativeIntField nrBottomField = buildIntField();
			final NonNegativeIntField nrTopField = buildIntField();
			
			final JButton sendButton = new JButton("Send");
			sendButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent _) {
					try {
						_sender.sendPedalDeviceParametersMessage(
								extractBoolean(echoCheckBox),
								curveAmountField.getInt(),
								extractBoolean(curveUpCheckBox),
								nrBottomField.getInt(),
								nrTopField.getInt());
					} catch (Throwable t) {
						Dialog.error(deviceParametersPanel, t.getLocalizedMessage());
					}
				}
			});
			
			deviceParametersPanel.add(Box.createHorizontalStrut(8));
			deviceParametersPanel.add(echoCheckBox);
			deviceParametersPanel.add(Box.createHorizontalStrut(8));
			deviceParametersPanel.add(new JLabel("Curvature amount: "));
			deviceParametersPanel.add(curveAmountField);
			deviceParametersPanel.add(Box.createHorizontalStrut(8));
			deviceParametersPanel.add(curveUpCheckBox);
			deviceParametersPanel.add(Box.createHorizontalStrut(8));
			deviceParametersPanel.add(new JLabel("Neutral range bottom: "));
			deviceParametersPanel.add(nrBottomField);
			deviceParametersPanel.add(Box.createHorizontalStrut(8));
			deviceParametersPanel.add(new JLabel("Neutral range top: "));
			deviceParametersPanel.add(nrTopField);
			deviceParametersPanel.add(Box.createHorizontalStrut(8));
			deviceParametersPanel.add(sendButton);
			deviceParametersPanel.add(Box.createHorizontalGlue());
		}
		
		result.add(deviceParametersPanel);
		
		return result;
	}
	
	private List<String> buildPedalNames() {
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
		final JButton sendButton = new JButton("Send");
		sendButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					action.onClick(inputFields, _sender);
				} catch (Throwable t) {
					Dialog.error(box, t.getLocalizedMessage());
				}
			}
		});
		box.add(sendButton);
		box.add(Box.createHorizontalGlue());
		
		return box;
	}
	
	private static NonNegativeIntField buildIntField() {
		final NonNegativeIntField result = new NonNegativeIntField();
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
			return Integer.parseInt(text) - 1;
	}
	
	private static int extractInt(JTextField textField) {
		return Integer.parseInt(textField.getText().trim());
	}
	
	private static boolean extractBoolean(JCheckBox checkBox) {
		return checkBox.isSelected();
	}
	
	private static int extractInt(JRadioButton[] buttons) {
		for (int i = 0; i < buttons.length; ++i)
			if (buttons[i].isSelected())
				return i;
		return -1;
	}
	
	private static interface ButtonAction {
		public void onClick(JTextField[] inputFields, MidiSolutionsMessageSender msms);
	}
}
