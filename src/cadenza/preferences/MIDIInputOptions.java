package cadenza.preferences;

public class MIDIInputOptions {
  private boolean _allowMIDIInput;
  private boolean _allowVolumeInput;
  private boolean _volumeIsStrict;
  private boolean _allowPatchUsageInput;
  private boolean _allowSinglePatchUsage;
  private boolean _allowRangePatchUsage;
  private boolean _allowWholePatchUsage;
  
  public MIDIInputOptions(boolean allowMIDIInput, boolean allowVolumeInput,
      boolean volumeIsStrict, boolean allowPatchUsageInput,
      boolean allowSinglePatchUsage, boolean allowRangePatchUsage,
      boolean allowWholePatchUsage) {
    _allowMIDIInput = allowMIDIInput;
    _allowVolumeInput = allowVolumeInput;
    _volumeIsStrict = volumeIsStrict;
    _allowPatchUsageInput = allowPatchUsageInput;
    _allowSinglePatchUsage = allowSinglePatchUsage;
    _allowRangePatchUsage = allowRangePatchUsage;
    _allowWholePatchUsage = allowWholePatchUsage;
  }
  
  public boolean allowMIDIInput() { return _allowMIDIInput; }
  public boolean allowVolumeInput() { return _allowVolumeInput; }
  public boolean isVolumeStrict() { return _volumeIsStrict; }
  public boolean allowPatchUsageInput() { return _allowPatchUsageInput; }
  public boolean allowSinglePatchUsage() { return _allowSinglePatchUsage; }
  public boolean allowRangePatchUsage() { return _allowRangePatchUsage; }
  public boolean allowWholePatchUsage() { return _allowWholePatchUsage; }
}