package cadenza.core.alias;

public class MidiNumber implements Comparable<MidiNumber> {
	private final int _value;
	
	private MidiNumber(int value) {
		_value = value;
	}
	
	public static MidiNumber valueOf(int value) {
		if (value < 0 || value > 127)
			throw new IllegalArgumentException("Value must be 0-127");
		return new MidiNumber(value);
	}
	
	public static MidiNumber valueOf(Integer value) {
		return valueOf(value.intValue());
	}
	
	public int intValue() {
		return _value;
	}
	
	@Override
	public String toString() {
		return String.valueOf(_value);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof MidiNumber))
			return false;
		return _value == ((MidiNumber) obj)._value;
	}
	
	@Override
	public int hashCode() {
		return _value;
	}
	
	@Override
	public int compareTo(MidiNumber o) {
		return _value - o._value;
	}
}
