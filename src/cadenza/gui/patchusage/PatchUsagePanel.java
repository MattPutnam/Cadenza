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
import cadenza.core.PatchAssignmentEntity;
import cadenza.core.patchmerge.PatchMerge;
import cadenza.core.patchusage.PatchUsage;
import cadenza.core.patchusage.SimplePatchUsage;
import cadenza.gui.CadenzaFrame;
import cadenza.gui.ImageStore;
import cadenza.gui.keyboard.KeyboardAdapter;
import cadenza.gui.keyboard.SingleKeyboardPanel;
import cadenza.gui.patch.PatchSelector;
import cadenza.gui.patchusage.merge.MergePatchDialog;

import common.swing.SwingUtils;
import common.swing.VerificationException;
import common.swing.dialog.Dialog;
import common.swing.dialog.OKCancelDialog;

@SuppressWarnings("serial")
public class PatchUsagePanel extends JPanel {
  private static final Color PATCH_BORDER = Color.DARK_GRAY;
  
  private final CadenzaFrame _frame;
  private final CadenzaData _data;
  
  private final List<PatchUsage> _patchUsages;
  private final List<PatchMerge> _patchMerges;
  private final List<SingleKeyboardPanel> _keyboardPanels;
  private final List<PatchUsageArea> _patchUsageAreas;
  
  public PatchUsagePanel(CadenzaFrame frame, Cue cue, CadenzaData data) {
    super();
    _frame = frame;
    _data = data;
    
    _patchUsages = new ArrayList<>(cue.patches);
    _patchMerges = new ArrayList<>(cue.merges);
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
  
  public List<PatchMerge> getPatchMerges() {
    return _patchMerges;
  }
  
  private void refreshDisplay() {
    _patchUsageAreas.forEach(PatchUsageArea::clearEntities);
    
    final List<PatchAssignmentEntity> allEntities = new ArrayList<>();
    allEntities.addAll(_patchUsages);
    allEntities.addAll(_patchMerges);
    final Map<Keyboard, List<PatchAssignmentEntity>> map = sortByKeyboard(allEntities);
    
    for (Keyboard keyboard : _data.keyboards) {
      final int index = _data.keyboards.indexOf(keyboard);
      final List<PatchAssignmentEntity> list = map.get(keyboard);
      if (list == null) continue;
      
      PatchAssignmentEntity last = null;
      while (!list.isEmpty()) {
        final PatchAssignmentEntity pae = findNext(list, last);
        last = pae;
        list.remove(pae);
        
        _patchUsageAreas.get(index).addPatchAssignmentEntity(pae);
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
  
  private static Map<Keyboard, List<PatchAssignmentEntity>> sortByKeyboard(List<PatchAssignmentEntity> patchAssignments) {
    return patchAssignments.stream().collect(Collectors.groupingBy(pae -> pae.getLocation().getKeyboard()));
  }
  
  private static PatchAssignmentEntity findNext(List<PatchAssignmentEntity> patchAssignments, PatchAssignmentEntity last) {
    if (last == null) {
      final PatchAssignmentEntity giant = findConflictingWithAll(patchAssignments);
      if (giant != null)
        return giant;
      else
        return findWithLowestHigh(patchAssignments);
    }
    
    final List<PatchAssignmentEntity> nonConflicting =
        patchAssignments.stream()
                   .filter(pu -> pu.getLocation().getLower().above(last.getLocation().getUpper()))
                   .collect(Collectors.toList());
    
    if (nonConflicting.isEmpty())
      return findWithLowestHigh(patchAssignments);
    else
      return findWithLowestHigh(nonConflicting);
  }
  
  private static PatchAssignmentEntity findWithLowestHigh(List<PatchAssignmentEntity> patchAssignments) {
    PatchAssignmentEntity found = patchAssignments.get(0);
    Note foundHigh = found.getLocation().getUpper();
    for (int i = 1; i < patchAssignments.size(); ++i) {
      final PatchAssignmentEntity pae = patchAssignments.get(i);
      final Note high = pae.getLocation().getUpper();
      if (high.below(foundHigh)) {
        found = pae;
        foundHigh = high;
      }
    }
    
    return found;
  }
  
  private static PatchAssignmentEntity findConflictingWithAll(List<PatchAssignmentEntity> patchAssignments) {
    outer:
    for (final PatchAssignmentEntity pae1 : patchAssignments) {
      for (final PatchAssignmentEntity pae2 : patchAssignments) {
        if (pae1 == pae2) continue;
        
        if (pae1.getLocation().getUpper().below(pae2.getLocation().getLower()) ||
            pae2.getLocation().getUpper().below(pae1.getLocation().getLower())) // non-conflicting
          continue outer;
      }
      return pae1;
    }
    return null;
  }
  
  private class PatchUsageEntity extends JPanel {
    private final PatchUsage _patchUsage;
    
    public PatchUsageEntity(PatchUsage patchUsage, int width, int height) {
      super();
      _patchUsage = patchUsage;
      
      setLayout(null);
      final JLabel label = new JLabel(patchUsage.patch.name, JLabel.CENTER);
      label.setForeground(patchUsage.patch.getTextColor());
      setBackground(patchUsage.patch.getDisplayColor());
      setToolTipText(_patchUsage.toString(false, false, true));
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
        add(SwingUtils.menuItem("Edit", ImageStore.EDIT, e ->
          OKCancelDialog.showDialog(new PatchUsageEditDialog(_frame, _patchUsage, _data), dialog -> {
            _patchUsages.remove(_patchUsage);
            _patchUsages.add(dialog.getPatchUsage());
            refreshDisplay();
          })
        ));
        
        final List<PatchUsage> others = buildOthers();
        if (others.size() > 0) {
          add(SwingUtils.menuItem("Create Merged Patch with...", null, e -> {
            OKCancelDialog.showDialog(new MergePatchDialog(_frame, null, _patchUsage, others), dialog -> {
              final PatchMerge merge = dialog.getPatchMerge();
              _patchMerges.add(merge);
              _patchUsages.removeAll(merge.accessPatchUsages());
              refreshDisplay();
            });
          }));
        }
        
        addSeparator();
        
        add(SwingUtils.menuItem("Delete", ImageStore.DELETE, e ->
          Dialog.confirm(this, "Are you sure you want to delete this patch?", () -> {
            _patchUsages.remove(_patchUsage);
            refreshDisplay();
          })
        ));
      }
    }
    
    private List<PatchUsage> buildOthers() {
      return _patchUsages.stream()
                         .filter(pu -> pu.location.getKeyboard() == _patchUsage.location.getKeyboard())
                         .filter(pu -> pu != _patchUsage)
                         .collect(Collectors.toList());
    }
  }
  
  private class PatchMergeEntity extends JPanel {
    private final PatchMerge _patchMerge;
    
    public PatchMergeEntity(PatchMerge merge, int width, int height) {
      _patchMerge = merge;
      
      setLayout(null);
      final String text = "<html>" + merge.toString(false, false, true) + "</html>";
      final JLabel label = new JLabel(text, JLabel.CENTER);
      setBackground(Color.WHITE);
      setToolTipText(text);
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
        int index = 0;
        for (final PatchUsage pu : _patchMerge.accessPatchUsages()) {
          final int findex = index++;
          add(SwingUtils.menuItem("Edit " + pu.toString(false, false, false), null, e -> {
            OKCancelDialog.showDialog(new PatchUsageEditDialog(_frame, pu, _data), dialog -> {
              _patchMerge.accessPatchUsages().set(findex, dialog.getPatchUsage());
              refreshDisplay();
            });
          }));
        }
        
        final List<PatchUsage> editOthers = buildOthers();
        editOthers.addAll(_patchMerge.accessPatchUsages());
        editOthers.remove(_patchMerge.accessPrimary());
        add(SwingUtils.menuItem("Edit merge properties", ImageStore.EDIT, e -> {
          OKCancelDialog.showDialog(new MergePatchDialog(_frame, _patchMerge, _patchMerge.accessPrimary(), editOthers), dialog -> {
            _patchMerges.remove(_patchMerge);
            _patchMerges.add(dialog.getPatchMerge());
            refreshDisplay();
          });
        }));
        
        addSeparator();
        
        add(SwingUtils.menuItem("Detach merged items", null, e -> {
          _patchUsages.addAll(_patchMerge.accessPatchUsages());
          _patchMerges.remove(_patchMerge);
          refreshDisplay();
        }));
        
        addSeparator();
        
        add(SwingUtils.menuItem("Delete (including contained patches)", ImageStore.DELETE, e -> {
          _patchMerges.remove(_patchMerge);
          refreshDisplay();
        }));
      }
    }
    
    private List<PatchUsage> buildOthers() {
      return _patchUsages.stream()
                         .filter(pu -> pu.location.getKeyboard() == _patchMerge.accessLocation().getKeyboard())
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
    
    public void addPatchAssignmentEntity(PatchAssignmentEntity entity) {
      if (rightMostNote != null && !rightMostNote.below(entity.getLocation().getLower())) {
        rightMostNote = null;
        yPos += HEIGHT + GAP;
        SwingUtils.freezeHeight(this, yPos + HEIGHT);
      }
      
      final int x = _keyboardPanel.accessKeyboardPanel().getKeyPosition(entity.getLocation().getLower()).x;
      final Rectangle r = _keyboardPanel.accessKeyboardPanel().getKeyPosition(entity.getLocation().getUpper());
      final int width = r.x + r.width - x;
      
      final JPanel brick;
      if (entity instanceof PatchUsage) {
        brick = new PatchUsageEntity((PatchUsage) entity, width, HEIGHT);
      } else if (entity instanceof PatchMerge) {
        brick = new PatchMergeEntity((PatchMerge) entity, width, HEIGHT);
      } else {
        throw new IllegalStateException("Unknown PatchAssignmentEntity subtype");
      }
      brick.setBounds(x, yPos, width, HEIGHT);
      add(brick);
      repaint();
      rightMostNote = entity.getLocation().getUpper();
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
