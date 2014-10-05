package cadenza.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;

import cadenza.core.CadenzaData;
import cadenza.core.Patch;
import cadenza.core.Synthesizer;
import cadenza.delegate.PatchChangeDelegate;

import common.Debug;

public class PreviewController extends CadenzaController {
  /** The currently assigned preview patch */
  private List<Patch> _previewPatches;
  
  /** Mapping of patches to assigned channels */
  private Map<Patch, Integer> _previewChannels;
  
  public PreviewController(CadenzaData data) {
    super(data);
    
    _previewPatches = new ArrayList<>();
    _previewChannels = new HashMap<>();
  }
  
  @Override
  public void send(MidiMessage message) {
    if (receiverReady() && message instanceof ShortMessage) {
      final ShortMessage sm = (ShortMessage) message;
      try {
        for (final Integer channel : _previewChannels.values()) {
          sm.setMessage(sm.getCommand(), channel.intValue(), sm.getData1(), sm.getData2());
          getReceiver().send(sm, -1);
        }
      } catch (InvalidMidiDataException e) {
        e.printStackTrace();
      }
    }
  }
  
  public synchronized void setPatches(List<Patch> patches) {
   _previewPatches = patches;
    _previewChannels.clear();
    
    Debug.println("Previewing patches: " + _previewPatches);
    
    if (receiverReady()) {
      try {
        final Map<Synthesizer, Integer> _synthIndexes = new IdentityHashMap<>();
        for (final Synthesizer synth : getData().synthesizers)
          _synthIndexes.put(synth, Integer.valueOf(0));
        
        for (final Patch patch : _previewPatches) {
          final Synthesizer synth = patch.getSynthesizer();
          final int index = _synthIndexes.get(synth).intValue();
          _synthIndexes.put(synth, Integer.valueOf(index+1));
          
          final int channel = synth.getChannels().get(index).intValue()-1;
          PatchChangeDelegate.performPatchChange(getReceiver(), patch, channel);
          _previewChannels.put(patch, Integer.valueOf(channel));
        }
        
        // don't notify, this method is called from a notify so we get an infinite loop
      } catch (InvalidMidiDataException e) {
        e.printStackTrace();
      }
    }
  }
  
  public List<Patch> getCurrentPreviewPatches() {
    return _previewPatches;
  }
  
  public synchronized void setVolume(int volume, Patch patch) {
    if (!receiverReady())
      return;
    
    final Integer channel = _previewChannels.get(patch);
    if (channel == null) {
      System.err.println("Patch '" + patch.name + "' not found");
      return;
    }
    
    final ShortMessage sm = new ShortMessage();
    try {
      sm.setMessage(ShortMessage.CONTROL_CHANGE, channel.intValue(), 7, volume);
      getReceiver().send(sm, -1);
    } catch (InvalidMidiDataException e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void notifyReceiver() {
    if (_previewPatches != null && _previewPatches.size() > 0)
      setPatches(_previewPatches);
  }
  
}
