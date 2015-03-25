package cadenza.gui.patchusage;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import cadenza.core.CadenzaData;
import cadenza.core.Cue;
import cadenza.core.Keyboard;
import cadenza.core.Location;
import cadenza.core.Note;
import cadenza.core.Patch;
import cadenza.core.patchusage.PatchUsage;
import cadenza.core.patchusage.SimplePatchUsage;
import cadenza.gui.CadenzaFrame;
import cadenza.gui.ImageStore;
import cadenza.gui.keyboard.KeyboardAdapter;
import cadenza.gui.keyboard.SingleKeyboardPanel;
import cadenza.gui.patch.PatchSelector;

import common.swing.SwingUtils;
import common.swing.VerificationException;
import common.swing.dialog.Dialog;
import common.swing.dialog.OKCancelDialog;

@SuppressWarnings("serial")
public class PatchUsagePanel extends JPanel {
  private static final Color PATCH_BORDER = Color.DARK_GRAY;
  
  private final CadenzaFrame _frame;
  private final Cue _cue;
  private final CadenzaData _data;
  
  private final List<PatchUsage> _patchUsages;
  private final List<SingleKeyboardPanel> _keyboardPanels;
  private final List<PatchUsageArea> _patchUsageAreas;
  
  public PatchUsagePanel(CadenzaFrame frame, Cue cue, CadenzaData data) {
    super();
    _frame = frame;
    _cue = cue;
    _data = data;
    
    _patchUsages = new ArrayList<>(_cue.patches);
    _keyboardPanels = new ArrayList<>(_data.keyboards.size());
    _patchUsageAreas = new ArrayList<>(_data.keyboards.size());
    
    for (final Keyboard keyboard : _data.keyboards) {
      final SingleKeyboardPanel skp = new SingleKeyboardPanel(keyboard.soundingLow, keyboard.soundingHigh);
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
    _patchUsageAreas.forEach(PatchUsageArea::clearEntities);
    
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
        if (pu.isSplit())
          list.remove(pu.splitTwin);
        
        _patchUsageAreas.get(index).addPatchUsage(pu);
      }
    }
    
    revalidate();
    repaint();
  }
  
  public void highlightKey(int keyboardIndex, int midiNum) {
    _keyboardPanels.get(keyboardIndex).accessKeyboardPanel().highlightNote(Note.valueOf(midiNum));
  }
  
  public void unHighlightKey(int keyboardIndex, int midiNum) {
    _keyboardPanels.get(keyboardIndex).accessKeyboardPanel().unhighlightNote(Note.valueOf(midiNum));
  }
  
  public void addPatchUsage(Location location) {
    SwingUtils.doInSwing(() -> {
      OKCancelDialog.showDialog(new PatchSelectorDialog(this, null), dialog -> {
        _patchUsages.add(new SimplePatchUsage(dialog.getSelectedPatch(), location));
        refreshDisplay();
      });
    }, false);
  }
  
  private static Map<Keyboard, List<PatchUsage>> sortByKeyboard(List<PatchUsage> patchUsages) {
    return patchUsages.stream().collect(Collectors.groupingBy(pu -> pu.location.getKeyboard()));
  }
  
  private static PatchUsage findNext(List<PatchUsage> patchUsages, PatchUsage last) {
    if (last == null) {
      final PatchUsage giant = findConflictingWithAll(patchUsages);
      if (giant != null)
        return giant;
      else
        return findWithLowestHigh(patchUsages);
    }
    
    final List<PatchUsage> nonConflicting =
        patchUsages.stream()
                   .filter(pu -> pu.location.getLower().above(last.location.getUpper()))
                   .collect(Collectors.toList());
    
    if (nonConflicting.isEmpty())
      return findWithLowestHigh(patchUsages);
    else
      return findWithLowestHigh(nonConflicting);
  }
  
  private static PatchUsage findWithLowestHigh(List<PatchUsage> patchUsages) {
    PatchUsage found = patchUsages.get(0);
    Note foundHigh = found.location.getUpper();
    for (int i = 1; i < patchUsages.size(); ++i) {
      final PatchUsage pu = patchUsages.get(i);
      final Note high = pu.location.getUpper();
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
        
        if (pu1.location.getUpper().below(pu2.location.getLower()) ||
            pu2.location.getUpper().below(pu1.location.getLower())) // non-conflicting
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
      final JLabel label;
      if (patchUsage.isSplit()) {
        final String text = "<html><nobr>" + patchUsage.buildSplitName(false, true) + "</nobr></html>";
        label = new JLabel(text, JLabel.CENTER);
        
        setBackground(Color.WHITE);
        setToolTipText(text);
      } else {
        label = new JLabel(patchUsage.patch.name, JLabel.CENTER);
        label.setForeground(patchUsage.patch.getTextColor());
        
        setBackground(patchUsage.patch.getDisplayColor());
        setToolTipText(_patchUsage.toString(false));
      }
      label.setBounds(0, 0, width, height);
      add(label);
      
      setBorder(BorderFactory.createLineBorder(PATCH_BORDER));
      setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      
      addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          final PopupMenu menu = new PopupMenu();
          menu.show(e.getComponent(), e.getX(), e.getY());
        }
      });
    }
    
    private class PopupMenu extends JPopupMenu {
      private PopupMenu() {
        final List<PatchUsage> others = buildOthers(_patchUsage);
        
        // EDIT OPTIONS
        if (_patchUsage.isSplit()) {
          final PatchUsage lower = _patchUsage.splitAbove ? _patchUsage.splitTwin : _patchUsage;
          final PatchUsage upper = _patchUsage.splitAbove ? _patchUsage : _patchUsage.splitTwin;
          
          add(SwingUtils.menuItem("Edit lower (" + lower.patch.name + ")", ImageStore.EDIT, e ->
            OKCancelDialog.showDialog(new PatchUsageEditDialog(_frame, lower, _data), dialog -> {
              final PatchUsage newLower = dialog.getPatchUsage();
              final Location newLocation = newLower.location;
              
              newLower.copySplitDataFrom(lower);
              upper.splitTwin = newLower;
              upper.location = newLocation;
              
              _patchUsages.remove(_patchUsage);
              _patchUsages.add(newLower);
              refreshDisplay();
            })
          ));
          
          add(SwingUtils.menuItem("Edit upper (" + upper.patch.name + ")", ImageStore.EDIT, e ->
            OKCancelDialog.showDialog(new PatchUsageEditDialog(_frame, lower, _data), dialog -> {
              final PatchUsage newUpper = dialog.getPatchUsage();
              final Location newLocation = newUpper.location;
              
              newUpper.copySplitDataFrom(upper);
              lower.splitTwin = newUpper;
              lower.location = newLocation;
              
              _patchUsages.remove(_patchUsage);
              _patchUsages.add(newUpper);
              refreshDisplay();
            })
          ));
          
          add(SwingUtils.menuItem("Edit smart split properties...", null, e ->
            OKCancelDialog.showDialog(new SmartSplitDialog(_frame, _patchUsage, others), dialog ->
              refreshDisplay()
            )
          ));
          
          add(SwingUtils.menuItem("Detach from smart split", null, e -> {
            _patchUsage.unsplit();
            refreshDisplay();
          }));
        } else {
          // edit options -> not splitting
          add(SwingUtils.menuItem("Edit", ImageStore.EDIT, e ->
            OKCancelDialog.showDialog(new PatchUsageEditDialog(_frame, _patchUsage, _data), dialog -> {
              _patchUsages.remove(_patchUsage);
              _patchUsages.add(dialog.getPatchUsage());
              refreshDisplay();
            })
          ));
          
          if (others.size() > 0) {
            add(SwingUtils.menuItem("Create smart split with...", null, e ->
              OKCancelDialog.showDialog(new SmartSplitDialog(_frame, _patchUsage, others), dialog ->
                refreshDisplay()
              )
            ));
          }
        }
        
        addSeparator();
        
        add(SwingUtils.menuItem("Delete", ImageStore.DELETE, e ->
          Dialog.confirm(this, "Are you sure you want to delete this patch?", () -> {
            _patchUsages.remove(_patchUsage);
            if (_patchUsage.isSplit())
              _patchUsages.remove(_patchUsage.splitTwin);
            refreshDisplay();
          })
        ));
      }
    }
    
    private List<PatchUsage> buildOthers(PatchUsage target) {
      return _patchUsages.stream()
                         .filter(pu -> pu.location.getKeyboard() == target.location.getKeyboard())
                         .filter(pu -> !pu.isSplit())
                         .filter(pu -> pu != target)
                         .collect(Collectors.toList());
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
      if (rightMostNote != null && !rightMostNote.below(patchUsage.location.getLower())) {
        rightMostNote = null;
        yPos += HEIGHT + GAP;
        SwingUtils.freezeHeight(this, yPos + HEIGHT);
      }
      
      final int x = _keyboardPanel.accessKeyboardPanel().getKeyPosition(patchUsage.location.getLower()).x;
      final Rectangle r = _keyboardPanel.accessKeyboardPanel().getKeyPosition(patchUsage.location.getUpper());
      final int width = r.x + r.width - x;
      
      final PatchUsageEntity pue = new PatchUsageEntity(patchUsage, width, HEIGHT);
      pue.setBounds(x, yPos, width, HEIGHT);
      add(pue);
      repaint();
      rightMostNote = patchUsage.location.getUpper();
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
      OKCancelDialog.showDialog(new PatchSelectorDialog(_anchor, null), dialog -> {
        _patchUsages.add(new SimplePatchUsage(dialog.getSelectedPatch(), new Location(_keyboard, note)));
        refreshDisplay();
      });
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
      
      OKCancelDialog.showDialog(new PatchSelectorDialog(_anchor, null), dialog -> {
        _patchUsages.add(new SimplePatchUsage(dialog.getSelectedPatch(), new Location(_keyboard, low, high)));
        refreshDisplay();
      });
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
      OKCancelDialog.showDialog(new PatchSelectorDialog((JButton) e.getSource(), null), dialog -> {
        final Patch patch = dialog.getSelectedPatch();
        _patchUsages.add(new SimplePatchUsage(patch, new Location(_keyboard, true),
            patch.defaultVolume, 0, false, -1, true, 0));
        refreshDisplay();
      });
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
      _selector = new PatchSelector(_frame, _data.patches, _data.synthesizers, _selected);
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
}
