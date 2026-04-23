package basementhost.randomchad.model;

import org.bukkit.Location;

import java.util.UUID;

public class PendingTeleportRequest {

	private final TeleportQuote quote;
	private final long createdAt;
	private final UUID requesterId;
	private final String requesterName;
	private final UUID targetId;
	private final String targetName;
	private final Location targetAnchorLocation;

	public PendingTeleportRequest(TeleportQuote quote) {
		this.quote = quote;
		this.createdAt = System.currentTimeMillis();
		this.requesterId = quote.getRequester().getUniqueId();
		this.requesterName = quote.getRequester().getName();
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

	public UUID getRequesterId() {
		return requesterId;
	}

	public String getRequesterName() {
		return requesterName;
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