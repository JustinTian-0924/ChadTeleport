package basementhost.randomchad.model;

import java.util.Map;

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

	public TeleportHeat afterTeleport(long now, Settings settings, String featureName,
			TeleportRoute route, double quotedPrice) {
		long heatGain = settings.ruleForCommand(featureName).gainMilliHeat(route, quotedPrice);
		if (heatGain == 0) {
			return this;
		}
		return new TeleportHeat(heatAt(now, settings) + heatGain, now);
	}

	public record Settings(boolean enabled, HeatRule defaultRule, int threshold, int coolingPerSecond,
			Map<String, HeatRule> heatPerCommand) {

		public Settings {
			heatPerCommand = Map.copyOf(heatPerCommand);
		}

		public HeatRule ruleForCommand(String featureName) {
			return heatPerCommand.getOrDefault(featureName, defaultRule);
		}
	}
}
