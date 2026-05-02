package basementhost.randomchad.model;

import org.bukkit.Location;
import org.bukkit.scheduler.BukkitTask;

public class ActiveTeleport {

	private final String featureName;
	private final TeleportOffer offer;
	private final Location destination;
	private BukkitTask task;

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

	public BukkitTask getTask() {
		return task;
	}

	public void setTask(BukkitTask task) {
		this.task = task;
	}
}