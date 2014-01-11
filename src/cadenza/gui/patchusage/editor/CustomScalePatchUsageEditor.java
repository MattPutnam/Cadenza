package cadenza.gui.patchusage.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import cadenza.core.Note;
import cadenza.core.Note.PitchClass;
import cadenza.core.Scale;
import cadenza.core.patchusage.CustomScalePatchUsage;
import cadenza.gui.keyboard.KeyboardPanel;
import cadenza.gui.keyboard.MultipleKeyboardAdapter;
import cadenza.gui.keyboard.MultipleKeyboardPanel;

import common.swing.GraphicsUtils;
import common.swing.SwingUtils;

public class CustomScalePatchUsageEditor extends JPanel {
	private static final Note B4 = new Note(PitchClass.B, 4);
	private static final Note C3 = new Note(PitchClass.C, 3);
	private static final Note C6 = new Note(PitchClass.C, 6);
	
	private static final int GAP = 40;
	
	private final Map<PitchClass, Integer> _map;
	private Scale _selectedScale;
	
	private final KeyboardPanel _srcPanel;
	private final KeyboardPanel _destPanel;
	private final KeyboardArea _keyboardArea;
	
	private JComboBox<Scale> _majors;
	private JComboBox<Scale> _minors;
	private JComboBox<Scale> _harmonics;
	private JComboBox<Scale> _wholes;
	
	public CustomScalePatchUsageEditor() {
		_map = new IdentityHashMap<>();
		
		_srcPanel = new KeyboardPanel(Note.C4, B4);
		_destPanel = new KeyboardPanel(C3, C6);
		_keyboardArea = new KeyboardArea();
		
		setLayout(new BorderLayout());
		add(_keyboardArea, BorderLayout.WEST);
		add(buildSelectors(), BorderLayout.CENTER);
		
		_keyboardArea.repaint();
	}
	
	public void populate(CustomScalePatchUsage template) {
		_map.clear();
		_map.putAll(template.map);
		_selectedScale = template.scale;
		
		if (_selectedScale != null) {
			_majors.setSelectedItem(_selectedScale);
			_minors.setSelectedItem(_selectedScale);
			_harmonics.setSelectedItem(_selectedScale);
			_wholes.setSelectedItem(_selectedScale);
		}
	}
	
	public Map<PitchClass, Integer> getMap() {
		return _map;
	}
	
	public Scale getSelectedScale() {
		return _selectedScale;
	}
	
	public boolean isScaleSelected() {
		return _selectedScale != null;
	}
	
	private JComponent buildSelectors() {
		final JPanel result = new JPanel(new BorderLayout());
		
		_majors = new JComboBox<>(Scale.Diatonic.ALL_MAJOR.toArray(new Scale[0]));
		_minors = new JComboBox<>(Scale.Diatonic.ALL_MINOR.toArray(new Scale[0]));
		_harmonics = new JComboBox<>(Scale.Diatonic.ALL_HARMONIC.toArray(new Scale[0]));
		_wholes = new JComboBox<>(Scale.WholeTone.ALL.toArray(new Scale[0]));
		
		final ScaleAction action = new ScaleAction();
		_majors.addActionListener(action);
		_minors.addActionListener(action);
		_harmonics.addActionListener(action);
		_wholes.addActionListener(action);
		
		final Box scales = SwingUtils.buildCenteredRow(_majors, _minors, _harmonics, _wholes);
		
		final JLabel instructionLabel = new JLabel("Drag from the top keyboard " +
				"to the bottom keyboard or select a predefined scale");
		
		result.add(instructionLabel, BorderLayout.NORTH);
		result.add(SwingUtils.hugNorth(scales), BorderLayout.CENTER);
		
		return result;
	}
	
	private class ScaleAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			final JComboBox<?> combo = (JComboBox<?>) e.getSource();
			_selectedScale = (Scale) combo.getSelectedItem();
			_map.clear();
			_map.putAll(_selectedScale.buildMapFromNaturals());
			revalidate();
			repaint();
		}
	}
	
	private class KeyboardArea extends MultipleKeyboardPanel {
		public KeyboardArea() {
			super(GAP, _srcPanel, _destPanel);
			
			addMultipleKeyboardListener(new MultipleKeyboardAdapter() {
				@Override
				public void keyClicked(Note note, KeyboardPanel source) {
					if (source == _srcPanel) {
						_map.remove(note.getPitchClass());
						_selectedScale = null;
						repaint();
					}
				}
				
				@Override
				public void keyDragged(Note startNote, KeyboardPanel startSource,
									   Note endNote, KeyboardPanel endSource) {
					if (startSource == _srcPanel && endSource == _destPanel && startNote.getPitchClass().isWhite()) {
						_map.put(startNote.getPitchClass(), endNote.getMidiNumber() - startNote.getMidiNumber());
						_selectedScale = null;
						repaint();
					}
				}
			});
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			g.setColor(Color.BLACK);
			
			for (Entry<PitchClass, Integer> entry : _map.entrySet()) {
				final Note start = new Note(entry.getKey(), 4);
				final Rectangle startRect = _srcPanel.getKeyPosition(start);
				final int x1 = startRect.x + (startRect.width/2) + _srcPanel.getLocation().x;
				final int y1 = _srcPanel.getHeight();
				
				final Note end = new Note(start.getMidiNumber() + entry.getValue());
				final Rectangle endRect = _destPanel.getKeyPosition(end);
				final int x2 = endRect.x + (endRect.width/2);
				final int y2 = _srcPanel.getHeight() + GAP;
				
				final Graphics2D g2d = (Graphics2D) g.create();
				GraphicsUtils.antialias(g2d);
				GraphicsUtils.drawArrow(g2d, x1, y1, x2, y2);
			}
		}
	}
}
