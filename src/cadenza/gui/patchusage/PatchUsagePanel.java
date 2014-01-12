package cadenza.gui.patchusage;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import cadenza.control.midiinput.AcceptsKeyboardInput;
import cadenza.core.CadenzaData;
import cadenza.core.Cue;
import cadenza.core.Keyboard;
import cadenza.core.Location;
import cadenza.core.Note;
import cadenza.core.Patch;
import cadenza.core.patchusage.PatchUsage;
import cadenza.core.patchusage.SimplePatchUsage;
import cadenza.gui.keyboard.KeyboardAdapter;
import cadenza.gui.keyboard.KeyboardPanel;
import cadenza.gui.keyboard.SingleKeyboardPanel;
import cadenza.gui.patch.PatchSelector;

import common.swing.SwingUtils;
import common.swing.VerificationException;
import common.swing.dialog.OKCancelDialog;
import common.swing.icon.DeleteIcon;

public class PatchUsagePanel extends JPanel implements AcceptsKeyboardInput {
	private static final Color PATCH_BORDER = Color.DARK_GRAY;
	
	private final Cue _cue;
	private final CadenzaData _data;
	
	private final List<PatchUsage> _patchUsages;
	private final List<SingleKeyboardPanel> _keyboardPanels;
	private final List<PatchUsageArea> _patchUsageAreas;
	
	public PatchUsagePanel(Cue cue, CadenzaData data) {
		super();
		_cue = cue;
		_data = data;
		
		_patchUsages = new ArrayList<>(_cue.patches);
		_keyboardPanels = new ArrayList<>(_data.keyboards.size());
		_patchUsageAreas = new ArrayList<>(_data.keyboards.size());
		
		for (final Keyboard keyboard : _data.keyboards) {
			final SingleKeyboardPanel skp = new SingleKeyboardPanel(keyboard);
			skp.addKeyboardListener(new PatchUsageAdder(keyboard, skp));
			_keyboardPanels.add(skp);
			_patchUsageAreas.add(new PatchUsageArea(skp));
		}
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		for (int i = 0; i < _data.keyboards.size(); ++i) {
			add(createTitleBox(_data.keyboards.get(i)));
			add(_keyboardPanels.get(i));
			add(_patchUsageAreas.get(i));
		}
		
		refreshDisplay();
	}
	
	private Box createTitleBox(Keyboard keyboard) {
		final Box result = Box.createHorizontalBox();
		result.add(new JLabel(keyboard.name + ": Drag a note range or "));
		result.add(new JButton(new AddToWholeAction(keyboard)));
		return result;
	}
	
	public List<PatchUsage> getPatchUsages() {
		return _patchUsages;
	}
	
	private void refreshDisplay() {
		for (final PatchUsageArea pua : _patchUsageAreas)
			pua.clearEntities();
		
		final Map<Keyboard, List<PatchUsage>> map = sortByKeyboard(_patchUsages);
		
		for (Keyboard keyboard : _data.keyboards) {
			final int index = _data.keyboards.indexOf(keyboard);
			final List<PatchUsage> list = map.get(keyboard);
			if (list == null) continue;
			
			PatchUsage last = null;
			while (!list.isEmpty()) {
				final PatchUsage pu = findNext(list, last);
				last = pu;
				list.remove(pu);
				
				_patchUsageAreas.get(index).addPatchUsage(pu);
			}
		}
		
		revalidate();
		repaint();
	}
	
	private static Map<Keyboard, List<PatchUsage>> sortByKeyboard(List<PatchUsage> patchUsages) {
		final Map<Keyboard, List<PatchUsage>> result = new HashMap<>();
		for (final PatchUsage pu : patchUsages) {
			List<PatchUsage> list = result.get(pu.location.getKeyboard());
			if (list == null) {
				list = new LinkedList<>();
				result.put(pu.location.getKeyboard(), list);
			}
			list.add(pu);
		}
		
		return result;
	}
	
	private static PatchUsage findNext(List<PatchUsage> patchUsages, PatchUsage last) {
		if (last == null) {
			final PatchUsage giant = findConflictingWithAll(patchUsages);
			if (giant != null)
				return giant;
			else
				return findWithLowestHigh(patchUsages);
		}
		
		final List<PatchUsage> nonConflicting = new LinkedList<>();
		for (final PatchUsage pu : patchUsages)
			if (pu.location.getLowest().above(last.location.getHighest()))
				nonConflicting.add(pu);
		
		if (nonConflicting.isEmpty())
			return findWithLowestHigh(patchUsages);
		else
			return findWithLowestHigh(nonConflicting);
	}
	
	private static PatchUsage findWithLowestHigh(List<PatchUsage> patchUsages) {
		PatchUsage found = patchUsages.get(0);
		Note foundHigh = found.location.getHighest();
		for (int i = 1; i < patchUsages.size(); ++i) {
			final PatchUsage pu = patchUsages.get(i);
			final Note high = pu.location.getHighest();
			if (high.below(foundHigh)) {
				found = pu;
				foundHigh = high;
			}
		}
		
		return found;
	}
	
	private static PatchUsage findConflictingWithAll(List<PatchUsage> patchUsages) {
		outer:
		for (final PatchUsage pu1 : patchUsages) {
			for (final PatchUsage pu2 : patchUsages) {
				if (pu1 == pu2) continue;
				
				if (pu1.location.getHighest().below(pu2.location.getLowest()) ||
					pu2.location.getHighest().below(pu1.location.getLowest())) // non-conflicting
					continue outer;
			}
			return pu1;
		}
		return null;
	}
	
	private class PatchUsageEntity extends JPanel {
		private PatchUsage _patchUsage;
		
		public PatchUsageEntity(PatchUsage patchUsage, int width, int height) {
			super();
			_patchUsage = patchUsage;
			
			setLayout(null);
			final JLabel label = new JLabel(patchUsage.patch.name, JLabel.CENTER);
			label.setBounds(0, 0, width, height);
			add(label);
			
			final JButton deleteButton = new JButton(new DeleteIcon(10));
			deleteButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					_patchUsages.remove(_patchUsage);
					refreshDisplay();
				}
			});
			deleteButton.setBounds(width-12, 2, 10, 10);
			add(deleteButton);
			
			setBackground(patchUsage.patch.getDisplayColor());
			setBorder(BorderFactory.createLineBorder(PATCH_BORDER));
			setToolTipText(_patchUsage.toString(false));
			
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() == 2) {
						e.consume();
						final PatchUsageEditDialog dialog = new PatchUsageEditDialog(
								PatchUsageEntity.this, _patchUsage, _data);
						dialog.showDialog();
						if (dialog.okPressed()) {
							_patchUsages.remove(_patchUsage);
							_patchUsages.add(dialog.getPatchUsage());
							refreshDisplay();
						}
					}
				}
			});
		}
	}
	
	private class PatchUsageArea extends JPanel {
		private static final int HEIGHT = 24;
		private static final int GAP = 1;
		
		private final SingleKeyboardPanel _keyboardPanel;
		
		private Note rightMostNote;
		int yPos;
		
		public PatchUsageArea(SingleKeyboardPanel keyboardPanel) {
			super();
			_keyboardPanel = keyboardPanel;
			
			SwingUtils.freezeWidth(this, _keyboardPanel.getSize().width);
			setLayout(null);
			
			SwingUtils.freezeHeight(this, HEIGHT + GAP);
		}
		
		public void addPatchUsage(PatchUsage patchUsage) {
			if (rightMostNote != null && !rightMostNote.below(patchUsage.location.getLowest())) {
				rightMostNote = null;
				yPos += HEIGHT + GAP;
				SwingUtils.freezeHeight(this, yPos + HEIGHT);
			}
			
			final int x = _keyboardPanel.accessKeyboardPanel().getKeyPosition(patchUsage.location.getLowest()).x;
			final Rectangle r = _keyboardPanel.accessKeyboardPanel().getKeyPosition(patchUsage.location.getHighest());
			final int width = r.x + r.width - x;
			
			final PatchUsageEntity pue = new PatchUsageEntity(patchUsage, width, HEIGHT);
			pue.setBounds(x, yPos, width, HEIGHT);
			add(pue);
			repaint();
			rightMostNote = patchUsage.location.getHighest();
		}
		
		public void clearEntities() {
			removeAll();
			repaint();
			rightMostNote = null;
			yPos = GAP;
			SwingUtils.freezeHeight(this, HEIGHT + GAP);
		}
		
		@Override
		public String toString() {
			return "RMN=" + rightMostNote + " yPos=" + yPos;
		}
	}
	
	private class PatchUsageAdder extends KeyboardAdapter {
		private final Keyboard _keyboard;
		private final Component _anchor;
		
		public PatchUsageAdder(Keyboard keyboard, Component anchor) {
			_keyboard = keyboard;
			_anchor = anchor;
		}
		
		@Override
		public void keyClicked(Note note) {
			final PatchSelectorDialog dialog = new PatchSelectorDialog(_anchor, null);
			dialog.showDialog();
			if (dialog.okPressed()) {
				final Patch patch = dialog.getSelectedPatch();
				_patchUsages.add(new SimplePatchUsage(patch,
													  Location.singleNote(_keyboard, note),
													  patch.defaultVolume,
													  0, false, -1, true, 0));
				refreshDisplay();
			}
		}
		
		@Override
		public void keyDragged(Note startNote, Note endNote) {
			final Note low, high;
			if (startNote.below(endNote)) {
				low = startNote; high = endNote;
			}
			else {
				low = endNote; high = startNote;
			}
			final PatchSelectorDialog dialog = new PatchSelectorDialog(_anchor, null);
			dialog.showDialog();
			if (dialog.okPressed()) {
				final Patch patch = dialog.getSelectedPatch();
				_patchUsages.add(new SimplePatchUsage(patch,
													  Location.range(_keyboard, low, high),
													  patch.defaultVolume,
													  0, false, -1, true, 0));
				refreshDisplay();
			}
		}
	}
	
	private class AddToWholeAction extends AbstractAction {
		private final Keyboard _keyboard;
		
		public AddToWholeAction(Keyboard keyboard) {
			super("add a patch to the whole keyboard");
			_keyboard = keyboard;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			final PatchSelectorDialog dialog = new PatchSelectorDialog((JButton) e.getSource(), null);
			dialog.showDialog();
			if (dialog.okPressed()) {
				final Patch patch = dialog.getSelectedPatch();
				_patchUsages.add(new SimplePatchUsage(patch, Location.wholeKeyboard(_keyboard),
						patch.defaultVolume, 0, false, -1, true, 0));
				refreshDisplay();
			}
		}
	}
	
	private class PatchSelectorDialog extends OKCancelDialog {
		private PatchSelector _selector;
		private final Patch _selected;
		
		public PatchSelectorDialog(Component anchor, Patch selected) {
			super(anchor);
			_selected = selected;
		}
		
		@Override
		protected JComponent buildContent() {
			_selector = new PatchSelector(_data.patches, _data.synthesizers, _selected);
			return _selector;
		}
		
		@Override
		protected String declareTitle() {
			return "Select Patch";
		}
		
		@Override
		protected void initialize() {
			setSize(800, 110);
		}
		
		@Override
		protected void verify() throws VerificationException {
			if (_selector.getSelectedPatch() == null)
				throw new VerificationException("Please select a patch");
		}
		
		public Patch getSelectedPatch() {
			return _selector.getSelectedPatch();
		}
	}
	
	private Keyboard _activeKeyboard = null;
	private Set<Integer> _currentlyPressedKeys = new HashSet<>(10);
	private Set<Integer> _accumulatedPressedKeys = new HashSet<>(10);
	@Override
	public void keyPressed(Keyboard source, int midiNumber, int velocity) {
		_keyboardPanels.get(_data.keyboards.indexOf(source)).accessKeyboardPanel().
				highlightNote(new Note(midiNumber), KeyboardPanel.HIGHLIGHT_COLOR);
		
		if (_activeKeyboard == null || source == _activeKeyboard) {
			_activeKeyboard = source;
			_accumulatedPressedKeys.add(Integer.valueOf(midiNumber));
			_currentlyPressedKeys.add(Integer.valueOf(midiNumber));
		} else {
			// key pressed on other keyboard
			_activeKeyboard = null;
			_accumulatedPressedKeys.clear();
			_currentlyPressedKeys.clear();
		}
	}
	
	@Override
	public void keyReleased(Keyboard source, int midiNumber) {
		_keyboardPanels.get(_data.keyboards.indexOf(source)).accessKeyboardPanel().unhighlightNote(new Note(midiNumber));
		
		if (source == _activeKeyboard) {
			_currentlyPressedKeys.remove(Integer.valueOf(midiNumber));
			if (_currentlyPressedKeys.isEmpty()) {
				if (_accumulatedPressedKeys.size() == 1) {
					// Spoof click
				} else if (_accumulatedPressedKeys.size() == 2) {
					// Spoof drag
				} else if (_accumulatedPressedKeys.size() >= 3) {
					// make whole
				}
			}
		}
	}
	
	@Override
	public void controlReceived(Keyboard source, int ccNumber, int value) { /* ignore */ }
}
