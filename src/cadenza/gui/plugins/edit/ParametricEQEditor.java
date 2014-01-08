package cadenza.gui.plugins.edit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cadenza.core.plugins.ParametricEQ;
import cadenza.core.plugins.ParametricEQ.Band;
import cadenza.gui.common.MidiValueField;
import cadenza.gui.plugins.view.ParametricEQView;

import common.swing.DoubleField;
import common.swing.NonNegativeDoubleField;
import common.swing.SimpleGrid;
import common.swing.SwingUtils;
import common.swing.icon.DeleteIcon;

public class ParametricEQEditor extends PluginEditor {
	private final List<Band> _bands;
	private final ParametricEQ _peq;
	private final ParametricEQView _peqView;
	
	private final JPanel _bandPanel;
	
	public ParametricEQEditor(ParametricEQ initial) {
		_bands = initial.getBands();
		_peq = new ParametricEQ(_bands);
		_peqView = new ParametricEQView(_peq);
		
		_bandPanel = new JPanel(new FlowLayout());
		refreshBands();
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(_peqView);
		add(new JScrollPane(_bandPanel, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
		add(SwingUtils.buildRow(new JButton(new AddAction()), new JButton(new SortAction())));
	}
	
	@Override
	public ParametricEQ getPlugin() {
		return _peq;
	}
	
	private void refreshBands() {
		_bandPanel.removeAll();
		for (final Band band : _bands)
			_bandPanel.add(new BandEditor(band));
		_bandPanel.repaint();
		_peq.update();
		revalidate();
		repaint();
	}
	
	private class BandEditor extends JPanel {
		private final Band _band;
		
		private final JSlider _frequencySlider;
		private final MidiValueField _frequencyField;
		
		private final JSlider _gainSlider;
		private final DoubleField _gainField;
		
		private final JSlider _qualitySlider;
		private final NonNegativeDoubleField _qualityField;
		
		private final JCheckBox _highShelfCheckBox;
		private final JCheckBox _lowShelfCheckBox;
		
		public BandEditor(Band band) {
			super();
			_band = band;
			
			_frequencySlider = new JSlider(JSlider.VERTICAL, 0, 127, band.getFrequency());
			_frequencyField = new MidiValueField(band.getFrequency());
			_frequencyField.setColumns(3);
			
			_gainSlider = new JSlider(JSlider.VERTICAL, -10000, 10000, (int) (1000*band.getGain()));
			_gainField = new DoubleField(band.getGain());
			_gainField.setColumns(3);
			
			_qualitySlider = new JSlider(JSlider.VERTICAL, 200, 10000, (int) (1000*band.getQuality()));
			_qualityField = new NonNegativeDoubleField(band.getQuality());
			_qualityField.setColumns(3);
			
			_highShelfCheckBox = new JCheckBox("High Shelf");
			_lowShelfCheckBox = new JCheckBox("Low Shelf");
			
			addListeners();
			
			setBorder(BorderFactory.createLineBorder(Color.BLACK));
			
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			add(SwingUtils.buildRow(Box.createHorizontalGlue(), new DeleteButton(_band)));
			add(new SimpleGrid(new JComponent[][]
			{
				{ new JLabel("f"),  new JLabel("G"), new JLabel("Q") },
				{ _frequencySlider,   _gainSlider,   _qualitySlider },
				{ _frequencyField,    _gainField,    _qualityField}
			}, Alignment.LEADING, Alignment.CENTER));
			add(SwingUtils.hugWest(_highShelfCheckBox));
			add(SwingUtils.hugWest(_lowShelfCheckBox));
		}
		
		private void addListeners() {
			_frequencySlider.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent _) {
					final int freq = _frequencySlider.getValue();
					_frequencyField.setText(String.valueOf(freq));
					_band.setFrequency(freq);
					_peq.update();
					_peqView.repaint();
				}
			});
			_frequencyField.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent _) {
					int freq = _frequencyField.getInt();
					if (freq > 127) {
						freq = 127;
						_frequencyField.setText("127");
					}
					_frequencySlider.setValue(freq);
					_band.setFrequency(freq);
					_peq.update();
					_peqView.repaint();
				}
			});
			_frequencyField.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent _) {
					int freq = _frequencyField.getInt();
					if (freq > 127) {
						freq = 127;
						_frequencyField.setText("127");
					}
					_frequencySlider.setValue(freq);
					_band.setFrequency(freq);
					_peq.update();
					_peqView.repaint();
				}
			});
			
			_gainSlider.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent _) {
					final double gain = _gainSlider.getValue() / 1000.0;
					_gainField.setText(String.valueOf(gain));
					_band.setGain(gain);
					_peq.update();
					_peqView.repaint();
				}
			});
			_gainField.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent _) {
					double gain = _gainField.getDouble();
					if (gain < -10.0) {
						gain = -10.0;
						_gainField.setText("-10.0");
					} else if (gain > 10.0) {
						gain = 10.0;
						_gainField.setText("10.0");
					}
					_gainSlider.setValue((int) (gain * 1000));
					_band.setGain(gain);
					_peq.update();
					_peqView.repaint();
				}
			});
			_gainField.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent _) {
					double gain = _gainField.getDouble();
					if (gain < -10.0) {
						gain = -10.0;
						_gainField.setText("-10.0");
					} else if (gain > 10.0) {
						gain = 10.0;
						_gainField.setText("10.0");
					}
					_gainSlider.setValue((int) (gain * 1000));
					_band.setGain(gain);
					_peq.update();
					_peqView.repaint();
				}
			});
			
			_qualitySlider.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent _) {
					double quality = _qualitySlider.getValue() / 1000.0;
					_qualityField.setText(String.valueOf(quality));
					_band.setQuality(quality);
					_peq.update();
					_peqView.repaint();
				}
			});
			_qualityField.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent _) {
					double quality = _qualityField.getDouble();
					if (quality > 10.0) {
						quality = 10.0;
						_qualityField.setText("10.0");
					}
					_qualitySlider.setValue((int) (quality*1000));
					_band.setQuality(quality);
					_peq.update();
					_peqView.repaint();
				}
			});
			_qualityField.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent _) {
					double quality = _qualityField.getDouble();
					if (quality > 10.0) {
						quality = 10.0;
						_qualityField.setText("10.0");
					}
					_qualitySlider.setValue((int) (quality*1000));
					_band.setQuality(quality);
					_peq.update();
					_peqView.repaint();
				}
			});
			
			_highShelfCheckBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent _) {
					final boolean isSelected = _highShelfCheckBox.isSelected();
					if (isSelected)
						_lowShelfCheckBox.setSelected(false);
					_band.setHighShelf(isSelected);
					_peq.update();
					_peqView.repaint();
				}
			});
			_lowShelfCheckBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent _) {
					final boolean isSelected = _lowShelfCheckBox.isSelected();
					if (isSelected)
						_highShelfCheckBox.setSelected(false);
					_band.setLowShelf(isSelected);
					_peq.update();
					_peqView.repaint();
				}
			});
		}
	}
	
	private class DeleteButton extends JButton {
		public DeleteButton(final Band band) {
			super(new DeleteIcon(10));
			setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
			addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent _) {
					_bands.remove(band);
					refreshBands();
				}
			});
		}
	}
	
	private class AddAction extends AbstractAction {
		public AddAction() {
			super("Add New Band");
		}
		
		@Override
		public void actionPerformed(ActionEvent _) {
			_bands.add(new Band(64, 3.0, 1.0));
			refreshBands();
		}
	}
	
	private class SortAction extends AbstractAction {
		public SortAction() {
			super("Sort Bands");
		}
		
		@Override
		public void actionPerformed(ActionEvent _) {
			Collections.sort(_bands, COMPARATOR);
			refreshBands();
		}
	}
	
	private static final BandComparator COMPARATOR = new BandComparator();
	private static class BandComparator implements Comparator<Band> {
		@Override
		public int compare(Band b1, Band b2) {
			return b1.getFrequency() - b2.getFrequency();
		}
	}
	
	public static void main(String[] args) {
		final JFrame frame = new JFrame();
		final JPanel panel = new JPanel(new BorderLayout());
		panel.add(new ParametricEQEditor(new ParametricEQ(new ArrayList<Band>())), BorderLayout.CENTER);
		frame.add(panel);
		frame.pack();
		frame.setVisible(true);
	}
}
