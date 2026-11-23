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
				quote.getRoute(),
				quote.getPayment(),
				quote.getWarmupSeconds(),
				quote.getWarmupTicks()
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

		boolean warmupEnabled = plugin.getConfigService().isWarmupEnabled(offer.getFeatureName());
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

		plugin.getTeleportPaymentService().payAndTeleport(offer, activeTeleport.getDestination().clone());
	}
}
