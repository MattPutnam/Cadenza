package cadenza.control;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cadenza.core.CadenzaData;
import cadenza.core.Patch;
import cadenza.delegate.PatchChangeDelegate;

public class QuickPreviewController extends CadenzaController {
  private static final Logger LOG = LogManager.getLogger(QuickPreviewController.class);
  
  private Patch _patch;
  private int _channel;
  // TODO: have selectable quick preview channel, as property of Synthesizer (requires new version)

  public QuickPreviewController(CadenzaData data) {
    super(data);
  }

  @Override
  protected void notifyReceiver() {
    setPatch(_patch);
  }

  @Override
  public void send(MidiMessage message) {
    if (_patch != null && receiverReady() && message instanceof ShortMessage) {
      final ShortMessage sm = (ShortMessage) message;
      try {
        sm.setMessage(sm.getCommand(), _channel, sm.getData1(), sm.getData2());
        getReceiver().send(sm, -1);
      } catch (InvalidMidiDataException|IllegalStateException e) {
        LOG.error("Error trying to send quick preview message", e);
      }
    }
  }

  public void setPatch(Patch patch) {
    _patch = patch;
    
    if (_patch != null && receiverReady()) {
      _channel = _patch.getSynthesizer().getChannels().get(0).intValue();
      
      try {
        PatchChangeDelegate.performPatchChange(getReceiver(), _patch, _channel);
      } catch (InvalidMidiDataException e) {
        LOG.error("Error trying to send patch change for quick preview", e);
      }
    }
  }
}
