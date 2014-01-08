package cadenza.core.metronome;

public class MetronomeAdapter implements MetronomeListener {
	@Override
	public void bpmSet(int bpm) {}

	@Override
	public void metronomeStarted() {}

	@Override
	public void metronomeClicked(int subdivision) {}

	@Override
	public void metronomeStopped() {}
}
