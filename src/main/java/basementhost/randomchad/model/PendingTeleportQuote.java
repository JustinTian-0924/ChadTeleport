package basementhost.randomchad.model;

import org.bukkit.Location;

import java.util.UUID;

public class PendingTeleportQuote {

	private final TeleportQuote quote;
	private final long createdAt;
	private final Location anchorLocation;

	private final UUID targetId;
	private final String targetName;
	private final Location targetAnchorLocation;

	public PendingTeleportQuote(TeleportQuote quote) {
		this.quote = quote;
		this.createdAt = System.currentTimeMillis();
		this.anchorLocation = quote.getFrom().clone();

		this.targetId = quote.getTarget().getUniqueId();
		this.targetName = quote.getTarget().getName();
		this.targetAnchorLocation = quote.getTarget().getLocation().clone();
	}

	public TeleportQuote getQuote() {
		return quote;
	}

	public long getCreatedAt() {
		return createdAt;
	}

	public Location getAnchorLocation() {
		return anchorLocation;
	}

	public UUID getTargetId() {
		return targetId;
	}

	public String getTargetName() {
		return targetName;
	}

	public Location getTargetAnchorLocation() {
		return targetAnchorLocation;
	}
}