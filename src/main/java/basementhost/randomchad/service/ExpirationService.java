package basementhost.randomchad.service;

import basementhost.randomchad.ChadteleportPlugin;
import basementhost.randomchad.model.PendingTeleportOffer;
import basementhost.randomchad.model.PendingTeleportQuote;
import basementhost.randomchad.model.PendingTeleportRequest;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class ExpirationService {

	private final ChadteleportPlugin plugin;
	private Runnable cancelTask;

	public ExpirationService(ChadteleportPlugin plugin) {
		this.plugin = plugin;
	}

	public void start() {
		stop();
		if (plugin.getConfigService().usesRegionThreading()) {
			var task = plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin,
					scheduled -> cleanupExpiredData(), 20L, 20L);
			cancelTask = task::cancel;
		} else {
			var task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::cleanupExpiredData, 20L, 20L);
			cancelTask = task::cancel;
		}
	}

	public void stop() {
		if (cancelTask != null) {
			cancelTask.run();
			cancelTask = null;
		}
	}

	private void cleanupExpiredData() {
		cleanupExpiredQuotes();
		cleanupExpiredOffers();
		cleanupExpiredRequests();
	}

	private void cleanupExpiredQuotes() {
		long now = System.currentTimeMillis();
		long maxAgeMillis = plugin.getConfigService().getQuoteExpireSeconds() * 1000L;

		for (Map.Entry<UUID, PendingTeleportQuote> entry : plugin.getTeleportManager().getAllPendingQuotes()) {
			UUID playerId = entry.getKey();
			PendingTeleportQuote pendingQuote = entry.getValue();

			if (now - pendingQuote.getCreatedAt() > maxAgeMillis
					&& plugin.getTeleportManager().removePendingQuote(playerId, pendingQuote)) {
				Player player = plugin.getServer().getPlayer(playerId);
				if (player != null && player.isOnline()) {
					notifyPlayer(player, "quote.expired", Map.of());
				}
			}
		}
	}

	private void cleanupExpiredOffers() {
		long now = System.currentTimeMillis();
		long maxAgeMillis = plugin.getConfigService().getQuoteExpireSeconds() * 1000L;

		for (Map.Entry<UUID, PendingTeleportOffer> entry : plugin.getTeleportManager().getAllPendingOffers()) {
			UUID playerId = entry.getKey();
			if (now - entry.getValue().getCreatedAt() > maxAgeMillis
					&& plugin.getTeleportManager().removePendingOffer(playerId, entry.getValue())) {
				Player player = plugin.getServer().getPlayer(playerId);
				if (player != null && player.isOnline()) {
					notifyPlayer(player, "quote.expired", Map.of());
				}
			}
		}
	}

	private void cleanupExpiredRequests() {
		long now = System.currentTimeMillis();
		long maxAgeMillis = plugin.getConfigService().getRequestExpireSeconds() * 1000L;

		for (PendingTeleportRequest request : plugin.getTeleportManager().getAllPendingRequests()) {
			if (now - request.getCreatedAt() > maxAgeMillis && plugin.getTeleportManager().removePendingRequest(request)) {
				Player target = plugin.getServer().getPlayer(request.getTargetId());

				Map<String, String> placeholders = Map.of(
						"requester", request.getRequesterName(),
						"target", request.getTargetName()
				);

				Player requester = plugin.getServer().getPlayer(request.getRequesterId());
				if (requester != null && requester.isOnline()) {
					notifyPlayer(requester, "request.expired", placeholders);
				}

				if (target != null && target.isOnline()) {
					notifyPlayer(target, "request.expired", placeholders);
				}
			}
		}
	}

	private void notifyPlayer(Player player, String key, Map<String, String> placeholders) {
		if (plugin.getConfigService().usesRegionThreading()) {
			plugin.getRegionTeleportService().notify(player, key, placeholders);
		} else {
			plugin.getLangService().send(player, key, placeholders);
		}
	}
}
