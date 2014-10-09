package cadenza.gui.trigger;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import cadenza.core.CadenzaData;
import cadenza.core.trigger.Trigger;
import cadenza.core.trigger.actions.TriggerAction;
import cadenza.core.trigger.predicates.TriggerPredicate;
import cadenza.gui.common.CadenzaTable;

import common.swing.IntField;
import common.swing.SwingUtils;
import common.swing.VerificationException;
import common.swing.dialog.OKCancelDialog;
import common.swing.table.ListTableModel;

@SuppressWarnings("serial")
public class TriggerEditDialog extends OKCancelDialog {
  private final Trigger _trigger;
  private final CadenzaData _data;
  
  private List<TriggerPredicate> _predicates;
  private PredicateTable _predicateTable;
  
  private JRadioButton _ANDButton;
  private JRadioButton _ORButton;
  private JCheckBox _sequenceBox;
  
  private List<TriggerAction> _actions;
  private ActionTable _actionTable;
  
  private IntField _safetyDelayField;
  
  public TriggerEditDialog(Component parent, Trigger trigger, CadenzaData data) {
    super(parent);
    _trigger = trigger;
    _data = data;
  }

  @Override
  protected JComponent buildContent() {
    _predicates = new ArrayList<>(_trigger.predicates);
    _predicateTable = new PredicateTable();
    
    _ANDButton = new JRadioButton("AND");
    _ORButton = new JRadioButton("OR");
    SwingUtils.groupAndSelectFirst(_ANDButton, _ORButton);
    
    _sequenceBox = new JCheckBox("in sequence");
    if (_trigger.AND) _ANDButton.setSelected(true);
    else _ORButton.setSelected(true);
    _sequenceBox.setSelected(_trigger.inorder);
    
    _actions = new ArrayList<>(_trigger.actions);
    _actionTable = new ActionTable();
    
    _safetyDelayField = new IntField(_trigger.safetyDelayMillis, 0, Integer.MAX_VALUE);
    _safetyDelayField.setColumns(8);
    SwingUtils.freezeWidth(_safetyDelayField);
    final Box safetyPanel = Box.createHorizontalBox();
    safetyPanel.add(new JLabel("Prevent repeated triggering for "));
    safetyPanel.add(_safetyDelayField);
    safetyPanel.add(new JLabel(" milliseconds"));
    safetyPanel.add(Box.createHorizontalGlue());
    
    final JPanel panel = new JPanel();
    final GroupLayout layout = new GroupLayout(panel);
    panel.setLayout(layout);
    
    layout.setHorizontalGroup(layout.createParallelGroup()
        .addComponent(_predicateTable)
        .addGroup(layout.createSequentialGroup()
            .addComponent(_ANDButton)
            .addComponent(_sequenceBox))
        .addComponent(_ORButton)
        .addComponent(_actionTable)
        .addComponent(safetyPanel));
    
    layout.setVerticalGroup(layout.createSequentialGroup()
        .addComponent(_predicateTable)
        .addGap(16)
        .addGroup(layout.createParallelGroup()
            .addComponent(_ANDButton)
            .addComponent(_sequenceBox))
        .addComponent(_ORButton)
        .addGap(16)
        .addComponent(_actionTable)
        .addGap(16)
        .addComponent(safetyPanel));
    
    return panel;
  }
  
  @Override
  protected void initialize() {
    setSize(800, 600);
  }

  @Override
  protected String declareTitle() {
    return "Edit Trigger";
  }

  @Override
  protected void verify() throws VerificationException {
    if (_predicates.isEmpty())
      throw new VerificationException("Please add at least one trigger predicate");
    if (_actions.isEmpty())
      throw new VerificationException("Please add at least one trigger action");
  }
  
  @Override
  protected void takeActionOnOK() {
    _trigger.predicates = _predicates;
    _trigger.AND = _ANDButton.isSelected();
    _trigger.inorder = _sequenceBox.isSelected();
    _trigger.actions = _actions;
    _trigger.safetyDelayMillis = _safetyDelayField.getInt();
  }
  
  private class PredicateTable extends CadenzaTable<TriggerPredicate> {
    public PredicateTable() {
      super(_predicates, true, true);
    }
    
    @Override
    protected ListTableModel<TriggerPredicate> createTableModel() {
      return new ListTableModel<TriggerPredicate>() {
        @Override
        public String[] declareColumns() {
          return new String[] {"When the following inputs are received:"};
        }
        
        @Override
        public Object resolveValue(TriggerPredicate row, int column) {
          return row.toString();
        }
      };
    }
    
    @Override
    protected String declareTypeName() {
      return "predicate";
    }
    
    @Override
    protected void takeActionOnAdd() {
      final TriggerPredicateEditDialog editor = new TriggerPredicateEditDialog(TriggerEditDialog.this, _data.keyboards, null);
      editor.showDialog();
      if (editor.okPressed()) {
        _predicates.add(editor.getPredicate());
      }
    }
    
    @Override
    protected void takeActionOnEdit(TriggerPredicate item) {
      final TriggerPredicateEditDialog editor = new TriggerPredicateEditDialog(TriggerEditDialog.this, _data.keyboards, item);
      editor.showDialog();
      if (editor.okPressed()) {
        _predicates.set(_predicates.indexOf(item), editor.getPredicate());
      }
    }
  }
  
  private class ActionTable extends CadenzaTable<TriggerAction> {
    public ActionTable() {
      super(_actions, true, true);
    }
    
    @Override
    protected ListTableModel<TriggerAction> createTableModel() {
      return new ListTableModel<TriggerAction>() {
        @Override
        public String[] declareColumns() {
          return new String[] {"Trigger the following actions:"};
        }
        
        @Override
        public Object resolveValue(TriggerAction row, int column) {
          return row.toString();
        }
      };
    }
    
    @Override
    protected String declareTypeName() {
      return "action";
    }
    
    @Override
    protected void takeActionOnAdd() {
      final TriggerActionEditDialog editor = new TriggerActionEditDialog(TriggerEditDialog.this, _data, null);
      editor.showDialog();
      if (editor.okPressed()) {
        _actions.add(editor.getAction());
      }
    }
    
    @Override
    protected void takeActionOnEdit(TriggerAction item) {
      final TriggerActionEditDialog editor = new TriggerActionEditDialog(TriggerEditDialog.this, _data, item);
      editor.showDialog();
      if (editor.okPressed()) {
        _actions.set(_actions.indexOf(item), editor.getAction());
      }
    }
  }

}
