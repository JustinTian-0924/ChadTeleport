package basementhost.randomchad.model;

public class PendingTeleportOffer {

	private final TeleportOffer offer;
	private final long createdAt;

	public PendingTeleportOffer(TeleportOffer offer) {
		this.offer = offer;
		this.createdAt = System.currentTimeMillis();
	}

	public TeleportOffer getOffer() {
		return offer;
	}

	public long getCreatedAt() {
		return createdAt;
	}
}