package basementhost.randomchad.model;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class TeleportQuote {

	private final Player requester;
	private final Player target;
	private final Location from;
	private final Location to;
	private final TeleportRoute route;
	private final TeleportPayment payment;
	private final double warmupSeconds;
	private final long warmupTicks;

	public TeleportQuote(Player requester, Player target, Location from, Location to,
						 TeleportRoute route, TeleportPayment payment, double warmupSeconds,
						 long warmupTicks) {
		this.requester = requester;
		this.target = target;
		this.from = from;
		this.to = to;
		this.route = route;
		this.payment = payment;
		this.warmupSeconds = warmupSeconds;
		this.warmupTicks = warmupTicks;
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
