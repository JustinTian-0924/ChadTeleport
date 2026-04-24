package basementhost.randomchad.service;

import basementhost.randomchad.ChadteleportPlugin;
import basementhost.randomchad.model.ActiveTeleport;
import basementhost.randomchad.model.TeleportOffer;
import basementhost.randomchad.model.TeleportQuote;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;

public class WarmupService {

	private final ChadteleportPlugin plugin;

	public WarmupService(ChadteleportPlugin plugin) {
		this.plugin = plugin;
	}

	public void startTpaWarmup(TeleportQuote quote, Location lockedDestination) {
		Player requester = quote.getRequester();
		Player target = quote.getTarget();

		if (requester == null || !requester.isOnline()) {
			return;
		}

		if (target == null || !target.isOnline()) {
			plugin.getLangService().send(requester, "request.target-offline-cancelled");
			return;
		}

		TeleportOffer offer = new TeleportOffer(
				"tpa",
				requester,
				target,
				quote.getFrom().clone(),
				lockedDestination.clone(),
				quote.getDistance(),
				quote.getPrice(),
				quote.getWarmupSeconds(),
				quote.getWarmupTicks(),
				quote.isCrossWorld()
		);

		startOfferWarmup(offer);
	}

	public void startOfferWarmup(TeleportOffer offer) {
		Player requester = offer.getRequester();
		Player target = offer.getTarget();

		if (requester == null || !requester.isOnline()) {
			return;
		}

		if (target != null && !target.isOnline()) {
			plugin.getLangService().send(requester, "request.target-offline-cancelled");
			return;
		}

		if (plugin.getTeleportManager().hasActiveTeleport(requester)) {
			cancelWarmup(requester, "quote.replaced");
		}

		boolean bypassWarmup = requester.hasPermission("chadteleport.bypass.warmup")
				|| requester.hasPermission("chadteleport.bypass.*");

		boolean warmupEnabled = offer.getFeatureName().equalsIgnoreCase("spawn")
				? plugin.getConfigService().isSpawnWarmupEnabled()
				: plugin.getConfigService().isWarmupEnabled(offer.getFeatureName());
		long warmupTicks = (warmupEnabled && !bypassWarmup) ? offer.getWarmupTicks() : 0L;

		ActiveTeleport activeTeleport = new ActiveTeleport(
				offer.getFeatureName(),
				offer,
				offer.getDestination().clone()
		);

		if (warmupTicks <= 0L) {
			finishTeleport(activeTeleport);
			return;
		}

		BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
			Player onlineRequester = offer.getRequester();
			if (onlineRequester != null) {
				completeWarmup(onlineRequester);
			}
		}, warmupTicks);

		activeTeleport.setTask(task);
		plugin.getTeleportManager().setActiveTeleport(requester, activeTeleport);

		plugin.getLangService().send(requester, "warmup.started", Map.of(
				"seconds", String.valueOf(offer.getWarmupSeconds())
		));
		plugin.getLangService().send(requester, "warmup.dont-move");

		if (target != null && target.isOnline()) {
			plugin.getLangService().send(target, "warmup.target-notified", Map.of(
					"requester", requester.getName()
			));
		}
	}

	public void completeWarmup(Player requester) {
		ActiveTeleport activeTeleport = plugin.getTeleportManager().removeActiveTeleport(requester);
		if (activeTeleport == null) {
			return;
		}

		finishTeleport(activeTeleport);
	}

	public boolean cancelWarmup(Player requester, String reasonKey) {
		ActiveTeleport activeTeleport = plugin.getTeleportManager().removeActiveTeleport(requester);
		if (activeTeleport == null) {
			return false;
		}

		if (activeTeleport.getTask() != null) {
			activeTeleport.getTask().cancel();
		}

		if (requester.isOnline()) {
			plugin.getLangService().send(requester, reasonKey);
		}

		Player target = activeTeleport.getOffer().getTarget();
		if (target != null && target.isOnline()) {
			plugin.getLangService().send(target, "warmup.target-cancelled", Map.of(
					"requester", requester.getName()
			));
		}

		return true;
	}

	private void finishTeleport(ActiveTeleport activeTeleport) {
		TeleportOffer offer = activeTeleport.getOffer();
		Player requester = offer.getRequester();
		Player target = offer.getTarget();

		if (requester == null || !requester.isOnline()) {
			return;
		}

		if (target != null && !target.isOnline()) {
			plugin.getLangService().send(requester, "request.target-offline-cancelled");
			return;
		}

		boolean moneyEnabled = activeTeleport.getFeatureName().equalsIgnoreCase("spawn")
				? plugin.getConfigService().isSpawnMoneyEnabled()
				: plugin.getConfigService().isMoneyEnabled(activeTeleport.getFeatureName());
		boolean shouldCharge = moneyEnabled
				&& !requester.hasPermission("chadteleport.bypass.fee")
				&& !requester.hasPermission("chadteleport.bypass.*");

		double price = offer.getPrice();

		if (shouldCharge && price > 0.0) {
			if (!plugin.getEconomyService().isAvailable()) {
				plugin.getEconomyService().tryHook();
			}

			if (!plugin.getEconomyService().isAvailable()) {
				plugin.getLangService().send(requester, "economy.unavailable-cancelled");
				return;
			}

			if (!plugin.getEconomyService().has(requester, price)) {
				plugin.getLangService().send(requester, "economy.insufficient");
				plugin.getLangService().send(requester, "economy.required", Map.of(
						"price", String.valueOf(price)
				));
				return;
			}
		}

		boolean success = requester.teleport(activeTeleport.getDestination().clone());

		if (!success) {
			plugin.getLangService().send(requester, "teleport.failed");
			return;
		}

		if (shouldCharge && price > 0.0) {
			boolean charged = plugin.getEconomyService().withdraw(requester, price);
			if (!charged) {
				plugin.getLangService().send(requester, "economy.charge-failed-after-teleport");
				plugin.getLogger().warning("Failed to charge " + requester.getName() + " after successful teleport.");
				return;
			}

			plugin.getLangService().send(requester, "teleport.success");
			plugin.getLangService().send(requester, "economy.charged", Map.of(
					"price", String.valueOf(price)
			));
		} else {
			plugin.getLangService().send(requester, "teleport.success");
		}
	}
}