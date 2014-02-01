package cadenza.core;

import java.awt.Color;
import java.io.Serializable;

import common.swing.ColorUtils;

/**
 * <p>A patch, as represented in Cadenza.  Patches do not contain a volume
 * property; rather, this is specified in each usage of the patch.</p>
 * 
 * <p>Patches can, however, contain a default volume.  If a Cue does not
 * specify a volume for a given Patch, then the default is used.</p>
 * 
 * @author Matt Putnam
 */
public class Patch implements Comparable<Patch>, Serializable {
	private static final long serialVersionUID = 1L;
	
	/** The synthesizer that this Patch is on */
	private Synthesizer _synthesizer;
	
	/** The display name */
	public String name;
	
	/** The bank */
	public String bank;
	
	/** The number in the bank */
	public int number;
	
	/** The default volume for this patch */
	public int defaultVolume;
	
	/** Color to display in the patch list and patch assign panels */
	private Color _displayColor = Color.WHITE;
	
	/** Color to display the text in patch list and patch assign panels */
	private Color _foregroundColor = Color.black;
	
	/**
	 * Creates a new Patch.  The default volume will be 100%.
	 * @param synthesizer - the synthesizer that the patch is on
	 * @param name - the name of the patch
	 * @param bank - the patch bank that the patch is in (null for synthesizers with no bank)
	 * @param number - the program number of the patch within the bank
	 */
	public Patch(Synthesizer synthesizer, String name, String bank, int number) {
		this(synthesizer, name, bank, number, 100);
	}
	
	/**
	 * Creates a new Patch.
	 * @param synthesizer - the synthesizer that the patch is on
	 * @param name - the name of the patch
	 * @param bank - the patch bank that the patch is in (null for synthesizers with no bank)
	 * @param number - the program number of the patch within the bank
	 * @param defaultVolume - the default volume for the patch
	 */
	public Patch(Synthesizer synthesizer, String name, String bank, int number, int defaultVolume) {
		if (synthesizer == null)
			throw new IllegalArgumentException("Synthesizer cannot be null");
		
		_synthesizer = synthesizer;
		this.name = name;
		this.bank = bank;
		this.number = number;
		this.defaultVolume = defaultVolume;
	}
	
	public Synthesizer getSynthesizer() {
		return _synthesizer;
	}
	
	public void setSynthesizer(Synthesizer synthesizer) {
		_synthesizer = synthesizer;
	}
	
	/**
	 * Mutates this Patch to match the given Patch
	 * @param other - the Patch to match
	 */
	public void copyFrom(Patch other, boolean includeVolumeAndColors) {
		_synthesizer = other._synthesizer;
		this.name = other.name;
		this.bank = other.bank;
		this.number = other.number;
		if (includeVolumeAndColors) {
			this.defaultVolume = other.defaultVolume;
			this._displayColor = other._displayColor;
			this._foregroundColor = other._foregroundColor;
		}
	}
	
	public Patch copyWithName(String newName) {
		return new Patch(_synthesizer, newName, bank, number, defaultVolume);
	}
	
	public void setDisplayColor(Color color) {
		_displayColor = color;
		_foregroundColor = ColorUtils.getBrightness(color) > 0.5 ? Color.BLACK : Color.WHITE;
	}
	
	public Color getDisplayColor() {
		return _displayColor;
	}
	
	public Color getTextColor() {
		return _foregroundColor;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		final Patch p = (Patch) obj;
		// foreground color is a function of display color
		return this._synthesizer.equals(p._synthesizer) &&
			   this.bank.equals(p.bank) &&
			   this.name.equals(p.name) &&
			   this.number == p.number &&
			   this.defaultVolume == p.defaultVolume &&
			   this._displayColor.equals(p._displayColor);
	}
	
	@Override
	public int hashCode() {
		// foreground color is a function of display color
		int hashCode = _synthesizer.hashCode();
		hashCode = 31*hashCode + (bank == null ? 0 : bank.hashCode());
		hashCode = 31*hashCode + name.hashCode();
		hashCode = 31*hashCode + number;
		hashCode = 31*hashCode + defaultVolume;
		hashCode = 31*hashCode + _displayColor.hashCode();
		return hashCode;
	}
	
	@Override
	public String toString() {
		return "Patch '" + name + "' = " + bank + " " + number + " (" + _synthesizer.getName() + ")";
	}

	// compare by name, then by volume just for display sorting purposes
	@Override
	public int compareTo(Patch o) {
		final int temp = this.name.compareTo(o.name);
		if (temp == 0) {
			return this.defaultVolume - o.defaultVolume;
		} else {
			return temp;
		}
	}
}
