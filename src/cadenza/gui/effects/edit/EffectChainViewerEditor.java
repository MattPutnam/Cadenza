package cadenza.gui.effects.edit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import cadenza.core.effects.Compressor;
import cadenza.core.effects.GraphicEQ;
import cadenza.core.effects.ParametricEQ;
import cadenza.core.effects.Effect;
import cadenza.core.effects.ParametricEQ.Band;
import cadenza.gui.ImageStore;
import cadenza.gui.effects.view.EffectView;
import common.swing.SwingUtils;
import common.swing.VerificationException;
import common.swing.dialog.Dialog;
import common.swing.dialog.OKCancelDialog;

@SuppressWarnings("serial")
public class EffectChainViewerEditor extends JPanel {
  private static final int INSERT_SIZE = 40;
  private static final int[] INSERT_xPoints = new int[] {0,  32, 26, 29, 40, 29, 26, 32, 0};
  private static final int[] INSERT_yPoints = new int[] {18, 18, 12, 9,  20, 31, 28, 22, 22};
  private static final Font INSERT_FONT = Font.decode("Arial 9");
  private static final String INSERT_TOP_TEXT = "Click to";
  private static final String INSERT_BOTTOM_TEXT = "add new";
  
  private List<Effect> _effects;
  private final boolean _allowEdit;
  
  private List<EffectView> _effectViews;
  private final JPanel _effectViewPanel;
  
  public EffectChainViewerEditor(List<Effect> initial, boolean allowEdit) {
    _effects = new ArrayList<>(initial.size());
    for (final Effect effect : initial)
      _effects.add(effect.copy());
    _allowEdit = allowEdit;
    
    _effectViewPanel = new JPanel();
    _effectViewPanel.setLayout(new BoxLayout(_effectViewPanel, BoxLayout.X_AXIS));
    final JScrollPane scrollPane = new JScrollPane(_effectViewPanel,
        JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setPreferredSize(new Dimension(600, 180));
    
    setLayout(new BorderLayout());
    add(scrollPane, BorderLayout.CENTER);
    refreshEffectViews();
  }
  
  public List<Effect> getEffects() {
    return _effects;
  }
  
  public void setEffects(List<Effect> effects) {
    _effects = effects;
    refreshEffectViews();
  }
  
  public void showInputValue(int index, int midiNum, int velocity) {
    _effectViews.get(index).showInputValue(midiNum, velocity);
  }
  
  public void clearInputValues() {
    for (final EffectView pv : _effectViews)
      pv.clearInputValue();
  }
  
  private void refreshEffectViews() {
    _effectViews = new ArrayList<>(_effects.size());
    for (final Effect effect : _effects) {
      _effectViews.add(effect.createView());
    }
    
    _effectViewPanel.removeAll();
    _effectViewPanel.add(Box.createHorizontalGlue());
    for (int i = 0; i < _effects.size(); ++i) {
      _effectViewPanel.add(new InsertEffectPanel(i));
      
      final EffectView effectView = _effectViews.get(i);
      _effectViewPanel.add(effectView);
      
      if (_allowEdit) {
        final int finali = i;
        effectView.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            if (e.getModifiersEx() == InputEvent.CTRL_DOWN_MASK ||
              e.getButton() == MouseEvent.BUTTON2 ||
              e.getButton() == MouseEvent.BUTTON3) {
              final JPopupMenu menu = new JPopupMenu();
              menu.add(new JMenuItem(new EditAction(effectView, finali)));
              menu.addSeparator();
              menu.add(new JMenuItem(new DeleteAction(finali)));
              menu.show(effectView, e.getX(), e.getY());
            }
            
            if (e.getClickCount() == 2) {
              final EffectEditor editor = effectView.createEditor();
              if (OKCancelDialog.showInDialog(EffectChainViewerEditor.this, "", editor)) {
                _effects.set(finali, editor.getEffect());
                refreshEffectViews();
              }
            }
          }
        });
      }
    }
    _effectViewPanel.add(new InsertEffectPanel(_effects.size()));
    _effectViewPanel.add(Box.createHorizontalGlue());
    revalidate();
    repaint();
  }
  
  private class InsertEffectPanel extends JPanel {
    public InsertEffectPanel(final int index) {
      super();
      
      SwingUtils.freezeSize(this, INSERT_SIZE, INSERT_SIZE);
      setLayout(null);
      
      if (_allowEdit) {
        addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            final AddEffectDialog dialog = new AddEffectDialog();
            dialog.showDialog();
            if (dialog.okPressed()) {
              _effects.add(index, dialog.getEffect());
              refreshEffectViews();
            }
          }
        });
      }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      
      g.setColor(Color.DARK_GRAY);
      g.fillPolygon(INSERT_xPoints, INSERT_yPoints, INSERT_xPoints.length);
      
      if (_allowEdit) {
        g.setFont(INSERT_FONT);
        g.drawString(INSERT_TOP_TEXT, 4, 8);
        g.drawString(INSERT_BOTTOM_TEXT, 2, 38);
      }
    }
  }
  
  private class AddEffectDialog extends OKCancelDialog {
    private JTabbedPane _tabbed;
    
    public AddEffectDialog() {
      super(EffectChainViewerEditor.this);
    }
    
    @Override
    protected JComponent buildContent() {
      final Band band = new Band(80, 3.0, 0.5);
      
      _tabbed = new JTabbedPane();
      _tabbed.addTab("Compressor/Limiter", new CompressorEditor(new Compressor(80, 2.5)));
      _tabbed.addTab("Parametric EQ", new ParametricEQEditor(new ParametricEQ(Collections.singletonList(band))));
      _tabbed.addTab("128-Band Graphic EQ", new GraphicEQEditor(new GraphicEQ(new int[128])));
      
      return _tabbed;
    }
    
    @Override
    protected void initialize() {
      setResizable(false);
    }
    
    @Override
    protected String declareTitle() {
      return "Add new effect";
    }
    
    public Effect getEffect() {
      return ((EffectEditor) _tabbed.getSelectedComponent()).getEffect();
    }
    
    @Override
    protected void verify() throws VerificationException { /* no-op */ }
  }
  
  private class EditAction extends AbstractAction {
    private final EffectView _effectView;
    private final int _index;
    
    public EditAction(EffectView effectView, int index) {
      super("Edit");
      putValue(SMALL_ICON, ImageStore.EDIT);
      
      _effectView = effectView;
      _index = index;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
      final EffectEditor editor = _effectView.createEditor();
      if (OKCancelDialog.showInDialog(EffectChainViewerEditor.this, "", editor)) {
        _effects.set(_index, editor.getEffect());
        refreshEffectViews();
      }
    }
  }
  
  private class DeleteAction extends AbstractAction {
    private final int _index;
    
    public DeleteAction(int index) {
      super("Delete");
      putValue(SMALL_ICON, ImageStore.DELETE);
      
      _index = index;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
      if (Dialog.confirm(EffectChainViewerEditor.this, "Are you sure you want to delete this effect?")) {
        _effects.remove(_index);
        refreshEffectViews();
      }
    }
  }
}
