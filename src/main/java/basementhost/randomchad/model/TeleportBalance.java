package basementhost.randomchad.model;

public record TeleportBalance(int points, long recoverySinceMillis) {

	public TeleportBalance recover(long now, Recovery settings) {
		if (points >= settings.maxPoints()) {
			// Full balances do not accumulate recovery time. Preserve any points above a lowered cap.
			return recoverySinceMillis == 0 ? this : new TeleportBalance(points, 0);
		}
		if (recoverySinceMillis == 0 || recoverySinceMillis > now) {
			return new TeleportBalance(points, now);
		}

		long intervals = (now - recoverySinceMillis) / settings.intervalMillis();
		if (intervals == 0) {
			return this;
		}
		int room = settings.maxPoints() - points;
		if (intervals >= Math.ceilDiv(room, settings.pointsPerInterval())) {
			return new TeleportBalance(settings.maxPoints(), 0);
		}

		// The cap check bounds the multiplication and preserves the unused time below the cap.
		int restored = (int) (intervals * settings.pointsPerInterval());
		return new TeleportBalance(points + restored, recoverySinceMillis + intervals * settings.intervalMillis());
	}

	public long secondsUntilRecovery(long now, Recovery settings) {
		long elapsed = Math.max(0, now - recoverySinceMillis);
		return Math.ceilDiv(Math.max(0, settings.intervalMillis() - elapsed), 1000L);
	}

	public record Recovery(int maxPoints, int intervalSeconds, int pointsPerInterval) {
		public Recovery {
			if (maxPoints <= 0 || intervalSeconds <= 0 || pointsPerInterval <= 0) {
				throw new IllegalArgumentException("Balance recovery settings must be positive integers.");
			}
		}

		public long intervalMillis() {
			return intervalSeconds * 1000L;
		}
	}
}
