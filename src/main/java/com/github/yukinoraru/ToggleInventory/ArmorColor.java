package com.github.yukinoraru.ToggleInventory;

/**
 * Enum ArmorColor.
 */
public enum ArmorColor {

	/** black. */
	BLACK(1973019),

	/** red. */
	RED(11743532),

	/** green. */
	GREEN(3887386),

	/** brown. */
	BROWN(5320730),

	/** blue. */
	BLUE(2437522),

	/** purple. */
	PURPLE(8073150),

	/** cyan. */
	CYAN(2651799),

	/** silver. */
	SILVER(2651799),

	/** gray. */
	GRAY(4408131),

	/** pink. */
	PINK(14188952),

	/** lime. */
	LIME(4312372),

	/** yellow. */
	YELLOW(14602026),

	/** light blue. */
	LIGHT_BLUE(6719955),

	/** magenta. */
	MAGENTA(12801229),

	/** orange. */
	ORANGE(15435844),

	/** white. */
	WHITE(15790320);

	private int color;

	private ArmorColor(int color) {
		this.color = color;
	}

	/**
	 * Gets the color.
	 * 
	 * @return color
	 */
	public int getColor() {
		return color;
	}
}
