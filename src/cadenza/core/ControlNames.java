package cadenza.core;

import java.util.HashMap;
import java.util.Map;

public class ControlNames {
	private static final Map<Integer, String> _names = new HashMap<>();
	static {
		_names.put(0, "Bank Select");
		_names.put(1, "Modulation Wheel");
		_names.put(2, "Breath Controller");
		_names.put(4, "Foot Controller");
		_names.put(5, "Portamento Time");
		_names.put(6, "Data Entry MSB");
		_names.put(7, "Main Volume");
		_names.put(8, "Balance");
		_names.put(10, "Pan");
		_names.put(11, "Expression");
		_names.put(12, "Effect Control 1");
		_names.put(13, "Effect Control 2");
		_names.put(16, "General Purpose Controller 1");
		_names.put(17, "General Purpose Controller 2");
		_names.put(18, "General Purpose Controller 3");
		_names.put(19, "General Purpose Controller 4");
		_names.put(64, "Damper Pedal (Sustain)");
		_names.put(65, "Portamento");
		_names.put(66, "Sostenuto");
		_names.put(67, "Soft Pedal");
		_names.put(68, "Legato Footswitch");
		_names.put(69, "Hold 2");
		_names.put(70, "Sound Controller 1 (default: Sound Variation)");
		_names.put(71, "Sound Controller 2 (default: Timbre/Harmonic Content)");
		_names.put(72, "Sound Controller 3 (default: Release Time)");
		_names.put(73, "Sound Controller 4 (default: Attack Time)");
		_names.put(74, "Sound Controller 5 (default: Brightness)");
		_names.put(75, "Sound Controller 6");
		_names.put(76, "Sound Controller 7");
		_names.put(77, "Sound Controller 8");
		_names.put(78, "Sound Controller 9");
		_names.put(79, "Sound Controller 10");
		_names.put(80, "General Purpose Controller 5");
		_names.put(81, "General Purpose Controller 6");
		_names.put(82, "General Purpose Controller 7");
		_names.put(83, "General Purpose Controller 8");
		_names.put(84, "Portamento Control");
		_names.put(91, "Effects 1 Depth (previously External Effects Depth)");
		_names.put(92, "Effects 2 Depth (previously Tremolo Depth)");
		_names.put(93, "Effects 3 Depth (previously Chorus Depth)");
		_names.put(94, "Effects 4 Depth (previously Detune Depth)");
		_names.put(95, "Effects 5 Depth (previously Phaser Depth)");
		_names.put(96, "Data Increment");
		_names.put(97, "Data Decrement");
		_names.put(98, "Non-Registered Parameter Number LSB");
		_names.put(99, "Non-Registered Parameter Number MSB");
		_names.put(100, "Registered Parameter Number LSB");
		_names.put(101, "Registered Parameter Number MSB");
		_names.put(121, "Reset All Controllers");
		_names.put(122, "Local Control");
		_names.put(123, "All Notes Off");
		_names.put(124, "Omni Off");
		_names.put(125, "Omni On");
		_names.put(126, "Mono On (Poly Off)");
		_names.put(127, "Poly On (Mono Off)");
	}
	
	private ControlNames() {}
	
	public static String getName(int controlNumber) {
		final String val = _names.get(controlNumber);
		return val == null ? "[Undefined]" : val;
	}
}
