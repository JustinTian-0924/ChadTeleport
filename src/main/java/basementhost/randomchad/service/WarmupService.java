package basementhost.randomchad.service;

import basementhost.randomchad.ChadteleportPlugin;
import basementhost.randomchad.model.ActiveTeleport;
import basementhost.randomchad.model.TeleportOffer;
import basementhost.randomchad.model.TeleportQuote;
import org.bukkit.Location;
import org.bukkit.entity.Player;

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

		if (plugin.getConfigService().usesRegionThreading()) {
			requester.getScheduler().run(plugin, task -> startOfferWarmup(offer), null);
		} else {
			startOfferWarmup(offer);
		}
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

		boolean regionThreading = plugin.getConfigService().usesRegionThreading();
		if (warmupTicks <= 0L) {
			if (regionThreading) {
				requester.getScheduler().run(plugin, task -> finishTeleport(activeTeleport), null);
			} else {
				finishTeleport(activeTeleport);
			}
			return;
		}

		Runnable complete = () -> {
			if (plugin.getTeleportManager().removeActiveTeleport(requester, activeTeleport)) {
				finishTeleport(activeTeleport);
			}
		};
		if (regionThreading) {
			var task = requester.getScheduler().runDelayed(plugin, scheduled -> complete.run(),
					() -> plugin.getTeleportManager().removeActiveTeleport(requester, activeTeleport),
					warmupTicks);
			if (task == null) {
				return;
			}
			activeTeleport.setCancelTask(task::cancel);
		} else {
			var task = plugin.getServer().getScheduler().runTaskLater(plugin, complete, warmupTicks);
			activeTeleport.setCancelTask(task::cancel);
		}

		plugin.getTeleportManager().setActiveTeleport(requester, activeTeleport);

		plugin.getLangService().send(requester, "warmup.started", Map.of(
				"seconds", String.valueOf(offer.getWarmupSeconds())
		));
		plugin.getLangService().send(requester, "warmup.dont-move");

		if (target != null && target.isOnline()) {
			notifyTarget(target, "warmup.target-notified", Map.of(
					"requester", requester.getName()
			));
		}
	}

	public boolean cancelWarmup(Player requester, String reasonKey) {
		return cancelWarmup(requester, plugin.getTeleportManager().getActiveTeleport(requester), reasonKey);
	}

	public boolean cancelWarmup(Player requester, ActiveTeleport activeTeleport, String reasonKey) {
		if (activeTeleport == null || !plugin.getTeleportManager().removeActiveTeleport(requester, activeTeleport)) {
			return false;
		}

		activeTeleport.cancelTask();

		if (requester.isOnline()) {
			plugin.getLangService().send(requester, reasonKey);
		}

		Player target = activeTeleport.getOffer().getTarget();
		if (target != null && target.isOnline()) {
			notifyTarget(target, "warmup.target-cancelled", Map.of(
					"requester", requester.getName()
			));
		}

		return true;
	}

	private void notifyTarget(Player target, String key, Map<String, String> placeholders) {
		if (plugin.getConfigService().usesRegionThreading()) {
			plugin.getRegionTeleportService().notify(target, key, placeholders);
		} else {
			plugin.getLangService().send(target, key, placeholders);
		}
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
