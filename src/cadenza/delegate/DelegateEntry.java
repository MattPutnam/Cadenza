package cadenza.delegate;

import java.util.List;

import common.tuple.Triple;

public class DelegateEntry {
	public static enum MessageType { CONTROL_CHANGE, PROGRAM_CHANGE, SYSEX };
	
	public final String bankName;
	public final int minNum;
	public final int maxNum;
	public final List<Triple<MessageType, Integer, ? extends Object>> commands;
	
	public DelegateEntry(String bankName, int minNum, int maxNum, List<Triple<MessageType, Integer, ? extends Object>> commands) {
		this.bankName = bankName;
		this.minNum = minNum;
		this.maxNum = maxNum;
		this.commands = commands;
	}
}
