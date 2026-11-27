package basementhost.randomchad.model;

import org.bukkit.Location;

public class ActiveTeleport {

	private final String featureName;
	private final TeleportOffer offer;
	private final Location destination;
	private Runnable cancelTask;

	public ActiveTeleport(String featureName, TeleportOffer offer, Location destination) {
		this.featureName = featureName;
		this.offer = offer;
		this.destination = destination;
	}

	public String getFeatureName() {
		return featureName;
	}

	public TeleportOffer getOffer() {
		return offer;
	}

	public Location getDestination() {
		return destination == null ? null : destination.clone();
	}

	public void setCancelTask(Runnable cancelTask) {
		this.cancelTask = cancelTask;
	}

	public void cancelTask() {
		if (cancelTask != null) {
			cancelTask.run();
		}
	}
}
