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
	private final TeleportRoute route;
	private final TeleportPayment payment;
	private final double warmupSeconds;
	private final long warmupTicks;

	public TeleportOffer(
			String featureName,
			Player requester,
			Player target,
			Location from,
			Location destination,
			TeleportRoute route,
			TeleportPayment payment,
			double warmupSeconds,
			long warmupTicks
	) {
		this.featureName = featureName;
		this.requester = requester;
		this.target = target;
		this.from = from;
		this.destination = destination;
		this.route = route;
		this.payment = payment;
		this.warmupSeconds = warmupSeconds;
		this.warmupTicks = warmupTicks;
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
		return from == null ? null : from.clone();
	}

	public Location getDestination() {
		return destination == null ? null : destination.clone();
	}

	public TeleportRoute getRoute() {
		return route;
	}

	public TeleportPayment getPayment() {
		return payment;
	}

	public double getWarmupSeconds() {
		return warmupSeconds;
	}

	public long getWarmupTicks() {
		return warmupTicks;
	}
}
