package basementhost.randomchad.model;

import org.bukkit.Location;
import org.bukkit.entity.Player;

// New generic offer thingy for handling all types of teleporting in the future
public class TeleportOffer {

	private final String featureName;
	private final Player requester;
	private final Player target;
	private final Location from;
	private final Location destination;
	private final double distance;
	private final double price;
	private final double warmupSeconds;
	private final long warmupTicks;
	private final boolean crossWorld;

	public TeleportOffer(
			String featureName,
			Player requester,
			Player target,
			Location from,
			Location destination,
			double distance,
			double price,
			double warmupSeconds,
			long warmupTicks,
			boolean crossWorld
	) {
		this.featureName = featureName;
		this.requester = requester;
		this.target = target;
		this.from = from;
		this.destination = destination;
		this.distance = distance;
		this.price = price;
		this.warmupSeconds = warmupSeconds;
		this.warmupTicks = warmupTicks;
		this.crossWorld = crossWorld;
	}

	public String getFeatureName() {
		return featureName;
	}

	public Player getRequester() {
		return requester;
	}

	public Player getTarget() {
		return target;
	}

	public Location getFrom() {
		return from;
	}

	public Location getDestination() {
		return destination;
	}

	public double getDistance() {
		return distance;
	}

	public double getPrice() {
		return price;
	}

	public double getWarmupSeconds() {
		return warmupSeconds;
	}

	public long getWarmupTicks() {
		return warmupTicks;
	}

	public boolean isCrossWorld() {
		return crossWorld;
	}
}