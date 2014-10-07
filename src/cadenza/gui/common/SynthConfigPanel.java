package cadenza.gui.common;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.Box;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import cadenza.core.Synthesizer;
import cadenza.synths.Synthesizers;

import common.Utils;
import common.swing.SimpleGrid;
import common.swing.VerificationException;
import common.tuple.Pair;

@SuppressWarnings("serial")
public class SynthConfigPanel extends JPanel {
	private static final String NONE = "[none]";
	private static final String HELP_TEXT = "<html>"
	    + "List the channels to be used by this synthesizer, as a<br>"
	    + "comma-separated list (with hyphens for a range, e.g.<br>"
	    + "\"1-3, 5\" specifies channels 1, 2, 3, and 5)<br>"
	    + "<br>"
	    + "You will need at least as many channels as you plan<br>"
	    + "to have simultaneous patches.  You may specify more<br>"
	    + "channels than needed; this will allow Cadenza to load<br>"
	    + "the next patches in unused channels, allowing you to<br>"
	    + "hold over notes into the next cue.</html>";
	
	private final List<Synthesizer> _otherSynthesizers;
	
	private DeviceCombo _mainCombo;
	private JTextField _channelField;
	private JPanel _cardPanel;
	private Map<String, JComboBox<String>> _combos;
	
	// Synthname -> Pair<displayPanel, ExpansionName -> Combo>
	private Map<String, Pair<JPanel, Map<String, JComboBox<String>>>> _cardPanels = new HashMap<>();
	private JPanel getCardPanel(String device, Map<String, String> selectedExps) {
		if (_cardPanels.containsKey(device)) {
			final Pair<JPanel, Map<String, JComboBox<String>>> entry = _cardPanels.get(device);
			_combos = entry._2();
			return entry._1();
		} else {
			final List<JComponent[]> rows = new ArrayList<>();
			
			final Map<String, String> expMap = Synthesizers.getExpansionsForSynth(device);
			_combos = new TreeMap<>();
			if (!expMap.isEmpty()) {
				rows.add(new JComponent[] {new JLabel("Expansion cards:"), null});
				
				for (Map.Entry<String, String> entry : expMap.entrySet()) {
					final List<String> exps = Synthesizers.getExpansionsOfType(entry.getValue());
					exps.add(0, NONE);
					
					final JComboBox<String> combo = new JComboBox<>(exps.toArray(new String[exps.size()]));
					if (selectedExps != null) {
						final String card = selectedExps.get(entry.getKey());
						if (card != null)
							combo.setSelectedItem(card);
					}
					
					rows.add(new JComponent[] {new JLabel(entry.getKey() + ":"), combo});
					
					_combos.put(entry.getKey(), combo);
				}
			}
			
			final JPanel panel = rows.isEmpty() ? new JPanel() :
				new SimpleGrid(rows.toArray(new JComponent[rows.size()][2]));
			
			_cardPanels.put(device, Pair.make(panel, _combos));
			return panel;
		}
	}
	
	public SynthConfigPanel(List<Synthesizer> synthesizers, Synthesizer initial) {
		super();
		_otherSynthesizers = new ArrayList<>(synthesizers);
		_otherSynthesizers.remove(initial);
		_mainCombo = new DeviceCombo(initial == null ? null : initial.getName());
		_channelField = new JTextField(initial == null ? buildInitialChannels() : Utils.makeRangeString(initial.getChannels()));
		init(initial == null ? null : initial.getExpansions());
	}
	
	private void init(final Map<String, String> selectedExps) {
		_mainCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final String device = _mainCombo.getDevice();
				_cardPanel.removeAll();
				_cardPanel.add(getCardPanel(device, selectedExps), BorderLayout.WEST);
				_cardPanel.revalidate();
				_cardPanel.repaint();
			}
		});
		
		setLayout(new BorderLayout());
		
		final Box channelBox = Box.createHorizontalBox();
		channelBox.add(_channelField);
		channelBox.add(createChannelHintLabel());
		channelBox.add(Box.createHorizontalStrut(8));
		channelBox.add(new HelpButton(HELP_TEXT));
		channelBox.add(Box.createHorizontalStrut(8));
		
		final JPanel top = new SimpleGrid(new JComponent[][]
		{
			{ new JLabel(" Synthesizer: "), _mainCombo },
			{ new JLabel(" Channels: "), channelBox }
		}, Alignment.CENTER, Alignment.LEADING);
		
		_cardPanel = new JPanel();
		_cardPanel.add(getCardPanel(_mainCombo.getDevice(), selectedExps), BorderLayout.WEST);
		
		add(top, BorderLayout.NORTH);
		add(_cardPanel, BorderLayout.CENTER);
	}
	
	private String buildInitialChannels() {
	  final List<Integer> possibleChannels = new ArrayList<>(16);
	  for (int i = 1; i <= 16; ++i)
	    possibleChannels.add(Integer.valueOf(i));
	  
	  for (final Synthesizer other : _otherSynthesizers)
	    possibleChannels.removeAll(other.getChannels());
	  
	  return Utils.makeRangeString(possibleChannels);
	}
	
	private static JLabel createChannelHintLabel() {
	  final JLabel result = new JLabel("Ex: 1-4, 9-12");
	  result.setFont(result.getFont().deriveFont(Font.ITALIC));
	  return result;
	}
	
	private List<Integer> buildList() {
		final String text = _channelField.getText().trim();
		final String[] tokens = text.split("\\s*,\\s*");
		final List<Integer> result = new ArrayList<>(tokens.length);
		for (final String token : tokens) {
			final int index = token.indexOf('-');
			if (index == -1) {
				result.add(Integer.valueOf(token));
			} else {
				final int min = Integer.parseInt(token.substring(0, index).trim());
				final int max = Integer.parseInt(token.substring(index+1).trim());
				for (int i = min; i <= max; ++i)
					result.add(Integer.valueOf(i));
			}
		}
		return result;
	}
	
	public void verify() throws VerificationException {
		if (_mainCombo.getSelectedItem() == null)
			throw new VerificationException("Please select a synthesizer", _mainCombo);
		
		try {
			final List<Integer> ints = buildList();
			for (final int i : ints) {
				if (i < 1)
					throw new VerificationException("Channels must be >= 1", _channelField);
				for (final Synthesizer synth : _otherSynthesizers)
					if (synth.getChannels().contains(Integer.valueOf(i)))
						throw new VerificationException("Another synthesizer already uses channel " + i, _channelField);
			}
		} catch (NumberFormatException e) {
			throw new VerificationException("Channels must be a list of integers separated by commas", _channelField);
		}
	}
	
	public Synthesizer getSynthesizer() {
		final Map<String, String> map = new TreeMap<>();
		for (Map.Entry<String, JComboBox<String>> entry : _combos.entrySet()) {
			final String slot = entry.getKey();
			final String card = (String) entry.getValue().getSelectedItem();
			
			if (!card.equals(NONE)) {
				map.put(slot, card);
			}
		}
		
		final String synthname = _mainCombo.getDevice();
		return new Synthesizer(synthname, Synthesizers.getBanksForSynth(synthname), map, buildList());
	}
	
}
