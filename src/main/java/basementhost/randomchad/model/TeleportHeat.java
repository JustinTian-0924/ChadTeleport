package basementhost.randomchad.model;

public record TeleportHeat(long milliHeat, long measuredAtMillis) {

	private static final long MILLI_PER_POINT = 1000L;

	public long heatAt(long now, Settings settings) {
		long elapsed = Math.max(0, now - measuredAtMillis);
		// Cooling is exact to a thousandth of a point. Check for zero before multiplying long gaps.
		if (elapsed >= Math.ceilDiv(milliHeat, settings.coolingPerSecond())) {
			return 0;
		}
		return milliHeat - elapsed * settings.coolingPerSecond();
	}

	public long secondsUntilAllowed(long now, Settings settings) {
		long excess = heatAt(now, settings) - settings.threshold() * MILLI_PER_POINT;
		if (excess <= 0) {
			return 0;
		}
		long coolingMillis = Math.ceilDiv(excess, settings.coolingPerSecond());
		long clockDelay = Math.max(0, measuredAtMillis - now);
		return Math.ceilDiv(clockDelay + coolingMillis, MILLI_PER_POINT);
	}

	public TeleportHeat afterTeleport(long now, Settings settings) {
		return new TeleportHeat(heatAt(now, settings) + settings.heatPerTeleport() * MILLI_PER_POINT, now);
	}

	public record Settings(boolean enabled, int heatPerTeleport, int threshold, int coolingPerSecond) {
	}
}
