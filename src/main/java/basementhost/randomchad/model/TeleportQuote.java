package basementhost.randomchad.model;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class TeleportQuote {

	private final Player requester;
	private final Player target;
	private final Location from;
	private final Location to;
	private final double distance;
	private final double price;
	private final double warmupSeconds;
	private final long warmupTicks;
	private final boolean crossWorld;

	public TeleportQuote(Player requester, Player target, Location from, Location to,
						 double distance, double price, double warmupSeconds,
						 long warmupTicks, boolean crossWorld) {
		this.requester = requester;
		this.target = target;
		this.from = from;
		this.to = to;
		this.distance = distance;
		this.price = price;
		this.warmupSeconds = warmupSeconds;
		this.warmupTicks = warmupTicks;
		this.crossWorld = crossWorld;
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

	public Location getTo() {
		return to;
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