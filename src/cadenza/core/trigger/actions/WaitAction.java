package cadenza.core.trigger.actions;

import cadenza.control.CadenzaController;
import cadenza.core.metronome.Metronome;
import cadenza.core.metronome.MetronomeAdapter;

public class WaitAction implements TriggerAction {
	private final int _num;
	private final boolean _isMillis;
	
	private volatile int _beatCounter;
	
	public WaitAction(int num, boolean isMillis) {
		_num = num;
		_isMillis = isMillis;
	}

	@Override
	public void takeAction(CadenzaController controller) {
		if (_isMillis) {
			try {
				Thread.sleep(_num);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else if (Metronome.getInstance().isRunning()) {
			_beatCounter = 0;
			final ClickCounter clickCounter = new ClickCounter();
			Metronome.getInstance().addMetronomeListener(clickCounter);
			while (_beatCounter < _num)
				Thread.yield();
			Metronome.getInstance().removeMetronomeListener(clickCounter);
		}
	}
	
	private class ClickCounter extends MetronomeAdapter {
		@Override
		public void metronomeClicked(int subdivision) {
			if (subdivision == 0)
				++_beatCounter;
		}
	}
	
	@Override
	public String toString() {
		return "wait " + _num + (_isMillis ? " milliseconds" : " beats");
	}
	
	public int getNum() {
		return _num;
	}
	
	public boolean isMillis() {
		return _isMillis;
	}

}
