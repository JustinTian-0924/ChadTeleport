package basementhost.randomchad.model;

import java.util.Objects;

public record TeleportRoute(String fromWorld, String toWorld, double fromDistance, double toDistance) {

	public TeleportRoute {
		Objects.requireNonNull(fromWorld, "fromWorld");
		Objects.requireNonNull(toWorld, "toWorld");
		PricingRule.requireNonNegative(fromDistance, "fromDistance");
		PricingRule.requireNonNegative(toDistance, "toDistance");
		if (fromWorld.equals(toWorld) && toDistance != 0.0) {
			throw new IllegalArgumentException("A same-world route uses only fromDistance.");
		}
	}

	public boolean isCrossWorld() {
		return !fromWorld.equals(toWorld);
	}

	public double totalDistance() {
		return fromDistance + toDistance;
	}
}
