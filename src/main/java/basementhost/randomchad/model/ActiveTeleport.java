package basementhost.randomchad.model;

import org.bukkit.Location;
import org.bukkit.scheduler.BukkitTask;

public class ActiveTeleport {

	private final String featureName;
	private final TeleportQuote quote;
	private final Location destination;
	private BukkitTask task;

	public ActiveTeleport(String featureName, TeleportQuote quote, Location destination) {
		this.featureName = featureName;
		this.quote = quote;
		this.destination = destination;
	}

	public String getFeatureName() {
		return featureName;
	}

	public TeleportQuote getQuote() {
		return quote;
	}

	public Location getDestination() {
		return destination;
	}

	public BukkitTask getTask() {
		return task;
	}

	public void setTask(BukkitTask task) {
		this.task = task;
	}
}