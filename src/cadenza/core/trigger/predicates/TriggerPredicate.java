package cadenza.core.trigger.predicates;

import java.io.Serializable;

import javax.sound.midi.MidiMessage;


/**
 * Interface for events that can trigger Triggers
 * 
 * @author Matt Putnam
 */
public interface TriggerPredicate extends Serializable {
  /**
   * Receives a MidiMessage and determines if the Trigger is to be triggered
   * @param message - the message received
   * @return true iff the condition has been met
   */
  public boolean receive(MidiMessage message);
  
  /**
   * Resets any internal state maintained in the predicate.  Default
   * implementation does nothing.
   */
  public default void reset() {}
}
