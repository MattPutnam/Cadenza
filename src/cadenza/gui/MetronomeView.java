package cadenza.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import cadenza.core.metronome.Metronome;
import cadenza.core.metronome.MetronomeListener;

import common.swing.IntField;
import common.swing.SwingUtils;

@SuppressWarnings("serial")
public class MetronomeView extends JFrame implements MetronomeListener {
	private static final Color METRONOME_OFF = Color.BLACK;
	private static final Color CLICK_ON = Color.GREEN;
	private static final Color CLICK_OFF = Color.BLACK;
	private static final long CLICK_LENGTH = 100;
	
	private static final MetronomeView INSTANCE = new MetronomeView();
	public static MetronomeView getInstance() {
		return INSTANCE;
	}
	
	private final JPanel _clickDisplay;
	private final IntField _bpmField;
	
	private MetronomeView() {
		_clickDisplay = new JPanel();
		_clickDisplay.setMinimumSize(new Dimension(24, 24));
		_clickDisplay.setBackground(METRONOME_OFF);
		
		_bpmField = new IntField(Metronome.getInstance().getBPM(), 1, 500);
		_bpmField.setColumns(4);
		_bpmField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Metronome.getInstance().setBPM(_bpmField.getInt());
			}
		});
		SwingUtils.freezeWidth(_bpmField);
		
		final JButton startButton = new JButton(new StartAction());
		final JButton stopButton = new JButton(new StopAction());
		
		final Box south = Box.createHorizontalBox();
		south.add(Box.createHorizontalGlue());
		south.add(startButton);
		south.add(_bpmField);
		south.add(stopButton);
		south.add(Box.createHorizontalGlue());
		
		setLayout(new BorderLayout());
		add(_clickDisplay, BorderLayout.CENTER);
		add(south, BorderLayout.SOUTH);
		
		pack();
		
		setSize(300, 200);
		setTitle("Metronome");
		setAlwaysOnTop(true);
		SwingUtils.goInvisibleOnClose(this);
		Metronome.getInstance().addMetronomeListener(this);
	}
	
	private class StartAction extends AbstractAction {
		public StartAction() {
			super("Start");
		}
		
		@Override
		public void actionPerformed(ActionEvent _) {
			Metronome.getInstance().start();
		}
	}
	
	private class StopAction extends AbstractAction {
		public StopAction() {
			super("Stop");
		}
		
		@Override
		public void actionPerformed(ActionEvent _) {
			Metronome.getInstance().stop();
		}
	}

	@Override
	public void bpmSet(final int bpm) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				_bpmField.setInt(bpm);
			}
		});
	}
	
	@Override
	public void metronomeStarted() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				_clickDisplay.setBackground(CLICK_OFF);
			}
		});
	}

	@Override
	public void metronomeClicked(int subdivision) {
		if (subdivision == 0) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					_clickDisplay.setBackground(CLICK_ON);
					SwingUtils.doDelayedInSwing(new Runnable() {
						@Override
						public void run() {
							_clickDisplay.setBackground(CLICK_OFF);
						}
					}, CLICK_LENGTH);
				}
			});
		}
	}
	
	@Override
	public void metronomeStopped() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				_clickDisplay.setBackground(METRONOME_OFF);
			}
		});
	}

}
