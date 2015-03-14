package cadenza.core.trigger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.sound.midi.MidiMessage;

import cadenza.control.PerformanceController;
import cadenza.core.trigger.actions.TriggerAction;
import cadenza.core.trigger.predicates.TriggerPredicate;

public class Trigger implements Serializable {
  private static final long serialVersionUID = 2L;
  
  public List<TriggerPredicate> predicates;
  public boolean AND;
  public boolean inorder;
  
  private int _orderIndex = 0;
  private List<TriggerPredicate> _remainingPredicates;
  
  public int safetyDelayMillis;
  private long _lastTriggered = Long.MIN_VALUE;
  
  public List<TriggerAction> actions;
  
  public Trigger() {
    predicates = new ArrayList<>();
    AND = true;
    inorder = false;
    safetyDelayMillis = 0;
    actions = new ArrayList<>();
    
    _orderIndex = 0;
    _remainingPredicates = new ArrayList<>(predicates);
  }

  public void receive(MidiMessage message, PerformanceController controller) {
    if (AND) {
      if (inorder) {
        if (predicates.get(_orderIndex).receive(message)) {
          ++_orderIndex;
          if (_orderIndex == predicates.size()) {
            trigger(controller);
            reset();
          }
        }
      } else { // !inorder
        final List<TriggerPredicate> toRemove = new LinkedList<>();
        for (final TriggerPredicate pred : _remainingPredicates) {
          if (pred.receive(message)) {
            toRemove.add(pred);
          }
        }
        
        _remainingPredicates.removeAll(toRemove);
        
        if (_remainingPredicates.isEmpty()) {
          trigger(controller);
          reset();
        }
      }
    } else { // OR
      for (final TriggerPredicate pred : predicates) {
        if (pred.receive(message)) {
          trigger(controller);
          break;
        }
      }
    }
  }
  
  public void reset() {
    _orderIndex = 0;
    _remainingPredicates.clear();
    _remainingPredicates.addAll(predicates);
    
    predicates.forEach(TriggerPredicate::reset);
  }
  
  private void trigger(final PerformanceController controller) {
    if (safetyDelayMillis > 0 && System.currentTimeMillis() < _lastTriggered + safetyDelayMillis) {
      return;
    }
    
    _lastTriggered = System.currentTimeMillis();
    
    new Thread(() -> {
      for (final TriggerAction action : actions)
        action.takeAction(controller);
    }).start();
  }

}
