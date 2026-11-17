package basementhost.randomchad.model;

import java.util.Locale;

public enum PricingMode {

	FREE,
	FIXED,
	DISTANCE;

	public static PricingMode parse(String value) {
		if (value == null) {
			throw new IllegalArgumentException("Pricing mode is required: free, fixed or distance.");
		}
		try {
			return valueOf(value.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Unknown pricing mode '" + value + "': use free, fixed or distance.");
		}
	}
}
