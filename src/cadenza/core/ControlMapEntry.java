package cadenza.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cadenza.core.patchusage.PatchUsage;

import common.Utils;

public class ControlMapEntry implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public int sourceCC;
	public List<Integer> destCCs;
	public List<PatchUsage> destPatches;
	
	public ControlMapEntry(int sourceCC, List<Integer> destCCs, List<PatchUsage> destPatches) {
		this.sourceCC = sourceCC;
		this.destCCs = destCCs;
		this.destPatches = destPatches;
	}
	
	public String getDestCCString() {
		final List<String> tokens = new ArrayList<>(destCCs.size());
		for (final Integer i : destCCs)
			tokens.add(i + ": " + ControlNames.getName(i));
		return Utils.mkString(tokens);
	}
	
	public String getDestPatchesString() {
		if (destPatches.get(0).equals(PatchUsage.ALL))
			return "ALL";
		
		final List<String> tokens = new ArrayList<>(destPatches.size());
		for (final PatchUsage pu : destPatches)
			tokens.add(pu.patch.name + " " + pu.location.toString(true));
		return Utils.mkString(tokens);
	}
}
